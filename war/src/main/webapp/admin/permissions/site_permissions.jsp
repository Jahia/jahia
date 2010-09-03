<%@include file="/admin/include/header.inc" %>
<%@page import="org.jahia.admin.roles.ManageSiteRoles" %>
<%@page import="org.jahia.data.viewhelper.principal.PrincipalViewHelper" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.security.license.LicenseActionChecker" %>
<%@ page import="org.jahia.services.usermanager.JahiaGroup" %>
<%@ page import="org.jahia.services.usermanager.JahiaUser" %>
<%@ page import="org.jahia.utils.LanguageCodeConverters" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.jahia.services.sites.JahiaSite" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="label.siteroles"/>: <%=((JahiaSite) request.getSession().getAttribute(ProcessingContext.SESSION_SITE)).getTitle()%></h2>
</div>
<internal:gwtGenerateDictionary/>
<internal:gwtImport module="org.jahia.ajax.gwt.module.admin.Admin" />

<div id="main">
    <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
        <tbody>
        <tr>
            <td style="vertical-align: top;" align="left">
                <%@include file="/admin/include/tab_menu.inc" %>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top;" align="left" height="100%">
                <div class="dex-TabPanelBottom">
                    <div class="tabContent">
                        <jsp:include page="/admin/include/left_menu.jsp">
                            <jsp:param name="mode" value="site"/>
                        </jsp:include>
                        <div id="content" class="fit">
                            <div class="head headtop">
                                <div class="object-title">
                                    <fmt:message key="label.siteroles"/>
                                </div>
                            </div>
                            <div class="content-item">
                                <div class="dex-subTabBar">
                                    <div id="gwtpermissionrole" config="siterolesmanager" siteKey="<%=((JahiaSite) request.getSession().getAttribute(ProcessingContext.SESSION_SITE)).getSiteKey()%>"  class="jahia-admin-gxt"></div>
                                </div>

                            </div>
                        </div>
                    </div>
                </div>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div id="actionBar">
    <span class="dex-PushButton">
        <span class="first-child">
            <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
        </span>
    </span>
    <span class="dex-PushButton">
        <span class="first-child">
            <a class="ico-ok" href="javascript:saveContent();"><fmt:message key="org.jahia.admin.saveChanges.label"/></a>
        </span>
    </span>
</div>
</div>
<%@include file="/admin/include/footer.inc" %>
