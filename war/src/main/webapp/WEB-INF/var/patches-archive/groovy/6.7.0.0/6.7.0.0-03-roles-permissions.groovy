import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry
import org.apache.log4j.Logger
import org.jahia.services.content.*

import javax.jcr.ImportUUIDBehavior
import javax.jcr.NodeIterator
import javax.jcr.PropertyType
import javax.jcr.RepositoryException
import javax.jcr.Value
import javax.jcr.query.Query
import javax.jcr.query.QueryResult

final Logger log = Logger.getLogger("org.jahia.tools.groovyConsole");

def callback = new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
        def refToNames = { String nodeType, String refPropName, String namesPropName ->
            QueryResult result = jcrsession.getWorkspace().getQueryManager().createQuery("select * from [" + nodeType + "]", Query.JCR_SQL2).execute();
            NodeIterator ni = result.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapperImpl next = (JCRNodeWrapper) ni.next();
                try {
                    if(next.hasProperty(refPropName)) {
                        JCRPropertyWrapper property = next.getProperty(refPropName);
                        JCRValueWrapper[] values = property.getRealValues();
                        def names = [];
                        values.collect(names, {
                            try {
                                def node = it.getNode();
                                if (node != null) {
                                    node.getName();
                                }
                            } catch (Exception e) {
                                log.error("Failed to get permission name", e);
                            }
                        });
                        next.setProperty(namesPropName, names.toArray(new String[names.size()]));
                        property.remove();
                        jcrsession.save();
                    }
                } catch (Exception e) {
                    log.error("Failed to change references to names", e);
                }
            }
        }
        def workspaceName = jcrsession.getWorkspace().getName();
        log.info("Changing permission references to names on roles on workspace " + workspaceName + "...");
        refToNames("jnt:role", "j:permissions", "j:permissionNames");
        log.info("...update done.")
        log.info("Changing permission references to names on external permissions on workspace " + workspaceName + "...");
        refToNames("jnt:externalPermissions", "j:permissions", "j:permissionNames");
        log.info("...update done.")
        log.info("Changing required permissions references to names on workspace " + workspaceName + "...");
        refToNames("jmix:requiredPermissions", "j:requiredPermissions", "j:requiredPermissionNames");
        log.info("...update done.")
    }
};
JCRTemplate.getInstance().doExecuteWithSystemSession(callback);
JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", callback);



JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
        if (jcrsession.nodeExists("/permissions")) {
            log.info("Start re-importing permissions...")
            jcrsession.getNode("/permissions").remove();
            JCRContentUtils.importSkeletons("WEB-INF/etc/repository/root-permissions.xml", "/", jcrsession, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW, null);
            JahiaPrivilegeRegistry.init(jcrsession);
            log.info("...permissions re-imported.")
        }
        jcrsession.save();

        JCRNodeWrapper rolesNode = jcrsession.getNode("/roles");
        if (rolesNode.hasNode("j:acl")) {
            log.info("Removing /roles/j:acl node.");
            rolesNode.getNode("j:acl").remove();
        }

        JCRNodeWrapper role;
        JCRNodeWrapper subNode;

        def setPermissions = {JCRNodeWrapperImpl node, List<String> permsToRemove, List<String> permsToAdd ->
            def permissionNames = [];
            node.getProperty("j:permissionNames").getValues().collect(permissionNames, {it.getString()});
            permissionNames.removeAll(permsToRemove);
            for (String permName : permsToAdd) {
                if (!permissionNames.contains(permName)) {
                    permissionNames.add(permName);
                }
            }
            node.setProperty("j:permissionNames", permissionNames.toArray(new String[permissionNames.size()]));
        }

        if (rolesNode.hasNode("contributor")) {
            log.info("Start updating contributor role...");
            role = rolesNode.getNode("contributor");
            role.setProperty("j:hidden", false);
            def permsToRemove = ["contributeMode", "viewContentTab", "viewMetadataTab", "viewCategoriesTab",
                    "viewTagsTab", "view-basic-wysiwyg-editor"];
            def permsToAdd = ["2-step-publication-finish-correction", "2-step-publication-start"];
            setPermissions(role, permsToRemove, permsToAdd);

            subNode = role.addNode("currentSite-access", "jnt:externalPermissions");
            subNode.setProperty("j:path", "currentSite");
            String[] permissionNames = ["contributeMode", "editorialContentManager", "fileManager"];
            subNode.setProperty("j:permissionNames", permissionNames);
            subNode = role.addNode("j:translation_en", "jnt:translation");
            subNode.setProperty("jcr:description", "Can edit content using contribute mode");
            subNode.setProperty("jcr:language", "en");
            subNode.setProperty("jcr:title", "Contributor");
            log.info("...update done.");
        }

        if (rolesNode.hasNode("editor")) {
            log.info("Start updating editor role...");
            role = rolesNode.getNode("editor");
            role.setProperty("j:hidden", false);
            def permsToRemove = ["editModeActions", "editModeAccess", "editSelector", "viewContentTab", "viewLayoutTab",
                    "viewMetadataTab", "viewOptionsTab", "viewCategoriesTab", "viewLiveRolesTab", "viewTagsTab",
                    "viewSeoTab", "viewVisibilityTab", "viewContributeModeTab", "contributeMode", "templates",
                    "fileManager", "portletManager", "view-full-wysiwyg-editor"];
            def permsToAdd = ["2-step-publication-finish-correction", "2-step-publication-start"];
            setPermissions(role, permsToRemove, permsToAdd);

            subNode = role.addNode("currentSite-access", "jnt:externalPermissions");
            subNode.setProperty("j:path", "currentSite");
            def permissionNames =  ["editModeAccess", "editModeActions", "editSelector", "viewCategoriesTab",
                    "viewContentTab", "viewContributeModeTab", "viewLayoutTab", "viewMetadataTab", "viewOptionsTab",
                    "viewSeoTab", "viewTagsTab", "viewVisibilityTab", "managers", "view-full-wysiwyg-editor"];
            subNode.setProperty("j:permissionNames", permissionNames.toArray(new String[permissionNames.size()]));
            subNode = role.addNode("j:translation_en", "jnt:translation");
            subNode.setProperty("jcr:description", "Can edit content using edit mode");
            subNode.setProperty("jcr:language", "en");
            subNode.setProperty("jcr:title", "Editor");
            log.info("...update done.");
        }

        if (rolesNode.hasNode("editor-in-chief")) {
            log.info("Start updating editor-in-chief role...");
            role = rolesNode.getNode("editor-in-chief");
            role.setProperty("j:hidden", false);
            def permsToRemove = ["jcr:all_default", "categoryManager", "editorialContentManager", "fileManager",
                    "portletManager", "siteManager", "tagManager", "jobs", "editMode", "view-basic-wysiwyg-editor",
                    "templates"];
            setPermissions(role, permsToRemove, []);

            subNode = role.addNode("currentSite-access", "jnt:externalPermissions");
            subNode.setProperty("j:path", "currentSite");
            subNode.setProperty("j:permissionNames", ["editMode"].toArray(new String[1]));
            subNode = role.addNode("j:translation_en", "jnt:translation");
            subNode.setProperty("jcr:description", "View all engine tabs, is also allowed to grant and revoke roles on content");
            subNode.setProperty("jcr:language", "en");
            subNode.setProperty("jcr:title", "Editor in chief");

            jcrsession.move("/roles/editor-in-chief", "/roles/editor/editor-in-chief");
            log.info("...update done.");
        }

        if (rolesNode.hasNode("web-designer")) {
            log.info("Start updating web-designer role...");
            role = rolesNode.getNode("web-designer");
            role.setProperty("j:hidden", false);
            def nodeTypes = [];
            role.getProperty("j:nodeTypes").getValues().collect(nodeTypes, {it.getString()});
            nodeTypes.remove("jnt:virtualsite");
            if (!nodeTypes.contains("rep:root")) {
                nodeTypes.add("rep:root");
            }
            role.setProperty("j:nodeTypes", nodeTypes.toArray(new String[nodeTypes.size()]));
            role.setProperty("j:permissionNames", ["adminTemplates"].toArray(new String[1]));
            role.setProperty("j:roleGroup", "server-role");

            if (role.hasNode("studio-access")) {
                role.getNode("studio-access").remove();
            }

            if (role.hasNode("modules-management")) {
                subNode = role.getNode("modules-management");
                subNode.setProperty("j:path", "/modules");
                def permsToRemove = ["editMode", "adminTemplates"];
                setPermissions(subNode, permsToRemove, []);
                subNode.rename("modules-access");

            }
            subNode = role.addNode("j:translation_en", "jnt:translation");
            subNode.setProperty("jcr:description", "Gives full access to the studio");
            subNode.setProperty("jcr:language", "en");
            subNode.setProperty("jcr:title", "Web designer");
            log.info("...update done.");
        }

        if (rolesNode.hasNode("site-administrator")) {
            log.info("Start updating site-administrator role...");
            role = rolesNode.getNode("site-administrator");
            role.setProperty("j:hidden", false);
            def nodeTypes = [];
            role.getProperty("j:nodeTypes").getValues().collect(nodeTypes, {it.getString()});
            nodeTypes.remove("jnt:virtualsite");
            role.setProperty("j:nodeTypes",  nodeTypes.toArray(new String[nodeTypes.size()]));
            def permsToRemove = ["administrationAccess", "siteAdminLanguages", "siteAdminUrlmapping",
                    "siteAdminHtmlSettings", "adminDocumentation", "siteAdminGroups", "adminIssueTracking",
                    "siteAdminTemplates", "siteAdminWcagCompliance"];
            def permsToAdd = ["remotePublicationManager", "repositoryExplorer", "site-admin"];
            setPermissions(role, permsToRemove, permsToAdd);
            role.setProperty("j:roleGroup", "site-role");

            subNode = role.addNode("j:translation_en", "jnt:translation");
            subNode.setProperty("jcr:description", "");
            subNode.setProperty("jcr:language", "en");
            subNode.setProperty("jcr:title", "");
            log.info("...update done.");
        }

        log.info("Start creating server-administrator role...");
        role = rolesNode.addNode("server-administrator", "jnt:role");
        role.setProperty("j:hidden", false);
        role.setProperty("j:nodeTypes", ["rep:root"].toArray(new String[1]));
        def permissionNames = ["jcr:all_default", "admin"];
        role.setProperty("j:permissionNames", permissionNames.toArray(new String[permissionNames.size()]));
        role.setProperty("j:privilegedAccess", true);
        role.setProperty("j:roleGroup", "server-role");
        subNode = role.addNode("j:translation_en", "jnt:translation");
        subNode.setProperty("jcr:description", "Grant access to the server administration");
        subNode.setProperty("jcr:language", "en");
        subNode.setProperty("jcr:title", "Server administrator");
        log.info("...creation done.");

        jcrsession.save();
        return null;
    }
});


JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
        if (jcrsession.nodeExists("/permissions")) {
            log.info("Removing /permissions node on live.");
            jcrsession.getNode("/permissions").remove();
        }
        if (jcrsession.nodeExists("/roles")) {
            log.info("Removing /roles node on live.");
            jcrsession.getNode("/roles").remove();
        }
        jcrsession.save();
    }
});

JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
        NodeIterator ni = jcrsession.getWorkspace().getQueryManager().createQuery("select * from [jnt:virtualsite]", Query.JCR_SQL2).execute().getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = ni.next();
            if (next.hasNode("files/contributed")) {
                JCRNodeWrapper contributed = next.getNode("files/contributed");
                contributed.revokeAllRoles();
            }
        }
        jcrsession.save()
    }
});
