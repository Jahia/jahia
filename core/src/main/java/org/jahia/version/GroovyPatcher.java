/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
