<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ include file="header.inc" %>
<jsp:useBean id="input" class="java.lang.String" scope="request"/>
<div class="head">
  <div class="object-title">
    <fmt:message key="org.jahia.serverSettings.label"/>
  </div>
</div>
<div id="pagebody">
   <%@ include file="error.inc" %>
  <table summary="<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.server.basicSettings.label"/>">
    <caption>
      <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.server.basicSettings.label"/>:
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
      <td  class="t3">
        <span>
          <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.server.servletContainerHomeDiskPath.label"/>
        </span>
      </td>
      <td headers="t2" class="t6">
        <input class="inputtype" type="text" name="home" value='<%=values.get("server_home")%>' size="<%=input%>" maxlength="250" />
      </td>
    </tr>
    <tr>
      <td colspan="2" class="t7">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.server.pathAutomaticallyDetected.label"/>
      </td>
    </tr>
  </table>
  <table summary="<fmt:message key="org.jahia.advancedSettings.label"/>">
    <caption>
      <fmt:message key="org.jahia.advancedSettings.label"/>(<fmt:message key="org.jahia.bin.JahiaConfigurationWizard.server.forExperiencedUsersOnly.label"/>):
    </caption>
    <tr>
      <th id="t3">
        <span>
          Parameter
        </span>
      </th>
      <th id="t4">
        <span>
          Value
        </span>
      </th>
    </tr>
    <tr>
      <td headers="t3" class="t5">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.server.currentlySelectedDatabase.label"/>:&nbsp;
      </td>
      <td headers="t4" class="t6">
        <%=values.get("database_script") %>
      </td>
    </tr>
    <tr>
      <td headers="t3" class="t5">
        <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.server.toUseAnotherDatabase.label"/>
      </td>
      <td headers="t4" class="t6">
        <div class="button">
          <a href="javascript:submitFormular('server_process','advanced');" title="<fmt:message key="org.jahia.advancedSettings.label" />"><fmt:message key="org.jahia.advancedSettings.label"/></a>
        </div>
      </td>
    </tr>
  </table>
  <input type="hidden" name="hosturl" value='<%=values.get("server_url")%>' size="<%=input%>" maxlength="250" /><input type="hidden" name="webappsdeploybaseurl" value='<%=values.get("webapps_deploybaseurl")%>' size="<%=input%>" maxlength="250" /><input type="hidden" name="jahiafiles" value='<%=values.get("server_jahiafiles")%>' size="<%=input%>" maxlength="250" />
</div>
<%@ include file="buttons.inc" %>
<%@ include file="footer.inc" %>