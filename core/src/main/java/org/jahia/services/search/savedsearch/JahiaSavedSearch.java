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
 package org.jahia.services.search.savedsearch;

import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.acl.JahiaACLException;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.engines.search.FileSearchViewHandler;
import org.jahia.engines.search.SearchCriteriaFactory;
import org.jahia.engines.search.SearchViewHandler;
import org.jahia.utils.JahiaTools;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.StringReader;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 22 f�vr. 2006
 * Time: 09:50:17
 * To change this template use File | Settings | File Templates.
 */
public class JahiaSavedSearch {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaSavedSearch.class);

    public static final String CDATA_OPEN_TAG = "CDATA_OPEN_TAG";
    public static final String CDATA_CLOSE_TAG = "CDATA_CLOSE_TAG";

    public static final String SEARCH_ELEMENT = "search";

    public static final String QUERY_ELEMENT = "query";

    private Integer id = new Integer(-1);

    private String title = "";

    private String descr = "";

    private String search = "";

    private Long creationDate = new Long(0);

    private String ownerKey = "";

    private String searchViewHandlerClass = "";

    private int sideId;

    private JahiaBaseACL acl;

    public JahiaSavedSearch(){
    }

    public JahiaSavedSearch(Integer id,
                            String title,
                            String descr,
                            String search,
                            Long creationDate,
                            String ownerKey,
                            String searchViewHandlerClass,
                            int siteId,
                            JahiaBaseACL acl){
        this.id = id;
        this.title = title;
        this.descr = descr;
        this.search = search;
        this.creationDate = creationDate;
        this.ownerKey = ownerKey;
        this.searchViewHandlerClass = searchViewHandlerClass;
        this.sideId = siteId;
        this.acl = acl;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public String getOwnerKey() {
        return ownerKey;
    }

    public void setOwnerKey(String ownerKey) {
        this.ownerKey = ownerKey;
    }

    public String getSearchViewHandlerClass() {
        return searchViewHandlerClass;
    }

    public void setSearchViewHandlerClass(String searchViewHandlerClass) {
        this.searchViewHandlerClass = searchViewHandlerClass;
    }

    public int getSideId() {
        return sideId;
    }

    public void setSideId(int sideId) {
        this.sideId = sideId;
    }

    public JahiaBaseACL getAcl() {
        return acl;
    }

    public void setAcl(JahiaBaseACL acl) {
        this.acl = acl;
    }

    public boolean isPublic() throws JahiaACLException {
        if (this.acl == null){
            return false;
        }
        JahiaGroup guestGroup = ServicesRegistry.getInstance().getJahiaGroupManagerService()
                .lookupGroup(this.sideId, JahiaGroupManagerService.GUEST_GROUPNAME);
        return this.acl.getPermission(guestGroup,JahiaBaseACL.READ_RIGHTS);
    }

    public void allowGuest(boolean allowGuest) throws JahiaACLException {
        if ( this.acl == null){
            return;
        }
        if ( isPublic() && !allowGuest ){
            JahiaGroup guestGroup = ServicesRegistry.getInstance().getJahiaGroupManagerService()
            .lookupGroup(this.sideId, JahiaGroupManagerService.GUEST_GROUPNAME);
            this.acl.removeGroupEntry(guestGroup);
        } else if ( !isPublic() && allowGuest ) {
            JahiaGroup guestGroup = ServicesRegistry.getInstance().getJahiaGroupManagerService()
            .lookupGroup(this.sideId, JahiaGroupManagerService.GUEST_GROUPNAME);
            // enable guest user to access the page
            JahiaAclEntry guestAclEntry = new JahiaAclEntry(1,0);
            this.acl.setGroupEntry(guestGroup,guestAclEntry);
        }
    }

    public String  getQuery() {
        String query = "";
        try
        {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new StringReader(getSearch()));
            Element root = document.getRootElement();
            if (root != null)
            {
                Element el = root.element(QUERY_ELEMENT);
                if ( el != null ){
                    query = decodeCDATA_Tag(el.getText());
                }
            }
        }
        catch (Exception t){
            logger.debug("Error parsing JahiaSavedSearch xml",t);
        }
        return query;
    }

    /**
     * Return the search mode @see SearchViewHandler.SEARCH_MODE. -1 on error or not presents
     * @return
     */
    public int getSearchMode() {
        int searchMode = -1;
        try {
            if (getSearchViewHandlerClass().equals(
                    FileSearchViewHandler.class.getName())) {
                searchMode = SearchViewHandler.SEARCH_MODE_JCR;
            } else {
                SAXReader reader = new SAXReader();
                Document document = reader.read(new StringReader(getSearch()));
                Element root = document.getRootElement();
                if (root != null) {
                    Element el = root.element(SearchViewHandler.SEARCH_MODE);
                    if (el != null) {
                        try {
                            searchMode = Integer.parseInt(el.getText().trim());
                        } catch (Exception t) {
                        }
                    }
                }
            }
        }
        catch (Exception t){
            logger.debug("Error parsing JahiaSavedSearch xml",t);
        }
        return searchMode;
    }

    public String getPermission() {
        try
        {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new StringReader(getSearch()));
            Element root = document.getRootElement();
            if (root != null)
            {
                Element el = root.element("accessPermission");
                if ( el != null ){
                    try {
                        return el.getText().trim();
                    } catch ( Exception t ){
                    }
                }
            }
        }
        catch (Exception t){
            logger.debug("Error parsing JahiaSavedSearch xml",t);
        }
        return "";
    }
    
    
    /**
     * Replace <![CDATA[...]]> by CDATA_OPEN_TAG...CDATA_CLOSE_TAG
     * @return
     */
    public static String encodeCDATA_Tag(String value){
        String result = JahiaTools.replacePatternIgnoreCase(value,"<![CDATA[",CDATA_OPEN_TAG);
        result = JahiaTools.replacePatternIgnoreCase(result,"]]>",CDATA_CLOSE_TAG);
        return result;
    }

    public static String decodeCDATA_Tag(String value){
        String result = JahiaTools.replacePatternIgnoreCase(value,CDATA_OPEN_TAG,"<![CDATA[");
        result = JahiaTools.replacePatternIgnoreCase(result,CDATA_CLOSE_TAG,"]]>");
        return result;
    }
    
    /**
     * Return the query as Document
     * @return
     */
    public Document getDocument() {
        Document document = null;
        try
        {
            SAXReader reader = new SAXReader();
            document = reader.read(new StringReader(getSearch()));
        }
        catch (Exception t){
            logger.debug("Error parsing JahiaSavedSearch xml",t);
        }
        return document;
    }    
    
    /**
     * Return the query as Document
     * @return
     */
    public Object getQueryObject() {
        Object object = null;
        if (getSearchViewHandlerClass().equals(
                FileSearchViewHandler.class.getName())) {
            object = SearchCriteriaFactory.deserialize(getSearch());
        } else {
            object = getDocument();
        }
        return object;
    }       

}
