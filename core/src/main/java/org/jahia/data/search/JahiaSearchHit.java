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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

// DJ       03.01.2001

package org.jahia.data.search;

import org.apache.commons.lang.math.NumberUtils;
import org.jahia.content.*;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.search.ParsedObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines what does a search "hit" looks like.
 * One hit is one page found by jahia containing at least one time the searchstring
 * in one of its fields.
 *
 * @see org.jahia.data.search.JahiaSearchResult
 * @see org.jahia.engines.search.Search_Engine
 * @author DJ
 */
public class JahiaSearchHit implements JahiaSearchHitInterface
{

    private String      id;
    private int         integerID = -1;

    private int         type;
    private Object      object;
    private float       score;
    private String      URL;

    private JahiaPage   page;
    private int         pageId;
    private List<String>   languageCodes = new ArrayList<String>();
    private ObjectKey   objectKey;

    // the sort order
    private int sortOrder = 0;

    /**
    * A small string coming from this page, usually containing one of the searched words.
    * Currently: one of the fields containing this word.
    */
    private String      teaser;

    private ParsedObject parsedObject;

    public JahiaPage getPage() {
        return page;
    }

    public void setPage(JahiaPage aPage) {
        this.page = aPage;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int aPageId) {
        this.pageId = aPageId;
    }

    /**
     *
     * @param aParsedObject ParsedObject
     */
    public JahiaSearchHit(ParsedObject aParsedObject){
        this.parsedObject = aParsedObject;
    }

    public String getId() {
        return id;
    }

    /**
     * You should use the method <code>setIntegerID(int integerID)</code> if the ID is an Integer.
     * It will parse the value to an integer anyway.
     *
     * @param anId
     */
    public void setId(String anId) {
        this.id = anId;
        if (NumberUtils.isNumber(anId)){
            try {
                this.integerID = Integer.parseInt(anId);
            } catch ( Exception t ){
            }
        }
    }

    /**
     * When the BitSet is used, this method is used to set the corresponding bit
     * @return
     */
    public int getIntegerID() {
        return integerID;
    }

    /**
     * When you use the BitSet, you should set the Id using this method instead of <code>setID(String anId)</code>
     * which will synchronize the internal String representation as well (call the setID(String.valueOf(integerID))
     *
     * @param integerID
     */
    public void setIntegerID(int integerID) {
        this.integerID = integerID;
        this.setId(String.valueOf(integerID));
    }

    public int getType() {
        return type;
    }

    public void setType(int aType) {
        this.type = aType;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object anObject) {
        this.object = anObject;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String anURL) {
        this.URL = anURL;
    }

    /**
     * Returns the list of locales available for this hit.
     * @return ArrayList
     */
    public List<String> getLanguageCodes(){
        return this.languageCodes;
    }

    /**
     * Returns the list of locales available for this hit.
     */
    public void setLanguageCodes(List<String> languagesCode){
        this.languageCodes = languagesCode;
    }

    /**
     * Returns the hit score
     */
    public float getScore(){
        return this.score;
    }

    /**
     *
     * @param aScore int
     */
    public void setScore(float aScore){
        this.score = aScore;
    }

    /**
     * Returns a small teaser about this hit
     */
    public String getTeaser(){
        return this.teaser;
    }

    /**
     * Returns a small teaser about this hit
     */
    public void setTeaser(String aTeaser){
        this.teaser = aTeaser;
    }

    /**
     * Returns a ParsedObject instance of this hit
     * @return ParsedObject
     */
    public ParsedObject getParsedObject(){
        return this.parsedObject;
    }

    /**
     * Returns a ParsedObject instance of this hit
     */
    public void setParsedObject(ParsedObject aParsedObject){
        this.parsedObject = aParsedObject;
    }

    //-------------------------------------------------------------------------
    /**
     * Compare between two objects, sort by their wordcount.
     *
     * @param obj
     */
    public int compareTo(JahiaSearchHitInterface obj) throws ClassCastException {

        JahiaSearchHitInterface hit = obj;
        if ( hit.getScore() > this.score ){
            return 1;
        } else if ( hit.getScore() == this.score ){
            return 1;
        }
        return -1;
    }

    public int hashCode(){
        return this.getId().hashCode();
    }

    public ObjectKey getSearchHitObjectKey(){
        if ( objectKey == null ){
            if ( this.getType() == JahiaSearchHitInterface.PAGE_TYPE ){
                objectKey = new ContentPageKey(this.integerID);
            } else if ( this.getType() == JahiaSearchHitInterface.CONTAINER_TYPE ){
                objectKey = new ContentContainerKey(this.integerID);
            } else if ( this.getType() == JahiaSearchHitInterface.CONTAINERLIST_TYPE ){
                objectKey = new ContentContainerListKey(this.integerID);
            } else if ( this.getType() == JahiaSearchHitInterface.FIELD_TYPE ){
                objectKey = new ContentFieldKey(this.integerID);
            } else if ( this.getType() == JahiaSearchHitInterface.FILE_TYPE ){
                objectKey = new ContentContainerKey(this.integerID);
            }
        }
        return objectKey;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int aSortOrder) {
        this.sortOrder = aSortOrder;
    }

}
