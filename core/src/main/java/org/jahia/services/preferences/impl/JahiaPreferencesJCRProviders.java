package org.jahia.services.preferences.impl;

import org.jahia.services.preferences.JahiaPreferencesProvider;
import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.preferences.JahiaPreferencesXpathHelper;
import org.jahia.services.preferences.exception.JahiaPreferencesNotValidException;
import org.jahia.services.preferences.exception.JahiaPreferenceNotDefinedAttributeException;
import org.jahia.services.preferences.exception.JahiaPreferenceNotDefinedPropertyException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.NodeIterator;
import javax.jcr.query.QueryManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.security.Principal;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 11 mars 2009
 * Time: 12:18:34
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPreferencesJCRProviders<T extends JCRNodeWrapper> implements JahiaPreferencesProvider<T> {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaPreferencesJCRProviders.class);

    // node name
    private static final String PREFERENCE = "preference";

    // bean values
    private String type;
    private String nodeType;
    private JCRStoreService jcrStoreService;

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public JCRStoreService getJCRStoreService() {
        return jcrStoreService;
    }

    public void setJCRStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }

    /**
     * Create a new JahiaPrefrence Node
     *
     * @param processingContext
     * @return
     */
    public JahiaPreference createJahiaPreferenceNode(ProcessingContext processingContext) {
        JahiaPreference jahiaPreference = createJahiaPreferenceNode(processingContext.getUser());
        return jahiaPreference;
    }

    /**
     * Create a new JahiaPreference Node
     *
     * @param principal
     * @return
     */
    public JahiaPreference createJahiaPreferenceNode(Principal principal) {
        try {
            logger.debug("Create Jahia Preference Node [" + principal + "]");

            Node preferencesNode = getProviderPreferencesNode(principal);
            if (preferencesNode != null) {
                Node newChildNode = preferencesNode.addNode(PREFERENCE, nodeType);
                return createJahiaPreference(principal, newChildNode);
            } else {
                logger.debug("Preferences node not found for user [" + principal + "]");
                return null;
            }
        } catch (Exception e) {
            logger.error(e, e);
            return null;
        }
    }

    /**
     * @param p
     * @param node
     * @return
     */
    private JahiaPreference createJahiaPreference(Principal p, Node node) {
        try {
            JahiaPreference pref = new JahiaPreference((JCRNodeWrapper) node);
            pref.setPrincipal(p);
            return pref;


//            if (node instanceof JahiaPreference) {
//                JahiaPreference pref = (JahiaPreference) node;
//                pref.setPrincipal(p);
//                return pref;
//            } else {
//                logger.error(node.getClass() + " not instance of JahiaPreference");
//                return null;
//            }
        } catch (Exception e) {
            logger.error(e, e);
            return null;
        }
    }

    /**
     * @param jahiaPreference
     * @return
     * @throws JahiaPreferencesNotValidException
     *
     */
    public boolean validate(JahiaPreference jahiaPreference) throws JahiaPreferencesNotValidException {
        return true;
    }

    /**
     * @param principal
     * @param xpathKey
     * @return
     */
    public JahiaPreference getJahiaPreference(Principal principal, String xpathKey) {
        return getJahiaPreference(principal, xpathKey, false);
    }

    /**
     * @param principal
     * @param xpathKey
     * @param notNull
     * @return
     */
    public JahiaPreference getJahiaPreference(Principal principal, String xpathKey, boolean notNull) {
        logger.debug("Get preference -" + getType() + " - " + principal.getName() + " - " + xpathKey + "- ");
        List<JahiaPreference<T>> jahiaPreferences = findJahiaPreferences(principal, xpathKey);
        if (jahiaPreferences != null && !jahiaPreferences.isEmpty()) {
            return jahiaPreferences.get(0);
        }

        logger.debug("Preference - " + xpathKey + "- not found.");
        if (notNull) {
            return createJahiaPreferenceNode(principal);
        }
        return null;
    }

    /**
     * @param principal
     * @return
     */
    public List<JahiaPreference<T>> getJahiaAllPreferences(Principal principal) {
        return findJahiaPreferences(principal, null);
    }

    /**
     * @param principal
     * @param xpath
     * @return
     */
    public List<JahiaPreference<T>> findJahiaPreferences(Principal principal, String xpath) {
        NodeIterator ni = findPreferenceNodeByJahiaPreferenceXPath(principal, xpath);
        if (ni == null) {
            return new ArrayList<JahiaPreference<T>>();
        }
        List<JahiaPreference<T>> jahiaPreferenceList = new ArrayList<JahiaPreference<T>>();
        while (ni.hasNext()) {
            Node currentPrefNode = ni.nextNode();
            jahiaPreferenceList.add(createJahiaPreference(principal, currentPrefNode));
        }
        return jahiaPreferenceList;
    }

    /**
     * Get all jahia preferences
     *
     * @param processingContext
     * @return
     */
    public List<JahiaPreference<T>> getAllJahiaPreferences(ProcessingContext processingContext) {
        return getJahiaAllPreferences(processingContext.getUser());
    }

    /**
     * Delete Jahia Preference
     *
     * @param principal
     * @param xpath
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     */
    public void deleteJahiaPreference(Principal principal, String xpath) throws JahiaPreferenceNotDefinedAttributeException {
        NodeIterator ni = findPreferenceNodeByJahiaPreferenceXPath(principal, xpath);
        while (ni.hasNext()) {
            try {
                Node next = ni.nextNode();

                //  Node parentNode = next.getParent();
                next.remove();
                //  parentNode.save();
            } catch (RepositoryException e) {
                logger.error("Error while deleting preference " + xpath, e);
            }
        }
        try {
            getProviderPreferencesNode(principal).save();
        } catch (RepositoryException e) {
            logger.error("Error while deleting preference " + xpath, e);
        }
    }

    /**
     * Delete Jahia Prefernce
     *
     * @param jahiaPreference
     */
    public void deleteJahiaPreference(JahiaPreference jahiaPreference) {
        logger.debug("Delete Jahia Preference");
        if (jahiaPreference == null) {
            return;
        }
        try {
            Node parentNode = jahiaPreference.getNode().getParent();
            jahiaPreference.getNode().remove();
            parentNode.save();
        } catch (RepositoryException re) {
            logger.error("Error while deleting preference " + jahiaPreference, re);
        }
    }

    /**
     * Set Jahia preference
     *
     * @param jahiaPreference
     */
    public void setJahiaPreference(JahiaPreference jahiaPreference) {
        if (jahiaPreference == null) {
            logger.warn("Can't set a null jahia preference");
            return;
        }
        try {
            JCRNodeWrapper node = jahiaPreference.getNode();
            node.getParent().save();
            logger.debug("Jahia preference [" + jahiaPreference + "] saved.");

            logger.debug("[" + node.getPath() + JahiaPreferencesXpathHelper.convetToXpath2(node.getPropertiesAsString()) + "] saved.");
        } catch (RepositoryException re) {
            logger.error("Error while setting preference " + jahiaPreference, re);
        }
    }

    /**
     * Set jahia preference by properties Map
     *
     * @param jahiaPreferenceAttributes
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     * @throws JahiaPreferenceNotDefinedPropertyException
     *
     * @throws JahiaPreferencesNotValidException
     *
     */
    public void setJahiaPreferenceByMaps(Map<String, String> jahiaPreferenceAttributes) throws JahiaPreferenceNotDefinedAttributeException, JahiaPreferenceNotDefinedPropertyException, JahiaPreferencesNotValidException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Delete all preferences by node
     *
     * @param principal
     */
    public void deleteAllPreferencesByPrincipal(Principal principal) {
        try {
            JCRNodeWrapper preferencesNode = getPreferencesNode(principal);
            if (preferencesNode != null && preferencesNode.isValid()) {
                Node ppNode = getPreferencesNode(principal).getNode(getType());
                if (ppNode != null) {
                    ppNode.remove();
                }
                preferencesNode.save();
            }
        } catch (RepositoryException e) {
            logger.error(e, e);
        }
    }


    /**
     * Get the JCR node that represents the jahia preference.
     *
     * @param principal
     * @return JCR node that depends on the provider implementation
     */
    private JCRNodeWrapper getPreferencesNode(Principal principal) {
        String nodePath = "/" + getPreferencesNodePath(principal);
        try {
            return getJCRStoreService().getFileNode(nodePath, (JahiaUser) principal);
        } catch (Exception e) {
            logger.error("Node path = [" + nodePath + "]");
            logger.error(e, e);
        }

        return null;
    }

    /**
     * Get provider node
     *
     * @param principal
     * @return
     */
    private Node getProviderPreferencesNode(Principal principal) {
        try {
            JCRNodeWrapper preferences = getPreferencesNode(principal);
            if (!preferences.hasNode(getType())) {
                Node p = preferences.addNode(getType(), "jnt:preferenceProvider");
                preferences.save();
                return p;
            }
            return preferences.getNode(getType());
        } catch (Exception e) {
            logger.error(e, e);
        }

        return null;
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
     * @param p
     * @param partialXPath
     * @return
     */
    private NodeIterator findPreferenceNodeByJahiaPreferenceXPath(Principal p, String partialXPath) {
        // create XPath value by JahiaPreferenceKey
        String xpathNode = getPreferenceProviderNodePath(p);
        StringBuffer prefPath = new StringBuffer(encodeXPath(xpathNode));
        prefPath.append(PREFERENCE);
        if (partialXPath != null) {
            prefPath.append(partialXPath);
        }
        return findNodeIteratorByXpath(p, prefPath.toString());
    }


    /**
     * Get a NodeIterator.
     *
     * @param p
     * @param path
     * @return
     * @throws RepositoryException
     */
    private NodeIterator findNodeIteratorByXpath(Principal p, String path) {
        logger.debug("Find node by xpath[ " + path + " ]");
        if (p instanceof JahiaGroup) {
            logger.warn("Preference provider not implemented for JahiaGroup");
            return null;
        }
        try {
            QueryManager queryManager = getJCRStoreService().getQueryManager((JahiaUser) p);
            if (queryManager != null) {
                Query q = queryManager.createQuery(path, Query.XPATH);
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
     * To Do: encode corretly all escaped charater.
     * @param xpath
     * @return
     */
    private String encodeXPath(String xpath) {
        if (xpath != null) {
            // To Do: to it for all escaped charater
            return xpath.replaceAll("@", "_0040_");
        }
        return null;

    }
}
