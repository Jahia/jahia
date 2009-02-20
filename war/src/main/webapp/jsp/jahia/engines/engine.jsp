<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page language="java" contentType="text/html;charset=UTF-8"
%><?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page import="org.jahia.bin.Jahia" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.services.lock.LockKey" %>
<%@ page import="org.jahia.services.lock.LockPrerequisites" %>
<%@ page import="org.jahia.services.lock.LockPrerequisitesResult" %>
<%@ page import="org.jahia.services.lock.LockRegistry"%>
<%@ page import="org.jahia.services.usermanager.JahiaUser"%>
<%@ page import="java.util.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<jsp:useBean id="engineTitle" class="java.lang.String" scope="request"/>
<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>
<jsp:useBean id="javaScriptPath" class="java.lang.String" scope="request"/>
<%!
private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("jsp.jahia.engines.engine");
// include pages
private static final Properties includes = new Properties();

static {
  // shared jsp
  includes.setProperty("close",                               "/jsp/jahia/engines/shared/close.jsp");
  includes.setProperty("cancel",                              "/jsp/jahia/engines/shared/cancel.jsp");
  includes.setProperty("apply",                               "/jsp/jahia/engines/shared/apply.jsp");
  includes.setProperty("not_editable_field",                  "/jsp/jahia/engines/shared/not_editable_field.jsp");

  // login engine
  includes.setProperty("login",                               "/jsp/jahia/engines/login/login.jsp");
  includes.setProperty("bad_login",                           "/jsp/jahia/engines/login/login.jsp?badLogin=true");
  includes.setProperty("login_close",                         "/jsp/jahia/engines/login/close.jsp");

  // page properties engine
  includes.setProperty("page_properties",                     "/jsp/jahia/engines/pages/pageproperties.jsp");

  // update field engine
  includes.setProperty("update_field",                        "/jsp/jahia/engines/updatefield/updatefield.jsp");

  // add container engine
  includes.setProperty("add_container",                       "/jsp/jahia/engines/addcontainer/addcontainer.jsp");

  // update container engine
  includes.setProperty("update_container",                    "/jsp/jahia/engines/addcontainer/addcontainer.jsp");

  // delete container engine
  includes.setProperty("delete_container",                    "/jsp/jahia/engines/deletecontainer/deletecontainer.jsp");

  // restore container engine
  includes.setProperty("restore_container",                    "/jsp/jahia/engines/restorelivecontainer/restorecontainer.jsp");

  // container list properties
  includes.setProperty("container_list_properties",           "/jsp/jahia/engines/containerlistproperties/containerlistproperties.jsp");

  // templates engine
  includes.setProperty("manage_template",                     "/jsp/jahia/engines/template/template.jsp");

  // applications engine
  includes.setProperty("manage_application",                  "/jsp/jahia/engines/application/application.jsp");

  // application categories
  includes.setProperty("application_manage_categories",                  "/jsp/jahia/engines/application/application_manage_categories.jsp");

  // edit category engine
  includes.setProperty("edit_category",                       "/jsp/jahia/engines/categories/edit_category.jsp");

  // select categories engine
  includes.setProperty("select_categories",                   "/jsp/jahia/engines/categories/select_categories.jsp");
  includes.setProperty("select_categories_close",             "/jsp/jahia/engines/categories/close.jsp");

  // select page engine
  includes.setProperty("select_page",                         "/jsp/jahia/engines/selectpage/select_page.jsp");
  includes.setProperty("selectPage_close",                    "/jsp/jahia/engines/selectpage/close.jsp");

  // filemanager engine
  includes.setProperty("helpGed",                             "/jsp/jahia/engines/filemanager/helpGed.jsp");
  includes.setProperty("ajaxfilemanager",                     "/jsp/jahia/engines/filemanager/AjaxFileManager.jsp");
  includes.setProperty("filemanager_fileupload",              "/jsp/jahia/engines/filemanager/fileupload.jsp");
  includes.setProperty("filemanager_fileuploadconfirm",       "/jsp/jahia/engines/filemanager/fileuploadconfirm.jsp");
  includes.setProperty("filemanager_filemoveconfirm",         "/jsp/jahia/engines/filemanager/filemoveconfirm.jsp");
  includes.setProperty("filemanager_unzipconfirm",            "/jsp/jahia/engines/filemanager/unzipconfirm.jsp");
  includes.setProperty("filemanager_filedelete",              "/jsp/jahia/engines/filemanager/filedelete.jsp");
  includes.setProperty("filemanager_filerename",              "/jsp/jahia/engines/filemanager/filerename.jsp");
  includes.setProperty("filemanager_error",                   "/jsp/jahia/engines/filemanager/error.jsp");
  includes.setProperty("filemanager_close",                   "/jsp/jahia/engines/filemanager/close.jsp");
  includes.setProperty("filemanager_reloadpage",              "/jsp/jahia/engines/filemanager/reloadpage.jsp");
  includes.setProperty("filemanager_view",                    "/jsp/jahia/engines/filemanager/view.jsp");
  includes.setProperty("filemanager_fileedit",                "/jsp/jahia/engines/filemanager/fileedit.jsp");
  includes.setProperty("filemanager_createdir",               "/jsp/jahia/engines/filemanager/createdir.jsp");
  includes.setProperty("filemanager_migration",               "/jsp/jahia/engines/filemanager/migration.jsp");
  includes.setProperty("filemanager_info",                    "/jsp/jahia/engines/filemanager/info.jsp");
  includes.setProperty("filemanager_fileunlock",              "/jsp/jahia/engines/filemanager/fileunlock.jsp");
  includes.setProperty("filemanager_zip",                     "/jsp/jahia/engines/filemanager/zipfiles.jsp");

  includes.setProperty("selectUG",                            "/jsp/jahia/engines/users/selectUG.jsp");
  includes.setProperty("selectusers_close",                   "/jsp/jahia/engines/users/close.jsp");

  includes.setProperty("versioning",                          "/jsp/jahia/engines/versioning/versioning.jsp");

  // Workflow
  includes.setProperty("showReport",                          "/jsp/jahia/engines/workflow/showReport.jsp");

  includes.setProperty("lock",                                "/jsp/jahia/engines/lock/lock.jsp");

  // metadata engine
  includes.setProperty("metadata_engine",                     "/jsp/jahia/engines/addcontainer/addcontainer.jsp");

  includes.setProperty("CustomizeSaveSearchView",             "/jsp/jahia/engines/search/customizesavesearchview.jsp");
}
%>
<%
final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
final String engineUrl = (String) engineMap.get("engineUrl");
pageContext.setAttribute("engineUrl", engineUrl);
final String theScreen = (String) engineMap.get("screen");
final String copyright=Jahia.COPYRIGHT;

request.setAttribute("includes", includes);

final String includePage = includes.getProperty(jspSource);

final String engineName = (String) engineMap.get("engineName");
if (logger.isDebugEnabled()) {
    logger.debug("LastScreen: " + theScreen + ", JspSource: " + jspSource + ", EngineName: " + engineName);
    logger.debug("JSP: " + includePage);
}
final LockKey lockKey = (LockKey) engineMap.get("LockKey");
final LockPrerequisitesResult results = LockPrerequisites.getInstance().getLockPrerequisitesResult(lockKey);
EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
final Locale theLocale = jParams.getLocale();
if (elh != null) {
    jParams.setCurrentLocale(elh.getCurrentLocale());
}
%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%=jParams.getLocale()%>" lang="<%=jParams.getLocale()%>">
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
<meta name="robots" content="noindex,nofollow"/>
<title>Jahia Engine - <%=engineTitle%></title>
<internal:gwtInit/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/jsp/jahia/css/andromeda.css" type="text/css" />
<internal:JSTools />

<script type="text/javascript" src="<%=URL%>../javascript/serverdatetime.js"></script>
<script type="text/javascript" src="<%=URL%>../javascript/engines.js"></script>
<script type="text/javascript">
jahia.config = {
  contextPath: '${pageContext.request.contextPath}',
  i18n: {
      'org.jahia.button.ok':          '<internal:engineResourceBundle resourceName="org.jahia.button.ok"/>',
      'org.jahia.button.saveAddNew':  '<internal:engineResourceBundle resourceName="org.jahia.button.saveAddNew"/>',
      'org.jahia.button.apply':       '<internal:engineResourceBundle resourceName="org.jahia.button.apply"/>'
  },
  theScreen: '<%=theScreen%>',
  <% if (results != null) {%>lockResults: true,<% } %>
  lockKey: '<%=lockKey%>',
  <% if (engineMap.containsKey("lock")) { %>lockType: '<%=((LockKey)engineMap.get("lock")).getType()%>',<% } %>
  pid: <%=jParams.getPageID()%>,
  needToRefreshParentPage: <%=session.getAttribute("needToRefreshParentPage") != null%>,
  jspSource: '${jspSource}',
  sendKeepAliveTimeOut: <%= session.getMaxInactiveInterval() * 1000 / 2 %>
};
<% if (results != null) {%>
    window.onunload = null;
<%} else {%>
    window.onunload = closeTheWindow;
<%}%>
</script>
<% if (request.getAttribute("org.jahia.engines.html.headers") != null) { %>
  <%= (String) request.getAttribute("org.jahia.engines.html.headers") %>
<% } %>
</head>
<% if (jspSource == "login" || jspSource == "bad_login") { %>
<body class="login" id="bodyLogin">
<% } else { %>
<body>
<% } %>
<center>
<div id="userShell">
  <!-- wrapper (start) -->
  <div id='main<%if(jspSource != null && ("login".equals(jspSource) || "bad_login".equals(jspSource))){%>Login<%}else{%>Client<%}%>Layout'>
    <form name="mainForm" method="post" action="${fn:escapeXml(engineUrl)}">
      <% if (results != null) { %>
        <div id="readOnly" class="msg-alert-info">
          <internal:engineResourceBundle resourceName="org.jahia.engines.readOnlyMode.label"/>: <%=LockKey.getFriendlyMessage(lockKey, jParams)%>
          <%
          final List locksInfo = LockRegistry.getInstance().getInfo(lockKey);
          final Iterator iterator = locksInfo.iterator();
          if (iterator.hasNext()) {
            final Map lockInfo = (Map) iterator.next();
            final JahiaUser jahiaUser = (JahiaUser) lockInfo.get(LockRegistry.OWNER);
            final String lockID = (String) lockInfo.get(LockRegistry.ID);
            boolean isSameContext = jahiaUser != null && jParams.getUser().getUserKey().equals(jahiaUser.getUserKey()) && jParams.getSessionID().equals(lockID);
            if ((! LockKey.WAITING_FOR_APPROVAL_TYPE.equals(lockKey.getType())) && LockRegistry.getInstance().hasAdminRights(lockKey, jParams.getUser()) || isSameContext) {
            %>
              <span class="dex-PushButton">
          			<span class="first-child">
                		<a class="ico-remove-lock" href="javascript:sendFormSteal();" title="<internal:engineResourceBundle resourceName="org.jahia.engines.lock.stealLock.label"/>">
                  <internal:engineResourceBundle resourceName="org.jahia.engines.lock.stealLock.label"/></a>
             		</span>
              </span>
              <input type="hidden" name="whichKeyToSteal" value=""/>
            <% } %>
          <% } %>
        </div>
      <% } %>
      <div id="pagebody <%=(results != null ? "disabled" : "")%>">
        <input type="hidden" name="screen" value="save"/><!-- Default value in case of a form.submit() -->
        <input type="hidden" name="lastscreen" value="<%=theScreen%>"/>
        <!-- include page (start) -->
        <% if (includePage == null || includePage.length() == 0) { logger.warn("Include page is null"); %>
          <p class="errorbold">Include page is null. Check your properties in engine.jsp</p>
        <% } else { %>
          <jsp:include page="<%=includePage%>" flush="true"/>
        <% } %>
        <!-- include page (end) -->
      </div>
    </form>
    <div id="copyright">
      <%=copyright%> Jahia <%=Jahia.VERSION%>.<%=Jahia.getPatchNumber()%> r<%=Jahia.getBuildNumber()%>
    </div>
    <!-- wrapper (end) -->
  </div>
</div>
</center>
<c:if test="${empty requestScope['jahia.engines.gwtModuleIncluded']}">
    <internal:gwtImport module="org.jahia.ajax.gwt.module.engines.Engines"/>
    <internal:gwtGenerateDictionary/>
</c:if>
</body>
</html>