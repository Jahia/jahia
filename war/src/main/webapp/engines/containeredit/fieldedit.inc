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
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle" %>
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