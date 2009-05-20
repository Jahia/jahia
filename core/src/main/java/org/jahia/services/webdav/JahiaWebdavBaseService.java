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
 package org.jahia.services.webdav;

import org.apache.log4j.Logger;
import org.jahia.data.files.JahiaFileField;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaService;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.fields.ContentField;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 7, 2003
 * Time: 1:00:56 PM
 * To change this template use Options | File Templates.
 * @deprecated Use JCRStoreService instead
 */
public class JahiaWebdavBaseService extends JahiaService implements JahiaWebdavService {

    private static Logger logger = Logger.getLogger (JahiaWebdavBaseService.class);
    private static JahiaWebdavBaseService instance;
    private JahiaFieldsDataManager fieldsDataManager = null;
    private JahiaFieldXRefManager fieldXRefManager = null;
    public static JahiaWebdavBaseService getInstance () {
        if (instance == null) {
            instance = new JahiaWebdavBaseService ();
        }
        return instance;
    }

    public void setFieldXRefManager(JahiaFieldXRefManager fieldXRefManager) {
        this.fieldXRefManager = fieldXRefManager;
    }

    public void setFieldsDataManager(JahiaFieldsDataManager fieldsDataManager) {
        this.fieldsDataManager = fieldsDataManager;
    }

    public JahiaWebdavBaseService() {
    }

    public void start() throws JahiaInitializationException {
    }

    public void stop() throws JahiaException {
    }

    public JCRNodeWrapper getDAVFileAccess (ProcessingContext jParams, String path) {
        return ServicesRegistry.getInstance().getJCRStoreService().getFileNode(path, jParams.getUser());
    }

    public JCRNodeWrapper getDAVFileAccess(String path, JahiaUser user) {
        return ServicesRegistry.getInstance().getJCRStoreService().getFileNode(path, user);
    }

    public JCRNodeWrapper getDAVFileAccess (JahiaSite site, String path) {
        return ServicesRegistry.getInstance().getJCRStoreService().getFileNode(path, null);
    }

    public JahiaFileField getJahiaFileField (ProcessingContext jParams, String path) {
        JCRNodeWrapper object = getDAVFileAccess (jParams, path);
        return object.getJahiaFileField ();
    }

    public JahiaFileField getJahiaFileField (JahiaSite site, String path) {
        JCRNodeWrapper object = getDAVFileAccess (site, path);
        return object.getJahiaFileField ();
    }

    public JahiaFileField getJahiaFileField (ProcessingContext jParams, JahiaSite site, JahiaUser user, String path) {
        JCRNodeWrapper object = getDAVFileAccess (path, user);
        return object.getJahiaFileField ();
    }


}