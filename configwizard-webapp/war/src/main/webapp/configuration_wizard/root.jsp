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
    <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.superUserAdministratorSettings.label"/>
  </div>
</div>
<div id="pagebody">
   <%@ include file="error.inc" %>
  <table summary="org.jahia.bin.JahiaConfigurationWizard.superUserAdministratorSettings.label">
   
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
        <span>
          <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.root.adminUserName.label"/>
        </span>
      </td>
      <td headers="t2" class="t6">
        <input class="inputtype" type="text" name="user" value='<%=values.get("root_user")%>' size="<%=input%>" maxlength="250" />
      </td>
    </tr>
    <tr>
      <td headers="t1" class="t5">
        <span>
          <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.root.administratorPassword.label"/>
        </span>
      </td>
      <td headers="t2" class="t6">
        <input class="inputtype" type="password" onfocus="this.select();" name="pwd" value='<%=values.get("root_pwd")%>' size="<%=input%>" maxlength="250" />
      </td>
    </tr>
    <tr>
      <td headers="t1" class="t5">
        <span>
          <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.root.confirmAdministratorPassword.label"/>
        </span>
      </td>
      <td headers="t2" class="t6">
        <input class="inputtype" type="password" onfocus="this.select();" name="confirm" value='<%=values.get("root_confirm")%>' size="<%=input%>" maxlength="250" />
      </td>
    </tr>
    <tr>
      <td headers="t1" class="t5">
        <span>
          <fmt:message key="org.jahia.firstName.label"/>
        </span>
      </td>
      <td headers="t2" class="t6">
        <input class="inputtype" type="text" name="firstname" value='<%=values.get("root_firstname")%>' size="<%=input%>" maxlength="250" />
      </td>
    </tr>
    <tr>
      <td headers="t1" class="t5">
        <span>
          <fmt:message key="org.jahia.lastName.label"/>
        </span>
      </td>
      <td headers="t2" class="t6">
        <input class="inputtype" type="text" name="lastname" value='<%=values.get("root_lastname")%>' size="<%=input%>" maxlength="250" />
      </td>
    </tr>
    <tr>
      <td headers="t1" class="t5">
        <span>
          <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.root.emailAddressOptionnal.label"/>
        </span>
      </td>
      <td headers="t2" class="t6">
        <input class="inputtype" type="text" name="mail" value='<%=values.get("root_mail")%>' size="<%=input%>" maxlength="250" />
      </td>
    </tr>
  </table>
</div>
<%@ include file="buttons.inc" %>
<%@ include file="footer.inc" %>