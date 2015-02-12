package com.raddle.tools;

import java.awt.datatransfer.DataFlavor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ClipResult implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean success;
    private String message;

    private Map<DataFlavor, Serializable> clipdata = new HashMap<DataFlavor, Serializable>();

    public ClipResult setClipdata(DataFlavor key, Serializable value) {
        clipdata.put(key, value);
        return this;
    }

    public Serializable getClipdata(DataFlavor key) {
        return clipdata.get(key);
    }

    public Map<DataFlavor, Serializable> getClipdata() {
        return clipdata;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
