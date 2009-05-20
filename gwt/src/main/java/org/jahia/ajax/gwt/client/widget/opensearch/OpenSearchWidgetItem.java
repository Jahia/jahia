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
package org.jahia.ajax.gwt.client.widget.opensearch;

import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngine;
import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaURL;
import org.jahia.ajax.gwt.client.widget.rss.RSSWidget;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;

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
    private GWTJahiaURL searchURLTemplate;
    private GWTJahiaURL rssSearchURLTemplate;
    private int index;
    private boolean rssFeedMode;
    private GWTJahiaOpenSearchEngine searchEngine;

    public OpenSearchWidgetItem(GWTJahiaOpenSearchEngine searchEngine) {
        super(0, "", 200,10); ///@todo retrieve from user preference
        this.serverPath = JahiaGWTParameters.getJahiaServerPath();
        this.searchEngine = searchEngine;
        this.name = searchEngine.getName();
        this.searchEngine = searchEngine;
        if (searchEngine.getDescriptor() != null){
            try {
                this.title = searchEngine.getDescriptor().getShortName();
                if (searchEngine.getDescriptor().getUrls() != null){
                    for (GWTJahiaURL searchEngineURL : searchEngine.getDescriptor().getUrls()){
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

    public GWTJahiaOpenSearchEngine getSearchEngine() {
        return searchEngine;
    }

    public void setSearchEngine(GWTJahiaOpenSearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }
}
