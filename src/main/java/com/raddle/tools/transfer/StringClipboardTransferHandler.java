/**
 * 
 */
package com.raddle.tools.transfer;

import java.awt.datatransfer.DataFlavor;
import java.io.UnsupportedEncodingException;

/**
 * @author raddle
 * 
 */
public class StringClipboardTransferHandler extends AbstractClipboardTransferHandler {
	@Override
	public boolean isSupported(DataFlavor dataFlavor) {
		if (dataFlavor.getRepresentationClass() != null) {
			return String.class.isAssignableFrom(dataFlavor.getRepresentationClass());
		}
		return false;
	}

	@Override
	public Object toClipboardData(DataFlavor dataFlavor, byte[] bytes) {
		if (!isSupported(dataFlavor)) {
			throw new IllegalArgumentException("not supported dataFlavor, " + dataFlavor);
		}
		if (bytes != null) {
			try {
				return new String(bytes, "utf-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return null;
	}

	@Override
	public byte[] toBytes(DataFlavor dataFlavor, Object clipboardData) {
		if (!isSupported(dataFlavor)) {
			throw new IllegalArgumentException("not supported dataFlavor, " + dataFlavor);
		}
		if (clipboardData != null) {
			try {
				return ((String) clipboardData).getBytes("utf-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return null;
	}

}
