package com.raddle.tools;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

import org.junit.Test;

import com.raddle.tools.monitor.ClipboardListener;
import com.raddle.tools.monitor.impl.TextClipboardMonitor;

public class ClipboardMonitorTest {

	@Test
	public void testClipboard() throws Exception {
		TextClipboardMonitor m = new TextClipboardMonitor(Toolkit.getDefaultToolkit().getSystemClipboard());
		m.addListener(new ClipboardListener() {

			@Override
			public void contentChanged(Clipboard clipboard) {
				System.out.println(System.currentTimeMillis() + " 剪切板内容变化");
			}
		});
		m.setEnabled(true);
		Thread.sleep(60 * 60 * 1000);
	}
}
