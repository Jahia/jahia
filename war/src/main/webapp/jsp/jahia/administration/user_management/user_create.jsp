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

<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/uiComponentsLib" prefix="ui" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.services.mail.MailHelper" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.bin.*,org.jahia.admin.users.*" %>
<c:set var="noneLabel"><internal:adminResourceBundle resourceName="org.jahia.userMessage.none" defaultValue="none"/></c:set>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>
<% // http files path. %>
<jsp:useBean id="userMessage" class="java.lang.String" scope="session"/>

<%
    final Map userProperties = (Map) request.getAttribute("userProperties");
    JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
    ProcessingContext jParams = ((JahiaData) request.getAttribute("org.jahia.data.JahiaData")).getProcessingContext();
    int stretcherToOpen = 1;
%>
<%!
    public String getUserProp(final Map userProps, final String propName) {
        final String propValue = (String) userProps.get(propName);
        if (propValue == null) {
            return "";
        } else {
            return propValue;
        }
    }
%>

<script language="javascript" type="text/javascript">
    function setFocus() {
        document.mainForm.username.focus();
    }

    function handleKeyCode(code) {
        if (code == 13) {
            document.mainForm.submit();
        }
    }
    function homePageSelected(pid, url, title) {
        var titleElement = document.getElementById('homePageLabel');
        titleElement.removeChild(titleElement.firstChild);
        titleElement.appendChild(document.createTextNode(title));
        return true;
    }
    function homePageRemoved() {
        document.getElementById('homePageID').value = ''; 
        var titleElement = document.getElementById('homePageLabel');
        titleElement.removeChild(titleElement.firstChild);
        titleElement.appendChild(document.createTextNode('${noneLabel}'));
        return true;
    }
</script>

<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><internal:adminResourceBundle
            resourceName="org.jahia.admin.users.ManageUsers.createNewUser.label"/></h2>
</div>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
       cellspacing="0">
<tbody>
<tr>
    <td style="vertical-align: top;" align="left">
        <%@include file="/jsp/jahia/administration/include/tab_menu.inc" %>
    </td>
</tr>
<tr>
<td style="vertical-align: top;" align="left" height="100%">
<div class="dex-TabPanelBottom">
<div class="tabContent">
<jsp:include page="/jsp/jahia/administration/include/left_menu.jsp">
    <jsp:param name="mode" value="site"/>
</jsp:include>

<div id="content" class="fit">
<div class="head">
    <div class="object-title">
        <internal:adminResourceBundle resourceName="org.jahia.admin.users.ManageUsers.createNewUser.label"/>
    </div>
</div>
<div class="content-item">
<%
    if (userMessage.length() > 0) {
%>
<p class="errorbold">
    <%=userMessage%>
</p>
<% } %>
<logic:present name="engineMessages">
<logic:equal name="engineMessages" property="size" value="1">
        <logic:iterate name="engineMessages" property="messages" id="msg">
            <span class="errorbold"><internal:message name="msg"/></span>
        </logic:iterate>
</logic:equal>
<logic:notEqual name="engineMessages" property="size" value="1">
        <ul>
            <logic:iterate name="engineMessages" property="messages" id="msg">
                <li class="errorbold"><internal:message name="msg"/></li>
            </logic:iterate>
        </ul>
</logic:notEqual>
</logic:present>

<form name="mainForm" action='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=processCreate")%>'
      method="post" onkeydown="javascript:handleKeyCode(event.keyCode);">
<!-- Create new user -->
<input type="hidden" name="actionType" value="save"/>

<p>&nbsp;&nbsp;<internal:adminResourceBundle resourceName="org.jahia.admin.users.ManageUsers.pleaseOk.label"/></p>

<p>&nbsp;&nbsp;<internal:adminResourceBundle resourceName="org.jahia.admin.users.ManageUsers.noteThat.label"/>&nbsp;:</p>
<ul>
    <li><internal:adminResourceBundle
            resourceName="org.jahia.admin.users.ManageUsers.userNameUniq.label"/></li>
    <li><internal:adminResourceBundle
            resourceName="org.jahia.admin.users.ManageUsers.onlyCharacters.label"/></li>
    <li><internal:adminResourceBundle
            resourceName="org.jahia.admin.users.ManageUsers.inputMaxCharacter.label"/></li>
</ul>

<table border="0" style="width:100%">
<tr>
    <td align="right">
        <internal:adminResourceBundle resourceName="org.jahia.admin.username.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name="username"
               size="40" maxlength="255" value='<%=userProperties.get("username")%>'>
        &nbsp;<font class="text2">(<internal:adminResourceBundle resourceName="org.jahia.admin.required.label"/>)</font>
    </td>
</tr>
<tr>
    <td align="right">
        <internal:adminResourceBundle resourceName="org.jahia.admin.firstName.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name='<%=ManageUsers.USER_PROPERTY_PREFIX+"firstname"%>'
               size="40" maxlength="255" value='<%=getUserProp(userProperties,"firstname")%>'>
    </td>
</tr>
<tr>
    <td align="right">
        <internal:adminResourceBundle resourceName="org.jahia.admin.lastName.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name='<%=ManageUsers.USER_PROPERTY_PREFIX+"lastname"%>'
               size="40" maxlength="255" value='<%=getUserProp(userProperties,"lastname")%>'>
    </td>
</tr>
<tr>
    <td align="right">
        <internal:adminResourceBundle resourceName="org.jahia.admin.eMail.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name='<%=ManageUsers.USER_PROPERTY_PREFIX+"email"%>'
               size="40" maxlength="255" value='<%=getUserProp(userProperties,"email")%>'>
    </td>
</tr>
<tr>
    <td align="right" nowrap>
        <internal:adminResourceBundle resourceName="org.jahia.admin.organization.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name='<%=ManageUsers.USER_PROPERTY_PREFIX+"organization"%>'
               size="40" maxlength="255" value='<%=getUserProp(userProperties,"organization")%>'>
    </td>
</tr>
<tr>
    <td align="right">
        <label for="emailNotificationsDisabled"><internal:adminResourceBundle
                resourceName="org.jahia.admin.emailNotifications.label"
                defaultValue="emailNotifications"/>&nbsp;</label>
    </td>
    <td>
        <%
            String propValue = getUserProp(userProperties, "emailNotificationsDisabled");
        %>
        <input type="checkbox" class="input" id="emailNotificationsDisabled"
               name='<%=ManageUsers.USER_PROPERTY_PREFIX + "emailNotificationsDisabled"%>' value="true"
               <% if ("true".equals(propValue)) {%>checked="checked"<% } %> />
    </td>
</tr>
<tr>
    <td align="right">
        <internal:adminResourceBundle resourceName="org.jahia.admin.preferredLanguage.label"
                                     defaultValue="preferredLanguage"/>&nbsp;
    </td>
    <td>
        <%
            propValue = getUserProp(userProperties, "preferredLanguage");
            if (propValue == null || propValue.length() == 0) {
                propValue = MailHelper.getPreferredLocale(null, jParams.getSite()).toString();
            }
        %>
        <select name='<%=ManageUsers.USER_PROPERTY_PREFIX + "preferredLanguage"%>'>
            <%
                for (java.util.Locale theLocale : MailHelper.getAvailableBundleLocalesSorted(jParams.getLocale())) {%>
            <option value="<%=theLocale %>"
                    <% if (theLocale.toString().equals(propValue)) { %>selected="selected"<% } %>><%= theLocale.getDisplayName(jParams.getLocale()) %>
            </option>
            <% } %>
        </select>
    </td>
</tr>
<%-- You can add your custom user properties here --%>
<tr>
    <td align="right">
        <internal:adminResourceBundle resourceName="org.jahia.admin.password.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="password" name="passwd"
               size="40" maxlength="255" value='<%=userProperties.get("passwd")%>'>
        &nbsp;<font class="text2">(<internal:adminResourceBundle resourceName="org.jahia.admin.required.label"/>)</font>
    </td>
</tr>
<tr>
    <td align="right">
        <internal:adminResourceBundle resourceName="org.jahia.admin.confirmPassword.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="password" name="passwdconfirm"
               size="40" maxlength="255" value='<%=userProperties.get("passwdconfirm")%>'>
        &nbsp;<font class="text2">(<internal:adminResourceBundle resourceName="org.jahia.admin.required.label"/>)</font>
    </td>
</tr>
<tr style="height:35px; vertical-align: top;">
    <td align="right">
        <internal:adminResourceBundle resourceName="org.jahia.admin.homePage.label"/>&nbsp;
    </td>
    <td>
        <b id="homePageLabel">${not empty homePageLabel ? homePageLabel : noneLabel}</b>
        <input type="hidden" name="homePageID" id="homePageID" value="${homePageID}">
        <br/>
        <span class="dex-PushButton">
            <span class="first-child">
                <c:set var="label"><internal:adminResourceBundle resourceName='org.jahia.admin.select.label' defaultValue='select'/></c:set>
                <c:set var="title"><internal:adminResourceBundle resourceName='org.jahia.admin.users.ManageUsers.setHomePage.label' defaultValue='select'/></c:set>
                <ui:pageSelector fieldId="homePageID" displayIncludeChildren="false" onSelect="homePageSelected" class="ico-home-add" label="${label}" title="${title}"/>
            </span>
        </span>
        <span class="dex-PushButton">
            <span class="first-child">
                <a href="#remove" class="ico-delete" onclick="homePageRemoved(); return false;"><internal:adminResourceBundle resourceName="org.jahia.admin.users.ManageGroups.altSetHomePageForThisGroupToNone.label" defaultValue="remove"/></a>
            </span>
        </span>
    </td>
</tr>
</table>
<!--  -->
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
      	 <a class="ico-cancel"
              href='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=display")%>'><internal:adminResourceBundle
                   resourceName="org.jahia.admin.cancel.label"/></a>
      </span>
     </span>
     <span class="dex-PushButton">
      <span class="first-child">
         <a class="ico-restore" href="javascript:document.mainForm.reset();"><internal:adminResourceBundle
                 resourceName="org.jahia.admin.resetChanges.label"/></a>
      </span>
     </span>
     <span class="dex-PushButton">
        <span class="first-child">
        <a class="ico-ok" href="javascript:document.mainForm.submit();"><internal:adminResourceBundle
                resourceName="org.jahia.admin.ok.label"/></a>
        </span>
      </span>

</div>


<script type="text/javascript" language="javascript">
    setFocus();
</script>

</div>
