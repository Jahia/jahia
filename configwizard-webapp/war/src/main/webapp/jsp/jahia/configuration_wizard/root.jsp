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