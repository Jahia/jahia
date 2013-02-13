<%@page contentType="text/html; charset=UTF-8" %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@page language = "java" %>
<%@page import = "org.jahia.bin.*" %>
<%@page import = "java.util.*" %>
<% List aclNameList = (List) request.getAttribute("aclNameList");
final Integer userNameWidth = new Integer(15);
request.getSession().setAttribute("userNameWidth", userNameWidth);
final String selectUsrGrp = (String) request.getAttribute("selectUsrGrp"); %>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import = "java.util.*"%>
<%@page import = "org.jahia.bin.JahiaAdministration"%>
<%@page import = "org.jahia.bin.Jahia"%>
<%@page import = "org.jahia.services.sites.JahiaSite"%>
<%@page import = "org.jahia.data.JahiaData"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<fmt:message key="org.jahia.copyright" var="i18nCopyright"/>
    <%

    String  jahiaDisplayMessage  = (String) request.getAttribute("jahiaDisplayMessage");
    String  URL = request.getContextPath() + "/engines/";
    JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");

    String  userAgent                        = request.getHeader("user-agent");
    int     inputSize                        = 38;
    boolean isLynx                           = false;

    if(userAgent != null) {
        if(userAgent.indexOf("MSIE") != -1) {
            inputSize = 36;
        } else if(userAgent.indexOf("Lynx") != -1) {
            isLynx    = true;
        }
    }

    JahiaSite currentSite = (JahiaSite)request.getAttribute("site");

    boolean isMSIE = request.getHeader("user-agent") != null && request.getHeader("user-agent").indexOf("MSIE") != -1;
    String copyright=Jahia.COPYRIGHT;
    String c = (String) pageContext.getAttribute("i18nCopyright");
    c = c != null && c.indexOf("???") == -1 ? c : "All rights reserved.";
    if (copyright.indexOf("All rights")!=-1){
        int p= copyright.indexOf("All rights");
     	  copyright=copyright.substring(0,p)+c;
    } else {
        copyright=copyright + "&nbsp;" +c;
    }

    final String contextPath = request.getContextPath();

    int stretcherToOpen = 1;
%>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=8"/>
    <title><fmt:message key="org.jahia.admin.jahiaAdministration.label"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <c:if test="${not empty sessionScope['org.jahia.usermanager.jahiauser'] && sessionScope['org.jahia.usermanager.jahiauser'].username != 'guest'}">
        <internal:gwtInit/>
    </c:if>
    <link rel="stylesheet" href="<c:url value='/css/admin-1.1.css'/>" type="text/css" />

    <c:if test="${not empty sessionScope['org.jahia.usermanager.jahiauser'] && sessionScope['org.jahia.usermanager.jahiauser'].username != 'guest'}">
        <script type="text/javascript">
            function showWorkInProgress() {
                if (typeof workInProgressOverlay != 'undefined') {
                    workInProgressOverlay.start();
                }
            }
        </script>
    </c:if>
</head>
<body class="jahiaAdministration" ${not empty jahiaAdministrationLogin ? 'id="bodyLogin"' : ''}>
<div id="mainAdminLayout">
<div id="AdminBar"></div>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%
stretcherToOpen   = 0; %>
<internal:gwtGenerateDictionary/>
<internal:gwtImport module="org.jahia.ajax.gwt.module.contentmanager.ContentManager" />
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><fmt:message key="label.serverroles"/></h2>
</div>
<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
<%--          <%@include file="/admin/include/tab_menu.inc" %>--%>
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
<%--
                <jsp:include page="/admin/include/left_menu.jsp">
                    <jsp:param name="mode" value="server"/>
                </jsp:include>
--%>
              <div id="content" class="fit">
                  <div class="head headtop">
                      <div class="object-title">
                          <fmt:message key="label.serverroles"/>
                      </div>
                  </div>
                  <div class="content-item-noborder">
                      <internal:contentManager embedded="true" conf="rolesmanager"/>
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
              <a  class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
            </span>
          </span>
          <%--
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="#ok" onclick="document.jahiaAdmin.submit(); return false;"><fmt:message key="org.jahia.admin.saveChanges.label"/></a>
            </span>
          </span>
          --%>
        </div>
      </div><%--<%@include file="/admin/include/footer.inc" %>--%>
