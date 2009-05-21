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
//
//
//
//
// 29.05.2002 NK added in Jahia


package org.jahia.content;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.containers.NumberFormats;
import org.jahia.data.search.JahiaSearchHit;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.data.search.JahiaSearchResultSorter;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.search.JahiaSearchConstant;

import java.io.Serializable;
import java.text.Collator;
import java.util.*;

public class ContentSorterByMetadata implements JahiaSearchResultSorter, Serializable {

    private static final transient Logger logger = Logger
            .getLogger(ContentSorterByMetadata.class);
    
    protected int siteId = -1;

    protected String fieldName;

    protected String contentType;

    protected boolean numberSort = false;

    private String numberFormat = NumberFormats.LONG_FORMAT;

    protected boolean ASC_Ordering = true; // by default ASCENDANT ORDER.

    protected List<JahiaSearchHit> result;

    protected EntryLoadRequest entryLoadRequest = EntryLoadRequest.CURRENT;

    protected Collator collator;

    /**
     *
     * @param siteId
     * @param fieldName
     * @param contentType
     * @param boolean , force field values to be converted to number representation before sorting ( if true ).
     * @param numberFormat, only used if numberSort is true. If null, the format used is NumberFormat.LONG_FORMAT
     * @param entryLoadRequest
     * @throws org.jahia.exceptions.JahiaException
     */
    public ContentSorterByMetadata(int siteId,
                                   String fieldName,
                                   String contentType,
                                   boolean numberSort,
                                   String numberFormat,
                                   EntryLoadRequest entryLoadRequest )
    throws JahiaException
    {

        this.siteId = siteId;
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.numberSort = numberSort;
        if (NumberFormats.isValidFormat(numberFormat)) {
            this.numberFormat = numberFormat;
        }
        if (entryLoadRequest != null){
            this.entryLoadRequest = entryLoadRequest;
        }
        collator = getCollator();
    }

    public void sort(JahiaSearchResult searchResult) {
        if ( searchResult.results().size() <= 1){
            return;
        }
        List<JahiaSearchHit> list = searchResult.results();


        Map<String, JahiaSearchHit> m = new TreeMap<String, JahiaSearchHit>(new Comparator<String>() {
            public int compare(String v1, String v2) {
                if (!ASC_Ordering) {
                    String temp = v1;
                    v1 = v2;
                    v2 = temp;
                }

                if (v1 == null ) v1="";
                if (v2 == null ) v2="";

                int i = 0;
                if (numberSort) {
                    i= NumberFormats.compareNumber(v1,v2 , numberFormat);
                } else {
                    i= collator.compare(v1,v2);
                }
                return (i == 0)?1:i;
            }
        });

        List<String> alreadyProcessedContentObject = new ArrayList<String>();

        for (JahiaSearchHit jahiaSearchHit : list) {
            try {
                String hitObjectKey = jahiaSearchHit.getParsedObject().getValue(JahiaSearchConstant.OBJECT_KEY);
                if ( hitObjectKey != null && !alreadyProcessedContentObject.contains(hitObjectKey) ){
                    alreadyProcessedContentObject.add(hitObjectKey);
                    ObjectKey k = ContentObjectKey.getInstance(hitObjectKey);
                    if ( contentType == null || (contentType.equalsIgnoreCase(k.getType())) ){
                        ContentObject c = (ContentObject) ContentObject.getInstance(k);
                        m.put(c.getMetadata(fieldName).getValue(Jahia.getThreadParamBean(), entryLoadRequest), jahiaSearchHit);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        searchResult.setResult(new ArrayList<JahiaSearchHit>(m.values()));
    }


    //--------------------------------------------------------------------------
    /**
     * Return the List of sorted ctnids.
     *
     */
    public List<JahiaSearchHit> result()
    {
        return this.result;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the order , true - > ASC, false -> DESC.
     *
     */
    public boolean isAscOrdering()
    {
        return this.ASC_Ordering;
    }

    //--------------------------------------------------------------------------
    /**
     * Return true, if the values are converted to number before sorting.
     *
     */
    public boolean isNumberOrdering()
    {
        return this.numberSort;
    }

    //--------------------------------------------------------------------------
    /**
     * Force or not value to be converted to number before doing the sort.
     *
     */
    public boolean setNumberOrdering(boolean val)
    {
        return this.numberSort = val;
    }

    //--------------------------------------------------------------------------
    /**
     * Set DESC ordering.
     *
     */
    public void setDescOrdering()
    {
        this.ASC_Ordering = false;
    }

    //--------------------------------------------------------------------------
    /**
     * Set ASC ordering.
     *
     */
    public boolean setAscOrdering()
    {
        return this.ASC_Ordering = true;
    }

    //--------------------------------------------------------------------------
    /**
     * Set ASC ordering.
     *
     */
    public boolean setAscOrdering(boolean val)
    {
        return this.ASC_Ordering = val;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the site id.
     *
     * @return
     */
    public int getSiteId()
    {
        return this.siteId;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the sorting field.
     *
     * @return String , the name of fields used for sorting.
     */
    public String getSortingFieldName ()
    {
        return this.fieldName;
    }

    //--------------------------------------------------------------------------
    public EntryLoadRequest getEntryLoadRequest(){
        return this.entryLoadRequest;
    }

    /**
     *  Return the collator instantiated with the first locale from the internal EntryLoadRequest.
     *  If the entryLoadRequest is null, the localtor is instantiated with the default locale of the system
     * @return
     */
    protected Collator getCollator(){
        Collator collator = Collator.getInstance();
        if ( this.getEntryLoadRequest() != null ){
            Locale locale = null;
            locale = this.getEntryLoadRequest().getFirstLocale(true);
            if ( locale != null ){
                collator = Collator.getInstance(locale);
            }
        }
        return collator;
    }
}
