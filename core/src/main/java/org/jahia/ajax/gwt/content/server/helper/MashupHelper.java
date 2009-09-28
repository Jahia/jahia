package org.jahia.ajax.gwt.content.server.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.aclmanagement.server.ACLHelper;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNewPortletInstance;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaPortletDefinition;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.PortletEntryPointDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRPortletNode;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:43:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class MashupHelper {
    private static JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();

    private static Logger logger = Logger.getLogger(MashupHelper.class);


    public static List<GWTJahiaPortletDefinition> searchPortlets(String match, ProcessingContext jParams) {
        List<GWTJahiaPortletDefinition> results = new ArrayList<GWTJahiaPortletDefinition>();
        try {
            List<ApplicationBean> appList = new LinkedList<ApplicationBean>();
            if (match != null) {
                ApplicationBean app = ServicesRegistry.getInstance().getApplicationsManagerService().getApplication(match);
                if (app != null) {
                    appList.add(app);
                } else {
                    logger.warn("Application not found for the UUID '" + match + "'");
                }
            } else {
                appList.addAll(ServicesRegistry.getInstance().getApplicationsManagerService().getApplications());
            }
            for (ApplicationBean appBean : appList) {
                if (JCRContentUtils.hasPermission(jParams.getUser(), Constants.JCR_READ_RIGHTS, appBean.getID())) {
                    List<EntryPointDefinition> l = appBean.getEntryPointDefinitions();
                    for (EntryPointDefinition aL : l) {
                        results.add(createGWTJahiaPortletDefinition(jParams, appBean, aL));
                    }
                }
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return results;
    }

    /**
     * Create a GWTJahiaPortletDefinition object from an applicationBean and an entryPointDefinition objects
     *
     * @param appBean              the application bean
     * @param entryPointDefinition the entry point definition
     * @return the portlet definition
     * @throws org.jahia.exceptions.JahiaException
     *          sthg bad happened
     */
    public static GWTJahiaPortletDefinition createGWTJahiaPortletDefinition(ProcessingContext jParams, ApplicationBean appBean, EntryPointDefinition entryPointDefinition) throws JahiaException {
        String portletType = null;
        int expTime = 0;
        String cacheScope = null;
        if (entryPointDefinition instanceof PortletEntryPointDefinition) {
            PortletEntryPointDefinition portletEntryPointDefinition = ((PortletEntryPointDefinition) entryPointDefinition);
            portletType = portletEntryPointDefinition.getInitParameter("portletType");
            expTime = portletEntryPointDefinition.getExpirationCache();
            cacheScope = portletEntryPointDefinition.getCacheScope();
        }
        if (portletType == null) {
            portletType = "jnt:portlet";
        }
        GWTJahiaNodeACL gwtJahiaNodeACL = new GWTJahiaNodeACL(new ArrayList<GWTJahiaNodeACE>());
        gwtJahiaNodeACL.setAvailablePermissions(JCRPortletNode.getAvailablePermissions(appBean.getContext(), entryPointDefinition.getName()));
        return new GWTJahiaPortletDefinition(appBean.getID(), appBean.getContext(), entryPointDefinition.getName(), entryPointDefinition.getDisplayName(jParams.getLocale()), portletType, gwtJahiaNodeACL, entryPointDefinition.getDescription(jParams.getLocale()), expTime, cacheScope);
    }

    /**
     * Create a  GWTJahiaNode object that represents a portlet instance.
     *
     * @param parentPath                 where to create the node
     * @param gwtJahiaNewPortletInstance the portlet instance
     * @param context                    the processing context
     * @return a node
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          sthg bad happened
     */
    public static GWTJahiaNode createPortletInstance(String parentPath, GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            String name = gwtJahiaNewPortletInstance.getInstanceName();

            if (name == null) {
                name = gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getDefinitionName().replaceAll("/", "___") + Math.round(Math.random() * 1000000l);
            }
            ContentManagerHelper.checkName(name);
            if (ContentManagerHelper.checkExistence(parentPath + "/" + name, context.getUser())) {
                throw new GWTJahiaServiceException("A node already exists with name '" + name + "'");
            }
            JCRNodeWrapper parentNode = sessionFactory.getThreadSession(context.getUser()).getNode(parentPath);
            JCRPortletNode node = (JCRPortletNode) ContentManagerHelper.addNode(parentNode, name, gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getPortletType(), gwtJahiaNewPortletInstance.getProperties());

            node.setApplication(gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getApplicationId(), gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getDefinitionName());
            node.revokeAllPermissions();

            // set modes permissions
            if (gwtJahiaNewPortletInstance.getModes() != null) {
                for (GWTJahiaNodeACE ace : gwtJahiaNewPortletInstance.getModes().getAce()) {
                    String user = ace.getPrincipalType() + ":" + ace.getPrincipal();
                    if (!ace.isInherited()) {
                        node.changePermissions(user, ace.getPermissions());
                    }
                }
            }

            // set roles permissions
            if (gwtJahiaNewPortletInstance.getRoles() != null) {
                for (GWTJahiaNodeACE ace : gwtJahiaNewPortletInstance.getRoles().getAce()) {
                    String user = ace.getPrincipalType() + ":" + ace.getPrincipal();
                    if (!ace.isInherited()) {
                        node.changePermissions(user, ace.getPermissions());
                    }
                }
            }
            node.changePermissions("g:users", "rw");
            node.changePermissions("g:guest", "r-");
            try {
                parentNode.save();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                throw new GWTJahiaServiceException("A system error happened");
            }
            return NavigationHelper.getGWTJahiaNode(node, true);
        } catch (RepositoryException e) {
            logger.error(e, e);
            throw new GWTJahiaServiceException("error");
        }
    }

    /**
     * Create a portlet instance
     *
     * @param parentPath
     * @param instanceName
     * @param appName
     * @param entryPointName
     * @param gwtJahiaNodeProperties
     * @param context
     * @return
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *
     */
    public static GWTJahiaNode createPortletInstance(String parentPath, String instanceName, String appName, String entryPointName, List<GWTJahiaNodeProperty> gwtJahiaNodeProperties, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            // get RSS GWTJahiaPortletDefinition
            GWTJahiaPortletDefinition gwtJahiaPortletDefinition = createJahiaGWTPortletDefinitionByName(appName, entryPointName, context);
            if (gwtJahiaPortletDefinition == null) {
                logger.error("[" + appName + "," + entryPointName + "]" + " portlet defintion not found --> Aboard creating  portlet instance");
            }

            GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance = new GWTJahiaNewPortletInstance();
            gwtJahiaNewPortletInstance.setGwtJahiaPortletDefinition(gwtJahiaPortletDefinition);

            // add url property
            gwtJahiaNewPortletInstance.getProperties().addAll(gwtJahiaNodeProperties);
            gwtJahiaNewPortletInstance.getProperties().add(new GWTJahiaNodeProperty("j:expirationTime", new GWTJahiaNodePropertyValue("0", GWTJahiaNodePropertyType.LONG)));

            GWTJahiaNodeACL acl = gwtJahiaPortletDefinition.getBaseAcl();

            // all modes for users of the current site
            GWTJahiaNodeACL modes = gwtJahiaNewPortletInstance.getModes();
            if (modes == null) {
                modes = new GWTJahiaNodeACL();
            }
            List<GWTJahiaNodeACE> modeAces = modes.getAce();
            if (modeAces == null) {
                modeAces = new ArrayList<GWTJahiaNodeACE>();
            }
            if (acl != null && acl.getAvailablePermissions() != null) {
                List<String> modesPermissions = acl.getAvailablePermissions().get(JCRClientUtils.MODES_ACL);
                modeAces.add(ACLHelper.createUsersGroupACE(modesPermissions, true, context));
            }
            modes.setAce(modeAces);
            gwtJahiaNewPortletInstance.setModes(modes);

            // all rodes for users of the current site
            GWTJahiaNodeACL roles = gwtJahiaNewPortletInstance.getRoles();
            if (roles == null) {
                roles = new GWTJahiaNodeACL();
            }
            List<GWTJahiaNodeACE> roleAces = roles.getAce();
            if (roleAces == null) {
                roleAces = new ArrayList<GWTJahiaNodeACE>();
            }
            if (acl != null && acl.getAvailablePermissions() != null) {
                List<String> rolesPermissions = acl.getAvailablePermissions().get(JCRClientUtils.ROLES_ACL);
                roleAces.add(ACLHelper.createUsersGroupACE(rolesPermissions, true, context));
            }
            roles.setAce(roleAces);
            gwtJahiaNewPortletInstance.setRoles(roles);

            // set name
            gwtJahiaNewPortletInstance.setInstanceName(instanceName);
            return createPortletInstance(parentPath, gwtJahiaNewPortletInstance, context);
        } catch (GWTJahiaServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unable to create an RSS portlet due to: ", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * Create an instance of an RSS portlet
     *
     * @param parentPath
     * @param name
     * @param url
     * @param context
     * @return
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *
     */
    public static GWTJahiaNode createRSSPortletInstance(String parentPath, String name, String url, ProcessingContext context) throws GWTJahiaServiceException {
        GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance = new GWTJahiaNewPortletInstance();
        String prefix = Jahia.getContextPath();
        if (prefix.equals("/")) {
            prefix = "";
        } else {
            prefix = prefix.substring(1) + "/";
        }
        // get RSS GWTJahiaPortletDefinition
        final String appName = prefix + "rss";
        GWTJahiaPortletDefinition gwtJahiaPortletDefinition = createJahiaGWTPortletDefinitionByName(appName, "JahiaRSSPortlet", context);
        if (gwtJahiaPortletDefinition == null) {
            logger.error("RSS portlet defintion not found --> Abort creating RSS portlet instance");
        }
        gwtJahiaNewPortletInstance.setGwtJahiaPortletDefinition(gwtJahiaPortletDefinition);

        // create portlet properties
        List<GWTJahiaNodeProperty> gwtJahiaNodeProperties = new ArrayList<GWTJahiaNodeProperty>();
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:title", new GWTJahiaNodePropertyValue(name, GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:description", new GWTJahiaNodePropertyValue(url, GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("j:expirationTime", new GWTJahiaNodePropertyValue("0", GWTJahiaNodePropertyType.LONG)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("url", new GWTJahiaNodePropertyValue(url, GWTJahiaNodePropertyType.STRING)));

        return createPortletInstance(parentPath, name, appName, "JahiaRSSPortlet", gwtJahiaNodeProperties, context);
    }

    /**
     * Create a google gadget node
     *
     * @param parentPath
     * @param name
     * @param script
     * @param context
     * @return
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *
     */
    public static GWTJahiaNode createGoogleGadgetPortletInstance(String parentPath, String name, String script, ProcessingContext context) throws GWTJahiaServiceException {
        GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance = new GWTJahiaNewPortletInstance();
        String prefix = Jahia.getContextPath();
        if (prefix.equals("/")) {
            prefix = "";
        } else {
            prefix = prefix.substring(1) + "/";
        }
        // get RSS GWTJahiaPortletDefinition
        final String appName = prefix + "googlegadget";
        // get RSS GWTJahiaPortletDefinition
        GWTJahiaPortletDefinition gwtJahiaPortletDefinition = createJahiaGWTPortletDefinitionByName(appName, "JahiaGoogleGadget", context);
        if (gwtJahiaPortletDefinition == null) {
            logger.error("Google gadget portlet defintion not found --> Abort creating Google Gadget portlet instance");
        }
        gwtJahiaNewPortletInstance.setGwtJahiaPortletDefinition(gwtJahiaPortletDefinition);

        // create portlet properties
        List<GWTJahiaNodeProperty> gwtJahiaNodeProperties = new ArrayList<GWTJahiaNodeProperty>();
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:title", new GWTJahiaNodePropertyValue(name, GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:description", new GWTJahiaNodePropertyValue("", GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("j:expirationTime", new GWTJahiaNodePropertyValue("0", GWTJahiaNodePropertyType.LONG)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("code", new GWTJahiaNodePropertyValue(script, GWTJahiaNodePropertyType.STRING)));

        return createPortletInstance(parentPath, name, appName, "JahiaGoogleGadget", gwtJahiaNodeProperties, context);
    }

    /**
     * @param appName
     * @param entryPointName
     * @param context
     * @return
     */
    public static GWTJahiaPortletDefinition createJahiaGWTPortletDefinitionByName(String appName, String entryPointName, ProcessingContext context) {
        if (appName != null && entryPointName != null) {
            try {
                // TO DO: replace this part of the method by a more perfoming one
                List<ApplicationBean> appList = ServicesRegistry.getInstance().getApplicationsManagerService().getApplications();
                for (ApplicationBean anAppList : appList) {
                    if (JCRContentUtils.hasPermission(context.getUser(), Constants.JCR_READ_RIGHTS, anAppList.getID())) {
                        List<EntryPointDefinition> l = anAppList.getEntryPointDefinitions();
                        for (EntryPointDefinition aL : l) {
                            boolean foundEntryPointDefinition = appName.equalsIgnoreCase(anAppList.getName()) && aL.getName().equalsIgnoreCase(entryPointName);
                            if (foundEntryPointDefinition) {
                                return createGWTJahiaPortletDefinition(context, anAppList, aL);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.error("Portlet definition not found: " + appName + "[Jahia context path:" + context.getContextPath() + "]");
        return null;
    }
}
