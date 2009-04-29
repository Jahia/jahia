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
 package org.jahia.services.importexport;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 30 août 2005
 * Time: 14:41:08
 * To change this template use File | Settings | File Templates.
 */

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.axis.encoding.Base64;
import org.apache.log4j.xml.DOMConfigurator;
import org.jahia.bin.ClassesPreloadManager;
import org.jahia.bin.Jahia;
import org.jahia.hibernate.manager.JahiaSiteManager;
import org.jahia.hibernate.manager.JahiaSitePropertyManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.mbeans.JahiaMBeanServer;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.FilePathResolver;
import org.jahia.utils.PathResolver;
import org.springframework.context.ApplicationContext;
import org.xml.sax.SAXException;

public class ImportExportBaseServiceTest extends TestCase {
    ImportExportBaseService importExportBaseService;
    private static final String LOG4J_FILE = "src" + File.separator +
                                             "test" + File.separator +
                                             "etc" + File.separator +
                                             "log4j.xml";
    private static final String PROP_FILE = "src" + File.separator +
                                            "test" + File.separator +
                                            "etc" + File.separator +
                                            "production" + File.separator +
                                            "jahia_productiontest.properties";

    private static final String PRELOAD_CLASSES_FILENAME = "src" + File.separator +
                                                           "test" + File.separator +
                                                           "etc" + File.separator +
                                                           "production" + File.separator +
                                                           "preloadclasses.xml";
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ImportExportBaseService.class);

    private final static String springRelativePath = "src" + File.separator +
                                                     "test" + File.separator +
                                                     "etc" + File.separator +
                                                     "production";
    private ApplicationContext context;

    protected synchronized void setUp() throws Exception {
        super.setUp();
        DOMConfigurator.configureAndWatch(LOG4J_FILE);
        PathResolver pathResolver = new FilePathResolver();
        File jahiaCacheProperties = new File(PROP_FILE);
        logger.info("Loading Jahia configuration from " + jahiaCacheProperties.getAbsoluteFile().toString());
        SettingsBean settingsBean = new SettingsBean(pathResolver, jahiaCacheProperties.getAbsoluteFile().toString(), "", Jahia.getBuildNumber());
        settingsBean.load();
        JahiaMBeanServer.getInstance().init(settingsBean);
        ServicesRegistry.getInstance().init(settingsBean);
        context = SpringContextSingleton.getInstance().getContext();
        importExportBaseService = (ImportExportBaseService) context.getBean("ImportExportService");
        importExportBaseService.setSettingsBean(settingsBean);
//        SchedulerService schedulerService = (SchedulerService) context.getBean("SchedulerService");
//        schedulerService.start();
//        ((JahiaACLManagerService)context.getBean("JahiaACLManagerService")).start();
        logger.info("Waiting for wake-up of all nodes...");
        // now let's preload some classes that have static initializations
        // that need to be performed before we go further...
        String preloadConfigurationFileName = PRELOAD_CLASSES_FILENAME;
        try {
            // the constructor does everything, including loading the classes,
            // so we don't need to do anything besides creating an instance that
            // we can dispose of immediately after.
            ClassesPreloadManager preloadManager = new ClassesPreloadManager(preloadConfigurationFileName);
            preloadManager = null;
        } catch (IOException ioe) {
            logger.debug("IO exception raised while trying to load classes preload XML configuration file [" +
                         preloadConfigurationFileName + "]", ioe);
        } catch (SAXException saxe) {
            logger.debug("IO exception while trying to parse classes preload XML configuration file [" +
                         preloadConfigurationFileName + "]", saxe);
        } catch (ClassNotFoundException cnfe) {
            logger.debug("Could not preload class because it couldn't be found",
                         cnfe);
        }
    }

    public void testStartProductionJob() throws Exception {
//        try {
            JahiaSiteManager siteManager = (JahiaSiteManager) context.getBean(JahiaSiteManager.class.getName());
            JahiaSite site = siteManager.getSiteById(1);
            Properties settings = site.getSettings();
            String value = "http://localhost:8080";
            settings.setProperty(ImportExportBaseService.PRODUCTION_TARGET_LIST_PROPERTY, value);
            settings.setProperty(ImportExportBaseService.PRODUCTION_CRON_PROPERTY + value, "0 */1 * * * ?");
            settings.setProperty(ImportExportBaseService.PRODUCTION_USERNAME_PROPERTY + value, "root");
            settings.setProperty(ImportExportBaseService.PRODUCTION_PASSWORD_PROPERTY + value, new String(Base64.encode("secret11".getBytes())));
            ((JahiaSitePropertyManager) context.getBean(JahiaSitePropertyManager.class.getName())).save(site);
//            importExportBaseService.startProductionJob(site);
            Thread.sleep(35000);
//        } catch (ParseException ex) {
//            fail("have thrown an Exception : " + ex.getMessage());
//        }
    }
}