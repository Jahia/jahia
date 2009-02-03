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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.components.opensearch.client.ui;

import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.templates.components.opensearch.client.model.GWTOpenSearchEngine;
import org.jahia.ajax.gwt.templates.components.opensearch.client.model.URL;
import org.jahia.ajax.gwt.commons.client.ui.rss.RSSWidget;
import org.jahia.ajax.gwt.config.client.JahiaGWTParameters;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 8 oct. 2007
 * Time: 21:21:33
 * To change this template use File | Settings | File Templates.
 */
public class OpenSearchWidgetItem extends RSSWidget {

    private String name;
    private String title;
    private String serverPath;
    private URL searchURLTemplate;
    private URL rssSearchURLTemplate;
    private int index;
    private boolean rssFeedMode;
    private GWTOpenSearchEngine searchEngine;

    public OpenSearchWidgetItem(GWTOpenSearchEngine searchEngine) {
        super(0, "", 200,10); ///@todo retrieve from user preference
        this.serverPath = JahiaGWTParameters.getJahiaServerPath();
        this.searchEngine = searchEngine;
        this.name = searchEngine.getName();
        this.searchEngine = searchEngine;
        if (searchEngine.getDescriptor() != null){
            try {
                this.title = searchEngine.getDescriptor().getShortName();
                if (searchEngine.getDescriptor().getUrls() != null){
                    for (URL searchEngineURL : searchEngine.getDescriptor().getUrls()){
                        if(searchEngineURL.getType().toLowerCase().indexOf("rss") >0){
                            this.rssSearchURLTemplate = searchEngineURL;
                        } else {
                            this.searchURLTemplate = searchEngineURL;
                        }
                    }
                }
                if (this.searchURLTemplate == null){
                    this.searchURLTemplate = this.rssSearchURLTemplate;
                }
            } catch (Exception e){
                Log.debug("Exception occured at loading search engine url templates",e);
            }
        }
        if (this.title == null){
            this.title = searchEngine.getName();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isRssFeedMode() {
        return rssFeedMode;
    }

    public void setRssFeedMode(boolean rssFeedMode) {
        this.rssFeedMode = rssFeedMode;
    }

    public void search(String searchTerm){
        if (!this.rssFeedMode) {
            try {
                if (this.searchURLTemplate != null && this.searchURLTemplate.getTemplate() != null){
                    String searchURL =  this.searchURLTemplate.getTemplate().getSearchURL(searchTerm);
                    int pos = searchURL.indexOf("${serverPath}");
                    if ( pos != -1 ){
                        searchURL = serverPath + searchURL.substring("${serverPath}".length());
                    }
                    this.setFeedUrl(searchURL);
                    this.setUrl(searchURL);
                }
            } catch ( Throwable t){
                Log.debug("Exception occured at search execution",t);
            }
        } else {
            try {
                if (this.rssSearchURLTemplate != null && this.rssSearchURLTemplate.getTemplate() != null){
                    String searchURL = this.rssSearchURLTemplate.getTemplate().getSearchURL(searchTerm);
                    int pos = searchURL.indexOf("${serverPath}");
                    if ( pos != -1 ){
                        searchURL = serverPath + searchURL.substring("${serverPath}".length());
                    }
                    this.setFeedUrl(searchURL);
                    this.execute();
                }                    
            } catch ( Throwable t ) {
                Log.debug("Exception occured at search execution",t);
            }
        }
    }

    public GWTOpenSearchEngine getSearchEngine() {
        return searchEngine;
    }

    public void setSearchEngine(GWTOpenSearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }
}
