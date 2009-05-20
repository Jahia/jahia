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
package org.jahia.ajax.gwt.client.data.opensearch;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 18 oct. 2007
 * Time: 17:18:14
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaOpenSearchEngineGroup implements Serializable {

    private String name;
    private List<String> engineNames;
    private List<GWTJahiaOpenSearchEngine> searchEngines = new ArrayList<GWTJahiaOpenSearchEngine>();

    public GWTJahiaOpenSearchEngineGroup() {
    }

    public GWTJahiaOpenSearchEngineGroup(String name, List<String> engineNames) {
        this.name = name;
        this.engineNames = engineNames;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getEngineNames() {
        return engineNames;
    }

    public void setEngineNames(List<String> engineNames) {
        this.engineNames = engineNames;
    }

    public List<GWTJahiaOpenSearchEngine> getSearchEngines() {
        return searchEngines;
    }

    public void setSearchEngines(List<GWTJahiaOpenSearchEngine> searchEngines) {
        this.searchEngines = searchEngines;
    }

    public void addSearchEngine(GWTJahiaOpenSearchEngine searchEngine){
        if (searchEngine != null){
            this.searchEngines.add(searchEngine);
        }
    }
}