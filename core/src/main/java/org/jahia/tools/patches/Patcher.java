/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.tools.patches;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Simple patch service that monitors specified folder (by default <code>WEB-INF/var/patches/</code>) for scripts, executes
 * them and renames the executed files.
 *
 * @author Sergiy Shyrkov
 */
public class Patcher implements JahiaAfterInitializationService, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(Patcher.class);

    public static final String README = "README";
    public static final String INSTALLED = ".installed";
    public static final String FAILED = ".failed";
    public static final String SKIPPED = ".skipped";

    private static class InstanceHolder {
        public static final Patcher instance = new Patcher();
    }

    private Patcher() {
    }

    public static Patcher getInstance() {
        return InstanceHolder.instance;
    }

    private List<PatchExecutor> patchers = Arrays.asList(
            new GroovyPatcher(),
            new SqlPatcher(),
            new GraphqlPatcher()
    );

    private static final Comparator<Resource> RESOURCE_COMPARATOR = Comparator.comparing(Resource::getFilename);

    public void executeScripts(String lifecyclePhase) {
        try {
            File lookupFolder = getPatchesFolder();
            if (lookupFolder == null) {
                return;
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Looking up patches in the folder {}", lookupFolder);
            }
            List<File> patches = new LinkedList<>(FileUtils.listFiles(lookupFolder, new NotFileFilter(new SuffixFileFilter(new String[] {README, INSTALLED, FAILED, SKIPPED})), TrueFileFilter.INSTANCE));

            if (patches.isEmpty()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("No patches were found");
                }
                return;
            }

            Resource[] resources = new Resource[patches.size()];
            for (int i = 0; i < patches.size(); i++) {
                resources[i] = patches.get(i) == null ? null : new FileSystemResource(patches.get(i));
            }

            Arrays.sort(resources, RESOURCE_COMPARATOR);
            executeScripts(resources, lifecyclePhase);
        } catch (Exception e) {
            logger.error("Error executing patches", e);
        }
    }

    public void executeScripts(Resource[] scripts, String lifecyclePhase) {
        long timer = System.currentTimeMillis();
        if (logger.isInfoEnabled()) {
            logger.info("Found new patch scripts {}. Executing...", StringUtils.join(scripts, ','));
        }

        for (Resource script : scripts) {
            try {
                long timerSingle = System.currentTimeMillis();
                String scriptContent = getContent(script);

                if (StringUtils.isNotEmpty(scriptContent)) {
                    for (PatchExecutor patcher : patchers) {
                        if (patcher.canExecute(script.getURL().getPath(), lifecyclePhase)) {
                            String result = patcher.executeScript(script.getURL().getPath(), scriptContent);
                            logger.info("Execution of script {} took {} ms",
                                    new String[]{script.getFilename(),
                                            String.valueOf(System.currentTimeMillis() - timerSingle)});
                            rename(script, result);
                            break;
                        }
                    }
                } else {
                    logger.warn("Content of the script {} is either empty or cannot be read. Skipping.", script.getFilename());
                    rename(script, SKIPPED);
                }
            } catch (Exception e) {
                logger.error("Execution of script " + script + " failed with error: " + e.getMessage(), e);
                rename(script, FAILED);
            }
        }

        logger.info("Execution took {} ms", (System.currentTimeMillis() - timer));
    }

    protected String getContent(Resource r) throws IOException {
        InputStream in = null;
        try {
            in = r.getInputStream();
            return IOUtils.toString(in, "UTF-8");
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private File getPatchesFolder() {
        String varFolder = System.getProperty("jahiaVarDiskPath");
        if (varFolder == null) {
            varFolder = SettingsBean.getInstance() != null ? SettingsBean.getInstance().getJahiaVarDiskPath() : null;
        }
        if (varFolder == null) {
            varFolder = servletContext.getRealPath("WEB-INF/var");
        }
        File lookupFolder = new File(varFolder, "patches");
        return lookupFolder.isDirectory() ? lookupFolder : null;
    }

    protected void rename(Resource script, String suffix) {
        File scriptFile;
        try {
            scriptFile = script.getFile();
            File dest = new File(scriptFile.getParentFile(), scriptFile.getName() + suffix);
            if (dest.exists()) {
                FileUtils.deleteQuietly(dest);
            }
            if (!scriptFile.renameTo(dest)) {
                logger.warn("Unable to rename script file {} to {}. Skip renaming.", script
                        .getFile().getPath(), dest.getPath());
            }
        } catch (IOException e) {
            logger.warn("Unable to rename the script file for resurce " + script
                    + " due to an error: " + e.getMessage(), e);
        }
    }

    private long interval = 5 * 60000L; // 5 minutes interval by default

    private String patchesLookup;

    private ServletContext servletContext;

    private Timer watchdog;

    public void destroy() throws Exception {
        if (watchdog != null) {
            watchdog.cancel();
        }
    }

    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {

        if (!SettingsBean.getInstance().isProcessingServer()) {
            logger.info("Script watchdog is disabled on a non-processing Jahia server");
            return;
        }

        if (interval > 5000 && SettingsBean.getInstance().isDevelopmentMode()) {
            // in development mode reduce monitoring interval to 5 seconds
            interval = 5000;
        }

        if (interval <= 0) {
            logger.info("The interval for the patcher is <= 0. Skip starting file watcher.");
            return;
        }

        if (StringUtils.isEmpty(getPatchesLookup())) {
            logger.info("The patches lookup path is not set. Skip starting file watcher.");
            return;
        }

        // execute scripts right now
        perform();

        // start watchdog for monitoring
        watchdog = new Timer(true);
        watchdog.schedule(new TimerTask() {
            @Override
            public void run() {
                perform();
            }
        }, 0, interval);
    }

    private void perform() {
        executeScripts("");
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void setPatchesLookup(String patchesLookup) {
        this.patchesLookup = patchesLookup;
    }

    public String getPatchesLookup() {
        if (patchesLookup == null) {
            File patchesFolder = getPatchesFolder();
            if (patchesFolder != null) {
                String absolutePath = patchesFolder.getAbsolutePath();
                absolutePath = StringUtils.replaceChars(absolutePath, '\\', '/');
                absolutePath = StringUtils.replace(absolutePath, " ", "%20");
                patchesLookup = "file://" + absolutePath + "/**/*";
            }
        }
        return patchesLookup;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setPatchers(List<PatchExecutor> patchers) {
        this.patchers = patchers;
    }
}
