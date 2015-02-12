/**
 * 
 */
package com.raddle.tools.monitor;

/**
 * @author raddle
 * 
 */
public interface ClipboardMonitor {
	public void addListener(ClipboardListener clipboardListener);

	public boolean isEnabled();

	public void setEnabled(boolean enabled);

	public void reset();
}
