<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.data.containers.JahiaContainer" %>
<%@ page import="org.jahia.data.fields.*" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.engines.validation.ValidationError" %>
<%@ page import="org.jahia.exceptions.JahiaException" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.engines.validation.EngineValidationHelper"%>
<%@ page import="org.jahia.params.ProcessingContext"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%!
private static String getIconClassName(final int type, final boolean small) {
  final String className;
  switch (type) {
    case FieldTypes.INTEGER:
        className = "number_type";
        break;

    case FieldTypes.SMALLTEXT:
        className = "small_type";
        break;

    case FieldTypes.BIGTEXT:
        className = "big_type";
        break;

    case FieldTypes.DATE:
        className = "date_type";
        break;

    case FieldTypes.PAGE:
        className = "page_type";
        break;

    case FieldTypes.FILE:
        className = "file_type";
        break;

    case FieldTypes.APPLICATION:
        className = "app_type";
        break;

    case FieldTypes.FLOAT:
        className = "number_type";
        break;

    case FieldTypes.BOOLEAN:
        className = "boolean_type";
        break;

    case FieldTypes.COLOR:
        className = "color_type";
        break;

    case FieldTypes.CATEGORY:
        className = "category_type";
        break;

    case FieldTypes.SMALLTEXT_SHARED_LANG:
        className = "small_shared_type";
        break;

    default:
        className = "undefined_type";
        break;
  }
  if (small)
      return className;
  else
      return className + "_big";
}
%>

<!-- Begin ContainerEdit.jsp -->
<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>
<jsp:useBean id="engineTitle" class="java.lang.String" scope="request"/>

<%
final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("jsp.jahia.engines.containeredit.containeredit");
final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
final String fieldForm = (String) engineMap.get(fieldsEditCallingEngineName + ".fieldForm");
final Map fieldForms = (Map) engineMap.get(fieldsEditCallingEngineName + ".fieldForms");
final String theScreen = (String) engineMap.get("screen");
final JahiaContainer theContainer = (JahiaContainer) engineMap.get("theContainer");
final FieldsEditHelper feh = (FieldsEditHelper) engineMap.get(fieldsEditCallingEngineName + "." + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);
final JahiaField theField = feh != null ? feh.getSelectedField() : null;
Integer fieldID = null;
if (theField != null) {
  fieldID = new Integer(theField.getID());
}

final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
final EngineValidationHelper evh = (EngineValidationHelper) engineMap.get(JahiaEngine.ENGINE_VALIDATION_HELPER);
final int pageDefID = jParams.getPage().getPageTemplateID();
final List fieldIDs = (List) engineMap.get(fieldsEditCallingEngineName + ".fieldIDs");
final boolean refreshMainPage = ("yes".equals(request.getParameter("refreshMainPage")));

List localeList = (List) request.getAttribute("localeList");
if (localeList == null) {
  localeList = new ArrayList();
  request.setAttribute("localeList", localeList);
}

Integer contextualContainerListId = (Integer)engineMap.get("contextualContainerListId");
if (contextualContainerListId==null){
  contextualContainerListId = new Integer(0);
}
final Map fieldErrors = new HashMap();
final boolean fieldAlone = theField != null && (theField.getType() == FieldTypes.BIGTEXT || theField.getType() == FieldTypes.PAGE || theField.getType() == FieldTypes.FILE || theField.getType() == FieldTypes.APPLICATION || theField.getType() == FieldTypes.CATEGORY);
%>
<div class="dex-TabPanelBottom">
<% if (fieldID != null) { %>
  <input type="hidden" name="editfid" value="<%=fieldID.intValue()%>"/>
  <input type="hidden" name="lastfid" value="<%=fieldID.intValue()%>"/>
<% } if (theContainer != null) { %>
  <input type="hidden" name="clistid" value="<%=theContainer.getListID()%>"/>
  <input type="hidden" name="cdefid" value="<%=theContainer.getctndefid()%>"/>
  <input type="hidden" name="cpid" value="<%=theContainer.getPageID()%>"/>
  <input type="hidden" name="contextualContainerListId"  value="<%=String.valueOf(contextualContainerListId.intValue())%>"/>
<% } %>
  <input type="hidden" name="cparentid" value="<%=engineMap.get("containerParentID")%>"/>
  <div class="tabContent">
    <%@ include file="containereditmenu.inc" %>
    <div id="content" class="fit w2">
      <%if(!fieldAlone){%>
      <div class="head">
         <div class="object-title"><%=engineTitle%></div>
      </div>
      <%}%>
      <%
      try {
        if (theField == null) {%>
          <p class="errorbold"><fmt:message key="org.jahia.engines.noFieldToEdit.label"/></p>
        <% } else { %>
          <% if (evh != null && evh.hasErrors()) { %>
            <p class="errorbold"><fmt:message key="org.jahia.engines.validation.errors.label"/></p>
            <%
            for (ValidationError ve : evh.getErrors()) {
              final String msg = ve.getMsgError();
              final Object obj = ve.getSource();
              JahiaField errorField;
              if (obj != null && (obj instanceof JahiaField)) {
                errorField = (JahiaField) obj;
                if (msg == null) {
                  fieldErrors.put(new Integer(errorField.getID()), "");
                } else {
                  fieldErrors.put(new Integer(errorField.getID()), msg);
                }
              }
            }
          }%>

          <input type="hidden" name="refreshMainPage" value="<%if (refreshMainPage){%>yes<%}else{%>no<%}%>"/>
          <% if (theField != null) { %>
            <input type="hidden" name="lastfname" value="<%=theField.getDefinition().getName()%>"/>
          <% } else { %>
            <input type="hidden" name="lastfname" value="">
          <% }%>

          <%if (fieldIDs.size() > 1) {%>
            <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
            <%
            final Iterator it = fieldIDs.iterator();
            while (it.hasNext()) {
              final Integer I = (Integer) it.next();
              final String aForm = (String) fieldForms.get(I);
              final JahiaField aField = feh.getField(I.intValue());
              %>
              <%@ include file="fieldedit.inc"%>
            <%}%>
            </table>
          <%} else {
            String aForm = fieldForm;
            JahiaField aField = theField;
            if(fieldAlone){%>
              <%@ include file="fieldedit_alone.inc"%>
            <%} else {%>
              <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
                <%@ include file="fieldedit.inc"%>
              </table>
            <%}%>
          <% } %>
        <% } %>
        <%
            } catch (JahiaException je) {
                logger.error("Exception in displaying field", je);
            }
        %>
        <% if (!engineMap.containsKey("focus")) { %>
        <script type="text/javascript">
            var fields = document.mainForm.elements;
            for (var i = 0; i < fields.length; i++) {
                if (fields[i].name != "goToId" && ! fields[i].disabled && fields[i].type == "text") {
                    //alert(fields[i].name);
                    fields[i].focus();
                    break;
                }
                //for (var i = 0; i <<!--%=fieldIDs.size()%-->; i++) {
                //var inputElem = document.getElementById("field_-" + (i + 1));
                //if (inputElem && ! inputElem.disabled) {
                //inputElem.focus();
                //break;
                //}
            }
        </script>
        <% } %>
    </div>
  </div>
</div>
<!-- End ContainerEdit.jsp -->