<%@include file="/admin/include/header.inc" %>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="org.jahia.data.templates.JahiaTemplatesPackage"%>
<%@page import="org.jahia.params.ParamBean,org.jahia.services.sites.JahiaSite" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    String warningMsg = (String) request.getAttribute("warningMsg");
    JahiaSite site = (JahiaSite) request.getAttribute("site");
    String siteTitle = (String) request.getAttribute("siteTitle");
    String siteServerName = (String) request.getAttribute("siteServerName");
    String siteDescr = (String) request.getAttribute("siteDescr");
    String siteKey = (String) request.getAttribute("siteKey");
    Boolean defaultSite = (Boolean) request.getAttribute("defaultSite");
    
    if (site.getInstalledModules() != null && !site.getInstalledModules().isEmpty()) {
        List<String> installedModules = new LinkedList<String>();
        Map<String, JahiaTemplatesPackage> templatePackageByNodeName = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByNodeName();
        for (String module : site.getInstalledModules()) {
            JahiaTemplatesPackage pkg = templatePackageByNodeName.get(module);
            if (pkg != null) {
                installedModules.add(pkg.getName());
            }
        }
        Collections.sort(installedModules);
        pageContext.setAttribute("installedModules", StringUtils.join(installedModules, ", "));
    }

    String gaUserAccountCustom = (String) request.getAttribute("gaUserAccountCustom");
    String gaProfileCustom = (String) request.getAttribute("gaProfileCustom");
    String gaUserAccountDefault = (String) request.getAttribute("gaUserAccountDefault");
    String gaProfileDefault = (String) request.getAttribute("gaProfileDefault");
    String gmailAccount = (String) request.getAttribute("gmailAccount");
    String gmailPassword = (String) request.getAttribute("gmailPassword");
    Boolean trackingEnabled = (Boolean) request.getAttribute("trackingEnabled");


    stretcherToOpen = 0;
//    Boolean versioningEnabled   = (Boolean)request.getAttribute("versioningEnabled");
//    Boolean stagingEnabled   = (Boolean)request.getAttribute("stagingEnabled");
    if (warningMsg != null) {
        if (warningMsg.trim().length() > 0) {
            jahiaDisplayMessage = warningMsg;
        }
    } %>
<script type="text/javascript">
    <!--

    function sendForm(){
        document.jahiaAdmin.versioningEnabled.disabled = false;
        document.jahiaAdmin.stagingEnabled.disabled = false;
        document.jahiaAdmin.submit();
    }

    -->
</script>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/></h2>
</div>
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
    <jsp:param name="mode" value="server"/>
</jsp:include>
<div id="content" class="fit">
<div class="head headtop">
    <div class="object-title"><fmt:message key="label.virtualSitesManagement"/>
    </div>
</div>
<div class="content-item">
<form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=processedit")%>'
      method="post">
<c:if test="${not empty warningMsg}">
    <p class="errorbold"><%=warningMsg%></p>
</c:if>
<input type="hidden" name="siteid" value="${siteID}">
<table border="0" cellpadding="5" cellspacing="0" style="width:100%">
<tr>
    <td>
        <fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/>*&nbsp;
    </td>
    <td valign="middle" align="left" class="text">
        :&nbsp;<b><%=siteKey %>
    </b>
    </td>
</tr>
<tr>
    <td>
        <fmt:message key="org.jahia.admin.site.ManageSites.siteTitle.label"/>*&nbsp;
        <br>
        <em><fmt:message key="org.jahia.admin.site.ManageSites.egSiteTitle.label"/></em>
    </td>
    <td>
        :&nbsp;<input class="input" type="text" name="siteTitle" value="<%=siteTitle%>" size="<%=inputSize%>"
                      maxlength="100">
    </td>
</tr>
<tr>
    <td>
        <fmt:message key="org.jahia.admin.site.ManageSites.siteServerName.label"/>*&nbsp;
        <br>
        <em><fmt:message key="org.jahia.admin.site.ManageSites.egSiteServerName.label"/></em>
    </td>
    <td>
        :&nbsp;<input class="input" type="text" name="siteServerName" value="<%=siteServerName%>" size="<%=inputSize%>"
                      maxlength="200">
    </td>
</tr>
</tr>
<tr>
    <td>
        <fmt:message key="org.jahia.admin.site.ManageSites.templateSet.label"/>
    </td>
    <td>
        :&nbsp;<%= request.getAttribute("siteTemplatePackageName") %>
    </td>
</tr>
<tr>
    <td>
        <fmt:message key="label.modules"/>
    </td>
    <td>
        :&nbsp;${not empty installedModules ? fn:escapeXml(installedModules) : ''}
    </td>
</tr>
<tr>
    <td>
        <label for="defaultSite">
            <fmt:message key="org.jahia.admin.site.ManageSites.setAsDefaultSite.label"/>&nbsp;
        </label>
    </td>
    <td>
        <% if (!defaultSite.booleanValue()) { %>
        :&nbsp;<input class="input" type="checkbox" name="defaultSite" id="defaultSite"/><% } else { %>
        :&nbsp;<fmt:message key="org.jahia.admin.site.ManageSites.isTheDefaultSite.label"/><input type="hidden"
                                                                                           name="defaultSite"
                                                                                           value="true"/><% } %>
    </td>
</tr>
<% /*
                    <!-- temporarily deactivated because this is not yet fully functional in the back-end -->
                    <!--
                    <tr>
                    <td>
                    <fmt:message key="org.jahia.admin.site.ManageSites.enableVersioning.label"/>&nbsp;
                    </td>
                    <td>
                    :&nbsp;
                    <input disabled class="input" type="checkbox" value="yes" name="versioningEnabled" if ( versioningEnabled.booleanValue() ) { out.print("checked=\"true\"") } >
                    </td>
                    </tr>
                    -->
                    */ %>
<tr>
    <td colspan="2" height="5">
        <input type="hidden" value="yes" name="versioningEnabled">
    </td>
</tr>
<% /*
                    <!-- temporarily deactivated because this is not yet fully functional in the back-end -->
                    <!--
                    <tr>
                    <td>
                    <fmt:message key="org.jahia.admin.site.ManageSites.enableStaging.label"/>&nbsp;
                    </td>
                    <td>
                    :&nbsp;
                    <input disabled class="input" type="checkbox" value="yes" name="stagingEnabled" if ( stagingEnabled.booleanValue() ) { out.print("checked=\"true\"") } >
                    </td>
                    </tr>
                    -->
                    */ %>
<tr>
    <td colspan="2" height="5">
        <input type="hidden" value="yes" name="stagingEnabled">
    </td>
</tr>
<tr>
    <td>
        <fmt:message key="org.jahia.admin.site.ManageSites.siteDesc.label"/>&nbsp;
    </td>
    <td>
        &nbsp;
        <textarea class="input" name="siteDescr" rows="2" cols='45'><%=siteDescr %>
        </textarea>
    </td>
</tr>
</table>
</form>
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
              <a class="ico-back" class="operationLink"
                 href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=list")%>'><fmt:message key="org.jahia.admin.site.ManageSites.backToSitesList.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-siteDelete"
                 href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=delete&siteid=" + request.getAttribute("siteID"))%>'><fmt:message key="label.delete"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="javascript:sendForm();"><fmt:message key="label.save"/></a>
            </span>
          </span>
</div>
</div>
<%@include file="/admin/include/footer.inc" %>