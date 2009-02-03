<%
    // server menu items 
    List serverMenuItems = new LinkedList();
    List row = new LinkedList();
    row.add(
            new MenuItem(
                    "sites", 
                    isServerActionAllowed("admin.sites.ManageSites", user, currentSiteID),
                    "org.jahia.admin.virtualSitesManagement.label",
                    JahiaAdministration.composeActionURL(request,response,"sites","&sub=list"),
                    "environment"
                    ));
    row.add(
            new MenuItem(
                    "categories", 
                    isServerActionAllowed("admin.categories.ManageCategories", user, currentSiteID),
                    "org.jahia.admin.manageCategories.label",
                    JahiaAdministration.composeActionURL(request,response,"categories","&sub=display"),
                    "branch"
                    ));
    row.add(
            new MenuItem(
                    "server", 
                    isServerActionAllowed("admin.server.ManageServer", user, currentSiteID),
                    "org.jahia.admin.emailSettings.label",
                    JahiaAdministration.composeActionURL(request,response,"server","&sub=display"),
                    "mail"
                    ));

    serverMenuItems.add(row);
    row = new LinkedList();
    
    row.add(
            new MenuItem(
                    "components", 
                    isServerActionAllowed("admin.components.ManageShareComponents", user, currentSiteID),
                    "org.jahia.admin.manageComponents.label",
                    JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=display"),
                    "application"
                    ));
    row.add(
            new MenuItem(
                    "logs", 
                    isServerActionAllowed("admin.audit.ManageLogs", user, currentSiteID),
                    "org.jahia.admin.administrativeAuditLog.label",
                    JahiaAdministration.composeActionURL(request,response,"logs","&sub=settings"),
                    "console_network"
                    ));
    row.add(
            new MenuItem(
                    "profile", 
                    isServerActionAllowed("admin.users.ManageSuperAdminProps", user, currentSiteID),
                    "org.jahia.admin.editSuperAdminProperties.label",
                    JahiaAdministration.composeActionURL(request,response,"users","&sub=superAdminProps"),
                    "businessman2"
                    ));

    serverMenuItems.add(row);
    row = new LinkedList();

    row.add(
            new MenuItem(
                    "stats", 
                    isServerActionAllowed("admin.status.ManageStatus", user, currentSiteID),
                    "org.jahia.admin.serverStatus.label",
                    JahiaAdministration.composeActionURL(request,response,"status","&sub=display"),
                    "line-chart"
                    ));
    row.add(
            new MenuItem(
                    "serverPermissions", 
                    isServerActionAllowed("admin.permissions.ManageServerPermissions", user, currentSiteID),
                    "org.jahia.admin.serverpermissions.mainMenu.label",
                    JahiaAdministration.composeActionURL(request,response,"serverPermissions","&sub=display"),
                    "policeman_usa"
                    ));
    row.add(
            new MenuItem(
                    "passwordPolicies",
                    isServerActionAllowed("admin.pwdpolicy.ManagePasswordPolicies", user, currentSiteID),
                    "org.jahia.admin.passwordPolicies.mainMenu.label",
                    JahiaAdministration.composeActionURL(request,response,"passwordPolicies","&sub=display"),
                    "key1"
                    ));
    serverMenuItems.add(row);
    row = new LinkedList();


    row.add(
            new MenuItem(
                    "patches", 
                    isServerActionAllowed("admin.server.ViewPatches", user, currentSiteID),
                    "org.jahia.admin.patchmanagement.mainMenu.label",
                    JahiaAdministration.composeActionURL(request,response,"patches",""),
                    "package_view"
                    ));
    row.add(
            new MenuItem(
                    "info", 
                    isServerActionAllowed("admin.info.LicenseInfo", user, currentSiteID),
                    "org.jahia.admin.aboutJahia.label",
                    JahiaAdministration.composeActionURL(request,response,"info",""),
                    "about"
                    ));

    serverMenuItems.add(row);

    pageContext.setAttribute("serverMenuItems", serverMenuItems);
%>
<div class="tabContent">
<div id="content" class="full">
<table border="0" cellspacing="0" cellpadding="10" class="adminmenu">
<c:forEach items="${serverMenuItems}" var="row">
<tr>
    <c:forEach items="${row}" var="item">
        <td>
        <c:if test="${not empty item}"> 
            <c:if test="${item.enabled}">
                    <span class="dex-PushButton-big">
                        <span class="first-child">
                            <a href="${item.link}"><img
                                    name="${item.name}" src="<%=URL%>images/icons/admin/adromeda/${item.icon}.png" width="32"
                                    height="32" border="0"><span><internal:adminResourceBundle
                                    resourceName="${item.label}"/></span></a>
                        </span>
                    </span>
            </c:if>
            <c:if test="${not item.enabled}">
                    <span class="dex-PushButton-big disabled">
                        <span class="first-child" style="cursor: default">
                            <a href="#${item.name}" onclick="return false;" style="cursor: default;"><img name="${item.name}" src="<%=URL%>images/icons/admin/adromeda/${item.icon}_grey.png" width="32"
                                             height="32" border="0"><span><internal:adminResourceBundle
                                    resourceName="${item.label}"/></a></span>
                        </span>
                    </span>
            </c:if>
        </c:if>
        <c:if test="${empty item}">&nbsp;</c:if>
        </td>
    </c:forEach>
</tr>
</c:forEach>
</table>
</div>
</div>

