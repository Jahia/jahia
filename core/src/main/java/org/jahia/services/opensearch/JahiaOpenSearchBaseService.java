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
package org.jahia.services.opensearch;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * OpenSearch Service.
 *
 * @author Khue Nguyen
 */
public class JahiaOpenSearchBaseService extends JahiaOpenSearchService {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger( JahiaOpenSearchBaseService.class);

    static private JahiaOpenSearchBaseService instance = null;

    private String configFileName = "opensearch_engines.xml";
    private String searchEngineConfigFilePath;
    private String searchEngineConfigFileFolder;
    private List<SearchEngineBean> searchEngines;
    private List<SearchEngineGroupBean> searchEngineGroups;
    private SearchEngineDigester searchEngineDigester;

    protected JahiaOpenSearchBaseService() {
    }

    public static JahiaOpenSearchBaseService getInstance() {
        if (instance == null) {
            instance = new JahiaOpenSearchBaseService();
        }
        return instance;
    }

    public void start()
            throws JahiaInitializationException {

        StringBuffer buff = new StringBuffer(settingsBean.getJahiaEtcDiskPath());
        buff.append(File.separator);
        buff.append("opensearch");
        searchEngineConfigFileFolder = buff.toString();
        buff.append(File.separator);
        buff.append(this.configFileName);
        searchEngineConfigFilePath = buff.toString();
        searchEngineDigester = new SearchEngineDigester();
        try {
            loadSearchEngines();
        } catch ( Throwable t ){
            logger.debug(t);
        }
    }

    public void stop() {
    }

    /**
     * @throws JahiaException
     */
    public void loadSearchEngines() throws JahiaException {

        searchEngineDigester.loadSearchEngines(searchEngineConfigFilePath);
        searchEngines = searchEngineDigester.getSearchEngineBeans();
        if (searchEngines == null){
            searchEngines = new ArrayList<SearchEngineBean>();
        }
        Iterator<SearchEngineBean> it = searchEngines.iterator();
        SearchEngineBean searchEngineBean = null;
        OpenSearchDescriptor searchDescriptor = null;
        while (it.hasNext()){
            searchEngineBean = it.next();
            try {
                searchDescriptor = new OpenSearchDescriptor(searchEngineConfigFileFolder
                    + File.separator + searchEngineBean.getDescriptorFile());
                searchEngineBean.setDescriptor(searchDescriptor);
            } catch (Throwable t){
                logger.debug(t);
            }
        }
        searchEngineGroups = searchEngineDigester.getSearchEngineGroupBeans();
        if (searchEngineGroups == null){
            searchEngineGroups = new ArrayList<SearchEngineGroupBean>();
        }
    }

    public List<SearchEngineBean> getSearchEngines() throws JahiaException {
        return searchEngines;
    }

    public List<SearchEngineGroupBean> getSearchEngineGroups() {
        return searchEngineGroups;
    }

    public void setSearchEngineGroups(List<SearchEngineGroupBean> searchEngineGroups) {
        this.searchEngineGroups = searchEngineGroups;
    }

}