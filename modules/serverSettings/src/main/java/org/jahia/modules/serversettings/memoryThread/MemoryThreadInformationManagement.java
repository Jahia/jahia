/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

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
