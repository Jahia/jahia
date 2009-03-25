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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ include file="header.inc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:useBean id="input" class="java.lang.String" scope="request"/>
<script type="text/javascript">
<!--
function testSettings() {
    if (document.mainForm.host.value.length == 0) {
        alert("<fmt:message key='org.jahia.admin.JahiaDisplayMessage.mailServer_mustSet.label'/>");
        document.mainForm.host.focus();
    } else if (document.mainForm.to.value.length == 0) {
        alert("<fmt:message key='org.jahia.admin.JahiaDisplayMessage.mailAdmin_mustSet.label'/>");
        document.mainForm.to.focus();
    } else if (document.mainForm.from.value.length == 0) {
        alert('<fmt:message key="org.jahia.admin.JahiaDisplayMessage.mailFrom_mustSet.label"/>');
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
        alert("<fmt:message key='org.jahia.admin.server.ManageServer.testSettings.success'/>");
    } else if (code == 400) {
        alert(text);
    } else {
        alert("<fmt:message key='org.jahia.admin.server.ManageServer.testSettings.failure'/> " + "\n" + code + " " + statusText + "\n" + text);
    }
}
function testSettingsFailure(text, code, statusText) {
    if (workInProgressOverlay) {
        workInProgressOverlay.stop();
    }
    alert("<fmt:message key='org.jahia.admin.server.ManageServer.testSettings.failure'/> " + "\n'" + code + " " + statusText + "\n" + text);
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
      <fmt:message key="org.jahia.mailSettings.label"/>:
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