<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/uiComponentsLib" prefix="ui" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ page import="java.util.Locale" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.utils.LanguageCodeConverters" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.bin.*,org.jahia.admin.users.*" %>
<%@page import="org.jahia.services.preferences.user.UserPreferencesHelper"%>
<%@ page import="org.apache.commons.lang.WordUtils" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<c:set var="noneLabel"><fmt:message key="org.jahia.userMessage.none"/></c:set>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>
<% // http files path. %>
<jsp:useBean id="userMessage" class="java.lang.String" scope="session"/>

<%
    final Map userProperties = (Map) request.getAttribute("userProperties");
    JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
    ProcessingContext jParams = ((JahiaData) request.getAttribute("org.jahia.data.JahiaData")).getProcessingContext();
    int stretcherToOpen = 0;
%>
<%!
    public String getUserProp(final Map userProps, final String propName) {
        final String propValue = (String) userProps.get(propName);
        if (propValue == null) {
            return "";
        } else {
            return StringEscapeUtils.escapeHtml(propValue);
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
</script>

<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.users.ManageUsers.createNewUser.label"/></h2>
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
<jsp:include page="/admin/include/left_menu.jsp">
    <jsp:param name="mode" value="server"/>
</jsp:include>

<div id="content" class="fit">
<div class="head">
    <div class="object-title">
        <fmt:message key="org.jahia.admin.users.ManageUsers.createNewUser.label"/>
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
<c:if test="${not empty engineMessages && engineMessages.size > 0}">
<c:if test="${engineMessages.size == 1}">
        <c:forEach items="${engineMessages.messages}" var="msg">
            <span class="errorbold"><internal:message name="msg"/></span>
        </c:forEach>
</c:if>
<c:if test="${engineMessages.size != 1}">
        <ul>
            <c:forEach items="${engineMessages.messages}" var="msg">
                <li class="errorbold"><internal:message name="msg"/></li>
            </c:forEach>
        </ul>
</c:if>
</c:if>

<form name="mainForm" action='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=processCreate")%>'
      method="post" onkeydown="javascript:handleKeyCode(event.keyCode);" autocomplete="off">
<!-- Create new user -->
<input type="hidden" name="actionType" value="save"/>

<p>&nbsp;&nbsp;<fmt:message key="org.jahia.admin.users.ManageUsers.pleaseOk.label"/></p>

<p>&nbsp;&nbsp;<fmt:message key="org.jahia.admin.users.ManageUsers.noteThat.label"/>&nbsp;:</p>
<ul>
    <li><fmt:message key="org.jahia.admin.users.ManageUsers.userNameUniq.label"/></li>
    <li><fmt:message key="org.jahia.admin.users.ManageUsers.onlyCharacters.label"/></li>
    <li><fmt:message key="org.jahia.admin.users.ManageUsers.inputMaxCharacter.label"/></li>
</ul>

<table border="0" style="width:100%">
<tr>
    <td align="right">
        <fmt:message key="label.username"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name="username"
               size="40" maxlength="40" value='<%=StringEscapeUtils.escapeHtml((String) userProperties.get("username"))%>'>
        &nbsp;<font class="text2">(<fmt:message key="org.jahia.admin.required.label"/>)</font>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="org.jahia.admin.firstName.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name='<%=ManageUsers.USER_PROPERTY_PREFIX+"j:firstName"%>'
               size="40" maxlength="255" value='<%=getUserProp(userProperties,"j:firstName")%>'>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="org.jahia.admin.lastName.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name='<%=ManageUsers.USER_PROPERTY_PREFIX+"j:lastName"%>'
               size="40" maxlength="255" value='<%=getUserProp(userProperties,"j:lastName")%>'>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="label.email"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name='<%=ManageUsers.USER_PROPERTY_PREFIX+"j:email"%>'
               size="40" maxlength="255" value='<%=getUserProp(userProperties,"j:email")%>'>
    </td>
</tr>
<tr>
    <td align="right" nowrap>
        <fmt:message key="org.jahia.admin.organization.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" name='<%=ManageUsers.USER_PROPERTY_PREFIX+"j:organization"%>'
               size="40" maxlength="255" value='<%=getUserProp(userProperties,"j:organization")%>'>
    </td>
</tr>
<tr>
    <td align="right">
        <label for="emailNotificationsDisabled"><fmt:message key="label.emailNotifications"/>&nbsp;</label>
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
        <fmt:message key="org.jahia.admin.preferredLanguage.label"/>&nbsp;
    </td>
    <td>
        <%
            propValue = getUserProp(userProperties, "preferredLanguage");
            if (propValue == null || propValue.length() == 0) {
                propValue = UserPreferencesHelper.getPreferredLocale(null, jParams.getSite()).toString();
            }
        %>
        <select name='<%=ManageUsers.USER_PROPERTY_PREFIX + "preferredLanguage"%>'>
            <%
                for (Locale theLocale : LanguageCodeConverters.getAvailableBundleLocalesSorted(jParams.getUILocale())) {%>
            <option value="<%=theLocale %>"
                    <% if (theLocale.toString().equals(propValue)) { %>selected="selected"<% } %>><%= WordUtils.capitalizeFully(theLocale.getDisplayName(jParams.getUILocale())) %>
            </option>
            <% } %>
        </select>
    </td>
</tr>
<tr>
    <td align="right">
        <label for="accountLocked"><fmt:message key="label.accountLocked"/>&nbsp;</label>
    </td>
    <td>
        <%
            propValue = getUserProp(userProperties, "j:accountLocked");
        %>
        <input type="checkbox" class="input" id="accountLocked"
               name='<%=ManageUsers.USER_PROPERTY_PREFIX + "j:accountLocked"%>' value="true"
               <% if ("true".equals(propValue)) {%>checked="checked"<% } %> />
    </td>
</tr>
<%-- You can add your custom user properties here --%>
<tr>
    <td align="right">
        <fmt:message key="label.password"/>&nbsp;
    </td>
    <td>
        <input class="input" type="password" name="passwd"
               size="40" maxlength="255" value='<%=userProperties.get("passwd")%>'>
        &nbsp;<font class="text2">(<fmt:message key="org.jahia.admin.required.label"/>)</font>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="label.comfirmPassword"/>&nbsp;
    </td>
    <td>
        <input class="input" type="password" name="passwdconfirm"
               size="40" maxlength="255" value='<%=userProperties.get("passwdconfirm")%>'>
        &nbsp;<font class="text2">(<fmt:message key="org.jahia.admin.required.label"/>)</font>
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
      	 <a class="ico-cancel"
              href='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=display")%>'><fmt:message key="label.cancel"/></a>
      </span>
     </span>
     <span class="dex-PushButton">
      <span class="first-child">
         <a class="ico-restore" href="javascript:document.mainForm.reset();"><fmt:message key="org.jahia.admin.resetChanges.label"/></a>
      </span>
     </span>
     <span class="dex-PushButton">
        <span class="first-child">
        <a class="ico-ok" href="#ok" onclick="showWorkInProgress(); document.mainForm.submit(); return false;"><fmt:message key="label.ok"/></a>
        </span>
      </span>

</div>


<script type="text/javascript" language="javascript">
    setFocus();
</script>

</div>
