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
