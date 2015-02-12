package com.raddle.tools;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.junit.Test;

public class ClipboardTest {

    @Test
    public void testClipboard() throws UnsupportedFlavorException, IOException {
        Clipboard sysc = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = sysc.getContents(null);
        // 剪切版里的数据类型
        System.out.println("-------------------");
        for (DataFlavor d : contents.getTransferDataFlavors()) {
            System.out.println(d);
        }
        System.out.println("---------------------");
        // 支持的传输类型
        final ClipResult result = ClipboardUtils.getClipResult();
        // 清空系统剪切板
        sysc = Toolkit.getDefaultToolkit().getSystemClipboard();
        sysc.setContents(new StringSelection(null), null);
        // 放入序列化好的剪切办内容
        ClipboardUtils.setClipResult(result);
        // 打印放入后的内容
        sysc = Toolkit.getDefaultToolkit().getSystemClipboard();
        contents = sysc.getContents(null);
        for (DataFlavor d : contents.getTransferDataFlavors()) {
            System.out.println(d);
            System.out.println(contents.getTransferData(d));
        }
    }
}
