package com.raddle.tools;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import com.raddle.tools.transfer.ClipboardTransferHandler;

/**
 * description: 
 * @author raddle
 * time : 2013-11-23 上午10:47:17
 */
public class ClipboardTransferable implements Transferable {
    private ClipResult result;
    private List<ClipboardTransferHandler> handlers;

    public ClipboardTransferable(ClipResult result, List<ClipboardTransferHandler> handlers) {
        this.result = result;
        this.handlers = handlers;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return result.getClipdata().keySet().toArray(new DataFlavor[0]);
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (ClipboardTransferHandler handler : handlers) {
            if (handler.isSupported(flavor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            for (ClipboardTransferHandler handler : handlers) {
                if (handler.isSupported(flavor)) {
                    return handler.toClipboardData(flavor, (byte[]) result.getClipdata(flavor));
                }
            }
        }
        return null;
    }

}
