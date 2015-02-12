/**
 * 
 */
package com.raddle.tools.monitor.impl;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.raddle.tools.monitor.ClipboardListener;
import com.raddle.tools.monitor.ClipboardMonitor;

/**
 * 根据检测剪切板的文本变化
 * 
 * @author raddle
 * 
 */
public class TextClipboardMonitor implements ClipboardMonitor {
	private List<ClipboardListener> listeners = new ArrayList<ClipboardListener>();
	private MonitorThread thread = new MonitorThread();
	private int interval = 300;
	private String predata;
	private volatile boolean enabled = false;
	private Clipboard clipboard;

	public TextClipboardMonitor(Clipboard clipboard) {
		this.clipboard = clipboard;
		thread.start();
		reset();
	}

	@Override
	public void addListener(ClipboardListener clipboardListener) {
		listeners.add(clipboardListener);
	}

	private class MonitorThread extends Thread {

		public MonitorThread() {
			setDaemon(true);
		}

		@Override
		public void run() {
			while (true) {
				try {
					// 放在最前面，防止出错跳过sleep
					Thread.sleep(interval);
					synchronized (TextClipboardMonitor.this) {
						if (enabled) {
							String data = null;
							Transferable contents = clipboard.getContents(null);
							if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
								data = (String) contents.getTransferData(DataFlavor.stringFlavor);
							}
							if (data != null) {
								data = DigestUtils.md5Hex(data);
							}
							if (enabled && !(predata == data || (predata != null && predata.equals(data)))) {
								for (ClipboardListener listener : listeners) {
									listener.contentChanged(clipboard);
								}
							}
							predata = data;
						}
					}
				} catch (InterruptedException e) {
					return;
				} catch (Exception e) {
					System.out.println("MonitorThread error," + e.getMessage());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						return;
					}
				}
			}
		}
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = Math.max(200, interval);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public synchronized void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public synchronized void reset() {
		enabled = false;
		boolean success = false;
		int times = 0;
		// 剪切板忙时，会获得不到，多等一段时间
		while (!success && times < 20) {
			times++;
			try {
				String data = (String) clipboard.getData(DataFlavor.stringFlavor);
				if (data != null) {
					data = DigestUtils.md5Hex(data);
				}
				predata = data;
				success = true;
			} catch (Exception e) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					return;
				}
			}
		}
	}
}
