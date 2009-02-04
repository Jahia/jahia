<%--
Copyright 2002-2008 Jahia Ltd

Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
Version 1.0 (the "License"), or (at your option) any later version; you may 
not use this file except in compliance with the License. You should have 
received a copy of the License along with this program; if not, you may obtain 
a copy of the License at 

 http://www.jahia.org/license/

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ include file="header.inc" %>
<jsp:useBean id="input" class="java.lang.String" scope="request"/>
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
    <fmt:bundle basename="JahiaAdministrationResources">
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
    </fmt:bundle>
      <td headers="t2" class="t6">
        <select class="choix" name="notificationLevel">
        <option value="Disabled"<% if ((values.get("mail_parano")).equals("Disabled")) { %> selected="selected"<%} %>><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.mail.disabled.label"/></option><option value="Standard"<% if ((values.get("mail_parano")).equals("Standard")) { %> selected="selected"<%} %>><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.mail.standard.label"/></option><option value="Wary"<% if ((values.get("mail_parano")).equals("Wary")) { %> selected="selected"<%} %>><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.mail.wary.label"/></option><option value="Paranoid"<% if ((values.get("mail_parano")).equals("Paranoid")) { %> selected="selected"<%} %>><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.mail.paranoid.label"/></option>
        </select>
      </td>
    </tr>
</table>
</div>
<%@ include file="buttons.inc" %>
<%@ include file="footer.inc" %>