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
//
//
//  WebAppsObserver
//
//  NK      23.04.2001
//
//


package org.jahia.services.deamons.filewatcher.webappobserver;

import java.io.Serializable;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.deamons.filewatcher.JahiaFileWatcherService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.webapps_deployer.JahiaWebAppsDeployerService;

/**
 * An Observable object for web apps
 *
 * @author Khue ng
 * @version 1.0
 */
public class WebAppsObserver implements Observer, Serializable {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(WebAppsObserver.class);

    /**
     * updatedOnce true if update() has been executed once *
     */
    private boolean mUpdatedOnce = false;

    private JahiaWebAppsDeployerService ads;
    private JahiaSitesService ss;

    /**
     * Constructor
     *
     * @param path      the path of directory to watch.
     * @param checkDate check by last modif date or not
     * @param interval  the interval of check
     * @param fileOnly  check both new files and directory
     */
    public WebAppsObserver(String path,
            boolean checkDate,
            long interval,
            boolean fileOnly,
            JahiaSitesService sitesService,
            JahiaFileWatcherService fws,
            JahiaWebAppsDeployerService wads)
            throws JahiaException {

        this.ss = sitesService;
        this.ads = wads;
        if (fws != null) {

            try {
                fws.addFileWatcher(path, path, checkDate, interval, fileOnly);
                fws.registerObserver(path, this);
                fws.startFileWatcher(path);

            } catch (JahiaException e) {

                logger.error("init:: ", e);
                throw new JahiaInitializationException(
                        "WebAppsObserver::init failed ", e);
            }
        }
    }

    /**
     * Handles new files detection
     */
    public void update(Observable subject,
                       Object args) {

        synchronized (args) {

            if (ads != null
                && ss != null && Jahia.isInitiated()) {

                List files = (List) args;
                try {
                    ads.deploy(files);
                } catch (Exception t) {
                    logger.error("Web apps observer error : ", t);
                }
            }
        }
    }

}