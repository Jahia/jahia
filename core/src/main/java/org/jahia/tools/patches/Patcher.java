/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.filters.jcr.JcrSessionFilter;
import org.jahia.commons.Version;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple patch service that monitors specified folder (by default <code>WEB-INF/var/patches/</code>) for scripts, executes
 * them and renames the executed files.
 *
 * @author Sergiy Shyrkov
 */
public final class Patcher implements JahiaAfterInitializationService, DisposableBean {

    public static final String README = "README";
    public static final String SUFFIX_INSTALLED = ".installed";
    public static final String SUFFIX_FAILED = ".failed";
    public static final String SUFFIX_SKIPPED = ".skipped";
    public static final String KEEP = "keep";
    public static final String REMOVE = "remove";

    private static final Logger logger = LoggerFactory.getLogger(Patcher.class);
    private static final String[] LIFECYCLE_PHASES = {
            "beforeContextInitializing",
            "contextInitializing",
            "contextInitialized",
            "contextInitialized-processingServer",
            "contextInitialized-nonProcessingServer",
            "jcrStoreProviderStarted",
            "rootContextInitialized"
    };
    private static final Pattern VERSION_PATTERN = Pattern.compile("([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+).*");
    public static final Comparator<Resource> RESOURCE_COMPARATOR = Comparator.comparing(Resource::getFilename);

    private static class InstanceHolder {
        public static final Patcher instance = new Patcher();
    }

    private Version jahiaPreviousVersion;
    private List<PatchExecutor> patchers = Arrays.asList(
            new GroovyPatcher(),
            new SqlPatcher(),
            new GraphqlPatcher(),
            new ProvisioningPatcher()
    );

    // 5 minutes interval by default
    private long interval = 5 * 60000L;
    private String patchesLookup;
    private ServletContext servletContext;
    private Timer watchdog;

    private Patcher() {
        initPreviousVersion();
    }

    public static Patcher getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * Execute scripts for the given lifecycle phase
     * @param lifecyclePhase the lifecycle phase
     */
    public void executeScripts(String lifecyclePhase) {
        try {
            if (System.getProperty("skipPatches") != null) {
                return;
            }

            File lookupFolder = getPatchesFolder();
            if (lookupFolder == null) {
                return;
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Looking up patches in the folder {}", lookupFolder);
            }
            List<File> patches = new LinkedList<>(FileUtils.listFiles(
                    lookupFolder.getCanonicalFile(),
                    new AndFileFilter(
                            new NotFileFilter(new SuffixFileFilter(new String[]{README, SUFFIX_INSTALLED, SUFFIX_FAILED, SUFFIX_SKIPPED})),
                            new LifecycleFilter(lifecyclePhase)
                    ),
                    TrueFileFilter.INSTANCE
            ));

            if (patches.isEmpty()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("No patches were found");
                }
                return;
            }

            List<Resource> resources = patches.stream()
                    .map(p -> p == null ? null : new FileSystemResource(p))
                    .sorted(RESOURCE_COMPARATOR)
                    .collect(Collectors.toList());

            executeScripts(resources, lifecyclePhase);
        } catch (Exception e) {
            logger.error("Error executing patches", e);
        }
    }

    /**
     * Execute scripts in the specified lifecycle phase
     * @param scripts scripts
     * @param lifecyclePhase lifecyclePhase
     */
    public void executeScripts(Collection<Resource> scripts, String lifecyclePhase) {
        executeScripts(scripts, lifecyclePhase, this::afterExecution);
    }

    /**
     * Execute scripts in the specified lifecycle phase, use a callback after execution
     * @param scripts scripts
     * @param lifecyclePhase lifecyclePhase
     * @param afterExecution afterExecution callback
     */
    public void executeScripts(Collection<Resource> scripts, String lifecyclePhase, BiConsumer<Resource, String> afterExecution) {
        if (scripts.isEmpty()) {
            return;
        }

        long timer = System.currentTimeMillis();
        if (logger.isInfoEnabled()) {
            logger.info("Found patch scripts {}. Executing...", StringUtils.join(scripts, ','));
        }

        for (Resource script : scripts) {
            try {
                if (shouldSkipMigrationScript(script.getFilename())) {
                    afterExecution.accept(script, SUFFIX_SKIPPED);
                } else {
                    executeScript(lifecyclePhase, script, afterExecution);
                }
            } catch (Exception e) {
                logger.error("Execution of script {} failed with error: ", e.getMessage(), e);
                afterExecution.accept(script, SUFFIX_FAILED);
            }
        }

        logger.info("Execution took {} ms", (System.currentTimeMillis() - timer));
    }

    private void executeScript(String lifecyclePhase, Resource script, BiConsumer<Resource, String> afterExecution) throws IOException {
        long timerSingle = System.currentTimeMillis();
        String scriptContent = getContent(script);

        if (StringUtils.isNotEmpty(scriptContent)) {
            for (PatchExecutor patcher : patchers) {
                if (patcher.canExecute(script.getURL().getPath(), lifecyclePhase)) {
                    String result = patcher.executeScript(script.getURL().getPath(), scriptContent);
                    logger.info("Execution of script {} : {} took {} ms", script.getFilename(), result, System.currentTimeMillis() - timerSingle);
                    afterExecution.accept(script, result);
                    break;
                }
            }
        } else {
            logger.warn("Content of the script {} is either empty or cannot be read. Skipping.", script.getFilename());
            afterExecution.accept(script, SUFFIX_SKIPPED);
        }
    }

    private boolean shouldSkipMigrationScript(String filename) {
        Matcher matcher = VERSION_PATTERN.matcher(filename);
        return matcher.matches() && (jahiaPreviousVersion == null || new Version(matcher.group(1)).compareTo(jahiaPreviousVersion) <= 0);
    }

    private String getContent(Resource r) throws IOException {
        InputStream in = null;
        try {
            in = r.getInputStream();
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public File getPatchesFolder() {
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

    private void afterExecution(Resource script, String result) {
        File scriptFile;
        try {
            scriptFile = script.getFile();
            if (REMOVE.equals(result)) {
                Files.delete(scriptFile.toPath());
            } else if (result.startsWith(".")) {
                File dest = new File(scriptFile.getParentFile(), scriptFile.getName() + result);
                if (dest.exists()) {
                    FileUtils.deleteQuietly(dest);
                }
                if (!scriptFile.renameTo(dest)) {
                    logger.warn("Unable to rename script file {} to {}. Skip renaming.", script.getFile().getPath(), dest.getPath());
                }
            }
        } catch (IOException e) {
            logger.warn("Unable to rename the script file for resource {} due to an error: {}", script, e.getMessage(), e);
        }
    }

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
                try {
                    JCRSessionFactory.getInstance().setCurrentUser(JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser());
                    perform();
                } finally {
                    JcrSessionFilter.endRequest();
                }
            }
        }, 0, interval);
    }

    private void perform() {
        executeScripts("");
    }

    public void setInterval(long interval) {
        this.interval = interval;
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

    public void setPatchesLookup(String patchesLookup) {
        this.patchesLookup = patchesLookup;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setPatchers(List<PatchExecutor> patchers) {
        this.patchers = patchers;
    }

    public Version getJahiaPreviousVersion() {
        return jahiaPreviousVersion;
    }

    private void initPreviousVersion() {
        File versionFile = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/info/version.properties");
        if (versionFile.exists()) {
            try(FileInputStream inputStream = new FileInputStream(versionFile)) {
                Properties p = new Properties();
                p.load(inputStream);
                jahiaPreviousVersion = new Version(p.getProperty("version"));
            } catch (IOException ioException) {
                logger.error("Cannot read version.txt", ioException);
            }
        }

        if (jahiaPreviousVersion == null) {
            initPreviousVersionFromBundlesDeployed();
        }

        if (!versionFile.getParentFile().exists()) {
            versionFile.getParentFile().mkdirs();
        }
        if (!versionFile.exists() || jahiaPreviousVersion == null || !jahiaPreviousVersion.toString().equals(Constants.JAHIA_PROJECT_VERSION)) {
            try {
                FileUtils.writeStringToFile(versionFile, "version=" + Constants.JAHIA_PROJECT_VERSION, StandardCharsets.UTF_8);
            } catch (IOException ioException) {
                logger.error("Cannot store version.txt", ioException);
            }
        }
    }

    private void initPreviousVersionFromBundlesDeployed() {
        Pattern p = Pattern.compile("^mvn:org.jahia.bundles/org.jahia.bundles.extender.jahiamodules/(.*)$");

        File file = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/bundles-deployed");
        if (file.exists()) {
            Arrays.stream(file.listFiles((File::isDirectory))).map(f -> new File(f, "bundle.info")).filter(File::exists).flatMap(f -> {
                try {
                    return FileUtils.readLines(f, StandardCharsets.UTF_8).stream();
                } catch (IOException ioException) {
                    logger.debug("Cannot read bundle.info", ioException);
                    return Stream.empty();
                }
            }).forEach(l -> {
                Matcher m = p.matcher(l);
                if (m.matches()) {
                    jahiaPreviousVersion = new Version(m.group(1));
                }
            });
        }
    }

    private static class LifecycleFilter implements IOFileFilter {
        private final String lifecyclePhase;

        public LifecycleFilter(String lifecyclePhase) {
            this.lifecyclePhase = lifecyclePhase;
        }

        private boolean isValid(String name) {
            String filePhase = StringUtils.substringAfterLast(StringUtils.substringBeforeLast(name, "."), ".");
            if (lifecyclePhase.equals("")) {
                return Arrays.stream(LIFECYCLE_PHASES).noneMatch(filePhase::equals);
            } else {
                return filePhase.equals(lifecyclePhase);
            }
        }

        @Override
        public boolean accept(File file) {
            return isValid(file.getName());
        }

        @Override
        public boolean accept(File dir, String name) {
            return isValid(name);
        }
    }
}
