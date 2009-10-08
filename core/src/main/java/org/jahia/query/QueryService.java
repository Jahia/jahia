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
package org.jahia.query;

import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.apache.log4j.Logger;
import org.jahia.data.beans.ContainerListBean;
import org.jahia.data.beans.PageBean;
import org.jahia.data.beans.SiteBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.query.qom.QueryExecute;
import org.jahia.services.JahiaService;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.impl.jahia.JahiaContentNodeImpl;
import org.jahia.services.content.impl.jahia.JahiaSiteNodeImpl;
import org.jahia.services.pages.ContentPage;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 20 nov. 2007
 * Time: 10:45:14
 * To change this template use File | Settings | File Templates.
 */
public class QueryService extends JahiaService {
    private static transient Logger logger = Logger.getLogger(QueryService.class);
    private static QueryService singletonInstance = null;

    private ValueFactory valueFactory = ValueFactoryImpl.getInstance();
    
    private transient JCRSessionFactory sessionFactory;

    protected QueryService() {
    }

    /**
     * Return the unique service instance. If the instance does not exist,
     * a new instance is created.
     *
     * @return The unique service instance.
     */
    public synchronized static QueryService getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new QueryService();
        }
        return singletonInstance;
    }

    /**
     * Initializes the servlet dispatching service with parameters loaded
     * from the Jahia configuration file.
     * @throws org.jahia.exceptions.JahiaInitializationException thrown in the case of an error
     * during this initialization, that will be treated as a critical error
     * in Jahia and probably stop execution of Jahia once and for all.
     */
    public void start()
        throws JahiaInitializationException {
    }

    public void stop() {
    }
    
    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Entry point to request a QueryObjectModelFactory
     *
     * @param queryExecute
     * @param context
     * @param properties
     * @return
     * @throws JahiaException
     */
    public QueryObjectModelFactory getQueryObjectModelFactory(QueryExecute queryExecute, ProcessingContext context, Properties properties)
            throws JahiaException {
        try {
            return sessionFactory.getCurrentUserSession().getWorkspace().getQueryManager().getQOMFactory();
        } catch (RepositoryException e) {
            throw new JahiaException("Error while creating QOMFactory","Error while creating QOMFactory",
                                     JahiaException.REGISTRY_ERROR,JahiaException.ERROR_SEVERITY,e);
        }
    }

    /**
     * Entry point to request a ValueFactory instance
     *
     * @return
     */
    public ValueFactory getValueFactory()
            throws JahiaException {
        return this.valueFactory;
    }

//    public PathFactory getPathFactory() {
//        return pathFactory;
//    }

    /**
     *
     * @param contentObject
     * @param context
     * @return
     * @throws JahiaException
     */

//    public Path getPath(ContentObject contentObject, ProcessingContext context)
//            throws JahiaException {
//        Path path = null;
//        if ( contentObject instanceof ContentPage) {
//            String jcrPath = contentObject.getJCRPath(context);
//            path = this.pathFactory.create(jcrPath);
//        }
//        return path;
//    }

    /**
     *
     * @param pathString
     * @param context
     * @return
     * @throws JahiaException
     */

//    public Path getPath(String pathString, ProcessingContext context)
//            throws JahiaException {
//        pathString = StringUtils.replaceChars(pathString,'/',Path.DELIMITER);
//        return this.pathFactory.create(pathString);
//    }

    /**
     *
     * @param pathString
     * @param context
     * @return
     * @throws JahiaException
     */
    public Object getPathObject(String pathString, ProcessingContext context)
            throws JahiaException, RepositoryException {
        if ("/".equals(pathString)){
            return pathString;
        }
        Node n = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(pathString).getRealNode();
        if (n instanceof JahiaContentNodeImpl) {
            Object result = ((JahiaContentNodeImpl)n).getContentObject();
            if (result instanceof ContentPage){
                result = new PageBean(((ContentPage)result).getPage(context),context);
            } else if (result instanceof ContentContainerList){
                result = new ContainerListBean(((ContentContainerList)result)
                        .getJahiaContainerList(context,context.getEntryLoadRequest()),
                        context);
            }
            return result;
        } else if (n instanceof JahiaSiteNodeImpl) {
            return new SiteBean(((JahiaSiteNodeImpl)n).getSite(), context);
        }
        return null;
//        if ("/".equals(pathString)){
//            return pathString;
//        }
//        if (pathString.startsWith("/")){
//            pathString = pathString.substring(1);
//        }
//        Object result = null;
//        Path path = getPath(pathString,context);
//        if (path==null){
//            return null;
//        }
//        if (path.getLength()==1){
//            try {
//                JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService()
//                    .getSiteByKey(path.getElements()[0].getName().getLocalName());
//                result = site.getSiteKey();
//                result = new SiteBean(site,context);
//            } catch ( Exception t ){
//                logger.debug(t);
//            }
//        } else {
//            Path.Element lastElement = path.getElements()[path.getElements().length-1];
//            result = getContentObject(lastElement,context);
//            if (result != null){
//                if (result instanceof ContentPage){
//                    result = new PageBean(((ContentPage)result).getPage(context),context);
//                } else if (result instanceof ContentContainerList){
//                    result = new ContainerListBean(((ContentContainerList)result)
//                            .getJahiaContainerList(context,context.getEntryLoadRequest()),
//                            context);
//                }
//            }
//        }
//        return result;
    }

//    public ContentObject getContentObject(Path.Element element, ProcessingContext context){
//        if (element == null){
//            return null;
//        }
//        ContentObject contentObject = null;
//        try {
//            contentObject = ContentObject.getContentObjectInstance(
//                    ObjectKey.getInstance(element.getName().getLocalName()));
//        } catch ( Exception t){
//            logger.debug(t);
//        }
//        return contentObject;
//    }

//    public void setPathFactory(PathFactory pathFactory) {
//        this.pathFactory = pathFactory;
//    }

//    public NameFactory getNameFactory() {
//        return nameFactory;
//    }

//    public void setNameFactory(NameFactory nameFactory) {
//        this.nameFactory = nameFactory;
//    }

}
