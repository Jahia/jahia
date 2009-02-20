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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page import="org.jahia.bin.Jahia" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.services.lock.LockKey" %>
<%@ page import="org.jahia.services.lock.LockPrerequisites" %>
<%@ page import="org.jahia.services.lock.LockPrerequisitesResult" %>
<%@ page import="org.jahia.services.usermanager.JahiaUser" %>
<%@ page import="org.jahia.views.engines.JahiaEngineCommonData" %>
<%@ page import="org.jahia.views.engines.JahiaEngineCommonDataInterface" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="org.jahia.services.lock.LockRegistry" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ page import="org.jahia.engines.JahiaEngine"%>

<%@ include file="/views/engines/common/taglibs.jsp" %>

<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>
<jsp:useBean id="javaScriptPath" class="java.lang.String" scope="request"/>
<%
    JahiaEngineCommonDataInterface jahiaEngineCommonData =
            (JahiaEngineCommonDataInterface) request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
    if (jahiaEngineCommonData == null) {
        // Minimal engine common data, engineURL and engine title are not set
        // to avoid null pointer exception
        jahiaEngineCommonData = new JahiaEngineCommonData(request);
        request.setAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA, jahiaEngineCommonData);
    }

    final ProcessingContext jParams = jahiaEngineCommonData.getParamBean();
    final String engineTitle = jahiaEngineCommonData.getEngineTitle();
    Map engineMap = (Map) request.getAttribute("jahia_session_engineMap");
    if (engineMap == null) {
        engineMap = (Map) session.getAttribute("jahia_session_engineMap");
    }
    if (engineMap == null) {
        // a fake engineMap
        engineMap = new HashMap();
        engineMap.put("engineUrl", "");
        engineMap.put("screen", "");
        engineMap.put("engineView", "");
        session.setAttribute("jahia_session_engineMap", engineMap);
        request.setAttribute("jahia_session_engineMap", engineMap);
    }
    request.setAttribute("org.jahia.engines.EngineHashMap", engineMap);
    final String engineUrl = (String) engineMap.get("engineUrl");
    final String theScreen = (String) engineMap.get("screen");
    final String engineView = (String) request.getAttribute("engineView");

    final boolean showEditMenu = (theScreen.equals("edit") || theScreen.equals("metadata") ||
            theScreen.equals("rightsMgmt") || theScreen.equals("timeBasedPublishing") ||
            theScreen.equals("ctneditview_rights"));
    request.setAttribute("showEditMenu", Boolean.valueOf(showEditMenu));

    final LockKey lockKey = (LockKey) engineMap.get("LockKey");
    final LockPrerequisitesResult results = LockPrerequisites.getInstance().getLockPrerequisitesResult(lockKey);
    EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%=jParams.getLocale()%>" lang="<%=jParams.getLocale()%>">
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
<title>Jahia Engine - <%=engineTitle%></title>
<internal:gwtInit modules="org.jahia.ajax.gwt.module.engines.Engines"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/jsp/jahia/css/andromeda.css" type="text/css" />
<%--
<link rel="stylesheet" href="${pageContext.request.contextPath}/jsp/jahia/engines/css/menu.css" type="text/css"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/jsp/jahia/css/colorsAndStyles.css" type="text/css"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/jsp/jahia/css/actions.css" type="text/css"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.stylesheet.css"/>" type="text/css"/>
--%>


<%--script type="text/javascript" src="<%=URL%>../javascript/json/json.js"></script--%>
<internal:JSTools/>

<script type="text/javascript" src="<%=URL%>../javascript/serverdatetime.js"></script>
<% if (request.getAttribute("org.jahia.engines.html.headers") != null) { %>
<%= (String) request.getAttribute("org.jahia.engines.html.headers") %>
<% } %>
<script type="text/javascript">
<!--
function flagSubmit() {
    if (submittedCount == 0) {
        submittedCount++;
    }
}

function check() {
    // override this function if needed in subengine to perform form data check
    // before submit !!!
    return true;
}

function saveContent() {
    // used by Html editors
    // override this for preprocessing before for submission
    if (typeof workInProgressOverlay != 'undefined') workInProgressOverlay.launch();
}
//-->
</script>

<script type="text/javascript" src="<%= request.getContextPath() %>/jsp/jahia/javascript/prototype/proto15sc17-compressed.js"></script>
<script type="text/javascript" src="<%=URL%>../javascript/engines.js"></script>
<script type="text/javascript">
<!--
jahia.config = {
    contextPath: '<%=request.getContextPath()%>',
    sendKeepAliveTimeOut: <%= session.getMaxInactiveInterval() * 1000 / 2 %>
};
var submittedCount = 0;

function teleportCaptainFlam(what) {
    workInProgressOverlay.launch();
    if (submittedCount == 0) {
        submittedCount++;
        document.mainForm.submit();
    }
}

function handleLanguageChange(lang) {
    document.mainForm.screen.value = "<%=theScreen%>";
    document.mainForm.engine_lang.value = lang;
    if (check()) {
        saveContent();
        teleportCaptainFlam(document.mainForm);
    }
}

function handleActionChange(what) {
    saveContent();
    document.mainForm.screen.value = what;
    teleportCaptainFlam(document.mainForm);
}

function sendFormSave() {
    if (check()) {
        document.mainForm.screen.value = "save";
        saveContent();
        teleportCaptainFlam(document.mainForm);
    }
}

function sendFormSaveAndAddNew() {
    document.mainForm.screen.value = "save";
    document.mainForm.addnew.value = "true";
    if (check()) {
        saveContent();
        teleportCaptainFlam(document.mainForm);
    }
}

function sendFormApply() {
    if (check()) {
        document.mainForm.screen.value = "apply";
        saveContent();
        teleportCaptainFlam(document.mainForm);
    }
}

function sendFormSteal() {
    if (check() && submittedCount == 0) {
        document.mainForm.screen.value = "apply";
        document.mainForm.whichKeyToSteal.value = '<%=lockKey%>';
        saveContent();
        teleportCaptainFlam(document.mainForm);
    }
}

function sendFormCancel() {
    document.mainForm.screen.value = "cancel";
    teleportCaptainFlam(document.mainForm);
}

function changeField(fieldID) {
    document.mainForm.screen.value = "<%=theScreen%>";
    document.mainForm.editfid.value = fieldID;
    if (check()) {
        saveContent();
        document.mainForm.submit();
    }
}

function setWaitingCursor(showWaitingImage) {
    document.body.style.cursor = "wait";
}

function sendForm(method, params) {
    workInProgressOverlay.launch();
    document.mainForm.method.value = method;
    document.mainForm.screen.value = "<%=theScreen%>";
    document.mainForm.engineview.value = "<%=engineView%>";
    if (params != null && params != "") {
        if (document.mainForm.action.indexOf("?") > 0) {
            if (params.charAt(0) == "&") {
                document.mainForm.action += params;
            } else {
                document.mainForm.action += "&" + params;
            }
        } else {
            if (params.charAt(0) == "&") {
                document.mainForm.action += "?" + params.substring(1);
            } else {
                document.mainForm.action += "?" + params;
            }
        }
    }
    flagSubmit();
    document.mainForm.submit();
}

/** should be overrided if needed **/
function windowOnload() {
    scroll(0, 0);
}

/** should be overrided if needed **/
function hideUserShell() {
}

/** should be overrided if needed **/
function showUserShell() {
}

 function closeTheWindow() {
        var last = "<%=theScreen%>";
        var src = "<%=jspSource%>";

        if (src == "apply" || src == "delete_container") return;
        //alert (submittedCount + "-" + last + "-" + src);
        if (submittedCount == 0) {
            if (last != "save" && last != "cancel" && last != "showReport" && src != "apply" &&
                src != "close" && src != "lock") {
                sendFormCancel();
                //alert ("sendFormCancel" + "-" + last + "-" + src);
            }
            if (src == "close") {
                // Do refresh opener by setting argument to "yes"
                CloseJahiaWindow("yes");
            } else {
                CloseJahiaWindow();
            }
        }
    }

function getElementsByClassName(el, clsName)
{
    var arr = new Array();
    var elems = el.getElementsByTagName("*");
    for (var cls, i = 0; ( elem = elems[i] ); i++)
    {
        if (elem.className == clsName)
        {
            arr[arr.length] = elem;
        }
    }
    return arr;
}

function addFlagSubmitToDisplayTagLinks(tableId, itemListingClass) {
    var table = document.getElementById(tableId);
    var thead = table.getElementsByTagName("thead")[0];
    var hrefs = thead.getElementsByTagName("a");
    // add event handlers so rows light up and are clickable
    for (i = 0; i < hrefs.length; i++) {
        hrefs[i].onclick = function() {
            flagSubmit();
        };
    }
    var el = getElementsByClassName(document, itemListingClass)[0];
    if (el) {
        var span = getElementsByClassName(el, "pagelinks")[0];
        if (span) {
            hrefs = span.getElementsByTagName("a");
            // add event handlers so rows light up and are clickable
            for (i = 0; i < hrefs.length; i++) {
                hrefs[i].onclick = function() {
                    flagSubmit();
                };
            }
        }
    }
}

window.onunload = closeTheWindow;
window.onload = windowOnload;

//-->
</script>
</head>

<body>
<div id="userShell">
  <!-- wrapper (start) -->
  <div id="mainClientLayout">
    <form name="mainForm" method="post" action="<%=engineUrl%>">
      <script language="javascript" type="text/javascript">
        <!--
        hideUserShell();
        //-->
      </script>
      <% if (results != null) { %>
        <div id="readOnly">
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.readOnlyMode.label"/>: <%=LockKey.getFriendlyMessage(lockKey, jParams)%>
          <%
          final List locksInfo = LockRegistry.getInstance().getInfo(lockKey);
          for (java.util.Iterator iterator = locksInfo.iterator(); iterator.hasNext();) {
            final Map lockInfo = (Map) iterator.next();
            final JahiaUser jahiaUser = (JahiaUser) lockInfo.get(LockRegistry.OWNER);
            final String lockID = (String) lockInfo.get(LockRegistry.ID);
            boolean isSameContext = jahiaUser != null && jParams.getUser().getUserKey().equals(jahiaUser.getUserKey()) && jParams.getSessionID().equals(lockID); %>
            <% if ((! LockKey.WAITING_FOR_APPROVAL_TYPE.equals(lockKey.getType())) && LockRegistry.getInstance().hasAdminRights(lockKey, jParams.getUser()) || isSameContext) { %>
            <div class="button">
              <a href="javascript:sendFormSteal();" title="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.lock.stealLock.label"/>">
                <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.lock.stealLock.label"/></a>
            </div>
            <input type="hidden" name="whichKeyToSteal" value=""/>
            <% } %>
          <% } %>
        </div>
      <% } %>
      <!-- pagebody (start) -->
      <div id="pagebody<%=(results != null ? "ReadOnly" : "")%>">
        <!-- include page starts -->

        <!-- Import all tiles attribute in request scope -->
        <tiles:importAttribute scope="request"/>

        <input type="hidden" name="screen" value=""/>
        <input type="hidden" name="lastscreen" value="<%=theScreen%>"/>
        <input type="hidden" name="engineview" value="<%=engineView%>"/>
        <input type="hidden" name="method" value=""/>

        <div id="header">
          <h1>Jahia</h1>
          <!-- Engine title -->
         <h2><%=engineTitle%></h2>
          <!-- End title -->
          <c:set var="navigationPresent"><tiles:getAsString name="navigation" ignore="true"/></c:set>
          <c:if test="${not empty navigationPresent}">
            <jsp:include page="../../jsp/jahia/engines/navigation.jsp" />
          </c:if>
        </div>
        <div id="mainContent">
          <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
            <tbody>
              <tr>
                <td style="vertical-align: top;" align="left" width="50%">
                  <div class="dex-TabBar">
                    <!-- Menubar -->
                    <tiles:insert attribute="menu-bar" ignore="true"/>
                    <!-- End Menubar -->
                  </div>
                </td>
               <td class="dex-langItem-wrapper-empty" style=" text-align:right; vertical-align: top;" align="right" nowrap="nowrap" width="50%">
                  <!-- Langs -->
                  <tiles:insert attribute="multilanguage-links" ignore="true"/>
                  <!-- End Langs -->
                </td>
              </tr>
              <tr>
                <td style="vertical-align: top;" align="left" height="100%" colspan="2">
                  <!-- Body main content -->
                  <tiles:insert attribute="body-content" ignore="true"/>
                </td>
              </tr>
            </tbody>
          </table>
          <!-- Buttons -->
          <tiles:insert attribute="buttons" ignore="true"/>
          <!-- End Buttons -->
        </div>
        <!-- include page ends -->
      </div>
      <!-- end pagebody -->
    </form>
    <div id="copyright">
      <%=Jahia.COPYRIGHT%> Jahia <%=Jahia.VERSION%>.<%=Jahia.getPatchNumber()%> r<%=Jahia.getBuildNumber()%>
    </div>
  </div>
</div>
</body>
</html>
<% out.flush(); %>