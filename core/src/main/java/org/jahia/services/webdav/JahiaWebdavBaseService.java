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