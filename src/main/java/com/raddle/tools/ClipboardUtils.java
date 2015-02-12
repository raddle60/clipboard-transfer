/**
 *
 */
package com.raddle.tools;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raddle.tools.transfer.ByteArrayClipboardTransferHandler;
import com.raddle.tools.transfer.ClipboardTransferHandler;
import com.raddle.tools.transfer.ImageClipboardTransferHandler;
import com.raddle.tools.transfer.InputStreamClipboardTransferHandler;
import com.raddle.tools.transfer.ReaderClipboardTransferHandler;
import com.raddle.tools.transfer.SerializableClipboardTransferHandler;
import com.raddle.tools.transfer.StringClipboardTransferHandler;

/**
 * @author xurong
 * 
 */
public class ClipboardUtils {
    public static final List<ClipboardTransferHandler> HANDLERS = new ArrayList<ClipboardTransferHandler>();
    static {
        HANDLERS.add(new StringClipboardTransferHandler());
        HANDLERS.add(new ReaderClipboardTransferHandler());
        HANDLERS.add(new ByteArrayClipboardTransferHandler());
        HANDLERS.add(new InputStreamClipboardTransferHandler());
        HANDLERS.add(new ImageClipboardTransferHandler());
        HANDLERS.add(new SerializableClipboardTransferHandler());
    }

    public static boolean isClipboardNotEmpty(Transferable clipT) {
        return clipT != null && clipT.getTransferDataFlavors() != null && clipT.getTransferDataFlavors().length > 0;
    }

    public static ClipResult getClipResult() {
        ClipResult result = new ClipResult();
        result.setSuccess(true);
        Clipboard sysc = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = sysc.getContents(null);
        if (contents.getTransferDataFlavors() != null) {
            Map<String, ClipboardTransferHandler> map = new HashMap<String, ClipboardTransferHandler>();
            for (DataFlavor d : contents.getTransferDataFlavors()) {
                String baseType = d.getPrimaryType() + "/" + d.getSubType();
                map.put(baseType, null);
            }
            for (int i = 0; i < map.size(); i++) {
                for (ClipboardTransferHandler handler : HANDLERS) {
                    boolean hasMatched = false;
                    // 将剪切板内容转成字节数组
                    for (DataFlavor d : contents.getTransferDataFlavors()) {
                        String baseType = d.getPrimaryType() + "/" + d.getSubType();
                        if (map.get(baseType) == null && handler.isSupported(d)) {
                            map.put(baseType, handler);
                            Object transferData = null;
                            try {
                                transferData = contents.getTransferData(d);
                            } catch (Exception e) {
                                // 忽略不支持的种类
                                System.out.println("获取剪切板内容失败," + d + " ," + e.getMessage());
                                continue;
                            }
                            if (transferData != null) {
                                System.out.println("已获取剪切板内容：" + d);
                                result.setClipdata(d, handler.toBytes(d, transferData));
                            }
                            hasMatched = true;
                            break;
                        }
                    }
                    if (hasMatched) {
                        break;
                    }
                }
            }
        }
        result.setMessage("获取剪切板成功");
        return result;
    }

    public static void setClipResult(ClipResult result) {
        if (result.isSuccess() && result.getClipdata().size() > 0) {
            for (DataFlavor d : result.getClipdata().keySet()) {
                System.out.println("设置剪切板内容：" + d);
            }
            Clipboard sysc = Toolkit.getDefaultToolkit().getSystemClipboard();
            sysc.setContents(new ClipboardTransferable(result, HANDLERS), null);
        }
    }

}
