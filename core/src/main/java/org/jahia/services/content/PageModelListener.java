/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.apache.commons.lang.mutable.MutableInt;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.importexport.ReferencesHelper;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jahia.api.Constants.forbiddenMixinToCopy;

/**
 * Custom listener on "jmix:createdFromPageModel" that copies the "j:templateName" referenced page properties and contents to the current node.
 */
public class PageModelListener extends DefaultEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PageModelListener.class);
    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED;
    }

    @Override
    public void onEvent(EventIterator events) {
        JCRSessionWrapper eventSession = ((JCREventIterator)events).getSession();
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(eventSession.getUser(), workspace, null, new JCRCallback<Object>() {

                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    while (events.hasNext()) {
                        Event event = events.nextEvent();
                        // Don't process nodes that have been moved or removed by another listener in the same session.
                        if (!session.nodeExists(event.getPath())) {
                            continue;
                        }
                        JCRNodeWrapper newPage = session.getNode(event.getPath());
                        if (newPage.isNodeType("jmix:createdFromPageModel")) {
                            JCRNodeWrapper pageModel = session.getNode(newPage.getPropertyAsString("j:templateName"));

                            // Store uuid mapping to resolve any reference.
                            session.getUuidMapping().put(pageModel.getIdentifier(), newPage.getIdentifier());
                            // Do not copy sub pages in case "j:copySubPages" is false
                            List<String> childNodeTypesToSkip = pageModel.hasProperty("j:copySubPages") && pageModel.getProperty("j:copySubPages").getBoolean() ? null : Collections.singletonList(Constants.JAHIANT_PAGE);

                            copySkippingTypes(newPage, pageModel, childNodeTypesToSkip, Collections.singletonList("j:templateName"));

                            // Clean up the new page.
                            for (String mixinToRemove : (Arrays.asList("jmix:vanityUrlMapped", "jmix:canBeUseAsTemplateModel", "jmix:createdFromPageModel"))) {
                                if (newPage.isNodeType(mixinToRemove)) {
                                    newPage.removeMixin(mixinToRemove);
                                }
                            }

                            newPage.revokeAllRoles();
                            session.save();
                        }
                    }

                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while creating page model", e);
        }
    }

    /**
     *  Copy properties and sub contents from the page model to the current page.
     * @param newPage page created from the page model
     * @param pageModel page model node.
     * @param childNodeTypesToSkip skip sub contents of the given type
     * @param propertiesToOverride List of properties to override on the
     * @throws RepositoryException when a JCR Error occurs.
     */
    private static void copySkippingTypes(JCRNodeWrapper newPage, JCRNodeWrapper pageModel, List<String> childNodeTypesToSkip, List<String> propertiesToOverride) throws RepositoryException {

        // Do mixin
        for (ExtendedNodeType mixin : pageModel.getMixinNodeTypes()) {
            if (!forbiddenMixinToCopy.contains(mixin.getName())) {
                newPage.addMixin(mixin.getName());
            }
        }
        Map<String, List<String>> references = new HashMap<>();

        // Do properties
        copyProperties(newPage, pageModel, references, propertiesToOverride);

        // Do sub contents
        for (JCRNodeWrapper childNode : pageModel.getNodes()) {
            // Do translation nodes
            if (newPage.hasNode(childNode.getName()) && childNode.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                final JCRNodeWrapper i18nNode = newPage.getNode(childNode.getName());
                // Do mixin on i18n node
                for (ExtendedNodeType mixin : childNode.getMixinNodeTypes()) {
                    if (!forbiddenMixinToCopy.contains(mixin.getName())) {
                        i18nNode.addMixin(mixin.getName());
                    }
                }
                copyProperties(i18nNode, childNode, references, propertiesToOverride);
            } else {
                childNode.copy(newPage, childNode.getName(), true, references, childNodeTypesToSkip, SettingsBean.getInstance().getImportMaxBatch(), new MutableInt(0));
            }
        }
        ReferencesHelper.resolveCrossReferences(newPage.getSession(), references, false);
    }

    /**
     * Same as {@link JCRNodeWrapperImpl#copyProperties} method but
     * copies only missing properties from the source node to the target node.
     * @param targetNode node on which properties will be copied
     * @param sourceNode node that contains properties to be copied
     * @param references store references for later resolution.
     * @param propertiesToOverride list of existing properties to override.
     * @throws RepositoryException when a JCR Error occurs.
     */
    private static void copyProperties(JCRNodeWrapper targetNode, JCRNodeWrapper sourceNode, Map<String, List<String>> references, List<String> propertiesToOverride) throws RepositoryException {
        PropertyIterator props = sourceNode.getProperties();

        while (props.hasNext()) {
            Property property = props.nextProperty();
            // Allow mixin property update only if the JCR allows it.
            boolean b = !property.getDefinition().getDeclaringNodeType().isMixin() || targetNode.getProvider().isUpdateMixinAvailable();
            // Skip existing properties but replace properties to override.
            b = b && (propertiesToOverride.contains(property.getName()) || !targetNode.hasProperty(property.getName()));
            try {
                if (!Constants.forbiddenPropertiesToCopy.contains(property.getName()) && b) {
                    if (property.getType() == PropertyType.REFERENCE || property.getType() == PropertyType.WEAKREFERENCE) {
                        if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                            for (Value value : property.getValues()) {
                                keepReference(targetNode, references, property, value.getString());
                            }
                        } else {
                            keepReference(targetNode, references, property, property.getValue().getString());
                        }
                    }
                    if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                        targetNode.setProperty(property.getName(), property.getValues());
                    } else {
                        targetNode.setProperty(property.getName(), property.getValue());
                    }
                }
            } catch (RepositoryException e) {
                logger.debug("Unable to copy property '" + property.getName() + "'. Skipping.", e);
            }
        }
    }

    private static void keepReference(JCRNodeWrapper destinationNode, Map<String, List<String>> references, Property property, String value) throws RepositoryException {
        if (!references.containsKey(value)) {
            references.put(value, new ArrayList<>());
        }
        references.get(value).add(destinationNode.getIdentifier() + "/" + property.getName());
    }
}
