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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ include file="header.inc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/functions" prefix="functions" %>
<jsp:useBean id="input" class="java.lang.String" scope="request"/>
<script type="text/javascript">
<!--
function testSettings() {
    if (document.mainForm.host.value.length == 0) {
        <fmt:message key="org.jahia.admin.JahiaDisplayMessage.mailServer_mustSet.label" var="msg"/>
        alert("${functions:escapeJavaScript(msg)}");
        document.mainForm.host.focus();
    } else if (document.mainForm.to.value.length == 0) {
        <fmt:message key="org.jahia.admin.JahiaDisplayMessage.mailAdmin_mustSet.label" var="msg"/>
        alert("${functions:escapeJavaScript(msg)}");
        document.mainForm.to.focus();
    } else if (document.mainForm.from.value.length == 0) {
        <fmt:message key="org.jahia.admin.JahiaDisplayMessage.mailFrom_mustSet.label" var="msg"/>
        alert("${functions:escapeJavaScript(msg)}");
        document.mainForm.from.focus();
    } else {
        if (typeof workInProgressOverlay != 'undefined') {
            workInProgressOverlay.start();
        }
        jahia.request('${pageContext.request.contextPath}/installation', {onSuccess: testSettingsSuccess, onFailure: testSettingsFailure,
            parameters: {
                call: 'testEmail',
                host: document.mainForm.host.value,
                from: document.mainForm.from.value,
                to: document.mainForm.to.value
            }});
    }
}
function testSettingsSuccess(text, code, statusText) {
    if (typeof workInProgressOverlay != 'undefined') {
        workInProgressOverlay.stop();
    }
    if (code == 200) {
        <fmt:message key="org.jahia.admin.server.ManageServer.testSettings.success" var="msg"/>
        alert("${functions:escapeJavaScript(msg)}");
    } else if (code == 400) {
        alert(text);
    } else {
        <fmt:message key="org.jahia.admin.server.ManageServer.testSettings.failure" var="msg"/>
        alert("${functions:escapeJavaScript(msg)}" + "\n" + code + " " + statusText + "\n" + text);
    }
}
function testSettingsFailure(text, code, statusText) {
    if (workInProgressOverlay) {
        workInProgressOverlay.stop();
    }
    <fmt:message key="org.jahia.admin.server.ManageServer.testSettings.failure" var="msg"/>
    alert("${functions:escapeJavaScript(msg)}" + "\n'" + code + " " + statusText + "\n" + text);
}
//-->
</script>
<div class="head">
  <div class="object-title">
    <fmt:message key="org.jahia.mailSettings.label"/>
  </div>
</div>
<div id="pagebody">
   <%@ include file="error.inc" %>
  <p>
    <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.mail.theSettingsAreOptionnal.label"/>.
    <br/>
    <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.mail.specifyingThem.label"/>.
  </p>
  <table summary="<fmt:message key="org.jahia.mailSettings.label"/>">
    <caption>
      <fmt:message key="org.jahia.mailSettings.label"/>&nbsp;<a href="http://jira.jahia.org/browse/JKB-17" target="_blank"><img src="${pageContext.request.contextPath}/configuration_wizard/images/about.gif" alt="info"/></a>:
    </caption>
    <tr>
      <th id="t1">
        <span>
          Parameter
        </span>
      </th>
      <th id="t2">
        <span>
          Value
        </span>
      </th>
    </tr>
    <tr>
      <td headers="t1" class="t5">
        <fmt:message key="org.jahia.admin.server.ManageServer.mailServer.label"/>
      </td>
      <td headers="t2" class="t6">
        <input class="inputtype" type="text" name="host" value='<%=values.get("mail_server")%>' size="<%=input%>" maxlength="250" />
      </td>
    </tr>
    <tr>
      <td headers="t1" class="t5">
        <fmt:message key="org.jahia.admin.server.ManageServer.mailAdministrator.label"/>
      </td>
      <td headers="t2" class="t6">
        <input class="inputtype" type="text" name="to" value='<%=values.get("mail_recipient")%>' size="<%=input%>" maxlength="250" />
      </td>
    </tr>
    <tr>
      <td headers="t1" class="t5">
        <fmt:message key="org.jahia.admin.server.ManageServer.mailFrom.label"/>
      </td>
      <td headers="t2" class="t6">
        <input class="inputtype" type="text" name="from" value='<%=values.get("mail_from")%>' size="<%=input%>" maxlength="250" />
      </td>
    </tr>
    <tr>
      <td headers="t1" class="t5">
        <fmt:message key="org.jahia.admin.server.ManageServer.eventNotificationLevel.label"/>
      </td>
      <td headers="t2" class="t6">
        <select class="choix" name="notificationLevel">
        <option value="Disabled"<% if ((values.get("mail_parano")).equals("Disabled")) { %> selected="selected"<%} %>><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.mail.disabled.label"/></option><option value="Standard"<% if ((values.get("mail_parano")).equals("Standard")) { %> selected="selected"<%} %>><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.mail.standard.label"/></option><option value="Wary"<% if ((values.get("mail_parano")).equals("Wary")) { %> selected="selected"<%} %>><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.mail.wary.label"/></option><option value="Paranoid"<% if ((values.get("mail_parano")).equals("Paranoid")) { %> selected="selected"<%} %>><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.mail.paranoid.label"/></option>
        </select>
      </td>
    </tr>
    <tr>
      <td headers="t1" colspan="2" style="text-align: right">
        <span class="dex-PushButton">
            <span class="first-child">
                <a class="ico-mail-test" href="#" onclick="testSettings(); return false;"><fmt:message key="org.jahia.admin.server.ManageServer.testSettings.label"/></a>
            </span>
        </span>
      </td>
    </tr>
</table>
</div>
<%@ include file="buttons.inc" %>
<%@ include file="footer.inc" %>