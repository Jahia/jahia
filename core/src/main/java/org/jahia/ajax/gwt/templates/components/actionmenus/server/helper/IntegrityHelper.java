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
package org.jahia.ajax.gwt.templates.components.actionmenus.server.helper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.actionmenu.GWTJahiaIntegrityState;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentObjectKey;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaBigTextField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.engines.validation.IntegrityChecksHelper;
import org.jahia.engines.validation.LinkIntegrityChecker;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.htmlparser.WAIValidator;

/**
 * Helper class for obtaining integrity state for a content object.
 * 
 * @author Sergiy Shyrkov
 */
public final class IntegrityHelper {

    private static Logger logger = Logger.getLogger(IntegrityHelper.class);

    /**
     * Returns a list of big text field names for the specified container.
     * 
     * @param container
     *            the content container object
     * @param ctx
     *            current processing context object
     * @return a list of big text field names for the specified container
     * @throws JahiaException
     *             in case of an error
     */
    private static List<String> getBigTextFieldNames(
            ContentContainer container, ProcessingContext ctx)
            throws JahiaException {
        List<String> fieldNames = new LinkedList<String>();
        JahiaContainerDefinition containerDef = JahiaContainerDefinitionsRegistry
                .getInstance().getDefinition(container.getDefinitionID());
        if (containerDef != null) {
            Iterator fieldDefEnum = containerDef.getStructure(
                    JahiaContainerStructure.JAHIA_FIELD);
            while (fieldDefEnum.hasNext()) {
                JahiaFieldDefinition fieldDef = (JahiaFieldDefinition) ((JahiaContainerStructure) fieldDefEnum
                        .next()).getObjectDef();
                if (fieldDef.getType() == FieldTypes.BIGTEXT) {
                    fieldNames.add(fieldDef.getName());
                }
            }
        } else {
            logger.warn("Unable to find cotainer definition for ID: "
                    + container.getDefinitionID(null));
        }

        return fieldNames.size() > 0 ? fieldNames : Collections.<String>emptyList();
    }

    private static String getEngineUrl(ContentContainer container, int fieldId,
            ProcessingContext ctx) {
        String url = null;
        try {
            url = ActionMenuURIFormatter.drawContainerUpdateUrl(ctx, container,
                    fieldId);
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }

        return url;
    }

    private static GWTJahiaIntegrityState getState(ContentContainer container,
            ProcessingContext ctx) throws JahiaException {

        GWTJahiaIntegrityState state = null;
        boolean urlIntegrityEnabled = ctx.getSite()
                .isURLIntegrityCheckEnabled();
        boolean waiChecksEnabled = ctx.getSite().isWAIComplianceCheckEnabled();

        if (urlIntegrityEnabled || waiChecksEnabled) {
            List<String> bigTextFieldNames = getBigTextFieldNames(container,
                    ctx);
            if (bigTextFieldNames.size() > 0) {
                JahiaContainer jahiaContainer = container.getJahiaContainer(
                        ctx, ctx.getEntryLoadRequest());
                if (jahiaContainer != null) {
                    boolean hasIntegrityBypassRole = IntegrityChecksHelper
                            .isAllowedToBypassLinkIntegrityChecks(
                                    ctx.getUser(), ctx.getSite());
                    boolean hasWAIBypassRole = IntegrityChecksHelper
                            .isAllowedToBypassWaiChecks(ctx.getUser(), ctx
                                    .getSite());

                    Map engineMap = new HashMap(1);
                    engineMap.put("theContainer", jahiaContainer);
                    boolean hasUrlIntegrityViolations = false;
                    boolean hasWaiViolations = false;
                    int guiltyFieldId = -1;
                    for (String fieldName : bigTextFieldNames) {
                        JahiaBigTextField field = (JahiaBigTextField) jahiaContainer
                                .getField(fieldName);
                        if (field != null && field.getValue() != null
                                && field.getValue().length() > 0) {
                            hasUrlIntegrityViolations = urlIntegrityEnabled
                                    && !LinkIntegrityChecker.checkField(ctx,
                                            engineMap, field, false).isEmpty();
                            if (hasUrlIntegrityViolations
                                    && (!hasIntegrityBypassRole || hasWAIBypassRole)) {
                                guiltyFieldId = field.getID();
                                break;
                            }

                            hasWaiViolations = waiChecksEnabled
                                    && !WAIValidator.getInstance().validate(
                                            field.getValue()).getErrors()
                                            .isEmpty();

                            if (hasWaiViolations
                                    && (!hasWAIBypassRole || hasIntegrityBypassRole)) {
                                guiltyFieldId = field.getID();
                                break;
                            }
                            
                            if (guiltyFieldId == -1 && (hasUrlIntegrityViolations || hasWaiViolations)) {
                                guiltyFieldId = field.getID();
                            }
                        }
                    }

                    if (hasUrlIntegrityViolations || hasWaiViolations) {
                        state = new GWTJahiaIntegrityState(getEngineUrl(
                                container, guiltyFieldId, ctx));
                        state.setBlocker(hasUrlIntegrityViolations
                                && !hasIntegrityBypassRole || hasWaiViolations
                                && !hasWAIBypassRole);
                    }
                } else {
                    logger.warn("Jahia container not found for ID: "
                            + container.getID() + " and loadRequest: "
                            + ctx.getEntryLoadRequest());
                }
            }
        }
        return state;
    }

    public static GWTJahiaIntegrityState getState(String objectKey, ProcessingContext ctx) {
        // no checks are enabled for the site --> return
        if (!ctx.getSite().isURLIntegrityCheckEnabled()
                && !ctx.getSite().isWAIComplianceCheckEnabled()) {
            return null;
        }
        
        GWTJahiaIntegrityState state = null;
        ContentObjectKey key = null;
        try {
            key = (ContentObjectKey) ContentObjectKey.getInstance(objectKey);
        } catch (ClassNotFoundException e) {
            throw new JahiaBadRequestException(e);
        }
        if (null == key) {
            throw new JahiaBadRequestException("Bad content object key: "
                    + objectKey);
        }

        // is it a container?
        if (ContentContainerKey.CONTAINER_TYPE.equals(key.getType())) {
            try {
                ContentContainer container = ContentContainer.getContainer(key
                        .getIdInType());
                if (container != null) {
                    state = getState(container, ctx);
                } else {
                    logger.warn("Content container is not found for ID: "
                            + key.getIDInType());
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return state;
    }

    /**
     * Initializes an instance of this class.
     */
    private IntegrityHelper() {
        super();
    }
}
