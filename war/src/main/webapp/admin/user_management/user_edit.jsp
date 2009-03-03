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

<%@page language="java" %>
<%@page import="org.jahia.admin.users.ManageUsers" %>
<%@page import="org.jahia.bin.JahiaAdministration" %>
<%@page import="org.jahia.data.JahiaData" %>
<%@page import="org.jahia.data.viewhelper.principal.PrincipalViewHelper" %>
<%@page import="org.jahia.services.mail.MailHelper" %>
<%@page import="org.jahia.params.ProcessingContext" %>
<%@page import="org.jahia.utils.i18n.JahiaResourceBundle" %>
<%@page import="org.jahia.services.usermanager.JahiaUser,org.jahia.services.usermanager.UserProperties,org.jahia.services.usermanager.UserProperty" %>
<%@ page import="org.jahia.utils.JahiaTools" %>
<%@ page import="java.security.Principal" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.MissingResourceException" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Set" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/uiComponentsLib" prefix="ui" %>
<%@include file="/admin/include/header.inc" %>

<c:set var="noneLabel"><fmt:message key="org.jahia.userMessage.none"/></c:set>


<jsp:useBean id="userMessage" class="java.lang.String" scope="session"/>

<%
    UserProperties userProps = (UserProperties) request.getAttribute("userProperties");
    JahiaUser jUser = (JahiaUser) request.getAttribute("theUser");
    String username = jUser.getUsername();
    String provider = jUser.getProviderName();
    String passwd = (String) request.getAttribute("passwd");
    String passwdConfirm = (String) request.getAttribute("passwdConfirm");
    ProcessingContext jParams = ((JahiaData) request.getAttribute("org.jahia.data.JahiaData")).getProcessingContext();
    String isSuperAdminProp = (String) request.getAttribute("isSuperAdminProp");
    // when isPopup = true, name and firts name are not editable
    boolean isPopup = request.getParameter("isPopup") != null ? true : false;
%>
<%!
    public String getUserProp(UserProperties userProps, String propName) {
        UserProperty propValue = (UserProperty) userProps.getUserProperty(propName);
        if (propValue == null) {
            return "";
        } else {
            return propValue.getValue();
        }
    }

    public boolean isPropReadOnly(UserProperties userProps, String propName) {
        UserProperty propValue = (UserProperty) userProps.getUserProperty(propName);
        if (propValue == null) {
            return false;
        } else {
            return propValue.isReadOnly();
        }
    }


%>
<%
    if (isSuperAdminProp == null) {
        stretcherToOpen = 1;
    } else {
        stretcherToOpen = 0;
    }
%>

<!-- For future version : <script language="javascript" src="../search_options.js"></script> -->
<script language="javascript" src="<%= URL%>../javascript/selectbox.js"></script>

<%if (isPopup) {%>
<script type="text/javascript">
    function closeWindowAndReloadParent() {
        //close
        self.close();
        // reload opener  without confirm post data box
        //window.opener.location.href = opener.location.href ;
        // window.opener.document.mainForm.sub.value='edit';
        //window.opener.document.mainForm.submit();
    }
</script>
<%}%>

<script language="javascript">
    window.onunload = closeEngineWin;
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

<!-- Administration page position -->


<div id="topTitle">
    <h1>Jahia</h1>
    <% if (provider.equalsIgnoreCase("ldap")) { %>
    <h2 class="edit"><fmt:message key="org.jahia.admin.users.ManageUsers.editUser.label"/></h2>
    <% } else { %>
    <h2 class="edit"><fmt:message key="org.jahia.admin.users.ManageUsers.viewUserProperties.label"/></h2>
    <% } %>

</div>

<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
       cellspacing="0">
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
<% String menuMode = isSuperAdminProp == null ? "site" : "server"; %>
<jsp:include page="/admin/include/left_menu.jsp">
    <jsp:param name="mode" value="<%= menuMode %>"/>
</jsp:include>

<div id="content" class="fit">
<div class="head headtop">
    <div class="object-title">
        <fmt:message key="org.jahia.admin.users.ManageUsers.editUser.label"/>
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

<form name="mainForm" action='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=processEdit")%>'
      method="post">
<!-- Edit user -->
    <span style="padding: 5px 5px 5px 20px;display: block">
    <p><fmt:message key="org.jahia.admin.users.ManageUsers.pleaseOk.label"/></p>
    <p><fmt:message key="org.jahia.admin.users.ManageUsers.noteThat.label"/>&nbsp;:</p>
    <ul>
        <li><fmt:message key="org.jahia.admin.users.ManageUsers.removeSpace.label"/></li>
        <li><fmt:message key="org.jahia.admin.users.ManageUsers.inputMaxCharacter.label"/></li>
    </ul>
    </span>
<table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable" width="100%">
<tr>
    <td align="right">
        <fmt:message key="org.jahia.admin.username.label"/>&nbsp;
    </td>
    <td>
        <!-- This hidden field is here when one will decide that a user name can be changed -->
        <input type="hidden" name="username" value="<%=username%>">
        <!-- This hidden field can be changed to 'update' so that we keep the edited data without confirming changes -->
        <input type="hidden" name="actionType" value="save"/>
        <b><%=username%>
        </b>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="org.jahia.admin.firstName.label"/>&nbsp;
    </td>
    <td>
        <%if (!isPopup) {%>
        <input class="input" type="text" size="40" maxlength="40"
               <% if (isPropReadOnly(userProps, "firstname")) { %>disabled="true"<%}%>
               name='<%=ManageUsers.USER_PROPERTY_PREFIX+"firstname"%>'
               value='<%=getUserProp(userProps, "firstname")%>'/>
        <%} else {%>
        <b><%=getUserProp(userProps, "firstname")%>
        </b>
        <%}%>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="org.jahia.admin.lastName.label"/>&nbsp;
    </td>
    <td>
        <%if (!isPopup) {%>
        <input class="input" type="text" size="40" maxlength="255"
               <% if (isPropReadOnly(userProps, "lastname")) { %>disabled="true"<%}%>
               name='<%=ManageUsers.USER_PROPERTY_PREFIX+"lastname"%>'
               value='<%=getUserProp(userProps, "lastname")%>'/>
        <%} else {%>
        <b><%=getUserProp(userProps, "lastname")%>
        </b>
        <%}%>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="org.jahia.admin.eMail.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" size="40" maxlength="255"
               <% if (isPropReadOnly(userProps, "email")) { %>disabled="true"<%}%>
               name='<%=ManageUsers.USER_PROPERTY_PREFIX+"email"%>'
               value='<%=getUserProp(userProps, "email")%>'/>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="org.jahia.admin.organization.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" size="40" maxlength="255"
               <% if (isPropReadOnly(userProps, "organization")) { %>disabled="true"<%}%>
               name='<%=ManageUsers.USER_PROPERTY_PREFIX+"organization"%>'
               value='<%=getUserProp(userProps, "organization")%>'/>
    </td>
</tr>
<tr>
    <td align="right">
        <label for="emailNotificationsDisabledView"><fmt:message key="org.jahia.admin.emailNotifications.label"/>&nbsp;</label>
    </td>
    <td>
        <%
            boolean iReadOnly = isPropReadOnly(userProps, "emailNotificationsDisabled");
            String propValue = getUserProp(userProps, "emailNotificationsDisabled");
        %>
        <input type="checkbox" class="input" id="emailNotificationsDisabledView" name="emailNotificationsDisabledView"
               value="true" <% if ("true".equals(propValue)) {%>checked="checked"<% } %>
               onclick="javascript:{document.getElementById('emailNotificationsDisabled').value= this.checked ? 'true' : 'false';}"
               <% if (iReadOnly) {%>disabled="disabled"<% } %>/>
        <input type="hidden" id="emailNotificationsDisabled"
               name='<%=ManageUsers.USER_PROPERTY_PREFIX+"emailNotificationsDisabled"%>'
               value='<%= "true".equals(propValue) ? "true" : "false" %>'/>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="org.jahia.admin.preferredLanguage.label"/>&nbsp;
    </td>
    <td>
        <%
            iReadOnly = isPropReadOnly(userProps, "preferredLanguage");
            propValue = getUserProp(userProps, "preferredLanguage");
            if (propValue == null || propValue.length() == 0) {
                propValue = MailHelper.getPreferredLocale(jUser, jParams.getSite()).toString();
            }
        %>
        <select name='<%=ManageUsers.USER_PROPERTY_PREFIX + "preferredLanguage"%>'
                <% if (iReadOnly) {%>disabled="disabled"<% } %>>
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
<% if (!jUser.isPasswordReadOnly()) { %>
<tr>
    <td align="right">
        <fmt:message key="org.jahia.admin.password.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="password" name="passwd"
               size="40" maxlength="255"
               value='<%=JahiaTools.nnString(passwd)%>'>
        &nbsp;<span class="text2"><fmt:message key="org.jahia.admin.users.ManageUsers.noChangeBlank.label"/></span>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="org.jahia.admin.confirmPassword.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="password" name="passwdconfirm"
               size="40" maxlength="255"
               value='<%=JahiaTools.nnString(passwdConfirm)%>'>
        &nbsp;<span class="text2"><fmt:message key="org.jahia.admin.users.ManageUsers.noChangeBlank.label"/></span>
    </td>
</tr>
<% } %>
<% if (isSuperAdminProp == null) {%>
<tr style="height: 35px; vertical-align: top;">
    <td align="right">
        <fmt:message key="org.jahia.admin.homePage.label"/>&nbsp;
    </td>
    <td>
        <b id="homePageLabel">${not empty homePageLabel ? homePageLabel : noneLabel}</b>
        <input type="hidden" name="homePageID" id="homePageID" value="${homePageID}">
        <% if (!jUser.isRoot()) { %>
        <br/>
        <span class="dex-PushButton">
            <span class="first-child">
                <c:set var="label"><fmt:message key='org.jahia.admin.select.label' /></c:set>
                <c:set var="title"><fmt:message key='org.jahia.admin.users.ManageUsers.setHomePage.label'/></c:set>
                <ui:pageSelector fieldId="homePageID" displayIncludeChildren="false" onSelect="homePageSelected" class="ico-home-add" label="${label}" title="${title}"/>
            </span>
        </span>
        <span class="dex-PushButton">
            <span class="first-child">
                <a href="#remove" class="ico-delete" onclick="homePageRemoved(); return false;"><fmt:message key="org.jahia.admin.users.ManageGroups.altSetHomePageForThisGroupToNone.label"/></a>
            </span>
        </span>
        <% } %>
    </td>
</tr>
<% } %>
<tr>
    <td colspan="2" align="center">&nbsp;
        <%
            Set groups = (Set) request.getAttribute("groups");
            if (groups.size() > 0) {
                String[] textPattern = jUser.isRoot() ? new String [] {"Principal", "Provider, 6", "Name, 15", "SiteTitle, 15", "Properties, 20"} : new String [] {"Principal", "Provider, 6", "Name, 15", "Properties, 20"};
                PrincipalViewHelper principalViewHelper = new PrincipalViewHelper(textPattern);
        %>
        <fmt:message key="org.jahia.admin.users.ManageGroups.groupList.label"/>
        <br>
        <select class="fontfix" name="selectMember" size="6" multiple
                <%if (groups.size() == 0) {%>disabled<%}%> >
            <%
                Iterator it = groups.iterator();
                while (it.hasNext()) {
                    Principal p = (Principal) it.next();
            %>
            <option value="<%=principalViewHelper.getPrincipalValueOption(p)%>">
                <%=principalViewHelper.getPrincipalTextOption(p)%>
            </option>
            <%
                    }
                }

            %>
        </select>
    </td>
</tr>

<tr>
    <td>&nbsp;
        <% if (provider.equalsIgnoreCase("ldap")) { %>
        <% if (isSuperAdminProp == null) {%>
        <internal:jahiaButton img="undoall"
                        href="javascript:document.mainForm.reset();"
                        altBundle="administration" altKey="org.jahia.admin.users.ManageUsers.resetChangeNotHome.label"/>
        <% } else { %>
        <internal:jahiaButton img="undoall"
                        href="javascript:document.mainForm.reset();"
                        altBundle="administration" altKey="org.jahia.admin.resetChanges.label"/>
        <% } %>
        <% } %>
    </td>
    <td>
        <br>

        <div class="buttonList" style="padding-top: 8px; padding-bottom: 8px">

        </div>
    </td>
</tr>
</table>
<!--  -->
</form>
</div>
<!-- end of content-item -->
<!--  -->
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
      	  
    <% if (isSuperAdminProp == null) {
        String cancelURL = JahiaAdministration.composeActionURL(request, response, "users", "&sub=display");
        if (isPopup) {
            cancelURL = "javascript:window.close();";
        }
    %>
        <a class="ico-cancel" href="<%=cancelURL%>"><fmt:message key="org.jahia.admin.cancel.label"/></a>
    <% } else {
        String cancelURL = JahiaAdministration.composeActionURL(request, response, "displaymenu", "");
        if (isPopup) {
            cancelURL = "javascript:window.close();";
        }
    %>
        <a class="ico-cancel" href="<%=cancelURL%>"><fmt:message key="org.jahia.admin.cancel.label"/></a>
    <% } %>
      </span>
     </span>
     <span class="dex-PushButton"> 
      <span class="first-child">
         <a class="ico-ok"
            href="javascript:document.mainForm.submit();<%if(isPopup){%>closeWindowAndReloadParent();<%}%>"><fmt:message key="org.jahia.admin.ok.label"/></a>
      </span>
     </span>

</div>

</div>

