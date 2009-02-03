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