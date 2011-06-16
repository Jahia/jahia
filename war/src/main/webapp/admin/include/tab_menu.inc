<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page import="java.util.Locale,
					org.jahia.bin.Jahia,
                    org.jahia.params.ParamBean,
                    org.jahia.params.ProcessingContext,
                    org.jahia.services.sites.JahiaSite,
                    org.jahia.services.sites.JahiaSitesService,
                    org.jahia.services.usermanager.JahiaUser,
                    org.jahia.registries.ServicesRegistry,
                    org.jahia.utils.LanguageCodeConverters,
                    org.jahia.services.preferences.user.UserPreferencesHelper"
        %>
<%@ page import="org.apache.commons.lang.WordUtils" %>
<%@ page import="org.jahia.services.content.JCRSessionFactory" %>
<%@ page import="org.jahia.settings.SettingsBean" %>
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
    pageContext.setAttribute("processingContext", Jahia.getThreadParamBean());
    pageContext.setAttribute("theSite", theSite);
%>
<div class="dex-TabBar">
    <ul class="dex-TabBarItem-wrapper" style="width: 79%;">
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
            String siteName = theSite.getTitle().substring(0,theSite.getTitle().length() > 18?18:theSite.getTitle().length());
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
        <%
            try {
                if (JCRSessionFactory.getInstance().getCurrentUserSession().getNode("/templateSets").hasPermission("studioModeAccess")
                        && !SettingsBean.getInstance().isProductionMode() && !SettingsBean.getInstance().isDistantPublicationServerMode()
                        ) {
        %>
        <li class="dex-TabBarItem tabExit">
            <div>
                <div>
                    <span class="tab-icon ico-studio">
                        <c:url var="studioUrl" value="/cms/studio/default/${processingContext.UILocale}/templateSets/templates-system/home.html"/>
                        <a href="${studioUrl}"><fmt:message key="org.jahia.admin.exitToStudio.label"/></a>
                    </span>
                </div>
            </div>
        </li>
        <%
                }
            } catch (Exception e) {
                }
        %>

        <%
            if (theSite != null && !SettingsBean.getInstance().isDistantPublicationServerMode()) {
                String siteName = theSite.getTitle();

        %>

        <li class="dex-TabBarItem tabExit">
            <div>
                <div>
                    <c:url var="editUrl" value="/cms/edit/default/${theSite.defaultLanguage}${theSite.node.home.path}.html"/>
                    <span class="tab-icon ico-edit-mode"><a href="${editUrl}"
                                                       title="<fmt:message key='org.jahia.admin.exit.label'/>" ><fmt:message key="org.jahia.admin.exit.label"/></a></span>
                </div>
            </div>
        </li>
        <% } %>
        <li class="dex-TabBarItem tabExit">
            <div>
                <div>
                    <c:url var="editUrl" value="/start"/>
                    <span class="tab-icon ico-start"><a href="${editUrl}"
                                                       title="<fmt:message key='label.start'/>" ><fmt:message key='label.start'/></a></span>
                </div>
            </div>
        </li>
        <li class="dex-TabBarRest">
            <div>
                &nbsp;
            </div>
        </li>
    </ul>
    <ul class="dex-langbar-wrapper" style="width: 21%;">
        <li>
            <c:url value="/cms/logout" var="logoutUrl">
                <c:param name="redirect" value="${pageContext.request.contextPath}/administration"/>
            </c:url>
            <span style="margin-right: 10px"><a href="${logoutUrl}"><img src="${pageContext.request.contextPath}/icons/logout.png" alt=" " height="16" width="16" align="top"/>&nbsp;<fmt:message key="label.logout"/></a></span>
            <%
                Locale sessionLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_UI_LOCALE);
                String userLocale = sessionLocale != null ? UserPreferencesHelper.getPreferredLocale((JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER), sessionLocale).toString() : UserPreferencesHelper.getPreferredLocale((JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER), LanguageCodeConverters.resolveLocaleForGuest(request)).toString();
            %>
            <select name="preferredLanguage" onchange="window.location.href='${pageContext.request.contextPath}/administration?switchUiLocale=' + this.value;">
                <%
                    for (Locale theLocale : LanguageCodeConverters.getAvailableBundleLocalesSorted()) {%>
                <option value="<%=theLocale.toString() %>"
                        <% if (theLocale.toString().equals(userLocale)) { %>selected="selected"<% } %>><%= WordUtils.capitalizeFully(theLocale.getDisplayName(theLocale)) %>
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