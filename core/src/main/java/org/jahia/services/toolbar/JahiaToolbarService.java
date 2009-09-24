/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.toolbar;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaService;
import org.jahia.services.toolbar.bean.Toolbar;
import org.jahia.services.toolbar.bean.ToolbarSet;
import org.jahia.services.toolbar.xml.ToolbarXMLParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jahia toolbar service.
 * User: jahia
 * Date: 7 avr. 2008
 * Time: 08:55:01
 */
public class JahiaToolbarService extends JahiaService {
    public static final String TOOLBAR_DESCRIPTOR_FILENAME = "toolbar.xml";
    private static Logger logger = Logger.getLogger(JahiaToolbarService.class);
    private static String JAHIA_TOOLBAR_DESCRIPTOR_PATH = null;
    private static JahiaToolbarService singletonInstance = null;
    private Map<String, ToolbarSet> toolbarSetMap = new HashMap<String, ToolbarSet>();
    private ToolbarSet defaultToolbarSet = null;
    public void start() throws JahiaInitializationException {
        init();
    }

    private void init() {
        JAHIA_TOOLBAR_DESCRIPTOR_PATH = settingsBean.getJahiaEtcDiskPath() + File.separator + "toolbar" + File.separator + TOOLBAR_DESCRIPTOR_FILENAME;
        if (logger.isDebugEnabled()) {
            logger
                    .debug("Starting toolbar service. Default toolbar configuration is read from the resource: "
                            + JAHIA_TOOLBAR_DESCRIPTOR_PATH);
        }
        // get all toolbar.xml paths
        List<String> toolbarFilePaths = new ArrayList<String>();
        toolbarFilePaths.add(JAHIA_TOOLBAR_DESCRIPTOR_PATH);

        // get default ToolbarSet from XML descriptor
        ToolbarXMLParser toolbarXMLParser = getToolbarXMLParser(toolbarFilePaths);
        if (toolbarXMLParser != null) {
            defaultToolbarSet = toolbarXMLParser.getToolbars();
        }
        logger.info("Toolbar service is started.");
    }

    public void stop() throws JahiaException {
        defaultToolbarSet = null;
        toolbarSetMap.clear();
        logger.info("Toolbar service is stopped.");
    }

    public static JahiaToolbarService getInstance() {
        if (singletonInstance == null) {
            synchronized (JahiaToolbarService.class) {
                singletonInstance = new JahiaToolbarService();
            }
        }
        return singletonInstance;
    }

    /**
     * Get toolbar set depending on the template set
     *
     * @param processingContext
     * @return
     */
    public ToolbarSet getToolbarSet(ProcessingContext processingContext) {
        //templateSetName == null --> get default toolbar
        String templateSetName = processingContext.getSite().getTemplatePackageName();
        logger.debug("Template set name: " + templateSetName);
        if (templateSetName == null) {
            if (defaultToolbarSet == null) {
                // get all toolbar.xml paths
                List<String> toolbarFilePaths = new ArrayList<String>();
                toolbarFilePaths.add(JAHIA_TOOLBAR_DESCRIPTOR_PATH);

                // get default ToolbarSet from XML descriptor
                ToolbarXMLParser toolbarXMLParser = getToolbarXMLParser(toolbarFilePaths);
                if (toolbarXMLParser != null) {
                    defaultToolbarSet = toolbarXMLParser.getToolbars();
                }
            }
            return defaultToolbarSet;
        }

        //templateSetName != null --> get toolbarSet from map
        ToolbarSet toolbarSet = toolbarSetMap.get(templateSetName);
        if (toolbarSet == null) {
            // get all toolbar.xml paths
            List<String> toolbarFilePaths = getToolbarDescriptorPaths(processingContext);

            // get ToolbarSet from XML descriptor
            ToolbarXMLParser toolbarXMLParser = getToolbarXMLParser(toolbarFilePaths);
            if (toolbarXMLParser != null) {
                toolbarSet = toolbarXMLParser.getToolbars();
                toolbarSetMap.put(templateSetName, toolbarSet);
            }
        }

        return toolbarSet;
    }

    public List<Toolbar> getToolbars() {
        ToolbarSet toolbarSet = getToolbarSet(Jahia.getThreadParamBean());
        if (toolbarSet != null) {
            return toolbarSet.getToolbarList();
        }
        return new ArrayList<Toolbar>();
    }

    public Toolbar getToolbarByIndex(int index) {
        return getToolbars().get(index);
    }

    private ToolbarXMLParser getToolbarXMLParser(List<String> toolbarFilePaths) {
        ToolbarXMLParser toolbarXMLParser = new ToolbarXMLParser(toolbarFilePaths);
        return toolbarXMLParser;
    }

    private List<String> getToolbarDescriptorPaths(ProcessingContext processingContext) {
        JahiaTemplatesPackage jahiaTemplatesPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(processingContext.getSite().getTemplatePackageName());
        List<String> templateSourcePaths = jahiaTemplatesPackage.getLookupPath();
        List<String> toolbarFilePaths = new ArrayList<String>();
        for (int i = 0; i < templateSourcePaths.size(); i++) {
            String currrentToolbarPath = settingsBean.getPathResolver().resolvePath(templateSourcePaths.get(i) + File.separator + "toolbar.xml");
            File f = new File(currrentToolbarPath);
            if (f.exists()) {
                toolbarFilePaths.add(currrentToolbarPath);
                logger.debug(currrentToolbarPath + " added.");
            } else {
                logger.debug(currrentToolbarPath + " does not exist.");
            }
        }
        toolbarFilePaths.add(JAHIA_TOOLBAR_DESCRIPTOR_PATH);
        return toolbarFilePaths;
    }
}
