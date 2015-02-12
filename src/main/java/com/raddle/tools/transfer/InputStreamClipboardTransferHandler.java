/**
 * 
 */
package com.raddle.tools.transfer;

import java.awt.datatransfer.DataFlavor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * @author raddle
 * 
 */
public class InputStreamClipboardTransferHandler extends AbstractClipboardTransferHandler {

	@Override
	public boolean isSupported(DataFlavor dataFlavor) {
		if (dataFlavor.getRepresentationClass() != null) {
			return InputStream.class.isAssignableFrom(dataFlavor.getRepresentationClass());
		}
		return false;
	}

	@Override
	public Object toClipboardData(DataFlavor dataFlavor, byte[] bytes) {
		if (!isSupported(dataFlavor)) {
			throw new IllegalArgumentException("not supported dataFlavor, " + dataFlavor);
		}
		if (bytes != null) {
			return new ByteArrayInputStream(bytes);
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
				return IOUtils.toByteArray((InputStream) clipboardData);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return null;
	}
}
