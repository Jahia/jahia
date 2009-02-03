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

package org.jahia.views.engines.versioning;

import org.jahia.params.ProcessingContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.fields.ContentField;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.content.ContentObject;
import org.jahia.registries.ServicesRegistry;

import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 6 sept. 2006
 * Time: 15:58:07
 * To change this template use File | Settings | File Templates.
 */
public class ContainerVersioningBean {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContainerVersioningBean.class);

    static private TimeZone tz = TimeZone.getTimeZone("UTC");

    private int id;
    private String definitionName;
    private String creator;
    private String lastContributor;
    private String deleter;
    private Date creationDate;
    private Date lastContributionDate;
    private Date deleteDate;
    private boolean isDeleted;
    private boolean isMarkedForDelete;

    public ContainerVersioningBean (){
    }

    public static ContainerVersioningBean getInstance(ContentContainer contentContainer,
                                                      ProcessingContext context,
                                                      Locale languageLocale,
                                                      String unknownUserLabel)
    throws JahiaException {
        if ( unknownUserLabel == null ){
            unknownUserLabel = "unknown";
        }
        ContainerVersioningBean bean = new ContainerVersioningBean();
        bean.setId(contentContainer.getID());
        try {
            int pageDefID = 0;
            if ( context != null && context.getContentPage() != null ){
                pageDefID = context.getContentPage()
                        .getDefinitionID(context.getEntryLoadRequest());
            }
            JahiaContainerDefinition def = (JahiaContainerDefinition)JahiaContainerDefinition
                .getContentDefinitionInstance(contentContainer.getDefinitionKey(null));
            if ( def != null ){
                bean.setDefinitionName(def.getName());
                bean.setDefinitionName(def.getTitle(languageLocale));
            } else {
                return null;
            }
        } catch ( Exception t ){
            logger.debug("Error with Container Definition ", t);
        }
        // creator
        bean.setCreator(getMetadataValue(contentContainer,CoreMetadataConstant.CREATOR,context,
                EntryLoadRequest.STAGED,unknownUserLabel));
        // creation date
        long dateValue = getMetadataDateValue(contentContainer,CoreMetadataConstant.CREATION_DATE,context,null,0);
        if ( dateValue != 0 ){
            bean.setCreationDate(new Date(dateValue));
        }
        boolean isMarkedForDelete = contentContainer.isMarkedForDelete();
        bean.setIsMarkedForDelete(isMarkedForDelete);
        boolean isDeleted = contentContainer.isDeleted(ServicesRegistry.getInstance()
                        .getJahiaVersionService().getCurrentVersionID());
        bean.setIsDeleted(isDeleted);
        if ( isMarkedForDelete ){
            // last contributor
            bean.setLastContributor(getMetadataValue(contentContainer,CoreMetadataConstant.LAST_CONTRIBUTOR,context,
                    EntryLoadRequest.CURRENT,unknownUserLabel));
            // last contribution date
            dateValue = getMetadataDateValue(contentContainer,CoreMetadataConstant.LAST_MODIFICATION_DATE,context,
                    EntryLoadRequest.CURRENT,0);
            if ( dateValue != 0 ){
                bean.setLastContributionDate(new Date(dateValue));
            }
            // deleter
            bean.setDeleter(getMetadataValue(contentContainer,CoreMetadataConstant.LAST_CONTRIBUTOR,context,
                    EntryLoadRequest.STAGED,unknownUserLabel));
            // delete date
            dateValue = getMetadataDateValue(contentContainer,CoreMetadataConstant.LAST_MODIFICATION_DATE,context,
                    EntryLoadRequest.STAGED,0);
            if ( dateValue != 0 ){
                bean.setDeleteDate(new Date(dateValue));
            }
        } else if ( isDeleted ){
            // delete date & deleter
            int deletedVersion = contentContainer.getDeleteVersionID();
            Calendar cal = Calendar.getInstance(tz);
            cal.setTimeInMillis(deletedVersion*1000L);
            bean.setDeleteDate(cal.getTime());
            EntryLoadRequest loadRequest = EntryLoadRequest.VERSIONED;
            loadRequest = (EntryLoadRequest)loadRequest.clone();
            loadRequest.setVersionID(deletedVersion);
            loadRequest.setWithDeleted(true);
            bean.setDeleter(getMetadataValue(contentContainer,CoreMetadataConstant.LAST_CONTRIBUTOR,context,
                loadRequest,unknownUserLabel));
            ContentObjectEntryState entryState =
                    new ContentObjectEntryState(0,deletedVersion,ContentObject.SHARED_LANGUAGE);
            entryState = contentContainer.getClosestVersionedEntryState(entryState,true);
            if ( entryState != null ){
                loadRequest = new EntryLoadRequest(entryState);
                // last contributor
                bean.setLastContributor(getMetadataValue(contentContainer,CoreMetadataConstant.LAST_CONTRIBUTOR,context,
                        loadRequest,unknownUserLabel));
                cal.setTimeInMillis(entryState.getVersionID()*1000L);
                bean.setLastContributionDate(cal.getTime());
            }
        }
        return bean;
    }

    public ContainerVersioningBean(int id,
                                   String definitionName,
                                   String creator,
                                   Date creationDate,
                                   String lastContributor,
                                   Date lastContributionDate,
                                   String deleter,
                                   Date deleteDate) {
        this.id = id;
        this.definitionName = definitionName;
        this.creator = creator;
        this.creationDate = creationDate;
        this.lastContributor = lastContributor;
        this.lastContributionDate = lastContributionDate;
        this.deleter = deleter;
        this.deleteDate = deleteDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDefinitionName() {
        return definitionName;
    }

    public void setDefinitionName(String definitionName) {
        this.definitionName = definitionName;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    public String getDeleter() {
        return deleter;
    }

    public void setDeleter(String deleter) {
        this.deleter = deleter;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getLastContributionDate() {
        return lastContributionDate;
    }

    public void setLastContributionDate(Date lastContributionDate) {
        this.lastContributionDate = lastContributionDate;
    }

    public String getLastContributor() {
        return lastContributor;
    }

    public void setLastContributor(String lastContributor) {
        this.lastContributor = lastContributor;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean getIsMarkedForDelete() {
        return isMarkedForDelete;
    }

    public void setIsMarkedForDelete(boolean markedForDelete) {
        isMarkedForDelete = markedForDelete;
    }

    static protected String getMetadataValue( ContentObject contentObject,
                                              String metadataName,
                                              ProcessingContext context,
                                              EntryLoadRequest loadRequest,
                                              String defaultValue){
        if ( loadRequest == null && context != null ){
            loadRequest = context.getEntryLoadRequest();
        }
        String value = defaultValue;
        try {
            ContentField contentField = contentObject.getMetadata(metadataName);
            JahiaField jahiaField = contentField.getJahiaField(loadRequest);
            jahiaField.load(LoadFlags.ALL,context,loadRequest);
            value = jahiaField.getValue();
        } catch ( Exception t ){
        }
        return value;
    }

    static protected long getMetadataDateValue( ContentObject contentObject,
                                                  String metadataName,
                                                  ProcessingContext context,
                                                  EntryLoadRequest loadRequest,
                                                  long defaultValue){
        if ( loadRequest == null && context != null ){
            loadRequest = context.getEntryLoadRequest();
        }
        long value = 0;
        Calendar cal = Calendar.getInstance(tz);
        try {
            ContentField contentField = contentObject.getMetadata(metadataName);
            JahiaField jahiaField = contentField.getJahiaField(loadRequest);
            jahiaField.load(LoadFlags.ALL,context,loadRequest);
            if ( jahiaField != null ){
                try {
                    cal.setTimeInMillis(Long.parseLong(jahiaField.getObject().toString()));
                    value = cal.getTimeInMillis();
                } catch ( Exception t){
                }
            }
        } catch ( Exception t ){
        }
        if ( value == 0 && defaultValue != 0 ){
            cal.setTime(new Date(defaultValue));
            value = cal.getTimeInMillis();
        }
        return value;
    }

    public static String getFormattedDate( Date date, Locale locale, String defaultValue){
        if ( date == null ){
            return defaultValue;
        }
        return java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM,
                java.text.DateFormat.SHORT, locale).format(date);
    }
}
