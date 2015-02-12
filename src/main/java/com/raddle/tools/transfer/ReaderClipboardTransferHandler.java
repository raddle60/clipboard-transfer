/**
 * 
 */
package com.raddle.tools.transfer;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;

/**
 * @author raddle
 * 
 */
public class ReaderClipboardTransferHandler extends AbstractClipboardTransferHandler {

	@Override
	public boolean isSupported(DataFlavor dataFlavor) {
		if (dataFlavor.getRepresentationClass() != null) {
			return Reader.class.isAssignableFrom(dataFlavor.getRepresentationClass());
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
				return new StringReader(new String(bytes, "utf-8"));
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
				return IOUtils.toByteArray((Reader) clipboardData, "utf-8");
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return null;
	}

}
