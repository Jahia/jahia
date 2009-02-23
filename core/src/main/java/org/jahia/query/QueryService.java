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

package org.jahia.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.ValueFactory;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelFactory;

import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.jahia.data.beans.ContainerListBean;
import org.jahia.data.beans.PageBean;
import org.jahia.data.beans.SiteBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.query.filtercreator.FilterCreator;
import org.jahia.query.qom.QueryExecute;
import org.jahia.query.qom.QueryObjectModelFactoryImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaService;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.content.impl.jahia.JahiaContentNodeImpl;
import org.jahia.services.content.impl.jahia.JahiaSiteNodeImpl;
import org.jahia.services.expressions.SearchExpressionContext;
import org.jahia.services.pages.ContentPage;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 20 nov. 2007
 * Time: 10:45:14
 * To change this template use File | Settings | File Templates.
 */
public class QueryService extends JahiaService {

    private static QueryService singletonInstance = null;

    private List<FilterCreator> defaultFilterCreators;

    private Map<String, FilterCreator> filterCreators;

    private ValueFactory valueFactory = ValueFactoryImpl.getInstance();

    protected QueryService() {
        filterCreators = new HashMap<String, FilterCreator>();
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

    public Map<String, FilterCreator> getFilterCreators() {
        return filterCreators;
    }

    public void setFilterCreators(Map<String, FilterCreator> filterCreators) {
        this.filterCreators = filterCreators;
    }

    public void setDefaultFilterCreators(List<FilterCreator> defaultFilterCreators) {
        this.defaultFilterCreators = defaultFilterCreators;
    }

    public List<FilterCreator> getDefaultFilterCreators(){
        return defaultFilterCreators;
    }

    public List<FilterCreator> getFilterCreators(List<String> orderedNames){
        List<FilterCreator> orderedFilterCreators = new ArrayList<FilterCreator>();
        for (String filterCreatorName : orderedNames){
            FilterCreator filterCreator = (FilterCreator)this.filterCreators.get(filterCreatorName);
            if (filterCreator != null){
                orderedFilterCreators.add(filterCreator);
            }
        }
        for (FilterCreator defaultFilterCreator : this
                .getDefaultFilterCreators()) {
            if (!orderedFilterCreators.contains(defaultFilterCreator)) {
                orderedFilterCreators.add(defaultFilterCreator);
            }
        }
        return orderedFilterCreators;
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
        SearchExpressionContext searchExpressionContext = new SearchExpressionContext(context);
        return new QueryObjectModelFactoryImpl(queryExecute,properties,searchExpressionContext,context.getUser());
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
            throws JahiaException {
        if ("/".equals(pathString)){
            return pathString;
        }
        Node n = ServicesRegistry.getInstance().getJCRStoreService().getFileNode(pathString,context.getUser()).getRealNode();
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
