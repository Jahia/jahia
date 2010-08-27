<%@page import="java.util.Locale,
                    org.jahia.params.ParamBean,
                    org.jahia.params.ProcessingContext,
                    org.jahia.services.sites.JahiaSite,
                    org.jahia.services.sites.JahiaSitesService,
                    org.jahia.services.usermanager.JahiaUser,
                    org.jahia.registries.ServicesRegistry,
                    org.jahia.utils.LanguageCodeConverters,
                    org.jahia.services.preferences.user.UserPreferencesHelper" 
%>
<%
    JahiaSitesService siteService = ServicesRegistry.getInstance().getJahiaSitesService();
    JahiaSite theSite = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);
    if (theSite != null && theSite.getID() == -1) {
        theSite = null;
    }
    Integer siteID = null;
    if (theSite == null) {
        siteID = jData.getProcessingContext().getSiteID();
        if (siteID != null && siteID.intValue() > 0) {
            theSite = siteService.getSite(siteID.intValue());
        }
    }
        
    if (theSite == null && siteService.getNbSites() > 0) {
        theSite = siteService.getDefaultSite();
    }
    siteID = theSite != null ? theSite.getID() : null;

    //Boolean configJahia = (Boolean) request.getAttribute("configJahia");
    Integer daysLeft = (Integer) request.getAttribute("daysLeft");
%>
<div class="dex-TabBar">
    <ul class="dex-TabBarItem-wrapper" style="width: 80%;">
        <li class="dex-TabBarFirst">
            <div>
                &nbsp;
            </div>
        </li>
        <li class="dex-TabBarItem<% if (stretcherToOpen == 0) { %>-selected<% } %>">
            <div>
                <div>
          <span class="tab-icon ico-server">
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","&sub=server")%>"><fmt:message key="label.serverSettings"/></a>
          </span>
                </div>
            </div>
        </li>
        <% if (theSite != null) {
            String siteName = theSite.getTitle().substring(0,theSite.getTitle().length() > 25?25:theSite.getTitle().length());
        %>
        <li class="dex-TabBarItem<% if (stretcherToOpen == 1) { %>-selected<% } %>">
            <div>
                <div>
          <span class="tab-icon ico-site">
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","&sub=site")%>"><fmt:message key="org.jahia.admin.siteSettings.label"/>: <span id="siteName"><%=siteName%></span></a>
          </span>
                </div>
            </div>
        </li>
        <%  }
        %>
        <li class="dex-TabBarItem tabExit">
            <div>
                <div>
          <span class="tab-icon ico-studio">
            <a href="<c:url value='/cms/studio/default/en/templateSets/templates-intranet/home.html'/>"><fmt:message key="org.jahia.admin.exitToStudio.label"/></a>
          </span>
                </div>
            </div>
        </li>


        <%
            if (theSite != null) {
            String siteName = theSite.getTitle();;
        %>

        <li class="dex-TabBarItem tabExit">
            <div>
                <div>
                    <span class="tab-icon ico-quit"><a href='<%=Jahia.getContextPath() + "/cms/edit/default/" + Jahia.getThreadParamBean().getLocale() + "/sites/" + theSite.getSiteKey() + "/home.html" %>'
                       title="<fmt:message key='org.jahia.admin.exit.label'/>" ><fmt:message key="org.jahia.admin.exit.label"/></a></span>
                </div>
            </div>
        </li>
        <% } %>
        <li class="dex-TabBarRest">
            <div>
                &nbsp;
            </div>
        </li>
    </ul>
    <ul class="dex-langbar-wrapper" style="width: 20%;">
        <li>
            <c:url value="/cms/logout" var="logoutUrl">
                <c:param name="redirect" value="${pageContext.request.contextPath}/administration"/>
            </c:url>
            <span style="margin-right: 10px"><a href="${logoutUrl}"><img src="${pageContext.request.contextPath}/css/images/andromeda/logout.png" alt=" " height="16" width="16" align="top"/>&nbsp;<fmt:message key="label.logout"/></a></span>
            <%
            String userLocale = UserPreferencesHelper.getPreferredLocale((JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER)).toString();
            %>
            <script type="text/javascript">
                function doSwitchLanguage(lang) {
                    var url = window.location.href;
                    if (url.indexOf('switchUiLocale=') != -1) {
                        url = url.replace(/switchUiLocale=..(_..)?/, 'switchUiLocale=' + lang);
                    } else {
                        url = url + (url.indexOf('?') == -1 ? '?' : '&') + 'switchUiLocale=' + lang;
                    }
                    window.location.href = url;
                }
            </script>
            <select name="preferredLanguage" onchange="doSwitchLanguage(this.value)">
            <%
                for (Locale theLocale : LanguageCodeConverters.getAvailableBundleLocalesSorted()) {%>
            <option value="<%=theLocale.toString() %>"
                    <% if (theLocale.toString().equals(userLocale)) { %>selected="selected"<% } %>><%= theLocale.getDisplayName(theLocale) %>
            </option>
            <% } %>
            </select>
        </li>
    </ul>
    <%--
    <!-- License expiration message -->
    <ul class="dex-license-wrapper" style="width: 10%">
        <li>
            <% if (daysLeft != null) { %>
            <fmt:message key="org.jahia.admin.daysLeftWarning.label"/>:&nbsp;<%=daysLeft %>
            <% } else { %>
            &nbsp;
            <% } %>
        </li>
    </ul>
    --%>
</div>
