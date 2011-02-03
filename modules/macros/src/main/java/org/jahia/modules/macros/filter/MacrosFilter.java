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
package org.jahia.modules.macros.filter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.utils.ScriptEngineUtils;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.RepositoryException;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 21/12/10
 */
public class MacrosFilter extends AbstractFilter implements InitializingBean {
    private transient static Logger logger = Logger.getLogger(MacrosFilter.class);

    private Pattern macrosPattern;
    private String macrosRegexp;
    private JahiaTemplateManagerService templateManagerService;
    private String macrosDirectoryName;
    private Map<String, String> scriptCache;
    private ScriptEngineUtils scriptEngineUtils;

    public void setMacrosRegexp(String macrosRegexp) {
        this.macrosRegexp = macrosRegexp;
        this.macrosPattern = Pattern.compile(macrosRegexp);
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        if (!"".equals(previousOut.trim())) {
            Matcher matcher = macrosPattern.matcher(previousOut);
            while (matcher.find()) {
                String macroName = matcher.group(1);
                //  Check if macros already in the cache of resolved script
                String macroPath = scriptCache.get(macroName);
                if (macroPath == null) {
                    //  find the macros script matching this name scan all modules macros directory
                    List<JahiaTemplatesPackage> availableTemplatePackages = templateManagerService.getAvailableTemplatePackages();
                    for (JahiaTemplatesPackage availableTemplatePackage : availableTemplatePackages) {
                        File file = new File(
                                availableTemplatePackage.getFilePath() + File.separator + macrosDirectoryName);
                        if (file.exists()) {
                            // Modules have macros
                            String[] files = file.list(new WildcardFileFilter(macroName + ".*"));
                            if (files.length == 1) {
                                //  Store it in a cache
                                macroPath = file.getAbsolutePath() + File.separator + files[0];
                                scriptCache.put(macroName, macroPath);
                            }
                        }
                    }
                }
                if (macroPath != null) {
                    //  execute macro
                    String extension = FilenameUtils.getExtension(macroPath);
                    ScriptEngine scriptEngine = scriptEngineUtils.getEngineByExtension(extension);
                    ScriptContext scriptContext = scriptEngine.getContext();
                    scriptContext.setWriter(new StringWriter());
                    scriptContext.setErrorWriter(new StringWriter());
                    scriptEngine.eval(new FileReader(macroPath), getBindings(renderContext, resource, scriptContext, matcher));
                    String scriptResult = scriptContext.getWriter().toString().trim();
                    previousOut = matcher.replaceFirst(scriptResult);
                } else {
                    previousOut = matcher.replaceFirst("macro " + macroName + " not found");
                }
                matcher = macrosPattern.matcher(previousOut);
            }
        }
        return previousOut;
    }

    private Bindings getBindings(RenderContext renderContext, Resource resource, ScriptContext scriptContext, Matcher matcher) {
        Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("currentUser", renderContext.getUser());
        bindings.put("currentNode", resource.getNode());
        bindings.put("currentResource", resource);
        bindings.put("renderContext", renderContext);
        bindings.put( "url", new URLGenerator(renderContext, resource));
        int i = 1;
        for (String s : StringUtils.split(matcher.group(2), ",")) {
            s = StringUtils.substringAfter(s,"\"");
            s = StringUtils.substringBeforeLast(s,"\"");
            bindings.put("param"+(i++), s);
        }
        try {
            bindings.put("currentAliasUser", renderContext.getMainResource().getNode().getSession().getAliasedUser());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return bindings;
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
        scriptCache = new LinkedHashMap<String, String>();
    }

    public void setMacrosDirectoryName(String macrosDirectoryName) {
        this.macrosDirectoryName = macrosDirectoryName;
    }

    public void setScriptEngineUtils(ScriptEngineUtils scriptEngineUtils) {
        this.scriptEngineUtils = scriptEngineUtils;
    }
}
