package com.gmspace.app.bean;

public class ProcessInfo {
    public int pid;
    public int uid;
    public long pss;
    public String processName;

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public long getPss() {
        return pss;
    }

    public void setPss(long pss) {
        this.pss = pss;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }
}
