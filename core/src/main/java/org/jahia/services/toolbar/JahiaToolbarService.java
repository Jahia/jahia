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
            List<String> toolbarFilePaths = getToobalDescriptorPaths(processingContext);

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

    private List<String> getToobalDescriptorPaths(ProcessingContext processingContext) {
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
