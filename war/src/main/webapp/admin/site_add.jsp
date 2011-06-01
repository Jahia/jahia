<%@page import="org.jahia.bin.JahiaAdministration"%>
<%@page import="org.jahia.params.ParamBean"%>
<%@include file="/admin/include/header.inc"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%

    /**
     * @version $Id$
     */

    final ParamBean jParams   = (ParamBean) request.getAttribute("org.jahia.params.ParamBean" );
    String warningMsg		= (String)request.getAttribute("warningMsg");
    JahiaSite newJahiaSite	= (JahiaSite)request.getAttribute("newJahiaSite");
    Boolean defaultSite     = (Boolean)request.getAttribute("defaultSite");
    Boolean newAdminOnly    = (Boolean)request.getAttribute("newAdminOnly");
    Integer nbSites		    = (Integer)request.getAttribute("nbSites");
    Integer siteLimit	    = (Integer)request.getAttribute("siteLimit");
    stretcherToOpen   = 0;
%>

<div id="topTitle">
    <h1>Jahia</h1>
    <h2 class="edit">
    	
    	<fmt:message key="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/></h2>
</div>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
       cellspacing="0">
<tbody>
<tr>
    <td style="vertical-align: top;" align="left">
        <%@include file="/admin/include/tab_menu.inc"%>
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
<div class="head">
    <div class="object-title">
        <fmt:message key="org.jahia.admin.site.ManageSites.siteProperties.label"/>
    </div>
    <div  class="object-shared">
        <fmt:message key="label.step"/> 1 / 3
    </div>
</div>
<% if ( ( newJahiaSite != null ) && ( (siteLimit.intValue()==-1) || (nbSites.intValue()<siteLimit.intValue()) ) ) { %>

<form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=processadd")%>' method="post">
    <c:if test="${not empty warningMsg}">
        <p class="errorbold">
            <%=warningMsg%>
        </p>
    </c:if>

    <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
        <tr>
            <td>
                <fmt:message key="org.jahia.admin.site.ManageSites.siteTitle.label"/>*&nbsp;
                <br><em><fmt:message key="org.jahia.admin.site.ManageSites.egSiteTitle.label"/></em>
            </td>
            <td>
                :&nbsp;<input class="input" type="text" name="siteTitle" value="<%=newJahiaSite.getTitle()%>" size="<%=inputSize%>" maxlength="100">
            </td>
        </tr>
        <tr>
            <td>
                <fmt:message key="org.jahia.admin.site.ManageSites.siteServerName.label"/>*&nbsp;
                <br><em><fmt:message key="org.jahia.admin.site.ManageSites.egSiteServerName.label"/></em>
            </td>
            <td>
                :&nbsp;<input class="input" type="text" name="siteServerName" value="<%=newJahiaSite.getServerName()%>" size="<%=inputSize%>" maxlength="200">
            </td>
        </tr>
        <tr>
            <td>
                <fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/>*&nbsp;
                <br><em><fmt:message key="org.jahia.admin.site.ManageSites.egSiteKey.label"/></em>
            </td>
            <td>
                :&nbsp;<input class="input" type="text" name="siteKey" value="<%=newJahiaSite.getSiteKey()%>" size="<%=inputSize%>" maxlength="50">
                <br><em>&nbsp;&nbsp;<fmt:message key="org.jahia.admin.site.ManageSites.siteKeyAppears.label"/></em>

            </td>
        </tr>
        <tr>
            <td>
                <fmt:message key="org.jahia.admin.site.ManageSites.siteDesc.label"/>&nbsp;
            </td>
            <td>
                &nbsp;<textarea class="input" name="siteDescr" rows="2" cols='45'><%=newJahiaSite.getDescr()%></textarea>
            </td>
        </tr>
        <tr>
            <td valign="top">
                <label for="setAsDefaultVirtualSite"><fmt:message key="org.jahia.admin.site.ManageSites.setAsDefaultVirtualSite.label"/></label>&nbsp;
            </td>
            <td valign="top">
                <input type="checkbox" name="defaultSite" <% if (defaultSite.booleanValue()) { %>checked<% } %> id="setAsDefaultVirtualSite" />
            </td>
        </tr>
    </table>
    <div class="head headtop">
        <div class="object-title">
            <fmt:message key="org.jahia.admin.site.ManageSites.administratorAccount.label"/>
        </div>
    </div>
    <table border="0">
        <tr>
            <td><input type="radio" name="siteAdmin" value="2" checked="checked" id="noSiteAdmin"/></td>
            <td><label for="noSiteAdmin"><fmt:message key="org.jahia.admin.site.ManageSites.noSiteAdmin.label"/></label></td>
        </tr>
        <tr>
            <td><input type="radio" name="siteAdmin" value="0" id="createNewSiteAdmin"/></td>
            <td><label for="createNewSiteAdmin"><fmt:message key="org.jahia.admin.site.ManageSites.createNewUser.label"/></label></td>
        </tr>
        <% if ( false && !newAdminOnly.booleanValue() ){
        %>
        <tr>
            <td><input type="radio" name="siteAdmin" value="1" id="useExistingSiteAdmin"/></td>
            <td><label for="useExistingSiteAdmin"><fmt:message key="org.jahia.admin.site.ManageSites.selectAdministrator.label"/></label></td>
        </tr>
        <% } %>
    </table>
</form>
<% } else { %>
<p class="errorbold">
    <fmt:message key="org.jahia.admin.licenseLimitation.label"/>
</p>
<p class="error">
    <fmt:message key="org.jahia.admin.site.ManageSites.numberSiteForLicense.label"/>
    <%=siteLimit.intValue()%>
</p>
<% } %>


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
      	 <a class="ico-back" class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=list")%>' onclick="showWorkInProgress(); return true;"><fmt:message key="org.jahia.admin.site.ManageSites.backToSitesList.label"/></a>
      </span>
     </span>
  	 <span class="dex-PushButton">
      <span class="first-child">
         <a class="ico-next" href="javascript:document.jahiaAdmin.submit();" onclick="showWorkInProgress(); return true;">
             <fmt:message key="label.nextStep"/>
         </a>
      </span>
     </span>
</div>

</div>

<%@include file="/admin/include/footer.inc"%>