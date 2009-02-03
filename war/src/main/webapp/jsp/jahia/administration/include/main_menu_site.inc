    <% if ((siteID.intValue() > 0) && (currentSite != null)) { 

        // site menu items 
        List siteMenuItems = new LinkedList();
        List row = new LinkedList();
        row.add(
                new MenuItem(
                        "pages", 
                        isSiteActionAllowed("admin.pages.ManagePages", user, currentSiteID),
                        "org.jahia.admin.pageSettings.label",
                        JahiaAdministration.composeActionURL(request,response,"pages","&sub=display"),
                        "server_document"
                        ));
        row.add(
                new MenuItem(
                        "users", 
                        isSiteActionAllowed("admin.users.ManageUsers", user, currentSiteID),
                        "org.jahia.admin.manageUsers.label",
                        JahiaAdministration.composeActionURL(request,response,"users","&sub=display"),
                        "user1_into"
                        ));
        row.add(
                new MenuItem(
                        "groups", 
                        isSiteActionAllowed("admin.users.ManageGroups", user, currentSiteID),
                        "org.jahia.admin.manageGroups.label",
                        JahiaAdministration.composeActionURL(request,response,"groups","&sub=display"),
                        "users_into"
                        ));

        siteMenuItems.add(row);
        row = new LinkedList();
        row.add(
                new MenuItem(
                        "templates", 
                        isSiteActionAllowed("admin.templates.ManageTemplates", user, currentSiteID),
                        "org.jahia.admin.manageTemplates.label",
                        JahiaAdministration.composeActionURL(request,response,"templates","&sub=display"),
                        "briefcase_document"
                        ));
        row.add(
                new MenuItem(
                        "search", 
                        isSiteActionAllowed("admin.search.ManageSearch", user, currentSiteID),
                        "org.jahia.admin.manageSearchEngine.label",
                        JahiaAdministration.composeActionURL(request,response,"search","&sub=display"),
                        "find"
                        ));
        row.add(
                new MenuItem(
                        "languages", 
                        isSiteActionAllowed("admin.languages.ManageSiteLanguages", user, currentSiteID),
                        "org.jahia.admin.manageLanguages.label",
                        JahiaAdministration.composeActionURL(request,response,"siteLanguages","&sub=display"),
                        "signpost"
                        ));

        siteMenuItems.add(row);
        
        row = new LinkedList();
        row.add(
                new MenuItem(
                        "export", 
                        isSiteActionAllowed("admin.production.ManageSiteProduction", user, currentSiteID),
                        "org.jahia.admin.productionManager.label",
                        JahiaAdministration.composeActionURL(request,response,"productionManager","&sub=display"),
                        "transform"
                        ));
        row.add(
                new MenuItem(
                        "sitePermissions", 
                        isSiteActionAllowed("admin.permissions.ManageSitePermissions", user, currentSiteID),
                        "org.jahia.admin.sitepermissions.mainMenu.label",
                        JahiaAdministration.composeActionURL(request,response,"sitePermissions","&sub=display"),
                        "security_agent"
                        ));
        row.add(
                new MenuItem(
                        "htmlsettings", 
                        isSiteActionAllowed("admin.htmleditors.HtmlEditorsAdmin", user, currentSiteID),
                        "org.jahia.admin.htmlsettings.label",
                        jParams.composeStrutsUrl("HtmlSettings","&group=htmlsettings&method=display"),
                        "text_rich_colored"
                        ));

        siteMenuItems.add(row);
        row = new LinkedList();
        row.add(
                new MenuItem(
                        "integrity", 
                        isSiteActionAllowed("admin.htmleditors.HtmlEditorsAdmin", user, currentSiteID),
                        "org.jahia.admin.integrity.label",
                        jParams.composeStrutsUrl("IntegrityChecks","&group=integrity&method=display"),
                        "text_ok"
                        ));

        row.add(new MenuItem(
                        "analytics",
                        isSiteActionAllowed("admin.analytics.ManageAnalytics", user, currentSiteID),
                        "org.jahia.admin.manageanalytics.label",
                        JahiaAdministration.composeActionURL(request,response,"analytics","&sub=display"),
                        "column-chart"
                        ));
        row.add(new MenuItem(
                        "themes",
                        isSiteActionAllowed("admin.themes.ManageThemes", user, currentSiteID),
                        "org.jahia.admin.themes.ManageThemes.label",
                        JahiaAdministration.composeActionURL(request,response,"themes","&sub=display"),
                        "window_earth"
                        ));
        siteMenuItems.add(row);

        pageContext.setAttribute("siteMenuItems", siteMenuItems);
%>
<div class="tabContent">
<div id="content" class="full">
<div id="changeSite">
    <% if (sitesList.size() > 1) { %>
    <script language="javascript">
        function changeSiteNow() {
        <%
           int countJS = 0;
           while(sitesJavaScript.hasNext()) {
               JahiaSite siteJS = (JahiaSite) sitesJavaScript.next();
               String siteChangeURL = JahiaAdministration.composeActionURL(request,response,"change","&changesite=" + siteJS.getID() + "#sites");
        %>
            if (document.jahiaAdmin.changesite.options[<%=countJS%>].selected) location.href = "<%=siteChangeURL%>";
        <%
               countJS++;
           }
        %>
        }
    </script>
    <form name="jahiaAdmin">
        <nobr><b><internal:adminResourceBundle resourceName="org.jahia.admin.selectWebsite.label"/>&nbsp;:&nbsp;&nbsp;&nbsp;</b>
            <select name="changesite" onChange="changeSiteNow();">
                <% while (sitesEnum.hasNext()) {
                    JahiaSite site = (JahiaSite) sitesEnum.next(); %>
                <option value="<%=site.getID()%>" <%if (siteID.intValue() == site.getID()) {%> selected <%}%>>
                    &nbsp;<%=site.getTitle()%>&nbsp;</option>
                <% }%>
            </select></nobr>
    </form>
    <% } %>
</div>

<table border="0" cellspacing="0" cellpadding="10" class="adminmenu">
<c:forEach items="${siteMenuItems}" var="row">
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
                                    resourceName="${item.label}" defaultValue="${item.label}"/></span></a>
                        </span>
                    </span>
            </c:if>
            <c:if test="${not item.enabled}">
                    <span class="dex-PushButton-big disabled">
                        <span class="first-child" style="cursor: default">
                            <a href="#${item.name}" onclick="return false;" style="cursor: default"><img name="${item.name}" src="<%=URL%>images/icons/admin/adromeda/${item.icon}_grey.png" width="32"
                                             height="32" border="0"><span><internal:adminResourceBundle
                                    resourceName="${item.label}" defaultValue="${item.label}"/></span></a>
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
<% } %>