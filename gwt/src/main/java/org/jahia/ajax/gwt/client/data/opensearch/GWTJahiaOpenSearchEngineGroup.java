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