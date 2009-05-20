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
            FilterCreator filterCreator = (FilterCreator)this.filterCreators.get(filterCreatorName.trim());
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
