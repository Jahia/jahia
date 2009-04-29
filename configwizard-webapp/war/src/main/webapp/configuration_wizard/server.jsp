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