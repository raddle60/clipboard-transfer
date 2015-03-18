/**
 * 
 */
package com.raddle.tools.monitor.impl;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import com.raddle.tools.monitor.ClipboardListener;
import com.raddle.tools.monitor.ClipboardMonitor;

/**
 * 使用FlavorListener,只能监听一次，不能很好的工作
 * 
 * @author raddle
 * 
 */
@Deprecated
public class FlavorListenerMonitor implements ClipboardMonitor, FlavorListener, ClipboardOwner {
    private volatile boolean enabled = false;
    private List<ClipboardListener> listeners = new ArrayList<ClipboardListener>();
    private Clipboard clipboard;

    public FlavorListenerMonitor(Clipboard clipboard) {
        this.clipboard = clipboard;
        clipboard.addFlavorListener(this);
    }

    @Override
    public void addListener(ClipboardListener clipboardListener) {
        listeners.add(clipboardListener);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void reset() {
        enabled = false;
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        System.out.println("ownership losted");
    }

    @Override
    public void flavorsChanged(FlavorEvent e) {
        clipboard.removeFlavorListener(this);
        if (enabled) {
            for (ClipboardListener listener : listeners) {
                listener.contentChanged(clipboard);
            }
        }
        clipboard.addFlavorListener(this);
    }

}
