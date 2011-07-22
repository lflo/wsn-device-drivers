package de.uniluebeck.itm.wsn.drivers.core.concurrent;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.uniluebeck.itm.wsn.drivers.core.operation.Operation;
import de.uniluebeck.itm.wsn.drivers.core.operation.OperationListener;
import de.uniluebeck.itm.wsn.drivers.core.operation.StateChangedEvent;

/**
 * Class that implements the queue with the single thread executorService from the Java Concurrency Framework. Only one
 * <code>Operation</code> is executed at once.
 *
 * @author Malte Legenhausen
 */
@Singleton
public class ExecutorServiceOperationExecutor implements OperationExecutor {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceOperationExecutor.class);

	/**
	 * List that contains all listeners.
	 */
	private final List<OperationExecutorListener> listeners = newArrayList();

	/**
	 * Queue for all <code>OperationContainer</code> that are in progress.
	 */
	private final Queue<Operation<?>> operations = newLinkedList();
	
	/**
	 * Maps the operation to the next runnable.
	 */
	private final Map<Operation<?>, Runnable> runnables = newHashMap();

	/**
	 * The single thread executorService that runs the <code>OperationContainer</code>.
	 */
	private final ExecutorService executorService;
	
	/**
	 * The idle runnable.
	 */
	private final Runnable idleRunnable;
	
	/**
	 * The future for the idle task.
	 */
	private Future<Object> idleFuture = null;

	/**
	 * Constructor.
	 */
	public ExecutorServiceOperationExecutor() {
		executorService = Executors.newFixedThreadPool(2);
		idleRunnable = null;
	}

	/**
	 * Constructor.
	 *
	 * @param executorService Set a custom <code>PausableExecutorService</code>.
	 */
	@Inject
	public ExecutorServiceOperationExecutor(ExecutorService executorService, 
			@Nullable @IdleRunnable Runnable idleRunnable) {
		this.executorService = executorService;
		this.idleRunnable = idleRunnable;
		startIdleThread();
	}

	/**
	 * Returns the executorService used by this queue.
	 *
	 * @return The executorService for this queue.
	 */
	public ExecutorService getExecutorService() {
		return executorService;
	}

	@Override
	public <T> OperationFuture<T> submitOperation(Operation<T> operation) {
		checkNotNull(operation, "Null Operation is not allowed.");
		prepareOperation(operation);
		return addOperationAndExecuteNext(operation);
	}
	
	private <T> void prepareOperation(Operation<T> operation) {
		operation.addListener(new OperationListener<T>() {
			@Override
			public void beforeStateChanged(StateChangedEvent<T> event) {
				fireBeforeStateChangedEvent(event);
			}
			
			@Override
			public void afterStateChanged(StateChangedEvent<T> event) {
				fireAfterStateChangedEvent(event);
			}
		});
	}
	
	private <T> OperationFuture<T> addOperationAndExecuteNext(final Operation<T> operation) {
		ListenableFutureTask<T> future = new ListenableFutureTask<T>(operation);
		Runnable afterFinishRunnable = new Runnable() {
			@Override
			public void run() {
				removeOperationAndExecuteNext(operation);
			}
		};
		future.addListener(afterFinishRunnable, executorService);
		synchronized (operations) {
			addOperation(operation, future);
			executeNextOrStartIdleThread();
		}
		return new SimpleOperationFuture<T>(future, operation);
	}
	
	private void removeOperationAndExecuteNext(Operation<?> operation) {
		synchronized (operations) {
			removeOperation(operation);
			executeNextOrStartIdleThread();
		}
	}
	
	private void executeNextOrStartIdleThread() {
		if (!operations.isEmpty()) {
			stopIdleThread();
			Operation<?> operation = operations.peek();
			Runnable runnable = runnables.get(operation);
			LOG.trace("Submit {} to executorService.", operation.getClass().getName());
			executorService.execute(runnable);
		} else {
			startIdleThread();
		}
	}

	private <T> void addOperation(Operation<T> operation, Runnable runnable) {
		operations.add(operation);
		runnables.put(operation, runnable);
		LOG.trace("{} added to internal operation list", operation.getClass().getName());
		fireAddedEvent(new AddedEvent<T>(this, operation));
	}

	private <T> void removeOperation(Operation<T> operation) {
		operations.remove(operation);
		runnables.remove(operation);
		LOG.trace("{} removed from internal operation list", operation.getClass().getName());
		fireRemovedEvent(new RemovedEvent<T>(this, operation));
	}
	
	private void stopIdleThread() {
		if (idleFuture != null) {
			LOG.trace("Stopping idle thread...");
			idleFuture.cancel(true);
			LOG.trace("IdleRunnable thread stopped.");
		}
	}
	
	private void startIdleThread() {
		if (idleRunnable != null) {
			LOG.trace("Starting idle thread...");
			idleFuture = executorService.submit(idleRunnable, null);
			LOG.trace("Stopping idle thread.");
		}
	}

	@Override
	public List<Operation<?>> getOperations() {
		return newArrayList(operations);
	}

	@Override
	public void addListener(final OperationExecutorListener listener) {
		checkNotNull(listener, "Null listener is not allowed.");
		listeners.add(listener);
	}

	@Override
	public void removeListener(final OperationExecutorListener listener) {
		checkNotNull(listener, "Null listener is not allowed.");
		listeners.remove(listener);
	}

	/**
	 * Notify all listeners that the state of a operation has changed.
	 *
	 * @param <T>   The type of the operation.
	 * @param event The event that will notify about the state change.
	 */
	private <T> void fireBeforeStateChangedEvent(final StateChangedEvent<T> event) {
		String msg = "Operation state of {} changed";
		LOG.trace(msg, new Object[] {event.getOperation().getClass().getName()});
		for (final OperationExecutorListener listener : listeners.toArray(new OperationExecutorListener[0])) {
			listener.beforeStateChanged(event);
		}
	}
	
	/**
	 * Notify all listeners that the state of a operation has changed.
	 *
	 * @param <T>   The type of the operation.
	 * @param event The event that will notify about the state change.
	 */
	private <T> void fireAfterStateChangedEvent(final StateChangedEvent<T> event) {
		String msg = "Operation state of {} changed";
		LOG.trace(msg, new Object[] {event.getOperation().getClass().getName()});
		for (final OperationExecutorListener listener : listeners.toArray(new OperationExecutorListener[0])) {
			listener.afterStateChanged(event);
		}
	}

	/**
	 * Notify all listeners that a operation was added to the queue.
	 *
	 * @param <T>   The type of the operation.
	 * @param event The event that will notify about the add of an operation.
	 */
	private void fireAddedEvent(final AddedEvent<?> event) {
		for (final OperationExecutorListener listener : listeners.toArray(new OperationExecutorListener[0])) {
			listener.onAdded(event);
		}
	}

	/**
	 * Notify all listeners that a operation was removed from the queue.
	 *
	 * @param <T>   The type of the operation.
	 * @param event The event that will notify about the remove of an operation.
	 */
	private void fireRemovedEvent(final RemovedEvent<?> event) {
		for (final OperationExecutorListener listener : listeners.toArray(new OperationExecutorListener[0])) {
			listener.onRemoved(event);
		}
	}
}