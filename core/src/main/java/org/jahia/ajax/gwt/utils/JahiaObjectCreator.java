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

package org.jahia.ajax.gwt.utils;

import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.JahiaObject;
import org.jahia.data.beans.*;
import org.jahia.params.ProcessingContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.fields.ContentField;

/**
 * Helper class to build content objects or content beans from various parameters.
 *
 * @author rfelden
 * @version 27 f�vr. 2008 - 11:53:50
 */
public class JahiaObjectCreator {

    // private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaObjectCreator.class);

    /**
     * Returns the ContentObject associated with the given key
     *
     * @param key The ObjectKey value represented as a String
     * @return The ContentObject or null if not found
     * @throws ClassNotFoundException if the requested class was not found
     */
    public static ContentObject getContentObjectFromString(final String key) throws ClassNotFoundException {
        final ObjectKey objectKey = ObjectKey.getInstance(key);
        return (ContentObject) JahiaObject.getInstance(objectKey);
    }

    /**
     * Returns the ContentObject associated with the given key
     *
     * @param objectKey The ObjectKey object
     * @return The ContentObject or null if not found
     * @throws ClassNotFoundException if the requested class was not found
     */
    public static ContentObject getContentObjectFromKey(final ObjectKey objectKey) throws ClassNotFoundException {
        return (ContentObject) JahiaObject.getInstance(objectKey);
    }

    /**
     * Construct a content bean associated with a given content object.
     *
     * @param contentObject the content object
     * @param jParams the processing context
     * @return the content bean
     * @throws ClassNotFoundException the type is not registered
     * @throws org.jahia.exceptions.JahiaException sthg bad happened
     */
    public static ContentBean getContentBeanFromContentObject(final ContentObject contentObject, final ProcessingContext jParams) throws ClassNotFoundException, JahiaException {
        final String key = contentObject.getObjectKey().getKey() ;
        final String beanType = key.substring(0, key.indexOf(ObjectKey.KEY_SEPARATOR)) ;
        if (beanType.equals(ContainerBean.TYPE)) {
            return new ContainerBean(((ContentContainer) getContentObjectFromString(key)).getJahiaContainer(jParams, jParams.getEntryLoadRequest()), jParams) ;
        } else if (beanType.equals(ContainerListBean.TYPE)) {
            return new ContainerListBean(((ContentContainerList) getContentObjectFromString(key)).getJahiaContainerList(jParams, jParams.getEntryLoadRequest()), jParams) ;
        } else if (beanType.equals(PageBean.TYPE)) {
            return new PageBean(((ContentPage) getContentObjectFromString(key)).getPage(jParams), jParams) ;
        } else if (beanType.equals(FieldBean.TYPE)) {
            return new FieldBean(((ContentField) getContentObjectFromString(key)).getJahiaField(jParams.getEntryLoadRequest()), jParams) ;
        } else {
            return null ; // should never happen
        }
    }

    /**
     * Construct a content bean associated with a given content object.
     *
     * @param key the object key as a string
     * @param jParams the processing context
     * @return the content bean
     * @throws ClassNotFoundException the type is not registered
     * @throws JahiaException sthg bad happened
     */
    public static ContentBean getContentBeanFromObjectKey(final String key, final ProcessingContext jParams) throws ClassNotFoundException, JahiaException {
        if (key == null || "".equals(key.trim()) ){
            // should never happens but it happens
            return null;
        }
        final String beanType = key.substring(0, key.indexOf(ObjectKey.KEY_SEPARATOR)) ;
        if (beanType.equals(ContainerBean.TYPE)) {
            return new ContainerBean(((ContentContainer) getContentObjectFromString(key)).getJahiaContainer(jParams, jParams.getEntryLoadRequest()), jParams) ;
        } else if (beanType.equals(ContainerListBean.TYPE)) {
            return new ContainerListBean(((ContentContainerList) getContentObjectFromString(key)).getJahiaContainerList(jParams, jParams.getEntryLoadRequest()), jParams) ;
        } else if (beanType.equals(PageBean.TYPE)) {
            return new PageBean(((ContentPage) getContentObjectFromString(key)).getPage(jParams), jParams) ;
        } else if (beanType.equals(FieldBean.TYPE)) {
            return new FieldBean(((ContentField) getContentObjectFromString(key)).getJahiaField(jParams.getEntryLoadRequest()), jParams) ;
        } else {
            return null ; // should never happen
        }
    }

}
