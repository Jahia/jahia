/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.value.StringValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTCompositeConstraintViolationException;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.content.server.UploadedPendingFile;
import org.jahia.api.Constants;
import org.jahia.bin.SessionNamedDataStorage;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.categories.Category;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedItemDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.utils.EncryptionUtils;
import org.jahia.utils.i18n.JahiaLocaleContextHolder;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.PropertyDefinition;
import java.io.InputStream;
import java.util.*;

/**
 * Helper class for setting node properties based on GWT bean values.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:45:42 PM
 */
public class PropertiesHelper {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesHelper.class);

    private ContentDefinitionHelper contentDefinition;
    private NavigationHelper navigation;
    private SessionNamedDataStorage<UploadedPendingFile> fileStorage;

    private Set<String> ignoredProperties = Collections.emptySet();

    public void setContentDefinition(ContentDefinitionHelper contentDefinition) {
        this.contentDefinition = contentDefinition;
    }

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }

    public void setFileStorage(SessionNamedDataStorage<UploadedPendingFile> fileStorage) {
        this.fileStorage = fileStorage;
    }

    public Map<String, GWTJahiaNodeProperty> getProperties(String path, JCRSessionWrapper currentUserSession, Locale uiLocale) throws GWTJahiaServiceException {
        JCRNodeWrapper objectNode;
        try {
            objectNode = currentUserSession.getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(path).append(Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale)).append(e.toString()).toString());
        }
        Map<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>();
        String propName = "null";
        try {
            PropertyIterator it = objectNode.getProperties();
            while (it.hasNext()) {
                Property prop = it.nextProperty();
                PropertyDefinition def = prop.getDefinition();
                Property translationProp = null;
                // definition can be null if the file is versionned
                if (def != null && !ignoredProperties.contains(def.getName()) && ((ExtendedPropertyDefinition) def).getSelectorOptions().get("password") == null) {
                    propName = def.getName();

                    // check that we're not dealing with a not-set property from the translation nodes,
                    // in which case it needs to be omitted
                    final Locale locale = currentUserSession.getLocale();
                    if (Constants.nonI18nPropertiesCopiedToTranslationNodes.contains(propName) && objectNode.hasI18N(locale, false)) {
                        // get the translation node for the current locale
                        final Node i18N = objectNode.getI18N(locale, false);
                        if (!i18N.hasProperty(propName)) {
                            // if the translation node doesn't have the property and it's part of the set of copied properties, then we shouldn't return it
                            continue;
                        } else {
                            translationProp = i18N.getProperty(propName);
                        }
                    }


                    // create the corresponding GWT bean
                    GWTJahiaNodeProperty nodeProp = new GWTJahiaNodeProperty();
                    nodeProp.setName(propName);
                    nodeProp.setMultiple(def.isMultiple());
                    Value[] values;
                    if (!def.isMultiple()) {
                        Value oneValue = translationProp == null ? prop.getValue() : translationProp.getValue();
                        values = new Value[]{oneValue};
                    } else {
                        values = translationProp == null ? prop.getValues() : translationProp.getValues();
                    }
                    List<GWTJahiaNodePropertyValue> gwtValues = new ArrayList<GWTJahiaNodePropertyValue>(values.length);

                    for (Value val : values) {
                        GWTJahiaNodePropertyValue convertedValue = contentDefinition.convertValue(val, (ExtendedPropertyDefinition) def);
                        if (convertedValue != null) {
                            gwtValues.add(convertedValue);
                        }
                    }
                    nodeProp.setValues(gwtValues);
                    props.put(nodeProp.getName(), nodeProp);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("The following property has been ignored " + prop.getName() + "," + prop.getPath());
                    }
                }
            }
            NodeIterator ni = objectNode.getNodes();
            while (ni.hasNext()) {
                Node node = ni.nextNode();
                if (node.isNodeType(Constants.NT_RESOURCE)) {
                    NodeDefinition def = node.getDefinition();
                    propName = def.getName();
                    // create the corresponding GWT bean
                    GWTJahiaNodeProperty nodeProp = new GWTJahiaNodeProperty();
                    nodeProp.setName(propName);
                    List<GWTJahiaNodePropertyValue> gwtValues = new ArrayList<GWTJahiaNodePropertyValue>();
                    gwtValues.add(new GWTJahiaNodePropertyValue(node.getProperty(Constants.JCR_MIMETYPE).getString(), GWTJahiaNodePropertyType.ASYNC_UPLOAD));
                    nodeProp.setValues(gwtValues);
                    props.put(nodeProp.getName(), nodeProp);
                } else if (node.isNodeType(Constants.JAHIANT_PAGE_LINK)) {

                    // case of link
                    NodeDefinition def = node.getDefinition();
                    propName = def.getName();
                    // create the corresponding GWT bean
                    GWTJahiaNodeProperty nodeProp = new GWTJahiaNodeProperty();
                    nodeProp.setName(propName);
                    List<GWTJahiaNodePropertyValue> gwtValues = new ArrayList<GWTJahiaNodePropertyValue>();
                    GWTJahiaNode linkNode = navigation.getGWTJahiaNode((JCRNodeWrapper) node);
                    if (node.isNodeType(Constants.JAHIANT_NODE_LINK)) {
                        linkNode.set("linkType", "internal");
                    } else if (node.isNodeType(Constants.JAHIANT_EXTERNAL_PAGE_LINK)) {
                        linkNode.set("linkType", "external");
                    }

                    // url
                    if (node.hasProperty(Constants.URL)) {
                        String linkUrl = node.getProperty(Constants.URL).getValue().getString();
                        linkNode.set(Constants.URL, linkUrl);
                    }

                    // title
                    if (node.hasProperty(Constants.JCR_TITLE)) {
                        String linkTitle = node.getProperty(Constants.JCR_TITLE).getValue().getString();
                        linkNode.set(Constants.JCR_TITLE, linkTitle);
                    }

                    // alt
                    if (node.hasProperty(Constants.ALT)) {
                        String alt = node.getProperty(Constants.ALT).getValue().getString();
                        linkNode.set(Constants.ALT, alt);
                    }

                    if (node.hasProperty(Constants.NODE)) {
                        JCRValueWrapper weekReference = (JCRValueWrapper) node.getProperty(Constants.NODE).getValue();
                        Node pageNode = weekReference.getNode();
                        if (pageNode != null) {
                            linkNode.set(Constants.NODE, navigation.getGWTJahiaNode((JCRNodeWrapper) pageNode));
                            linkNode.set(Constants.ALT, pageNode.getName());
                            linkNode.set(Constants.URL, ((JCRNodeWrapper) pageNode).getUrl());
                            linkNode.set(Constants.JCR_TITLE, ((JCRNodeWrapper) pageNode).getUrl());
                        } else {
                            String resource = Messages.getInternal("label.error.invalidlink", uiLocale);
                            linkNode.set(Constants.JCR_TITLE, resource);
                            linkNode.set(Constants.ALT, resource);
                        }

                    }


                    GWTJahiaNodePropertyValue proper = new GWTJahiaNodePropertyValue(linkNode, GWTJahiaNodePropertyType.PAGE_LINK);
                    gwtValues.add(proper);
                    nodeProp.setValues(gwtValues);
                    props.put(nodeProp.getName(), nodeProp);
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot access property " + propName + " of node " + objectNode.getName(), e);
        }
        return props;
    }

    /**
     * A batch-capable save properties method.
     *
     * @param nodes              the nodes to save the properties of
     * @param newProps           the new properties
     * @param removedTypes
     * @param currentUserSession @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     * @param uiLocale
     */
    public void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps, Set<String> removedTypes, JCRSessionWrapper currentUserSession, Locale uiLocale, String httpSessionID) throws RepositoryException {
        for (GWTJahiaNode aNode : nodes) {
            JCRNodeWrapper objectNode = currentUserSession.getNode(aNode.getPath());
            List<String> types = aNode.getNodeTypes();
            if (removedTypes != null && !removedTypes.isEmpty()) {
                for (ExtendedNodeType mixin : objectNode.getMixinNodeTypes()) {
                    if (removedTypes.contains(mixin.getName())) {
                        List<ExtendedItemDefinition> items = mixin.getItems();
                        for (ExtendedItemDefinition item : items) {
                            removeItemFromNode(item, objectNode, currentUserSession);
                        }
                        objectNode.removeMixin(mixin.getName());
                    }
                }
                for (ExtendedNodeType mixin : objectNode.getPrimaryNodeType().getDeclaredSupertypes()) {
                    if (removedTypes.contains(mixin.getName())) {
                        List<ExtendedItemDefinition> items = mixin.getItems();
                        for (ExtendedItemDefinition item : items) {
                            removeItemFromNode(item, objectNode, currentUserSession);
                        }
                    }
                }
            }
            for (String type : types) {
                if (!objectNode.isNodeType(type)) {
                    currentUserSession.checkout(objectNode);
                    objectNode.addMixin(type);
                }
            }
            setProperties(objectNode, newProps, httpSessionID);
        }
    }

    private void removeItemFromNode(ExtendedItemDefinition item, JCRNodeWrapper objectNode, JCRSessionWrapper currentUserSession) throws RepositoryException {
        if (!item.isUnstructured()) {
            if (item.isNode()) {
                if (objectNode.hasNode(item.getName())) {
                    currentUserSession.checkout(objectNode);
                    objectNode.getNode(item.getName()).remove();
                }
            } else {
                if (item instanceof ExtendedPropertyDefinition) {
                    ExtendedPropertyDefinition itemProperty = (ExtendedPropertyDefinition) item;
                    if (itemProperty.isInternationalized()) {
                        NodeIterator nodeIterator = objectNode.getI18Ns();
                        while (nodeIterator.hasNext()) {
                            Node i18nNode = nodeIterator.nextNode();
                            if (i18nNode.hasProperty(item.getName())) {
                                currentUserSession.checkout(i18nNode);
                                objectNode.getProvider().getPropertyWrapper(i18nNode.getProperty(item.getName()), objectNode.getSession()).remove();
                            }
                        }
                    } else {
                        if (objectNode.hasProperty(item.getName())) {
                            currentUserSession.checkout(objectNode);
                            objectNode.getProperty(item.getName()).remove();
                        }
                    }
                }
            }
        }
    }

    public void setProperties(JCRNodeWrapper objectNode, List<GWTJahiaNodeProperty> newProps, String httpSessionID) throws RepositoryException {

        if (objectNode == null || newProps == null || newProps.isEmpty()) {
            logger.debug("node or properties are null or empty");
            return;
        }
        if (!objectNode.isCheckedOut()) {
            objectNode.checkout();
        }
        for (GWTJahiaNodeProperty prop : newProps) {
            try {
                if (prop != null &&
                        !prop.getName().equals("*") &&
                        !Constants.forbiddenPropertiesToCopy.contains(prop.getName())) {
                    if (prop.isMultiple()) {
                        List<Value> values = new ArrayList<Value>();
                        for (GWTJahiaNodePropertyValue val : prop.getValues()) {
                            if (val.getString() != null) {
                                values.add(contentDefinition.convertValue(val, objectNode.getApplicablePropertyDefinition(prop.getName(), 0, true)));
                            }
                        }
                        Value[] finalValues = new Value[values.size()];
                        values.toArray(finalValues);
                        objectNode.setProperty(prop.getName(), finalValues);
                    } else {

                        if (prop.getValues().size() > 0) {

                            GWTJahiaNodePropertyValue propValue = prop.getValues().get(0);
                            if (httpSessionID != null && propValue.getType() == GWTJahiaNodePropertyType.ASYNC_UPLOAD) {

                                // propValue.getString() value is actually file content type like "application/pdf" rather than file name in case we
                                // open a file component for edit, but do not change its content, and then save. Code below relies on the fact that
                                // there is unlikely any uploaded file named like "application/pdf" or similarly, and null will be returned by the
                                // storage in this case. QA-8249 is to refactor the front end to not submit fake values like "application/pdf" as an
                                // actual file names.
                                UploadedPendingFile fileItem = fileStorage.get(httpSessionID, propValue.getString());
                                try {

                                    boolean clear = propValue.getString().equals("clear");
                                    if (!clear && fileItem == null) {
                                        continue;
                                    }

                                    ExtendedNodeDefinition end = ((ExtendedNodeType) objectNode.getPrimaryNodeType()).getChildNodeDefinitionsAsMap().get(prop.getName());
                                    if (end != null) {
                                        try {
                                            if (!clear) {
                                                Node content;
                                                String s = end.getRequiredPrimaryTypeNames()[0];
                                                if (objectNode.hasNode(prop.getName())) {
                                                    content = objectNode.getNode(prop.getName());
                                                } else {
                                                    content = objectNode.addNode(prop.getName(), s.equals("nt:base") ? "jnt:resource" : s);
                                                }
                                                content.setProperty(Constants.JCR_MIMETYPE, fileItem.getContentType());
                                                InputStream is = fileItem.getContentStream();
                                                try {
                                                    content.setProperty(Constants.JCR_DATA, is);
                                                } finally {
                                                    IOUtils.closeQuietly(is);
                                                }
                                                content.setProperty(Constants.JCR_LASTMODIFIED, new GregorianCalendar());
                                            } else {
                                                if (objectNode.hasNode(prop.getName())) {
                                                    objectNode.getNode(prop.getName()).remove();
                                                }
                                            }
                                        } catch (Exception e) {
                                            logger.error(e.getMessage(), e);
                                        }
                                    }
                                } finally {
                                    if (fileItem != null) {
                                        fileItem.close();
                                        fileStorage.remove(httpSessionID, propValue.getString());
                                    }
                                }
                            } else if (propValue.getType() == GWTJahiaNodePropertyType.PAGE_LINK) {

                                if (objectNode.hasNode(prop.getName())) {
                                    Node content = objectNode.getNodes(prop.getName()).nextNode();
                                    content.remove();
                                }

                                // case of link sub-node
                                GWTJahiaNode gwtJahiaNode = propValue.getLinkNode();
                                String linkUrl = gwtJahiaNode.get(Constants.URL);
                                String linkTitle = gwtJahiaNode.get(Constants.JCR_TITLE);
                                String alt = gwtJahiaNode.get(Constants.ALT);
                                String linkType = gwtJahiaNode.get("linkType");
                                GWTJahiaNode nodeReference = gwtJahiaNode.get(Constants.NODE);

                                // case of external
                                if (linkType.equalsIgnoreCase("external") && linkUrl != null) {
                                    Node content = objectNode.addNode(prop.getName(), Constants.JAHIANT_EXTERNAL_PAGE_LINK);
                                    content.setProperty(Constants.JCR_TITLE, linkTitle);
                                    content.setProperty(Constants.URL, linkUrl);
                                    content.setProperty(Constants.ALT, alt);
                                    content.setProperty(Constants.JCR_LASTMODIFIED, new GregorianCalendar());
                                }

                                // case of internal link
                                else if (linkType.equalsIgnoreCase("internal") && nodeReference != null) {
                                    Node content = objectNode.addNode(prop.getName(), Constants.JAHIANT_NODE_LINK);
                                    content.setProperty(Constants.JCR_TITLE, linkTitle);
                                    content.setProperty(Constants.NODE, nodeReference.getUUID());
                                    content.setProperty(Constants.JCR_LASTMODIFIED, new GregorianCalendar());
                                }
                            } else {
                                ExtendedPropertyDefinition epd = objectNode.getApplicablePropertyDefinition(prop.getName());
                                if (epd != null && epd.getSelectorOptions().containsKey("password")) {
                                    if (propValue != null && propValue.getString() != null) {
                                        String enc = encryptPassword(propValue.getString());
                                        Value value = new StringValue(enc);
                                        objectNode.setProperty(prop.getName(), value);
                                    }
                                } else if (propValue != null && propValue.getString() != null) {
                                    Value value = contentDefinition.convertValue(propValue, epd);
                                    objectNode.setProperty(prop.getName(), value);
                                } else {
                                    if (objectNode.hasProperty(prop.getName())) {
                                        objectNode.getProperty(prop.getName()).remove();
                                    }
                                }
                            }
                        } else if (objectNode.hasProperty(prop.getName())) {
                            objectNode.getProperty(prop.getName()).remove();
                        }
                    }
                }
            } catch (PathNotFoundException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Property with the name '" + prop.getName() + "' not found on the node " + objectNode.getPath() + ". Skipping.",
                            e);
                } else {
                    logger.info("Property with the name '" + prop.getName() + "' not found on the node " + objectNode.getPath() + ". Skipping.");
                }
            }
        }
    }

    public List<Value> getCategoryPathValues(String value) {
        if (value == null || value.length() == 0) {
            return Collections.emptyList();
        }
        List<Value> values = new LinkedList<Value>();
        String[] categories = StringUtils.split(value, ",");
        for (String categoryKey : categories) {
            try {
                values.add(new StringValue(Category.getCategoryPath(categoryKey.trim())));
            } catch (JahiaException e) {
                logger.warn("Unable to retrieve category path for category key '" + categoryKey + "'. Cause: " + e.getMessage(), e);
            }
        }
        return values;
    }

    public void setIgnoredProperties(Set<String> ignoredProperties) {
        if (ignoredProperties != null) {
            this.ignoredProperties = ignoredProperties;
        } else {
            this.ignoredProperties = Collections.emptySet();
        }
    }

    public String encryptPassword(String pwd) {
        return StringUtils.isNotEmpty(pwd) ? EncryptionUtils.passwordBaseEncrypt(pwd) : StringUtils.EMPTY;
    }

    public void convertException(NodeConstraintViolationException violationException) throws GWTJahiaServiceException {
        GWTCompositeConstraintViolationException gwt = new GWTCompositeConstraintViolationException(violationException.getMessage());
        addConvertedException(violationException, gwt);
        throw gwt;
    }

    public void convertException(CompositeConstraintViolationException e) throws GWTJahiaServiceException {
        GWTCompositeConstraintViolationException gwt = new GWTCompositeConstraintViolationException(e.getMessage());
        for (ConstraintViolationException violationException : e.getErrors()) {
            if (violationException instanceof NodeConstraintViolationException) {
                addConvertedException((NodeConstraintViolationException) violationException, gwt);
            }
        }
        throw gwt;
    }

    private void addConvertedException(NodeConstraintViolationException violationException, GWTCompositeConstraintViolationException gwt) throws GWTJahiaServiceException {
        if (violationException instanceof PropertyConstraintViolationException) {
            PropertyConstraintViolationException v = (PropertyConstraintViolationException) violationException;
            gwt.addError(v.getPath(), v.getConstraintMessage(), v.getLocale() != null ? v.getLocale().toString() : null, v.getDefinition().getName(), v.getDefinition().getLabel(JahiaLocaleContextHolder.getLocale(), v.getDefinition().getDeclaringNodeType()));
        } else {
            NodeConstraintViolationException v = violationException;
            gwt.addError(v.getPath(), v.getConstraintMessage(), v.getLocale() != null ? v.getLocale().toString() : null, null, null);
        }
    }

}
