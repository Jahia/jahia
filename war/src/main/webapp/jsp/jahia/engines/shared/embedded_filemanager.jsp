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

<internal:gwtImport module="org.jahia.ajax.gwt.subengines.filepicker.FilePicker" />
<c:set var="jahia.engines.gwtModuleIncluded" value="true" scope="request"/>

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
    final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }
%>

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
        <internal:engineResourceBundle resourceName="org.jahia.applyToAllLanguages.label"/>&nbsp;:&nbsp;
      </td>
      <td>
        <% if (allSameTitles) { %>
        <a id="switchIcons_<%=theField.getID()%>" href="javascript:switchIcons('switchIcons_<%=theField.getID()%>', 'apply_change_to_all_lang_<%=theField.getID()%>');" title='<internal:engineResourceBundle resourceName="org.jahia.applyToAllLanguages.label"/>' class="sharedLanguageYes">&nbsp;</a>
        <% } else { %>
        <a id="switchIcons_<%=theField.getID()%>" href="javascript:switchIcons('switchIcons_<%=theField.getID()%>', 'apply_change_to_all_lang_<%=theField.getID()%>');" title='<internal:engineResourceBundle resourceName="org.jahia.applyToSingleLanguage.label"/>' class="sharedLanguageNo">&nbsp;</a>
        <% } %>
        <input id="apply_change_to_all_lang_<%=theField.getID()%>" type="hidden" name="apply_change_to_all_lang_<%=theField.getID()%>" value="<%=applyChangeToAllLang || allSameTitles %>"/>
      </td>
      <%}%>
    </tr>
  </table> 
</div>
<internal:fileManager startPath="<%=path%>" enginemode="true" nodeTypes="jnt:file" mimeTypes="<%=mimeTypes%>" filters="<%=filters%>" conf="filepicker" rootPath="files" />

