/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.version;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 23 août 2007
 * Time: 18:40:49
 * To change this template use File | Settings | File Templates.
 */
public class GroovyPatcher implements Patcher {
    private Logger logger = Logger.getLogger(GroovyPatcher.class);
    private GroovyScriptEngine scriptEngine;

    public GroovyPatcher() {
        try {
            scriptEngine = new GroovyScriptEngine(org.jahia.settings.SettingsBean.getInstance().getJahiaVarDiskPath() + File.separator + "patches");
        } catch (IOException e) {
            logger.error("Cannot instantiate groovyt engine",e);
        }
    }

    public boolean canHandlePatch(Patch patch, int lastVersion, int currentVersion) {
        if ("groovy".equals(patch.getExt()) && scriptEngine != null) {
            return patch.getNumber() == 0 || (patch.getNumber() > lastVersion && patch.getNumber() <= currentVersion);
        }
        return false;
    }

    public int execute(Patch patch) {
        try {
            Binding binding = new Binding();
            File file = patch.getFile();
            String absolutePath = file.getAbsolutePath();
            logger.info("absolutePath = " + absolutePath);
            String patchesPath = (new File(org.jahia.settings.SettingsBean.getInstance().getJahiaVarDiskPath() + File.separator + "patches")).getAbsolutePath();
            logger.info("patchesPath = " + patchesPath);
            String path = absolutePath.substring( patchesPath.length()+ File.separator.length());
            logger.info("path = " + path);
            scriptEngine.run(path, binding);
        } catch (Exception e) {
            logger.error("Exception when executing script",e);
            return 2;
        }

        return 0;
    }
}
