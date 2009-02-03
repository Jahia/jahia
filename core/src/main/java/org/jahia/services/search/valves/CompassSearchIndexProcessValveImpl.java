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

 package org.jahia.services.search.valves;

import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.services.search.*;
import org.jahia.services.pages.JahiaPage;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.JahiaField;

import java.util.List;
import java.util.Map;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author not attributable
 * @version 1.0
 */
public class CompassSearchIndexProcessValveImpl implements SearchIndexationPipeline, Valve {

    public static final String COMPASS_RESOURCE_ALIAS = "compass.resource.alias";
    public static final String CONTAINER_RESOURCE_ALIAS = "jahiacontainer";
    public static final String PAGE_RESOURCE_ALIAS = "jahiapage";
    public static final String FIELD_RESOURCE_ALIAS = "jahiafield";
    public static final String JAHIACONTENT_RESOURCE_ALIAS = "jahiacontent";
    public static final String JAHIASITE_RESOURCE_ALIAS = "jahiasite";

    public CompassSearchIndexProcessValveImpl() {
    }

    /**
     * Retrieve the IndexableDocument from the context and store in the corresponding
     * Compass Alias Resource name
     *
     * @param context
     * @param valveContext
     * @throws PipelineException
     */
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {

        Map<String, Object> contextMap = (Map<String, Object>) context;
        Object srcObject = contextMap.get(SOURCE_OBJECT);
        if ( srcObject == null ){
            valveContext.invokeNext(context);
            return;
        }
        List<IndexableDocument> docs = (List<IndexableDocument>)contextMap.get(INDEXABLE_DOCUMENTS);
        if ( docs == null ) {
            valveContext.invokeNext(context);
            return;
        }
        for (IndexableDocument doc : docs) {
            if (srcObject instanceof JahiaContainer) {
                doc.setFieldValue(COMPASS_RESOURCE_ALIAS,
                        CONTAINER_RESOURCE_ALIAS);
            } else if (srcObject instanceof JahiaPage) {
                doc.setFieldValue(COMPASS_RESOURCE_ALIAS, PAGE_RESOURCE_ALIAS);
            } else if (srcObject instanceof JahiaField) {
                doc.setFieldValue(COMPASS_RESOURCE_ALIAS, FIELD_RESOURCE_ALIAS);
            }
        }
        valveContext.invokeNext(context);
    }

    public void initialize() {
    }

}
