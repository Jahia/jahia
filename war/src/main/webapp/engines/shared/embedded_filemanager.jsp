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
<%@ page import="org.jahia.params.ParamBean"%>
<%@ page import="org.jahia.data.fields.*" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.data.containers.ContainerFacadeInterface" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ page import="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>

<internal:gwtImport module="org.jahia.ajax.gwt.module.filepicker.FilePicker" />
<c:set var="jahia.engines.gwtModuleIncluded" value="true" scope="request"/>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    String path = jParams.getParameter("select-file");
    if (path == null || path.equals("<empty>")) {
        path = "" ;
    }
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
    final FieldsEditHelper feh = (FieldsEditHelper) engineMap.get(fieldsEditCallingEngineName + "." + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);
    final JahiaField theField = feh != null ? feh.getSelectedField() : null;
    final ExtendedPropertyDefinition epd = theField.getDefinition().getPropertyDefinition();
    Map<String,String> options = epd.getSelectorOptions();
    String mimeTypes = options.get("mime");
    String filters = options.get("filters");
    if (mimeTypes == null) mimeTypes = "";
    if (filters == null) filters = "";

    boolean folder = options.containsKey("folder");
    String nodeType = folder ? "jnt:folder" : "jnt:file";

    final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }
%>
<utility:setBundle basename="JahiaInternalResources"/>
<div class="head">
  <table cellpadding="0" cellspacing="0" border="0" width="100%" class="object-title">
    <tr>
      <th width="100%">    
        <%=theField.getDefinition().getTitle(elh.getCurrentLocale()) %>
        <%      
        ContainerFacadeInterface jahiaContentContainerFacade;
        if (theField.getIsMetadata()) {
          jahiaContentContainerFacade = (ContainerFacadeInterface) engineMap.get("Metadata_Engine.ContentMetadataFacade");
        } else {
          jahiaContentContainerFacade = (ContainerFacadeInterface) engineMap.get("UpdateContainer_Engine.JahiaContentContainerFacade");
        }
        boolean applyChangeToAllLang = "true".equals(jParams.getParameter("apply_change_to_all_lang_" + theField.getID()));
        boolean allSameTitles = false;
        if (jParams.getSite().getLanguageSettings(true).size() > 1) {
            allSameTitles = feh.areValuesTheSameInAllActiveLanguages(theField, jahiaContentContainerFacade);
        }%>
      </th>
      <% if (jParams.getSite().getLanguageSettings(true).size() > 1 &&  ServicesRegistry.getInstance().getJahiaACLManagerService().hasWriteAccesOnAllLangs(jParams)) { %>
      <td nowrap="nowrap">
        <fmt:message key="org.jahia.applyToAllLanguages.label"/>&nbsp;:&nbsp;
      </td>
      <td>
        <% if (allSameTitles) { %>
        <a id="switchIcons_<%=theField.getID()%>" href="javascript:switchIcons('switchIcons_<%=theField.getID()%>', 'apply_change_to_all_lang_<%=theField.getID()%>');" title='<fmt:message key="org.jahia.applyToAllLanguages.label"/>' class="sharedLanguageYes">&nbsp;</a>
        <% } else { %>
        <a id="switchIcons_<%=theField.getID()%>" href="javascript:switchIcons('switchIcons_<%=theField.getID()%>', 'apply_change_to_all_lang_<%=theField.getID()%>');" title='<fmt:message key="org.jahia.applyToSingleLanguage.label"/>' class="sharedLanguageNo">&nbsp;</a>
        <% } %>
        <input id="apply_change_to_all_lang_<%=theField.getID()%>" type="hidden" name="apply_change_to_all_lang_<%=theField.getID()%>" value="<%=applyChangeToAllLang || allSameTitles %>"/>
      </td>
      <%}%>
    </tr>
  </table> 
</div>
<internal:fileManager startPath="<%=path%>" enginemode="true" nodeTypes="<%=nodeType%>" mimeTypes="<%=mimeTypes%>" filters="<%=filters%>" conf="filepicker" rootPath="files" />

