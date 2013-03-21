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
    private  String maxMemory;
    private  String totalMemory;
    private  String freeMemory;
    private long memoryUsage;
    private String usedMemory;

    public MemoryThreadInformationManagement() {
        refresh();
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

    public String getFreeMemory() {
        return freeMemory;
    }

    public String getMaxMemory() {
        return maxMemory;
    }

    public String getTotalMemory() {
        return totalMemory;
    }

    public long getMemoryUsage() {
        return memoryUsage;
    }

    public String getUsedMemory() {
        return usedMemory;
    }

    public void doGarbageCollection() {
        System.gc();
    }

    public void performThreadDump(String output) {
        ThreadMonitor.getInstance().dumpThreadInfo("sysout".equals(output), "file".equals(output));
    }

    public void scheduleThreadDump(String output, Integer count, Integer interval) {
        ThreadMonitor.getInstance().dumpThreadInfoWithInterval("sysout".equals(output), "file".equals(output),
                count > 0 ? count : 10, interval > 0 ? interval : 10);
    }

    public boolean isThreadMonitorActivated() {
        return ThreadMonitor.getInstance().isActivated();
    }

    public void toggleThreadMonitor() {
        ThreadMonitor.getInstance().setActivated(!ThreadMonitor.getInstance().isActivated());
    }

    public boolean isErrorFileDumperActivated() {
        return ErrorFileDumper.isFileDumpActivated();
    }

    public void toggleErrorFileDumper() {
        ErrorFileDumper.setFileDumpActivated(!ErrorFileDumper.isFileDumpActivated());
    }

    public String executeThreadDump() {
        StringBuilder stringBuilder = new StringBuilder();
        ErrorFileDumper.outputSystemInfo(new PrintWriter(new StringBuilderWriter(stringBuilder)), false, false, false, false, false, true, false, false);
        return stringBuilder.toString();
    }
}
