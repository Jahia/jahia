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