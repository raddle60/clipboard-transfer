/**
 * 
 */
package com.raddle.tools.transfer;

import java.awt.datatransfer.DataFlavor;

/**
 * @author raddle
 * 
 */
public class ByteArrayClipboardTransferHandler extends AbstractClipboardTransferHandler {

	@Override
	public boolean isSupported(DataFlavor dataFlavor) {
		if (dataFlavor.getRepresentationClass() != null) {
			return byte[].class.isAssignableFrom(dataFlavor.getRepresentationClass());
		}
		return false;
	}

	@Override
	public Object toClipboardData(DataFlavor dataFlavor, byte[] bytes) {
		if (!isSupported(dataFlavor)) {
			throw new IllegalArgumentException("not supported dataFlavor, " + dataFlavor);
		}
		return bytes;
	}

	@Override
	public byte[] toBytes(DataFlavor dataFlavor, Object clipboardData) {
		if (!isSupported(dataFlavor)) {
			throw new IllegalArgumentException("not supported dataFlavor, " + dataFlavor);
		}
		return (byte[]) clipboardData;
	}

}
