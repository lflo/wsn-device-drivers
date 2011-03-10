package de.uniluebeck.itm.devicedriver.event;

import de.uniluebeck.itm.devicedriver.operation.Operation;


/**
 * Event that is used when an <code>Operation</code> was removed from the queue.
 * 
 * @author Malte Legenhausen
 *
 * @param <T> The type of the operation.
 */
public class RemovedEvent<T> extends OperationEvent<T> {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 6596578633061914325L;

	/**
	 * Constructor.
	 * 
	 * @param source The source of the event.
	 * @param operation The operation associated with this event.
	 */
	public RemovedEvent(final Object source, final Operation<T> operation) {
		super(source, operation);
	}
}
