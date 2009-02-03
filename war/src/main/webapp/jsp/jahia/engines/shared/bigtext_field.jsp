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

<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="org.jahia.engines.shared.BigText_Field" %>
<%@ page import="org.jahia.engines.shared.HtmlEditorsViewHelper" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.services.htmleditors.HtmlEditor" %>
<%@ page import="org.jahia.services.htmleditors.HtmlEditorCSS" %>
<%@ page import="org.jahia.services.sites.JahiaSite" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.data.fields.*" %>
<%@ page import="java.util.regex.Pattern"%>
<%@ page import="org.jahia.services.pages.JahiaPageDefinition"%>
<%@ page import="org.jahia.services.pages.ContentPage"%>
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ page import="org.jahia.data.fields.JahiaFieldDefinitionProperties"%>
<%@ page import="org.jahia.data.containers.ContainerFacadeInterface" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<internal:i18n/>
<%!
    private static final Pattern siteNamePattern = Pattern.compile("%SITE_NAME%");
%>
<%
final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
final String engineUrl = (String) engineMap.get("engineUrl");
final String theScreen = (String) engineMap.get("screen");
//final boolean ignoreALl = ((Boolean) request.getAttribute("ignoreAllWarnings")).booleanValue();
//final String checked = (ignoreALl) ? "checked=\"checked\"" : "";

final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");

final FieldsEditHelper feh = (FieldsEditHelper) engineMap.get(fieldsEditCallingEngineName + "." + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);
final JahiaField theField = feh != null ? feh.getSelectedField() : null;
final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
if (elh != null) {
    jParams.setCurrentLocale(elh.getCurrentLocale());
}
final HtmlEditorsViewHelper heViewHelper = (HtmlEditorsViewHelper) engineMap.get(BigText_Field.HTMLEDITOR_VIEW_HELPER_ATTRIBUTE);
HtmlEditor defaultEditor = heViewHelper.getDefaultEditor(true);
final List editors = new ArrayList();


//    final boolean isSafariOrOpera = request.getHeader("User-Agent") != null
//        && (request.getHeader("User-Agent").indexOf("Safari") != -1 || request
//                .getHeader("User-Agent").indexOf("Opera") != -1);

final Iterator editorsEnum = heViewHelper.getEditors();
while (editorsEnum.hasNext()) {
  final HtmlEditor editor = (HtmlEditor) editorsEnum.next();

  if (null == defaultEditor) {
      defaultEditor = editor;
  }

        // do not show FCKeditor on Safari and Opera
/*
        if (isSafariOrOpera && "FCKEditor".equals(editor.getId())) {
            if ("FCKEditor".equals(defaultEditor.getId())) {
                defaultEditor = null;
                }
            continue;
        }
*/
  editors.add(editor);
}

final Map cssList;
if (defaultEditor != null && defaultEditor.enableCSS()) {
  cssList = heViewHelper.getEnabledCSSs();
} else {
  cssList = new HashMap();
}
if (cssList.size() > 0) {
    String cssId = theField.getDefinition().getProperty(JahiaFieldDefinitionProperties.FIELD_STYLESHEET_ID_PROP);
    HtmlEditorCSS htmlEditorCSS = null;
    if (cssId != null && cssId.length() > 0) {
      htmlEditorCSS = (HtmlEditorCSS)cssList.get(cssId);
    }
    if (htmlEditorCSS == null) {
      htmlEditorCSS = (HtmlEditorCSS)cssList.get(heViewHelper.getDefaultCSSID());
    }

    if (htmlEditorCSS == null) {
      final int pageID = (theField.getPageID() > 0) ? theField.getPageID() : jParams.getPageID();
      final ContentPage thePage = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(pageID, true);
      final JahiaPageDefinition pageTemplate = thePage.getPageTemplate(jParams);
      final String templateSourcePath = pageTemplate != null ? pageTemplate.getSourcePath() : "";

      Iterator cssListEnum = cssList.values().iterator();
      while (cssListEnum.hasNext() && htmlEditorCSS == null) {
        HtmlEditorCSS editorCSS = (HtmlEditorCSS) cssListEnum.next();
        if (editorCSS.isSiteAllowed(jParams.getSiteKey()) && editorCSS.isTemplateAllowed(templateSourcePath)){
          htmlEditorCSS = editorCSS;
        }
      }
    }
    if (htmlEditorCSS != null) {
      String htmlEditorCSSUrl = htmlEditorCSS.getURL();
      String htmlEditorCSSDef = htmlEditorCSS.getStylesDef();
      final int siteWildcardIndex = htmlEditorCSSUrl.indexOf("%SITE_NAME%");
      if (siteWildcardIndex != -1) {
          htmlEditorCSSUrl = siteNamePattern.matcher(
              htmlEditorCSSUrl).replaceAll(jParams.getSiteKey());
          htmlEditorCSSDef = siteNamePattern.matcher(
              htmlEditorCSSDef).replaceAll(jParams.getSiteKey());
      }
      engineMap.put("htmlEditorCSS", htmlEditorCSS);
      engineMap.put("htmlEditorCSSUrl", htmlEditorCSSUrl);
      engineMap.put("htmlEditorCSSDef", htmlEditorCSSDef);
    }
}

String theOldField = theField.getValue();
String theNewField;
final String strToRemove[] = {"&lt;jahia", "_htmleditor>", "&lt;empty>"};

for (int i = 0; i < strToRemove.length; i++) {
  final String lowerCaseField = theOldField.toLowerCase();
  final int index = lowerCaseField.indexOf(strToRemove[i]);
  if (index != -1) {
    theNewField = theOldField.substring(0, index) + theOldField.substring(index + strToRemove[i].length(), theOldField.length());
    theOldField = theNewField;
  }
}
%>

<script type="text/javascript">
<!--//
  function getHtmlEditorText(jahiaTextHiddenInput) {
    // Default implementation : do nothing
    // Must be handle correctly by Html Editors include files
    // alert("getHtmlEditorText.doNothing");
  }

  function saveContent() {
    // alert("saveContent Started");
    getHtmlEditorText(document.mainForm.elements["_<%=theField.getID()%>"]);
    if (workInProgressOverlay) workInProgressOverlay.launch();
  }

  function changeHtmlEditor(what) {
    saveContent();
    // alert("changeHtmlEditor : " + document.mainForm.elements["_<%=theField.getID()%>"].value);
    document.mainForm.method = "post";
    document.mainForm.action = "<%=engineUrl%>";
    document.mainForm.screen.value = what;
    teleportCaptainFlam(document.mainForm);
  }

  function changeCSS(what) {
    saveContent();
    document.mainForm.method = "post";
    document.mainForm.action = "<%=engineUrl%>";
    document.mainForm.screen.value = what;
    teleportCaptainFlam(document.mainForm);
  }
//-->
</script>
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
<div class="content-body">
  <div id="operationMenu">
    <select name="htmlEditor" onchange="changeHtmlEditor('<%=theScreen%>');">
      <%for (Iterator it = editors.iterator(); it.hasNext();) {
          final HtmlEditor editor = (HtmlEditor) it.next();%>
      <option value="<%=editor.getId()%>"<% if (defaultEditor != null && editor.getId().equals(defaultEditor.getId())) {%> selected="selected"<%}%>>
        <%=editor.getDisplayName()%>
      </option>
      <%}%>
    </select>
  </div>
</div>
<logic:present name='<%=theField.getID()+".engineMessages"%>'>
  <p class="errorbold">
    <internal:engineResourceBundle resourceName="org.jahia.engines.shared.BigText_Field.error.label"/>:
  </p>
  <ul>
    <logic:iterate name='<%=theField.getID()+".engineMessages"%>' property="messages" id="curMessage">
      <li><internal:message name="curMessage"/></li>
    </logic:iterate>
  </ul>
</logic:present>
<% boolean displayIgnoreCheckBox = false; %>
<logic:present name='<%=theField.getID()+".warning.engineMessages"%>'>
  <% displayIgnoreCheckBox = true;
    pageContext.setAttribute("warnings", request.getAttribute(theField.getID()+".warning.engineMessages")); 
  %>
  <p>
    <span class="errorbold">${warnings.size}&nbsp;<internal:engineResourceBundle resourceName="org.jahia.engines.shared.BigText_Field.warning.label"/>:</span>&nbsp;&nbsp;
    <br>
    <ul>
      <logic:iterate name='<%=theField.getID()+".warning.engineMessages"%>' property="messages" id="curMessage">
        <li><internal:message name="curMessage"/></li>
      </logic:iterate>
    </ul>
  </p>
</logic:present>
<logic:present name='<%=theField.getID()+".WAIwarning.engineMessages"%>'>
  <% displayIgnoreCheckBox = true;
    pageContext.setAttribute("warnings", request.getAttribute(theField.getID()+".WAIwarning.engineMessages")); 
  %>
  <p>
    <span class="errorbold">${warnings.size}&nbsp;<internal:engineResourceBundle resourceName="org.jahia.engines.shared.BigText_Field.WAIwarning.label"/>:</span>
    <br>
    <ul>
      <logic:iterate name='<%=theField.getID()+".WAIwarning.engineMessages"%>' property="messages" id="curMessage">
        <li><internal:message name="curMessage"/></li>
      </logic:iterate>
    </ul>
  </p>
</logic:present>
<%
if (displayIgnoreCheckBox) {
  final JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSite(jParams.getSiteID());
  if (site.isURLIntegrityCheckEnabled() || site.isWAIComplianceCheckEnabled()) { %>
    <input type="checkbox" name="ignoreAllWarnings" id="ignoreAllWarnings"/>&nbsp;<label for="ignoreAllWarnings"><internal:engineResourceBundle resourceName="org.jahia.engines.shared.BigText_Field.ignoreAllWarning.label"/></label>
  <% } %>
<% } %>
<% if (defaultEditor == null) {
  final String simpleTextEditor = jParams.settings().getHtmlEditorsContext() + "/simpletext/simpletext_htmleditor.jsp";
  %>
  <jsp:include page="<%=simpleTextEditor%>" flush="true"/>
<% } else {
  final StringBuffer buff = new StringBuffer();
  buff.append(jParams.settings().getHtmlEditorsContext()).append("/").append(defaultEditor.getBaseDirectory()).append("/").append(defaultEditor.getIncludeFile());
  %>
  <jsp:include page="<%=buff.toString()%>" flush="true"/>
<% } %>
