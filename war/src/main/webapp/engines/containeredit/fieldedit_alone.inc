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
}%>

<%=aForm%>
<% if (errorMsg != null && errorMsg.length() > 0) { %>
  <br/>&nbsp;<br/>&nbsp;
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

<!-- End fieldedit.jsp -->