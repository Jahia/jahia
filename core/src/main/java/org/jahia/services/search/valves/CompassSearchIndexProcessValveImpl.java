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
