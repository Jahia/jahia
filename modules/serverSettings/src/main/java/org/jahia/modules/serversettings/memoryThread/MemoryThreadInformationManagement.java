package org.jahia.modules.serversettings.memoryThread;

import org.apache.commons.io.output.StringBuilderWriter;
import org.jahia.bin.errors.ErrorFileDumper;
import org.jahia.tools.jvm.ThreadMonitor;
import org.jahia.utils.FileUtils;

import java.io.PrintWriter;
import java.io.Serializable;

/**
 * @author rincevent
 */
public class MemoryThreadInformationManagement implements Serializable {
    private static final long serialVersionUID = 9142360328755986891L;
    private  String freeMemory;
    private  String maxMemory;
    private long memoryUsage;
    private String mode = "memory";
    private  String totalMemory;
    private String usedMemory;

    public MemoryThreadInformationManagement() {
        refresh();
    }

    public void doGarbageCollection() {
        System.gc();
    }

    public String executeThreadDump() {
        StringBuilder stringBuilder = new StringBuilder();
        ErrorFileDumper.outputSystemInfo(new PrintWriter(new StringBuilderWriter(stringBuilder)), false, false, false, false, false, true, false, false);
        return stringBuilder.toString();
    }

    public String getFreeMemory() {
        return freeMemory;
    }

    public String getMaxMemory() {
        return maxMemory;
    }

    public long getMemoryUsage() {
        return memoryUsage;
    }

    public String getMode() {
        return mode;
    }

    public String getTotalMemory() {
        return totalMemory;
    }

    public String getUsedMemory() {
        return usedMemory;
    }

    public boolean isErrorFileDumperActivated() {
        return ErrorFileDumper.isFileDumpActivated();
    }

    public boolean isThreadMonitorActivated() {
        return ThreadMonitor.getInstance().isActivated();
    }

    public void performThreadDump(String output) {
        ThreadMonitor.getInstance().dumpThreadInfo("sysout".equals(output), "file".equals(output));
    }

    public MemoryThreadInformationManagement refresh() {
        long freeMem = Runtime.getRuntime().freeMemory();
        freeMemory = FileUtils.humanReadableByteCount(freeMem, true);
        totalMemory = FileUtils.humanReadableByteCount(Runtime.getRuntime().totalMemory(), true);
        long maxMem = Runtime.getRuntime().maxMemory();
        maxMemory = FileUtils.humanReadableByteCount(maxMem, true);
        usedMemory = FileUtils.humanReadableByteCount(maxMem - freeMem, true);
        memoryUsage = 100 - Math.round((double) freeMem / (double) maxMem * 100d);
        return this;
    }

    public void scheduleThreadDump(String output, Integer count, Integer interval) {
        ThreadMonitor.getInstance().dumpThreadInfoWithInterval("sysout".equals(output), "file".equals(output),
                count > 0 ? count : 10, interval > 0 ? interval : 10);
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void toggleErrorFileDumper() {
        ErrorFileDumper.setFileDumpActivated(!ErrorFileDumper.isFileDumpActivated());
    }

    public void toggleThreadMonitor() {
        ThreadMonitor.getInstance().setActivated(!ThreadMonitor.getInstance().isActivated());
    }
}
