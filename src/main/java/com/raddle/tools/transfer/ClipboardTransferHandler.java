/**
 * 
 */
package com.raddle.tools.transfer;

import java.awt.datatransfer.DataFlavor;

/**
 * @author raddle
 * 
 */
public interface ClipboardTransferHandler {

	public Object toClipboardData(DataFlavor dataFlavor, byte[] bytes);

	public byte[] toBytes(DataFlavor dataFlavor, Object clipboardData);

	public boolean isSupported(DataFlavor dataFlavor);

}
