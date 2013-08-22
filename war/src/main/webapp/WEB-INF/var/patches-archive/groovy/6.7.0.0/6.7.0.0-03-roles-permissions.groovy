import org.apache.log4j.Logger
import org.jahia.services.content.*

import javax.jcr.PropertyType
import javax.jcr.RepositoryException
import javax.jcr.Value

final Logger log = Logger.getLogger("org.jahia.tools.groovyConsole");

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
        JCRNodeWrapper permissionsNode = jcrsession.getNode("/permissions");
        if (permissionsNode.hasNode("j:acl")) {
            permissionsNode.getNode("j:acl").remove();
        }

        if (!permissionsNode.hasNode("site-admin")) {
            permissionsNode.addNode("site-admin", "jnt:permission");
        }

        JCRNodeWrapper adminNode = permissionsNode.getNode("admin");
        if(adminNode.hasNode("adminGroups")) {
            jcrsession.move("/permissions/admin/adminGroups", "/permissions/site-admin/siteAdminGroups");
        }
        if(adminNode.hasNode("adminSiteLanguages")) {
            jcrsession.move("/permissions/admin/adminSiteLanguages", "/permissions/site-admin/siteAdminLanguages");
        }
        if(adminNode.hasNode("adminHtmlSettings")) {
            jcrsession.move("/permissions/admin/adminHtmlSettings", "/permissions/site-admin/siteAdminHtmlSettings");
        }
        if(adminNode.hasNode("adminUrlmapping")) {
            jcrsession.move("/permissions/admin/adminUrlmapping", "/permissions/site-admin/siteAdminUrlmapping");
        }
        if(adminNode.hasNode("adminSiteTemplates")) {
            jcrsession.move("/permissions/admin/adminSiteTemplates", "/permissions/site-admin/siteAdminTemplates");
        }
        if(adminNode.hasNode("adminWcagCompliance")) {
            jcrsession.move("/permissions/admin/adminWcagCompliance", "/permissions/site-admin/siteAdminWcagCompliance");
        }

        if (permissionsNode.hasNode("managers/componentManager")) {
            permissionsNode.getNode("managers/componentManager").remove();
        }

        if (!permissionsNode.hasNode("components")) {
            permissionsNode.addNode("components", "jnt:permission");
        }


        JCRNodeWrapper rolesNode = jcrsession.getNode("/roles");
        if (rolesNode.hasNode("j:acl")) {
            rolesNode.getNode("j:acl").remove();
        }

        JCRNodeWrapper role;
        JCRNodeWrapper subNode;

        def pathToRefValue = {jcrsession.getValueFactory().createValue(((JCRNodeWrapperImpl)jcrsession.getNode(it)).getRealNode(), true)};

        def setPermissions = {JCRNodeWrapperImpl node, List<String> permsToRemove, List<String> permsToAdd ->
            def values = [];
            for (JCRValueWrapper value : node.getProperty("j:permissions").getRealValues()) {
                String path = value.getNode().getPath()
                if (!permsToRemove.contains(path)) {
                    values.add(value);
                }
                permsToAdd.remove(path);
            }
            permsToAdd.each() {values.add(pathToRefValue(it))};
            node.setProperty("j:permissions", values.toArray(new Value[values.size()]), PropertyType.WEAKREFERENCE);
        }

        if (rolesNode.hasNode("contributor")) {
            role = rolesNode.getNode("contributor");
            role.setProperty("j:hidden", false);
            def permsToRemove = ["/permissions/contributeMode",
                    "/permissions/editMode/engineTabs/viewContentTab",
                    "/permissions/editMode/engineTabs/viewMetadataTab",
                    "/permissions/editMode/engineTabs/viewCategoriesTab",
                    "/permissions/editMode/engineTabs/viewTagsTab",
                    "/permissions/wysiwyg-editor-toolbar/view-basic-wysiwyg-editor"];
            def permsToAdd = ["/permissions/workflow-tasks/2-step-publication-finish-correction",
                    "/permissions/workflow-tasks/2-step-publication-start"];
            setPermissions(role, permsToRemove, permsToAdd);

            subNode = role.addNode("currentSite-access", "jnt:externalPermissions");
            subNode.setProperty("j:path", "currentSite");
            def values = [];
            ["/permissions/contributeMode",
                    "/permissions/managers/editorialContentManager",
                    "/permissions/managers/fileManager"].collect(values, pathToRefValue);
            subNode.setProperty("j:permissions", values.toArray(new Value[values.size()]), PropertyType.WEAKREFERENCE);
            subNode = role.addNode("j:translation_en", "jnt:translation");
            subNode.setProperty("jcr:description", "Can edit content using contribute mode");
            subNode.setProperty("jcr:language", "en");
            subNode.setProperty("jcr:title", "Contributor");
        }

        if (rolesNode.hasNode("editor")) {
            role = rolesNode.getNode("editor");
            role.setProperty("j:hidden", false);
            def permsToRemove = ["/permissions/editMode/editModeActions",
                    "/permissions/editMode/editModeAccess",
                    "/permissions/editMode/editSelector",
                    "/permissions/editMode/engineTabs/viewContentTab",
                    "/permissions/editMode/engineTabs/viewLayoutTab",
                    "/permissions/editMode/engineTabs/viewMetadataTab",
                    "/permissions/editMode/engineTabs/viewOptionsTab",
                    "/permissions/editMode/engineTabs/viewCategoriesTab",
                    "/permissions/editMode/engineTabs/viewRolesTab/viewLiveRolesTab",
                    "/permissions/editMode/engineTabs/viewTagsTab",
                    "/permissions/editMode/engineTabs/viewSeoTab",
                    "/permissions/editMode/engineTabs/viewVisibilityTab",
                    "/permissions/editMode/engineTabs/viewContributeModeTab",
                    "/permissions/contributeMode",
                    "/permissions/templates",
                    "/permissions/managers/fileManager",
                    "/permissions/managers/portletManager",
                    "/permissions/wysiwyg-editor-toolbar/view-full-wysiwyg-editor"];
            def permsToAdd = ["/permissions/workflow-tasks/2-step-publication-finish-correction",
                    "/permissions/workflow-tasks/2-step-publication-start"];
            setPermissions(role, permsToRemove, permsToAdd);

            subNode = role.addNode("currentSite-access", "jnt:externalPermissions");
            subNode.setProperty("j:path", "currentSite");
            def values = [];
            ["/permissions/editMode/editModeAccess",
                    "/permissions/editMode/editModeActions",
                    "/permissions/editMode/editSelector",
                    "/permissions/editMode/engineTabs/viewCategoriesTab",
                    "/permissions/editMode/engineTabs/viewContentTab",
                    "/permissions/editMode/engineTabs/viewContributeModeTab",
                    "/permissions/editMode/engineTabs/viewLayoutTab",
                    "/permissions/editMode/engineTabs/viewMetadataTab",
                    "/permissions/editMode/engineTabs/viewOptionsTab",
                    "/permissions/editMode/engineTabs/viewSeoTab",
                    "/permissions/editMode/engineTabs/viewTagsTab",
                    "/permissions/editMode/engineTabs/viewVisibilityTab",
                    "/permissions/managers",
                    "/permissions/wysiwyg-editor-toolbar/view-full-wysiwyg-editor"].collect(values, pathToRefValue);
            subNode.setProperty("j:permissions", values.toArray(new Value[values.size()]), PropertyType.WEAKREFERENCE);
            subNode = role.addNode("j:translation_en", "jnt:translation");
            subNode.setProperty("jcr:description", "Can edit content using edit mode");
            subNode.setProperty("jcr:language", "en");
            subNode.setProperty("jcr:title", "Editor");
        }

        if (rolesNode.hasNode("editor-in-chief")) {
            role = rolesNode.getNode("editor-in-chief");
            role.setProperty("j:hidden", false);
            def permsToRemove = ["/permissions/repository-permissions/jcr:all_default",
                    "/permissions/managers/categoryManager",
                    "/permissions/managers/editorialContentManager",
                    "/permissions/managers/fileManager",
                    "/permissions/managers/portletManager",
                    "/permissions/managers/siteManager",
                    "/permissions/managers/tagManager",
                    "/permissions/jobs",
                    "/permissions/editMode",
                    "/permissions/wysiwyg-editor-toolbar/view-basic-wysiwyg-editor",
                    "/permissions/templates"];
            setPermissions(role, permsToRemove, []);

            subNode = role.addNode("currentSite-access", "jnt:externalPermissions");
            subNode.setProperty("j:path", "currentSite");
            def values = [];
            ["/permissions/editMode"].collect(values, pathToRefValue);
            subNode.setProperty("j:permissions", values.toArray(new Value[values.size()]), PropertyType.WEAKREFERENCE);
            subNode = role.addNode("j:translation_en", "jnt:translation");
            subNode.setProperty("jcr:description", "View all engine tabs, is also allowed to grant and revoke roles on content");
            subNode.setProperty("jcr:language", "en");
            subNode.setProperty("jcr:title", "Editor in chief");

            jcrsession.move("/roles/editor-in-chief", "/roles/editor/editor-in-chief");
        }

        if (rolesNode.hasNode("web-designer")) {
            role = rolesNode.getNode("web-designer");
            role.setProperty("j:hidden", false);
            role.setProperty("j:nodeTypes", "rep:root");
            role.setProperty("j:permissions", Arrays.asList(pathToRefValue("/permissions/admin/adminTemplates")), PropertyType.WEAKREFERENCE);
            role.setProperty("j:roleGroup", "server-role");

            if (role.hasNode("studio-access")) {
                role.getNode("studio-access").remove();
            }

            if (role.hasNode("modules-management")) {
                subNode = role.getNode("modules-management");
                subNode.setProperty("j:path", "/modules");
                def permsToRemove = ["/permissions/editMode",
                        "/permissions/admin/adminTemplates"];
                setPermissions(subNode, permsToRemove, []);
                subNode.rename("modules-access");

            }
            subNode = role.addNode("j:translation_en", "jnt:translation");
            subNode.setProperty("jcr:description", "Gives full access to the studio");
            subNode.setProperty("jcr:language", "en");
            subNode.setProperty("jcr:title", "Web designer");
        }

        if (rolesNode.hasNode("site-administrator")) {
            role = rolesNode.getNode("site-administrator");
            role.setProperty("j:hidden", false);
            role.getProperty("j:nodeTypes").remove();
            def permsToRemove = ["/permissions/admin/administrationAccess",
                    "/permissions/site-admin/siteAdminLanguages",
                    "/permissions/site-admin/siteAdminUrlmapping",
                    "/permissions/site-admin/siteAdminHtmlSettings",
                    "/permissions/admin/adminDocumentation",
                    "/permissions/site-admin/siteAdminGroups",
                    "/permissions/admin/adminIssueTracking",
                    "/permissions/site-admin/siteAdminTemplates",
                    "/permissions/site-admin/siteAdminWcagCompliance"];
            def permsToAdd = ["/permissions/managers/remotePublicationManager",
                    "/permissions/managers/repositoryExplorer",
                    "/permissions/site-admin"];
            setPermissions(role, permsToRemove, permsToAdd);
            role.setProperty("j:roleGroup", "site-role");

            subNode = role.addNode("j:translation_en", "jnt:translation");
            subNode.setProperty("jcr:description", "");
            subNode.setProperty("jcr:language", "en");
            subNode.setProperty("jcr:title", "");
        }

        if (rolesNode.hasNode("server-administrator")) {
            role = rolesNode.getNode("server-administrator");
            role.setProperty("j:hidden", false);
            role.setProperty("j:nodeTypes", "rep:root");
            def permsToRemove = ["/permissions/admin/administrationAccess",
                    "/permissions/admin/adminVirtualSites",
                    "/permissions/admin/adminPortlets",
                    "/permissions/admin/adminEmailSettings",
                    "/permissions/admin/adminRootUser",
                    "/permissions/admin/adminCache",
                    "/permissions/admin/adminPasswordPolicy",
                    "/permissions/admin/adminIssueTracking",
                    "/permissions/admin/adminDocumentation",
                    "/permissions/admin/adminAbout",
                    "/permissions/admin/adminUsers",
                    "/permissions/admin/adminDBSettings",
                    "/permissions/admin/adminTemplates",
                    "/permissions/admin/adminSystemInfos",
                    "/permissions/admin/adminManageMemory"];
            def permsToAdd = ["/permissions/repository-permissions/jcr:all_default",
                    "/permissions/admin"];
            setPermissions(role, permsToRemove, permsToAdd);
            role.setProperty("j:privilegedAccess", true);
            role.setProperty("j:roleGroup", "server-role");

            if (role.hasNode("root-access")) {
                role.getNode("root-access").remove();
            }
            if (role.hasNode("permissions-access")) {
                role.getNode("permissions-access").remove();
            }
            if (role.hasNode("roles-access")) {
                role.getNode("roles-access").remove();
            }
            subNode = role.addNode("j:translation_en", "jnt:translation");
            subNode.setProperty("jcr:description", "Grant access to the server administration");
            subNode.setProperty("jcr:language", "en");
            subNode.setProperty("jcr:title", "Server administrator");
        }

        jcrsession.save();
        return null;
    }
});

