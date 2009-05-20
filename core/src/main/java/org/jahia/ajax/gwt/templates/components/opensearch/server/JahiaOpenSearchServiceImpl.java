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
package org.jahia.ajax.gwt.templates.components.opensearch.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jahia.ajax.gwt.commons.server.AbstractJahiaGWTServiceImpl;
import org.jahia.ajax.gwt.client.service.opensearch.GWTOpenSearchService;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngineGroup;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchDescriptor;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngine;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.opensearch.JahiaOpenSearchService;
import org.jahia.services.opensearch.OpenSearchDescriptor;
import org.jahia.services.opensearch.SearchEngineBean;
import org.jahia.services.opensearch.SearchEngineGroupBean;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 18 oct. 2007
 * Time: 14:29:34
 * To change this template use File | Settings | File Templates.
 */
public class JahiaOpenSearchServiceImpl extends AbstractJahiaGWTServiceImpl implements GWTOpenSearchService {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaOpenSearchServiceImpl.class);

    private List<GWTJahiaOpenSearchEngine> searchEngines;

    private Map<String, GWTJahiaOpenSearchEngine> searchEnginesMap;

    private List<GWTJahiaOpenSearchEngineGroup> searchEngineGroups;
    
    public List<GWTJahiaOpenSearchEngine> getSearchEngines() {
        if (searchEngines==null){
            try {
                loadSearchEngines();
            } catch (Throwable t){
                logger.debug(t);
            }
        }
        return searchEngines;
    }

    public List<GWTJahiaOpenSearchEngineGroup> getSearchEngineGroups() {
        if (searchEngineGroups == null) {
            try {
                loadSearchEngines();
            } catch (Throwable t){
                logger.debug(t);
            }
        }
        return searchEngineGroups;
    }

    private void loadSearchEngines() throws Exception {

        searchEngines = new ArrayList<GWTJahiaOpenSearchEngine>();
        searchEnginesMap = new HashMap<String, GWTJahiaOpenSearchEngine>();

        JahiaOpenSearchService openSearchSrv = ServicesRegistry.getInstance().getOpenSearchService();
        List<SearchEngineBean> searchEngineBeans = openSearchSrv.getSearchEngines();
        Iterator<SearchEngineBean> it = searchEngineBeans.iterator();
        SearchEngineBean searchEngineBean;
        GWTJahiaOpenSearchEngine gwtOpenSearchEngine;
        while (it.hasNext()){
            searchEngineBean = it.next();
            gwtOpenSearchEngine = getGWTOpenSearchEngine(searchEngineBean);
            if (gwtOpenSearchEngine != null){
                searchEngines.add(gwtOpenSearchEngine);
                searchEnginesMap.put(searchEngineBean.getName(),gwtOpenSearchEngine);
            }
        }

        searchEngineGroups = new ArrayList<GWTJahiaOpenSearchEngineGroup>();
        List<SearchEngineGroupBean> searchEngineGroupBeans = openSearchSrv.getSearchEngineGroups();
        Iterator<SearchEngineGroupBean> searchEngineGroupIt = searchEngineGroupBeans.iterator();
        SearchEngineGroupBean searchEngineGroupBean;
        GWTJahiaOpenSearchEngineGroup gwtOpenSearchEngineGroup;
        while (searchEngineGroupIt.hasNext()){
            searchEngineGroupBean = searchEngineGroupIt.next();
            gwtOpenSearchEngineGroup = getGWTOpenSearchEngineGroup(searchEngineGroupBean);
            if (gwtOpenSearchEngineGroup != null){
                searchEngineGroups.add(gwtOpenSearchEngineGroup);
                List<String> engineNames = gwtOpenSearchEngineGroup.getEngineNames();
                if (engineNames != null){
                    for(String engineName : engineNames){
                        gwtOpenSearchEngine = searchEnginesMap.get(engineName);
                        gwtOpenSearchEngineGroup.addSearchEngine(gwtOpenSearchEngine);
                    }
                }
            }
        }
    }

    private GWTJahiaOpenSearchEngine getGWTOpenSearchEngine(SearchEngineBean searchEngineBean){
        if (searchEngineBean == null){
            return null;
        }
        GWTJahiaOpenSearchEngine gwtOSE = new GWTJahiaOpenSearchEngine();
        gwtOSE.setName(searchEngineBean.getName());
        gwtOSE.setUrlType(searchEngineBean.getUrlType());
        gwtOSE.setDescriptorType(searchEngineBean.getDescriptorType());
        GWTJahiaOpenSearchDescriptor gwtOSD = new GWTJahiaOpenSearchDescriptor();
        OpenSearchDescriptor descriptor = searchEngineBean.getDescriptor();
        if (descriptor != null){
            gwtOSD.setDescription(descriptor.getDescription());
            gwtOSD.setShortName(descriptor.getShortName());
            gwtOSD.setImages(descriptor.getImages());
            gwtOSD.setUrls(descriptor.getUrls());
            gwtOSD.setNamespaces(descriptor.getNamespaces());
            gwtOSE.setDescriptor(gwtOSD);
        }
        return gwtOSE;
    }

    private GWTJahiaOpenSearchEngineGroup getGWTOpenSearchEngineGroup(SearchEngineGroupBean searchEngineGroup){
        if (searchEngineGroup == null){
            return null;
        }
        GWTJahiaOpenSearchEngineGroup gwtOSEG = new GWTJahiaOpenSearchEngineGroup();
        gwtOSEG.setName(searchEngineGroup.getName());
        gwtOSEG.setEngineNames(searchEngineGroup.getEngineNames());
        return gwtOSEG;
    }

}
