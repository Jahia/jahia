<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@page import="org.jahia.bin.*" %>
<%@include file="/admin/include/header.inc" %>
<%@page import = "org.jahia.services.usermanager.*" %>
<%
String  warningMsg		= (String)request.getAttribute("warningMsg");
String  siteTitle		= (String)request.getAttribute("siteTitle");
String  siteServerName	= (String)request.getAttribute("siteServerName");
String  siteDescr		= (String)request.getAttribute("siteDescr");
String  siteKey         = (String)request.getAttribute("siteKey");
List 	usrProviders 	= (List)request.getAttribute("usrProviders");
stretcherToOpen   = 0;
if ( usrProviders == null ){
usrProviders = new ArrayList();
} %>
<internal:gwtInit modules="org.jahia.ajax.gwt.module.engines.Engines"/>
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
                <div class="head">
                  <div class="object-title">
                    <fmt:message key="org.jahia.admin.site.ManageSites.doYouWantToContinue.label"/>
                  </div>
                </div>
                <form method="post" action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=processdelete&siteid=" + request.getParameter("siteid"))%>' name="mainForm">
                  <p class="errorbold">
                    <fmt:message key="org.jahia.admin.site.ManageSites.pleaseBeCareful.label"/>
                  </p>
                  <table border="0" cellpadding="10" cellspacing="0" style="width:100%" class="topAlignedTable">
                    <tr>
                      <td>
                        <fmt:message key="org.jahia.admin.site.ManageSites.siteTitle.label"/>&nbsp;
                      </td>
                      <td>
                        :&nbsp;<%=siteTitle %>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <fmt:message key="org.jahia.admin.site.ManageSites.siteServerName.label"/>&nbsp;
                      </td>
                      <td>
                        :&nbsp;<%=siteServerName %>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/>&nbsp;
                      </td>
                      <td>
                        :&nbsp;<%=siteKey %>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <fmt:message key="org.jahia.admin.site.ManageSites.siteDesc.label"/>&nbsp;
                      </td>
                      <td>
                        &nbsp;
                        <textarea class="input" name="siteDescr" rows="6" cols='45'><%=siteDescr %></textarea>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <fmt:message key="org.jahia.admin.site.ManageSites.purgeOptions.label"/>&nbsp;:
                      </td>
                      <td>
                        <table border="0" cellpadding="0" cellspacing="0">
                        <%-- 
                          <tr>
                            <td>
                              <input name="deleteTemplates" type="checkbox" value="1" checked>&nbsp;<fmt:message key="org.jahia.admin.site.ManageSites.deleteSiteTemplates.label"/>
                            </td>
                          </tr>
                          --%>
                          <tr>
                            <td>
                              <input name="deleteFileRepository" type="checkbox" value="1" checked>&nbsp;<fmt:message key="org.jahia.admin.site.ManageSites.deleteSiteFileRepository.label"/>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </form>
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
              <a class="ico-back" class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=list")%>'><fmt:message key="org.jahia.admin.site.ManageSites.backToSitesList.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-cancel" href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=edit&siteid=" + request.getParameter("siteid"))%>'><fmt:message key="org.jahia.admin.cancel.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-delete" href="#" onclick="javascript:{ showWorkInProgress(); document.mainForm.submit(); return false; }"><fmt:message key="org.jahia.admin.delete.label"/></a>
            </span>
          </span>
        </div>
      </div><%@include file="/admin/include/footer.inc" %>
