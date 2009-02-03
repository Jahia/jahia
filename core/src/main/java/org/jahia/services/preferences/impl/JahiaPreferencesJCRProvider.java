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

package org.jahia.services.preferences.impl;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.preferences.AbstractJahiaPreferencesProvider;
import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.preferences.JahiaPreferenceKey;
import org.jahia.services.preferences.JahiaPreferenceValue;
import org.jahia.services.preferences.exception.JahiaPreferenceNotDefinedAttributeException;
import org.jahia.services.preferences.exception.JahiaPreferenceNotDefinedPropertyException;
import org.jahia.services.preferences.exception.JahiaPreferencesException;
import org.jahia.services.preferences.exception.JahiaPreferencesNotValidException;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Value;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.*;

/**
 * User: ktlili
 * Date: 15 mai 2008
 * Time: 16:23:14
 * This provider store the preferences in the JCR.
 * JCR Format: content/users/preferences/${userName}/preferences/${provider_type}/preference[key_properties]/preferenceValue[value_properties]
 * with key_properties = properties of the JahiaPreferenceKey bean and calue_properties =  properties of the JahiaPreferenceValue
 * <p/>
 * The mapping between preferences bean (JahiaPreferenceKey and JahiaPreferenceValue) and the JCR tree is done automatically.
 * <p/>
 * Example:
 * BookmarkJahiaPreferenceKey = {pid=3}
 * BookmarkJahiaPreferenceValue = {name='homepage',description='My description'}
 * currentPrincipale = root
 * <p/>
 * ==> Following JCR Format:
 * content/users/preferences/root/preferences/bookmark/preference[@pid=3]/preferenceValue[@name='homepage' and @description='my home page']
 */
public class JahiaPreferencesJCRProvider extends AbstractJahiaPreferencesProvider {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaPreferencesJCRProvider.class);

    // node type
    private static final String JNT_PREFERENCE_VALUE = "jnt:preferenceValue";
    private static final String JNT_PREFERENCE = "jnt:preference";
    private static final String JNT_PREFERENCE_PROVIDER = "jnt:preferenceProvider";
    private static final String JNT_PREFERENCES = "jnt:preferences";
    // node name
    private static final String PREFERENCE_VALUE = "preferenceValue";
    private static final String PREFERENCE = "preference";

    // bean values
    private String providerType;
    private String preferenceClass;
    private String keyClass;
    private String valueClass;

    public JahiaPreferencesJCRProvider() {

    }

    public JahiaPreferencesJCRProvider(String preferenceClass, String keyClass, String valueClass) {
        this.preferenceClass = preferenceClass;
        this.keyClass = keyClass;
        this.valueClass = valueClass;
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public String getPreferenceClass() {
        return preferenceClass;
    }

    public void setPreferenceClass(String preferenceClass) {
        this.preferenceClass = preferenceClass;
    }

    public String getKeyClass() {
        return keyClass;
    }

    public void setKeyClass(String keyClass) {
        this.keyClass = keyClass;
    }

    public String getValueClass() {
        return valueClass;
    }

    public void setValueClass(String valueClass) {
        this.valueClass = valueClass;
    }


    /**
     * Create an empty JahiaPreferenceKey object. The implementation depends on the provider
     *
     * @return
     */
    public JahiaPreferenceKey createEmptyJahiaPreferenceKey() {
        try {
            Object o = Class.forName(getKeyClass()).newInstance();
            if (o instanceof JahiaPreferenceKey) {
                return (JahiaPreferenceKey) o;
            } else {
                logger.error(getKeyClass() + " not instance of JahiaPreferenceKey");
                return null;
            }
        } catch (Exception e) {
            logger.error(e, e);
            return null;
        }
    }

    /**
     * Create an empty JahiaPreference object. The implementation depends on the provider
     *
     * @return
     */
    public JahiaPreferenceValue createEmptyJahiaPreferenceValue() {
        try {
            Object o = Class.forName(getValueClass()).newInstance();
            if (o instanceof JahiaPreferenceValue) {
                return (JahiaPreferenceValue) o;
            } else {
                logger.error(getKeyClass() + " not instance of JahiaPreferenceValue");
                return null;
            }
        } catch (Exception e) {
            logger.error(e, e);
            return null;
        }
    }

    /**
     * Create an empty JahiaPreference object. The implementation depends on the provider
     *
     * @return
     */
    public JahiaPreference createEmptyJahiaPreference() {
        try {
            Object o = Class.forName(getPreferenceClass()).newInstance();
            if (o instanceof JahiaPreference) {
                return (JahiaPreference) o;
            } else {
                logger.error(getKeyClass() + " not instance of JahiaPreference");
                return null;
            }
        } catch (Exception e) {
            logger.error(e, e);
            return null;
        }
    }

    /**
     * Get the type of the provider. Each provider has a unique type.
     *
     * @return
     */
    public String getType() {
        return getProviderType();
    }

    /**
     * @param jahiaPreferenceValue
     * @return true if the preference value is correct
     * @throws org.jahia.services.preferences.exception.JahiaPreferencesNotValidException
     *
     */
    public boolean validate(JahiaPreferenceValue jahiaPreferenceValue) throws JahiaPreferencesNotValidException {
        return true;
    }

    /**
     * Get jahia preference by a jahia preference jahiaPreferenceKey.
     *
     * @param jahiaPreferenceKey
     * @return
     */
    public JahiaPreference getJahiaPreference(JahiaPreferenceKey jahiaPreferenceKey) {
        JahiaPreferenceValue jahiaPreferenceValue = findJahiaPreferenceValueByJahiaPreferenceKey(jahiaPreferenceKey);
        JahiaPreference jahiaPreference = createJahiaPreference(jahiaPreferenceKey, jahiaPreferenceValue);
        return jahiaPreference;

    }

    /**
     * Get all preferences of a principal
     * WARNING: if there is lots of preferences, it can be time consuming.
     *
     * @param principal
     * @return
     */
    public List<JahiaPreference> getJahiaAllPreferences(Principal principal) {
        JahiaPreferenceKey key = createPartialJahiaPreferenceKey(principal);
        return findJahiaPreferenceByJahiaPreferenceKey(key, new ArrayList<String>());
    }

    /**
     * Get a List of preferences depending on the revelant properties
     * Example:
     * JahiaPreferenceKey = {pid,wid}
     * getJahiaPreferencesByPartialKey(key,{wid}) --> get list of preferences with key.getWid() == wid ; pid properties is ignored.
     *
     * @param jahiaPreferenceKey
     * @param revelantProperties
     * @return
     */
    public List<JahiaPreference> getJahiaPreferencesByPartialKey(JahiaPreferenceKey jahiaPreferenceKey, List<String> revelantProperties) {
        return findJahiaPreferenceByJahiaPreferenceKey(jahiaPreferenceKey, revelantProperties);
    }

    /**
     * Get a JahiaPreferenceValue from a JahiaPreferenceKey
     *
     * @param jahiaPreferenceKey
     * @return
     */
    private JahiaPreferenceValue findJahiaPreferenceValueByJahiaPreferenceKey(JahiaPreferenceKey jahiaPreferenceKey) {
        try {
            // find preference node

            Node preferenceNode = findPreferenceNodeByJahiaPreferenceKey(jahiaPreferenceKey);
            if (preferenceNode == null) {
                if (logger.isDebugEnabled()) {
                    printJahiaPreferenceKey(jahiaPreferenceKey);
                }
                return null;
            }

            // get preferenceValue node
            Node preferenceValueNode = preferenceNode.getNode(PREFERENCE_VALUE);
            return fillJahiaPreferenceValue(preferenceValueNode);
        } catch (Exception e) {
            logger.error(e, e);
        }
        return null;
    }

    /**
     * Get a JahiaPreference by a partial JahiaPreferenceKey and a list of revelant properties
     *
     * @param jahiaPreferenceKey
     * @return
     */
    private List<JahiaPreference> findJahiaPreferenceByJahiaPreferenceKey(JahiaPreferenceKey jahiaPreferenceKey, List<String> revelantProperties) {
        // find preference node
        try {
            NodeIterator preferencesNode = findPreferenceNodeByJahiaPreferenceKey(jahiaPreferenceKey, revelantProperties);
            if (preferencesNode == null) {
                if (logger.isDebugEnabled()) {
                    printJahiaPreferenceKey(jahiaPreferenceKey);
                }
                return null;
            }

            // get preferenceValue node

            List<JahiaPreference> jahiaPreferenceList = new ArrayList<JahiaPreference>();
            while (preferencesNode.hasNext()) {
                Node preferenceNode = preferencesNode.nextNode();
                Node preferenceValueNode = preferenceNode.getNode(PREFERENCE_VALUE);

                // create a jahiaPreferenceKey object
                JahiaPreferenceKey currentJahiaPreferenceKey = fillJahiaPreferenceKey(jahiaPreferenceKey.getPrincipal(), preferenceNode);

                // create a jahiaPreferenceValue object
                JahiaPreferenceValue currentJahiaPreferenceValue = fillJahiaPreferenceValue(preferenceValueNode);

                // create a jahiaPreference object
                JahiaPreference currentJahiaPreference = createEmptyJahiaPreference();
                currentJahiaPreference.setKey(currentJahiaPreferenceKey);
                currentJahiaPreference.setValue(currentJahiaPreferenceValue);

                // add it to list
                jahiaPreferenceList.add(currentJahiaPreference);
            }

            return jahiaPreferenceList;
        } catch (Exception e) {
            logger.error(e, e);
        }
        return null;
    }


    /**
     * For debug mode: print( logger.debug(...)) Preference Key properties
     *
     * @param jahiaPreferenceKey
     */
    private void printJahiaPreferenceKey(JahiaPreferenceKey jahiaPreferenceKey) {
        StringBuffer prefPath = new StringBuffer();
        Map propertiesMap = jahiaPreferenceKey.describe();
        if (propertiesMap != null && !propertiesMap.isEmpty()) {
            Iterator<?> propertiesIterator = propertiesMap.keySet().iterator();
            boolean isFirstProperty = true;
            boolean hasProperties = false;
            while (propertiesIterator.hasNext()) {
                String propertyName = (String) propertiesIterator.next();
                String propertyvalue = (String) propertiesMap.get(propertyName);
                // add only if value is not null
                if (propertyvalue != null) {
                    if (isFirstProperty) {
                        prefPath.append("[");
                        isFirstProperty = false;
                    } else {
                        prefPath.append(" and ");
                    }
                    hasProperties = true;
                    prefPath.append("@" + propertyName + "=" + propertyvalue);
                }
            }

            //close
            if (hasProperties) {
                prefPath.append("]");
            }
        }
        logger.debug("JahiaPreference path:" + prefPath.toString());
    }


    /**
     * Get JahiaPreferenceNode by a JahiaPreferenceKey.
     *
     * @param jahiaPreferenceKey
     * @return
     */
    private Node findPreferenceNodeByJahiaPreferenceKey(JahiaPreferenceKey jahiaPreferenceKey) {
        NodeIterator ni = findPreferenceNodeByJahiaPreferenceKey(jahiaPreferenceKey, null);
        // return first node
        Node node = null;
        if (ni != null && ni.getSize() > 0) {
            node = ni.nextNode();

            if (logger.isDebugEnabled()) {
                if (ni.getSize() > 1) {
                    logger.debug("Found more than one (" + ni.getSize() + ") preferences key[" + jahiaPreferenceKey + "].");
                } else {
                    logger.debug("Found (" + ni.getSize() + ") preferences key.");
                }
                try {
                    logger.debug(node.getPath());
                    while (ni.hasNext()) {
                        logger.debug(ni.nextNode().getPath());
                    }
                } catch (RepositoryException e) {
                    logger.error(e, e);
                }
            }
        }
        return node;
    }


    /**
     * Get JahiaPreferenceNode by a JahiaPreferenceKey and revelant properties.
     * if revelantProperties == null, then all properties are revelant
     * if revelantProperties != null and size=0, then retrieve all preference node
     *
     * @param jahiaPreferenceKey
     * @param revelantProperties
     * @return
     */
    private NodeIterator findPreferenceNodeByJahiaPreferenceKey(JahiaPreferenceKey jahiaPreferenceKey, List<String> revelantProperties) {
        // create XPath value by JahiaPreferenceKey
        String xpathNode = getPreferenceProviderNodePath(jahiaPreferenceKey.getPrincipal());
        if (xpathNode != null) {
            StringBuffer prefPath = new StringBuffer(xpathNode);
            // case of all preferences
            if (revelantProperties != null && revelantProperties.size() == 0) {
                prefPath.append("/" + PREFERENCE);
            }

            // case of sub-preferences, depending on properties values
            else {
                prefPath.append(PREFERENCE);
                Map propertiesMap = jahiaPreferenceKey.describe();
                if (propertiesMap != null && !propertiesMap.isEmpty()) {
                    Iterator propertiesIterator = propertiesMap.keySet().iterator();
                    boolean isFirstProperty = true;
                    boolean hasProperties = false;
                    while (propertiesIterator.hasNext()) {
                        String propertyName = (String) propertiesIterator.next();
                        // filter by revelant properties -  revelantProperties == null --> all properties
                        if (revelantProperties == null || revelantProperties.contains(propertyName)) {
                            String propertyvalue = (String) propertiesMap.get(propertyName);
                            // add only if value is not null
                            if (propertyvalue != null) {
                                if (isFirstProperty) {
                                    prefPath.append("[");
                                    isFirstProperty = false;
                                } else {
                                    prefPath.append(" and ");
                                }
                                hasProperties = true;
                                prefPath.append("(@" + propertyName + "='" + propertyvalue + "')");
                            }
                        } else {
                            logger.debug("[" + propertyName + "] is not a revelant property");
                        }
                    }

                    //close
                    if (hasProperties) {
                        prefPath.append("]");
                    }
                } else {
                    logger.error("JahiaPreferenceKey has no properties. Check JahiaPreferenceKey[" + jahiaPreferenceKey.getClass() + "] bean definition.");
                }
                logger.debug("Jahia preference XPath by Jahia jahiaPreferenceKey: " + prefPath.toString());
            }
            return findNodeIteratorByXpath((JahiaUser) jahiaPreferenceKey.getPrincipal(), prefPath.toString());
        } else {
            return null;
        }
    }

    /**
     * Get a NodeIterator.
     *
     * @param jahiaUser
     * @param path
     * @return
     * @throws RepositoryException
     */
    private NodeIterator findNodeIteratorByXpath(JahiaUser jahiaUser, String path) {
        logger.debug("Find node by xpath[ " + path + " ]");
        try {
            QueryManager queryManager = getJCRStoreService().getQueryManager(jahiaUser);
            if (queryManager != null) {
                Query q = queryManager.createQuery(path.toString(), Query.XPATH);
                // execute query
                QueryResult queryResult = q.execute();

                // get node iterator
                NodeIterator ni = queryResult.getNodes();
                if (ni.hasNext()) {
                    logger.debug("Path[" + path + "] --> found [" + ni.getSize() + "] values.");
                    return ni;
                } else {
                    logger.debug("Path[" + path + "] --> empty result.");
                }
            }
        } catch (javax.jcr.PathNotFoundException e) {
            logger.debug("javax.jcr.PathNotFoundException: Path[" + path + "]");
        } catch (javax.jcr.ItemNotFoundException e) {
            logger.debug(e, e);
        } catch (javax.jcr.query.InvalidQueryException e) {
            logger.error("InvalidQueryException ---> [" + path + "] is not valid.", e);
        }
        catch (RepositoryException e) {
            logger.error(e, e);
        }
        return null;
    }

    /**
     * Create a JahiaPreference object
     *
     * @param jahiaPreferenceKey
     * @param jahiaPreferenceValue
     * @return JahiaPreference that depends on the provider implementation.
     */
    private JahiaPreference createJahiaPreference(JahiaPreferenceKey jahiaPreferenceKey, JahiaPreferenceValue jahiaPreferenceValue) {
        JahiaPreference jahiaPreference = createEmptyJahiaPreference();
        jahiaPreference.setKey(jahiaPreferenceKey);
        jahiaPreference.setValue(jahiaPreferenceValue);
        return jahiaPreference;
    }

    /**
     * Create a JahiaPreferenceValue object from node
     *
     * @param preferenceValueNode
     * @return JahiaPreferenceValue object that depends on the provider implementation
     * @throws RepositoryException
     */
    private JahiaPreferenceValue fillJahiaPreferenceValue(Node preferenceValueNode) {
        JahiaPreferenceValue jahiaPreferenceValue = createEmptyJahiaPreferenceValue();
        try {
            PropertyIterator propIterator = preferenceValueNode.getProperties();
            while (propIterator.hasNext()) {
                Property prop = propIterator.nextProperty();
                String name = prop.getName();
                if (prop.getDefinition().isMultiple()) {
                    Value[] jcrValues = prop.getValues();
                    if (jcrValues.length == 1) {
                        String value = jcrValues[0].getString();
                        try {
                            jahiaPreferenceValue.setProperty(name, value);
                        } catch (JahiaPreferenceNotDefinedPropertyException e) {
                            logger.error(e, e);
                        }
                    } else {
                        String[] values = new String[jcrValues.length];
                        for (int i = 0; i < jcrValues.length; i++) {
                            values[i] = jcrValues[i].getString();
                        }
                        try {
                            jahiaPreferenceValue.setProperty(name, values);
                        } catch (JahiaPreferenceNotDefinedPropertyException e) {
                            logger.error(e, e);
                        }
                    }
                } else {
                    String value = prop.getString();
                    try {
                        jahiaPreferenceValue.setProperty(name, value);
                    } catch (JahiaPreferenceNotDefinedPropertyException e) {
                        logger.error(e, e);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e, e);
        }

        return jahiaPreferenceValue;
    }

    /**
     * Create a JahiaPreferenceValue object from node
     *
     * @param preferenceKeyNode
     * @return JahiaPreferenceValue object that depends on the provider implementation
     * @throws RepositoryException
     */
    private JahiaPreferenceKey fillJahiaPreferenceKey(Principal principal, Node preferenceKeyNode) {
        JahiaPreferenceKey jahiaPreferenceKey = createEmptyJahiaPreferenceKey();
        jahiaPreferenceKey.setPrincipal(principal);
        try {
            PropertyIterator propIterator = preferenceKeyNode.getProperties();
            while (propIterator.hasNext()) {
                Property prop = propIterator.nextProperty();
                // multi-value not supported by key bean object
                if (prop != null && !prop.getDefinition().isMultiple()) {
                    String name = prop.getName();
                    String value = prop.getString();
                    try {
                        jahiaPreferenceKey.setProperty(name, value);
                    } catch (JahiaPreferenceNotDefinedAttributeException e) {
                        logger.error(e, e);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e, e);
        }

        return jahiaPreferenceKey;
    }

    /**
     * Get jahia preferences of by principal_key and principal_type.
     *
     * @param jahiaPreferenceKey
     * @return map <key,preference> that contains all preferences of the principal.
     */
    public Map<JahiaPreferenceKey, JahiaPreference> getJahiaPreferences(JahiaPreferenceKey jahiaPreferenceKey) {
        // get principal
        Map<JahiaPreferenceKey, JahiaPreference> prefMap = new HashMap<JahiaPreferenceKey, JahiaPreference>();
        Principal principal = jahiaPreferenceKey.getPrincipal();
        try {
            // get preferences node corresponding to the current user
            Node preferencesNode = getPreferencesNode(jahiaPreferenceKey);

            // get provider preferences node corresponding to the current user
            Node preferenceProviderNode = preferencesNode.getNode(getType());

            if (preferenceProviderNode != null) {
                // get provider node
                logger.debug("Preference Provider Node" + preferenceProviderNode.getPath());
                NodeIterator preferenceIt = preferenceProviderNode.getNodes(PREFERENCE);
                if (preferenceIt != null) {
                    logger.debug(getType() + ":find " + preferenceIt.getSize() + " preference(s)");
                    while (preferenceIt.hasNext()) {
                        Node currentPrefNode = preferenceIt.nextNode();
                        try {
                            // get value node
                            Node prefValueNode = currentPrefNode.getNode(PREFERENCE_VALUE);

                            // create beans
                            JahiaPreferenceValue currentJahiaPreferenceValue = fillJahiaPreferenceValue(prefValueNode);
                            JahiaPreferenceKey currentJahiaPreferenceKey = fillJahiaPreferenceKey(principal, currentPrefNode);
                            JahiaPreference currentPreference = createJahiaPreference(currentJahiaPreferenceKey, currentJahiaPreferenceValue);

                            // put into map
                            prefMap.put(currentJahiaPreferenceKey, currentPreference);
                        } catch (RepositoryException e) {
                            logger.error(e, e);
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e, e);
        }

        return prefMap;
    }

    /**
     * Set a jahia preference value.
     *
     * @param jahiaPreferenceKey
     * @param jahiaPreferenceValue
     * @throws org.jahia.services.preferences.exception.JahiaPreferencesNotValidException
     *
     */
    public void setJahiaPreference(JahiaPreferenceKey jahiaPreferenceKey, JahiaPreferenceValue jahiaPreferenceValue) {
        try {
            if (jahiaPreferenceKey == null) {
                logger.error("JahiaPreferenceKey is null");
                return;
            }
            // get preferences node corresponding to the current user
            Node preferencesNode = getPreferencesNode(jahiaPreferenceKey);

            // get provider preferences node corresponding to the current user
            Node providerNode = preferencesNode.getNode(getType());

            // set values
            if (providerNode != null) {
                logger.debug("Jahia Preference Provider [" + providerNode.isNodeType(JNT_PREFERENCE_PROVIDER) + "] path: " + providerNode.getPath());
                // case of delete
                if (jahiaPreferenceValue == null) {
                    Node preferenceNode = findPreferenceNodeByJahiaPreferenceKey(jahiaPreferenceKey);
                    if (preferenceNode != null) {
                        preferenceNode.remove();
                        providerNode.save();
                        logger.debug("JahiaPreference deleted");
                    } else {
                        logger.debug("There is no jahiaPreference with given key[" + jahiaPreferenceKey + "].");
                    }
                    return;
                }

                // case of add/update
                Node preferenceValueNode;

                // retrieve JahiaPreferenceNode
                Node preferenceNode = findPreferenceNodeByJahiaPreferenceKey(jahiaPreferenceKey);
                if (preferenceNode == null) {
                    // create preference node
                    logger.debug("Create Jahia Preference node ");
                    preferenceNode = providerNode.addNode(PREFERENCE, JNT_PREFERENCE);
                    logger.debug("Jahia Preference [" + preferenceNode.isNodeType(JNT_PREFERENCE) + "] path: " + preferenceNode.getPath());

                    // fill preference Node
                    fillPreferenceNode(jahiaPreferenceKey, preferenceNode);
                    logger.debug("Jahia Preference[" + preferenceNode.isNodeType(JNT_PREFERENCE) + "] path - " + preferenceNode.getPath());

                    //create preference node value
                    preferenceValueNode = preferenceNode.addNode(PREFERENCE_VALUE, JNT_PREFERENCE_VALUE);
                    logger.debug("Jahia Preference Value[" + preferenceValueNode.isNodeType(JNT_PREFERENCE_VALUE) + "] path - " + preferenceValueNode.getPath());

                } else {
                    preferenceValueNode = preferenceNode.getNode(PREFERENCE_VALUE);
                }

                // fill prefence Value Node
                logger.debug("Fill preference value Node");
                fillPreferenceValueNode(jahiaPreferenceValue, preferenceValueNode);
                // save
                logger.debug("Save into the JCR");
                preferencesNode.save();

            } else {
                logger.error("Preference node not found.");
            }
        } catch (RepositoryException re) {
            logger.error("Error while setting preference " + jahiaPreferenceKey, re);
        } catch (IllegalAccessException iae) {
            logger.error("Error while setting preference " + jahiaPreferenceKey, iae);
        } catch (NoSuchMethodException nsme) {
            logger.error("Error while setting preference " + jahiaPreferenceKey, nsme);
        } catch (InvocationTargetException ite) {
            logger.error("Error while setting preference " + jahiaPreferenceKey, ite);
        }

    }

    /**
     * fill a preference key node from a jahia preference key object
     *
     * @param jahiaPreferenceKey
     * @param preferenceNode
     * @throws RepositoryException
     */
    private void fillPreferenceNode(JahiaPreferenceKey jahiaPreferenceKey, Node preferenceNode) throws RepositoryException {
        if (jahiaPreferenceKey == null || preferenceNode == null) {
            logger.debug("preferenceKey or preferenceValue is null.");
            return;
        }

        if (preferenceNode.isNodeType(JNT_PREFERENCE)) {
            logger.debug("Jahia Preference [" + preferenceNode.getName() + "-jnt:preferenceKey] path: " + preferenceNode.getPath());
            Map propertiesMap = jahiaPreferenceKey.describe();
            if (propertiesMap != null) {
                logger.debug("Found " + propertiesMap.size() + " properties");
                Iterator propertiesIterator = propertiesMap.keySet().iterator();
                while (propertiesIterator.hasNext()) {
                    String propertyName = (String) propertiesIterator.next();
                    String propertyValue = (String) propertiesMap.get(propertyName);
                    if (propertyValue != null) {
                        logger.debug("set property [" + propertyName + "," + propertyValue + "]");
                        preferenceNode.setProperty(propertyName, propertyValue);
                    }
                }
            } else {
                logger.debug("There is no property for the current key.");
            }
        } else {
            logger.error("Node must have [jnt:preference] type");
        }

    }

    /**
     * fill a preference key node from a jahia preference key object
     *
     * @param jahiaPreferenceValue
     * @param preferenceValueNode
     * @throws RepositoryException
     */
    private void fillPreferenceValueNode(JahiaPreferenceValue jahiaPreferenceValue, Node preferenceValueNode) throws RepositoryException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (jahiaPreferenceValue == null || preferenceValueNode == null) {
            logger.debug("one of the parameters is null.");
            return;
        }
        Map propertiesMap = jahiaPreferenceValue.describe();
        if (propertiesMap != null) {
            Iterator propertiesIterator = propertiesMap.keySet().iterator();
            while (propertiesIterator.hasNext()) {
                String propertyName = (String) propertiesIterator.next();
                PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(jahiaPreferenceValue, propertyName);
                if (propertyDescriptor.getPropertyType() == String[].class) {
                    String[] multiValuedString = BeanUtils.getArrayProperty(jahiaPreferenceValue, propertyName);
                    preferenceValueNode.setProperty(propertyName, multiValuedString);
                } else {
                    String propertyValue = (String) propertiesMap.get(propertyName);
                    logger.debug("Set Value property [" + propertyName + "," + propertyValue + "]");
                    String[] jcrMultiValue = new String[1];
                    jcrMultiValue[0] = propertyValue;
                    preferenceValueNode.setProperty(propertyName, jcrMultiValue);
                }
            }
        }
    }

    /**
     * Delete all preferences of principal.
     *
     * @param principal
     */
    public void deleteAllPreferencesByPrincipal(Principal principal) {
        try {
            JahiaPreferenceKey key = createPartialJahiaPreferenceKey(principal);
            // get preferences node corresponding to the current user
            Node preferencesNode = getPreferencesNode(key);

            // get provider preferences node corresponding to the current user
            Node prefrerenceProviderNode = preferencesNode.getNode(getType());
            if (prefrerenceProviderNode != null) {
                NodeIterator ni = prefrerenceProviderNode.getNodes();
                if (ni != null) {
                    while (ni.hasNext()) {
                        Node n = ni.nextNode();
                        n.remove();
                    }
                }
                prefrerenceProviderNode.save();
                logger.error("all preferences deleted ");
            }
        } catch (RepositoryException e) {
            logger.error(e, e);
        }
    }

    /**
     * Import preferences
     *
     * @param xmlDocument
     * @throws org.jahia.services.preferences.exception.JahiaPreferencesException
     *
     */
    public void importPreferences(String xmlDocument) throws JahiaPreferencesException {

    }

    /**
     * @return A string representing an XML document
     * @throws org.jahia.services.preferences.exception.JahiaPreferencesException
     *
     */
    public String exportPreferences() throws JahiaPreferencesException {

        return null;
    }

    /**
     * Get the JCR node that represents the jahia preference.
     *
     * @param jahiaPreferenceKey
     * @return JCR node that depends on the provider implementation
     */
    private Node getPreferencesNode(JahiaPreferenceKey jahiaPreferenceKey) {
        String nodePath = "/" + getPreferencesNodePath(jahiaPreferenceKey.getPrincipal());
        try {
            JCRNodeWrapper preferencesNode = getJCRStoreService().getFileNode(nodePath, (JahiaUser) jahiaPreferenceKey.getPrincipal());
            if (preferencesNode == null) {
                logger.error("Unable to find user preference node - path[" + nodePath + " ]");

            } else {
                // make sure that the corresponding do the current provider exist
                if (preferencesNode.isValid() && !preferencesNode.hasNode(getType())) {
                    preferencesNode.addNode(getType(), JNT_PREFERENCE_PROVIDER);
                }
            }
            return preferencesNode;
        } catch (Exception e) {
            logger.error("Node path = [" + nodePath + "]");
            logger.error(e, e);
        }

        return null;
    }

    /**
     * Initialsite the PreferecenceService for the princiapl
     *
     * @param p
     */
    private void initUserPreferencesProviderNode(Principal p) {
        if (p instanceof JahiaUser) {
            getJCRStoreService().getMainStoreProvider().getQueryManager((JahiaUser) p);
        }
    }

    /**
     * Get path of the preference provider node
     *
     * @return String that depends on the provider implementation
     */
    private String getPreferenceProviderNodePath(Principal principal) {
        return getPreferencesNodePath(principal) + getType() + "/";
    }

    /**
     * Get path of the preference node
     *
     * @return String that depends on the provider implementation
     */
    private String getPreferencesNodePath(Principal principal) {
        String principalName;
        if (principal instanceof JahiaUser) {
            principalName = ((JahiaUser) principal).getUsername();
        } else {
            principalName = ((JahiaGroup) principal).getGroupname();
        }
        return "content/users/" + principalName + "/preferences/";
    }

    /**
     * Get the JCR store service
     *
     * @return
     */
    private JCRStoreService getJCRStoreService() {
        return ServicesRegistry.getInstance().getJCRStoreService();
    }
}
