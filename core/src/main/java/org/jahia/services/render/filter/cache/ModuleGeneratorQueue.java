/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render.filter.cache;

import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 12 oct. 2010
 */
public class ModuleGeneratorQueue implements InitializingBean {
    private Map<String,String> notCacheableModule = new ConcurrentHashMap<String, String>(2503);
    private Map<String, CountDownLatch> generatingModules;
    private int maxModulesToGenerateInParallel = 50;
    private long moduleGenerationWaitTime = 5000;
    private Semaphore availableProcessings = null;
    private long minimumIntervalAfterLastAutoThreadDump = 60000; // in milliseconds
    private boolean useLatchOnlyForPages = false;
    private boolean threadDumpToSystemOut = true;
    private boolean threadDumpToFile = true;

    public Map<String, String> getNotCacheableModule() {
        return notCacheableModule;
    }

    public Map<String, CountDownLatch> getGeneratingModules() {
        return generatingModules;
    }

    public Semaphore getAvailableProcessings() {
        if (availableProcessings != null)
            return availableProcessings;
        synchronized (this) {
            if (availableProcessings != null)
                return availableProcessings;
            availableProcessings = new Semaphore(getMaxModulesToGenerateInParallel(), true);            
        }
        return availableProcessings;
    }

    public int getMaxModulesToGenerateInParallel() {
        return maxModulesToGenerateInParallel;
    }

    public long getModuleGenerationWaitTime() {
        return moduleGenerationWaitTime;
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        generatingModules = new HashMap<String, CountDownLatch>(maxModulesToGenerateInParallel);
    }

    public void setMaxModulesToGenerateInParallel(int maxModulesToGenerateInParallel) {
        this.maxModulesToGenerateInParallel = maxModulesToGenerateInParallel;
    }

    public void setModuleGenerationWaitTime(long moduleGenerationWaitTime) {
        this.moduleGenerationWaitTime = moduleGenerationWaitTime;
    }

    public void setMinimumIntervalAfterLastAutoThreadDump(long minimumIntervalAfterLastAutoThreadDump) {
        this.minimumIntervalAfterLastAutoThreadDump = minimumIntervalAfterLastAutoThreadDump;
    }    
    
    public long getMinimumIntervalAfterLastAutoThreadDump() {
        return minimumIntervalAfterLastAutoThreadDump;
    }

    public boolean isUseLatchOnlyForPages() {
        return useLatchOnlyForPages;
    }

    public void setUseLatchOnlyForPages(boolean useLatchOnlyForPages) {
        this.useLatchOnlyForPages = useLatchOnlyForPages;
    }

    public boolean isThreadDumpToSystemOut() {
        return threadDumpToSystemOut;
    }

    public void setThreadDumpToSystemOut(boolean threadDumpToSystemOut) {
        this.threadDumpToSystemOut = threadDumpToSystemOut;
    }

    public boolean isThreadDumpToFile() {
        return threadDumpToFile;
    }

    public void setThreadDumpToFile(boolean threadDumpToFile) {
        this.threadDumpToFile = threadDumpToFile;
    }
}
