<%@page language="java" %>
<%@page import="org.jahia.admin.users.ManageUsers" %>
<%@page import="org.jahia.bin.JahiaAdministration" %>
<%@page import="org.jahia.data.JahiaData" %>
<%@page import="org.jahia.data.viewhelper.principal.PrincipalViewHelper" %>
<%@page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.utils.LanguageCodeConverters" %>
<%@page import="org.jahia.services.preferences.user.UserPreferencesHelper"%>
<%@page import="org.jahia.services.pwdpolicy.JahiaPasswordPolicyService" %>
<%@page import="org.jahia.services.usermanager.JahiaUser,org.jahia.services.usermanager.UserProperties,org.jahia.services.usermanager.UserProperty" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="java.security.Principal" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.apache.commons.lang.WordUtils" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/uiComponentsLib" prefix="ui" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
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
    pageContext.setAttribute("isSuperAdminProp", Boolean.valueOf(isSuperAdminProp != null));
    // when isPopup = true, name and firts name are not editable
    boolean isPopup = request.getParameter("isPopup") != null ? true : false;
%>
<%!
    public String getUserProp(UserProperties userProps, String propName) {
        UserProperty propValue = (UserProperty) userProps.getUserProperty(propName);
        if (propValue == null) {
            return "";
        } else {
            return propValue.getValue() != null ? StringEscapeUtils.escapeXml(propValue.getValue()) : propValue.getValue();
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
    stretcherToOpen = 0;
%>

<!-- For future version : <script language="javascript" src="../search_options.js"></script> -->
<script language="javascript" src="<%= URL%>../javascript/selectbox.js"></script>

<%if (isPopup) {%>
<script type="text/javascript">
    function closeWindowAndReloadParent() {
        //close
        self.close();
    }
</script>
<%}%>

<script language="javascript">
    window.onunload = closeEngineWin;
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
<jsp:include page="/admin/include/left_menu.jsp">
    <jsp:param name="mode" value="server"/>
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
<p class="${not isError ? 'blueColor' : 'errorbold'}">
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

<form name="mainForm" action='<%=JahiaAdministration.composeActionURL(request,response, isSuperAdminProp == null ? "users" : "profile","&sub=processEdit")%>'
      method="post">
<!-- Edit user -->
    <span style="padding: 5px 5px 5px 20px;display: block">
    <p><fmt:message key="org.jahia.admin.users.ManageUsers.pleaseOk.label"/></p>
    </span>
<table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable" width="100%">
<tr>
    <td align="right">
        <fmt:message key="label.username"/>&nbsp;
    </td>
    <td>
        <!-- This hidden field is here when one will decide that a user name can be changed -->
        <input type="hidden" name="username" value="<%=username%>">
        <!-- This hidden field can be changed to 'update' so that we keep the edited data without confirming changes -->
        <input type="hidden" name="actionType" value="save"/>
        <b>${user:displayName(theUser)}
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
               <% if (isPropReadOnly(userProps, "j:firstName")) { %>disabled="true"<%}%>
               name='<%=ManageUsers.USER_PROPERTY_PREFIX+"j:firstName"%>'
               value='<%=getUserProp(userProps, "j:firstName")%>'/>
        <%} else {%>
        <b><%=getUserProp(userProps, "j:firstName")%>
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
               <% if (isPropReadOnly(userProps, "j:lastName")) { %>disabled="true"<%}%>
               name='<%=ManageUsers.USER_PROPERTY_PREFIX+"j:lastName"%>'
               value='<%=getUserProp(userProps, "j:lastName")%>'/>
        <%} else {%>
        <b><%=getUserProp(userProps, "j:lastName")%>
        </b>
        <%}%>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="label.email"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" size="40" maxlength="255"
               <% if (isPropReadOnly(userProps, "j:email")) { %>disabled="true"<%}%>
               name='<%=ManageUsers.USER_PROPERTY_PREFIX+"j:email"%>'
               value='<%=getUserProp(userProps, "j:email")%>'/>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="org.jahia.admin.organization.label"/>&nbsp;
    </td>
    <td>
        <input class="input" type="text" size="40" maxlength="255"
               <% if (isPropReadOnly(userProps, "j:organization")) { %>disabled="true"<%}%>
               name='<%=ManageUsers.USER_PROPERTY_PREFIX+"j:organization"%>'
               value='<%=getUserProp(userProps, "j:organization")%>'/>
    </td>
</tr>
<tr>
    <td align="right">
        <label for="emailNotificationsDisabledView"><fmt:message key="label.emailNotifications"/>&nbsp;</label>
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
                propValue = UserPreferencesHelper.getPreferredLocale(jUser, jParams.getSite()).toString();
            }
        %>
        <select name='<%=ManageUsers.USER_PROPERTY_PREFIX + "preferredLanguage"%>'
                <% if (iReadOnly) {%>disabled="disabled"<% } %>>
            <%
                for (Locale theLocale : LanguageCodeConverters.getAvailableBundleLocalesSorted(jParams.getUILocale())) {%>
            <option value="<%=theLocale %>"
                    <% if (theLocale.toString().equals(propValue)) { %>selected="selected"<% } %>><%= WordUtils.capitalizeFully(theLocale.getDisplayName(jParams.getUILocale())) %>
            </option>
            <% } %>
        </select>
    </td>
</tr>
<c:if test="${not isSuperAdminProp}">
<tr>
    <td align="right">
        <label for="accountLockedView"><fmt:message key="label.accountLocked"/>&nbsp;</label>
    </td>
    <td>
        <%
            iReadOnly = isPropReadOnly(userProps, "j:accountLocked");
            propValue = getUserProp(userProps, "j:accountLocked");
        %>
        <input type="checkbox" class="input" id="accountLockedView" name="accountLockedView"
               value="true" <% if ("true".equals(propValue)) {%>checked="checked"<% } %>
               onclick="javascript:{document.getElementById('accountLocked').value= this.checked ? 'true' : 'false';}"
               <% if (iReadOnly) {%>disabled="disabled"<% } %>/>
        <input type="hidden" id="accountLocked"
               name='<%=ManageUsers.USER_PROPERTY_PREFIX+"j:accountLocked"%>'
               value='<%= "true".equals(propValue) ? "true" : "false" %>'/>
    </td>
</tr>
</c:if>
<% if (!JahiaPasswordPolicyService.getInstance().isPasswordReadOnly(jUser)) { %>
<tr>
    <td align="right">
        <fmt:message key="label.password"/>&nbsp;
    </td>
    <td>
        <input class="input" type="password" name="passwd"
               size="40" maxlength="255"
               value='<%=StringUtils.defaultString(passwd)%>'>
        &nbsp;<span class="text2"><fmt:message key="org.jahia.admin.users.ManageUsers.noChangeBlank.label"/></span>
    </td>
</tr>
<tr>
    <td align="right">
        <fmt:message key="label.comfirmPassword"/>&nbsp;
    </td>
    <td>
        <input class="input" type="password" name="passwdconfirm"
               size="40" maxlength="255"
               value='<%=StringUtils.defaultString(passwdConfirm)%>'>
        &nbsp;<span class="text2"><fmt:message key="org.jahia.admin.users.ManageUsers.noChangeBlank.label"/></span>
    </td>
</tr>
<% } %>
<%-- You can add your custom user properties here --%>

<tr>
    <td colspan="2" align="center">&nbsp;
        <%
            Set groups = (Set) request.getAttribute("groups");
            if (groups.size() > 0) {
                String[] textPattern = new String [] {"Name, 20", "SiteTitle, 15", "Properties, 20"};
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
        <a class="ico-cancel" href="<%=cancelURL%>"><fmt:message key="label.cancel"/></a>
    <% } else {
        String cancelURL = JahiaAdministration.composeActionURL(request, response, "displaymenu", "");
        if (isPopup) {
            cancelURL = "javascript:window.close();";
        }
    %>
        <a class="ico-cancel" href="<%=cancelURL%>"><fmt:message key="label.cancel"/></a>
    <% } %>
      </span>
     </span>
     <span class="dex-PushButton">
      <span class="first-child">
         <a class="ico-ok"
            href="#ok" onclick="showWorkInProgress(); document.mainForm.submit(); <%if(isPopup){%>closeWindowAndReloadParent();<%}%> return false;"><fmt:message key="label.ok"/></a>
      </span>
     </span>

</div>

</div>

