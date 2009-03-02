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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.
--%>
<%@ page import="org.jahia.data.containers.ContainerFacadeInterface" %>
<%@ page import="org.jahia.data.containers.JahiaContainer" %>
<%@ page import="org.jahia.data.containers.JahiaContainerList"%>
<%@ page import="org.jahia.data.containers.JahiaContentContainerFacade" %>
<%@ page import="org.jahia.data.fields.*" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.engines.validation.EngineValidationHelper" %>
<%@ page import="org.jahia.engines.validation.ValidationError" %>
<%@ page import="org.jahia.exceptions.JahiaException" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.resourcebundle.JahiaResourceBundle" %>
<%@ page import="org.jahia.services.lock.LockPrerequisites" %>
<%@ page import="org.jahia.services.pages.JahiaPage" %>
<%@ page import="org.jahia.services.pages.JahiaPageDefinition" %>
<%@ page import="org.jahia.utils.JahiaTools" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.*" %>
<!-- Begin fieldedit.jsp -->

<tr>
  <th width="150">
    <%
    ContainerFacadeInterface jahiaContentContainerFacade;
    if (theField.getIsMetadata()) {
      jahiaContentContainerFacade = (ContainerFacadeInterface) engineMap.get("Metadata_Engine.ContentMetadataFacade");
    } else {
      jahiaContentContainerFacade = (ContainerFacadeInterface) engineMap.get("UpdateContainer_Engine.JahiaContentContainerFacade");
    }
    boolean applyChangeToAllLang = "true".equals(jParams.getParameter("apply_change_to_all_lang_" + aField.getID()));
    String errorMsg = (String) fieldErrors.get(new Integer(aField.getID()));
    if (errorMsg == null) {
      errorMsg = "";
    }
    if (aField instanceof JahiaAllowApplyChangeToAllLangField) { %>
      <script type="text/javascript">
        function switchIcons(callingElementID, inputElementID) {
          var element = document.getElementById(inputElementID);
          var callingElement = document.getElementById(callingElementID);
          if (element) {
            var theValue = element.value;
            if (theValue == "true") {
              theValue = "false";
              callingElement.className = "sharedLanguageNo";
              callingElement.title = '<fmt:message key="org.jahia.applyToSingleLanguage.label"/>';
            } else {
              theValue = "true";
              callingElement.className = "sharedLanguageYes";
              callingElement.title = '<fmt:message key="org.jahia.applyToAllLanguages.label"/>';
            }
            element.value = theValue;
          }
        }
      </script>
      <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
          <td width="100%">
            <label class="<%=getIconClassName(aField.getType(), false) %>">
              <%
              boolean allSameTitles = false;
              if (localeList.size() > 1) {
                allSameTitles = feh.areValuesTheSameInAllActiveLanguages(aField, jahiaContentContainerFacade);
              }
              %>
              <%=aField.getDefinition().getTitle(elh.getCurrentLocale()) %>
              <% if (EngineValidationHelper.isFieldMandatory(theContainer, aField, jParams)) { %>
                <span class="errorbold">(*)</span>
              <% } %>
            </label>
          </td>
          <td>
            <input id="apply_change_to_all_lang_<%=aField.getID()%>" type="hidden" name="apply_change_to_all_lang_<%=aField.getID()%>" value="<%=applyChangeToAllLang || allSameTitles %>"/>
          </td>
          <td class="sharedLanguage">
            <%if (localeList.size() > 1 && !readOnlyMode && !aField.isShared() && ServicesRegistry.getInstance().getJahiaACLManagerService().hasWriteAccesOnAllLangs(jParams)) { %>
              <% if (allSameTitles) { %>
                <a id="switchIcons_<%=aField.getID()%>" href="javascript:switchIcons('switchIcons_<%=aField.getID()%>', 'apply_change_to_all_lang_<%=aField.getID()%>');" title='<fmt:message key="org.jahia.applyToAllLanguages.label"/>' class="sharedLanguageYes">&nbsp;</a>
              <% } else { %>
                <a id="switchIcons_<%=aField.getID()%>" href="javascript:switchIcons('switchIcons_<%=aField.getID()%>', 'apply_change_to_all_lang_<%=aField.getID()%>');" title='<fmt:message key="org.jahia.applyToSingleLanguage.label"/>' class="sharedLanguageNo">&nbsp;</a>
              <% } %>
            <% } %>
          </td>
        </tr>
       </table>
    <% } %>
  </th>
  <td>
    <% if (errorMsg != null && errorMsg.length() > 0) { %>
        <span class="error"><%=errorMsg%></span><br/>
    <% } %>
    <%=aForm%>
    <% if (errorMsg != null && errorMsg.length() > 0) { %>
      <script type="text/javascript">
        var elem = document.getElementById("errorMsg");
        if (elem) {
          elem.innerHTML = "<span class=\"error\"><%=errorMsg%></span><br/>";
          var goElem = document.getElementById("go");
          if (goElem) {
            goElem.value = "close";
          }
        }
      </script>
    <% } %>
  </td>
</tr>
<!-- End fieldedit.jsp -->