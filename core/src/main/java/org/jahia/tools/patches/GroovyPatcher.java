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

package org.jahia.tools.patches;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.ScriptEngineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

/**
 * Simple patch service that monitors specified folder (by default <code>WEB-INF/var/patches/groovy/</code>) for Groovy scripts, executes
 * them and renames the executed files.
 * 
 * @author Sergiy Shyrkov
 */
public class GroovyPatcher implements JahiaAfterInitializationService, DisposableBean {

    private static final Comparator<Resource> RESOURCE_COMPARATOR = new Comparator<Resource>() {
        public int compare(Resource o1, Resource o2) {
            try {
                return o1.getURI().compareTo(o2.getURI());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            return 0;
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(GroovyPatcher.class);

    private long interval = 5 * 60000L; // 5 minutes interval by default

    private String patchesLookup = "/WEB-INF/var/patches/groovy/**/*.groovy";

    private Timer watchdog;

    public void destroy() throws Exception {
        if (watchdog != null) {
            watchdog.cancel();
        }
    }

    protected void executeScripts(Resource[] scripts) {
        long timer = System.currentTimeMillis();
        if (logger.isInfoEnabled()) {
            logger.info("Found new patch scripts {}. Executing...", StringUtils.join(scripts));
        }

        for (Resource script : scripts) {
            try {
                long timerSingle = System.currentTimeMillis();
                String scriptContent = null;
                InputStream in = null;
                try {
                    in = script.getInputStream();
                    scriptContent = IOUtils.toString(in, "UTF-8");
                } finally {
                    IOUtils.closeQuietly(in);
                }
                if (StringUtils.isNotEmpty(scriptContent)) {
                    ScriptEngine engine = getEngine();
                    ScriptContext ctx = new SimpleScriptContext();
                    ctx.setWriter(new StringWriter());
                    Bindings bindings = engine.createBindings();
                    bindings.put("log", new LoggerWrapper(logger, logger.getName(), ctx.getWriter()));
                    ctx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

                    engine.eval(scriptContent, ctx);
                    String result = ((StringWriter) ctx.getWriter()).getBuffer().toString();
                    logger.info(
                            "Execution of script {} took {} ms with result:\n{}",
                            new String[] { script.toString(),
                                    String.valueOf(System.currentTimeMillis() - timerSingle),
                                    result });
                } else {
                    logger.warn("Content of the script {} is either empty or cannot be read. Skipping.");
                }
                rename(script, ".installed");
            } catch (Exception e) {
                logger.error(
                        "Execution of script " + script + " failed with error: " + e.getMessage(), e);
                rename(script, ".failed");
            }
        }

        logger.info("Execution took {} ms", (System.currentTimeMillis() - timer));
    }

    protected ScriptEngine getEngine() throws ScriptException {
        try {
            return ScriptEngineUtils.getInstance().scriptEngine("groovy");
        } catch (ScriptException e) {
            if (e instanceof ScriptException && e.getMessage() != null
                    && e.getMessage().startsWith("Script engine not found for extension")) {
                return null;
            } else {
                throw e;
            }
        }
    }

    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        if (interval > 5000 && SettingsBean.getInstance().isDevelopmentMode()) {
            // in development mode reduce monitoring interval to 5 seconds
            interval = 5000;
        }

        if (interval <= 0) {
            logger.info("The interval for the Groovy patcher is <= 0. Skip starting file watcher.");
            return;
        }

        if (StringUtils.isEmpty(patchesLookup)) {
            logger.info("The patches lookup path is not set. Skip starting file watcher.");
            return;
        }

        try {
            if (getEngine() == null) {
                logger.error("The Groovy engine is not evailable. Skip starting file watcher.");
                return;
            }
        } catch (ScriptException e) {
            throw new JahiaInitializationException(e.getMessage(), e);
        }

        watchdog = new Timer(true);
        watchdog.schedule(new TimerTask() {
            @Override
            public void run() {
                if (logger.isTraceEnabled()) {
                    logger.trace("Checking for avilable Groovy patches in {}", patchesLookup);
                }
                Resource[] resources = null;
                try {
                    resources = SpringContextSingleton.getInstance().getResources(patchesLookup,
                            false);
                } catch (IOException e) {
                    logger.error(
                            "Error looking up patches in " + patchesLookup + ". Cause: "
                                    + e.getMessage(), e);
                }
                if (resources == null || resources.length == 0) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("No new Groovy patches found in {}. Sleeping...",
                                patchesLookup);
                    }
                    return;
                }

                Arrays.sort(resources, RESOURCE_COMPARATOR);
                executeScripts(resources);
            }
        }, 0, interval);
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

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void setPatchesLookup(String patchesLookup) {
        this.patchesLookup = patchesLookup;
    }

}
