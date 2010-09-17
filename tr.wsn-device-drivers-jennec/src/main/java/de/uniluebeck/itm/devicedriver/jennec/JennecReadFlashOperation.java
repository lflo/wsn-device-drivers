package de.uniluebeck.itm.devicedriver.jennec;

import de.uniluebeck.itm.devicedriver.Monitor;
import de.uniluebeck.itm.devicedriver.operation.AbstractReadFlashOperation;

public class JennecReadFlashOperation extends AbstractReadFlashOperation {

	private final JennecDevice device;
	
	public JennecReadFlashOperation(JennecDevice device) {
		this.device = device;
	}
	
	@Override
	public byte[] execute(Monitor monitor) throws Exception {
		// Send flash program request
		device.sendBootLoaderMessage(Messages.flashReadRequestMessage(address, length));

		// Read flash program response
		byte[] response = device.receiveBootLoaderReply(Messages.FLASH_READ_RESPONSE);

		// Remove type and success octet
		byte[] data = new byte[response.length - 2];
		System.arraycopy(response, 2, data, 0, response.length - 2);

		// Return data
		return data;
	}

}