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