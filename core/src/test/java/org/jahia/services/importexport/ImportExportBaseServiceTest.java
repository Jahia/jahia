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
 package org.jahia.services.importexport;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 30 août 2005
 * Time: 14:41:08
 * To change this template use File | Settings | File Templates.
 */

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.axis.encoding.Base64;
import org.apache.log4j.xml.DOMConfigurator;
import org.jahia.bin.Jahia;
import org.jahia.hibernate.manager.JahiaSiteManager;
import org.jahia.hibernate.manager.JahiaSitePropertyManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.FilePathResolver;
import org.jahia.utils.PathResolver;
import org.springframework.context.ApplicationContext;

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

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ImportExportBaseService.class);

    private ApplicationContext context;

    protected synchronized void setUp() throws Exception {
        super.setUp();
        DOMConfigurator.configureAndWatch(LOG4J_FILE);
        PathResolver pathResolver = new FilePathResolver();
        File jahiaCacheProperties = new File(PROP_FILE);
        logger.info("Loading Jahia configuration from " + jahiaCacheProperties.getAbsoluteFile().toString());
        SettingsBean settingsBean = new SettingsBean(pathResolver, jahiaCacheProperties.getAbsoluteFile().toString(), "", Jahia.getBuildNumber());
        settingsBean.load();
        ServicesRegistry.getInstance().init(settingsBean);
        context = SpringContextSingleton.getInstance().getContext();
        importExportBaseService = (ImportExportBaseService) context.getBean("ImportExportService");
        importExportBaseService.setSettingsBean(settingsBean);
//        SchedulerService schedulerService = (SchedulerService) context.getBean("SchedulerService");
//        schedulerService.start();
//        ((JahiaACLManagerService)context.getBean("JahiaACLManagerService")).start();
        logger.info("Waiting for wake-up of all nodes...");
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