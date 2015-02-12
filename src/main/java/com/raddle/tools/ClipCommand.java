package com.raddle.tools;

import java.io.Serializable;

/**
 * 网络通信命令
 * 
 * @author xurong
 */
public class ClipCommand implements Serializable {

    public final static String CMD_SHUTDOWN = "shutdown";
    public final static String CMD_GET_CLIP = "getClip";
    public final static String CMD_SET_CLIP = "setClip";

    private static final long serialVersionUID = 1L;
    private String cmdCode;
    private ClipResult result;

    public String getCmdCode() {
        return cmdCode;
    }

    public void setCmdCode(String cmdCode) {
        this.cmdCode = cmdCode;
    }

    public ClipResult getResult() {
        return result;
    }

    public void setResult(ClipResult result) {
        this.result = result;
    }

}
