package de.uniluebeck.itm.wsn.drivers.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.uniluebeck.itm.wsn.drivers.core.MacAddress;
import de.uniluebeck.itm.wsn.drivers.core.operation.WriteMacAddressOperation;


/**
 * Mock operation for writing a <code>MacAddress</code> in the given <code>MockConfiguration</code>.
 * 
 * @author Malte Legenhausen
 */
public class MockWriteMacAddressOperation extends AbstractMockOperation<Void> implements WriteMacAddressOperation {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(MockWriteMacAddressOperation.class);
	
	/**
	 * The <code>MockConfiguration</code> to which the <code>MacAddress</code> has to be assigned.
	 */
	private final MockConfiguration configuration;
	
	/**
	 * The <code>MacAddress</code> that has to be written to the configuration.
	 */
	private MacAddress macAddress;
	
	/**
	 * Constructor.
	 * 
	 * @param configuration The configuration of the <code>MockDevice</code>.
	 */
	@Inject
	public MockWriteMacAddressOperation(MockConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	protected Void returnResult() {
		LOG.debug("Writing mac address: " + macAddress);
		configuration.setMacAddress(macAddress);
		return null;
	}

	@Override
	public void setMacAddress(MacAddress macAddress) {
		this.macAddress = macAddress;
	}

}
