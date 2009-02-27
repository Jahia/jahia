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

<%@ page import="java.util.*" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.data.fields.JahiaAllowApplyChangeToAllLangField" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.data.fields.FieldTypes"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
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

<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>

<%
    final Map<String, Object> engineMap = (Map<String, Object>) request.getAttribute("org.jahia.engines.EngineHashMap");
    final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
    final String fieldForm = (String) engineMap.get(fieldsEditCallingEngineName + ".fieldForm");
    final String logForm = (String) engineMap.get("logForm");
    final String versioningForm = (String) engineMap.get("versioningForm");
    final String engineUrl = (String) engineMap.get("engineUrl");
    final String theScreen = (String) engineMap.get("screen");
    final JahiaField theField = (JahiaField) engineMap.get("theField");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final boolean showEditMenu = (theScreen.equals("edit") || theScreen.equals("metadata") ||
            theScreen.equals("rightsMgmt") || theScreen.equals("timeBasedPublishing") ||
            theScreen.equals("ctneditview_rights"));
    request.setAttribute("showEditMenu", new Boolean(showEditMenu));

    List<Locale> localeList = (List<Locale>) request.getAttribute("localeList");
    if (localeList == null) {
        localeList = new ArrayList<Locale>();
        request.setAttribute("localeList", localeList);
    }

%>
<!-- Begin updateField.jsp -->
<h3 class="field">
    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.updatefiled.UpdateField_Engine.updateField.label"/>
    <span>
        [ID = <%=theField.getID()%>]
    </span>
</h3>

<%
    if (theField.getPageID() != jParams.getPageID()) {
%>
<p class="error">
    <utility:resourceBundle resourceBundle="JahiaInternalResources"
            resourceName="org.jahia.engines.updatefiled.UpdateField_Engine.rememberValidate.label"/>
    <%=theField.getPageID()%>
    <utility:resourceBundle resourceBundle="JahiaInternalResources"
            resourceName="org.jahia.engines.updatefiled.UpdateField_Engine.toPublishContent.label"/>
</p>
<% } %>

<!-- Langs -->
<jsp:include page="../multilanguage_links.jsp" flush="true" />
<!-- End Langs -->

<!-- Buttons -->
<jsp:include page="../buttons.jsp" flush="true" />
<!-- End Buttons -->

<!-- Menubar -->
<jsp:include page="../menuBar.jsp" flush="true" />
<!-- End Menubar -->

<% if (theScreen.equals("metadata")) { %>
<jsp:include page="../containeredit/containeredit.jsp" flush="true"/>
<% } else { %>

<% if (theScreen.equals("logs")) { %>
<%=logForm%>
<% } else if (theScreen.equals("versioning")) { %>
<%=versioningForm%>
<% } else if (theScreen.equals("edit")) { %>

<div class="menuwrapper">
    <%@ include file="../menu.inc" %>
    <div class="content">
        <div id="editor">
            <h4 class="<%=getIconClassName(theField.getType(), false)%>"><utility:resourceBundle resourceBundle="JahiaInternalResources"
                resourceName="org.jahia.engines.updatefiled.UpdateField_Engine.fieldName.label"/>:
                <%=theField.getDefinition().getTitle(jParams.getLocale())%>
            </h4>
            <%
                boolean applyChangeToAllLang = (request.getParameter("apply_change_to_all_lang_" + theField.getID()) != null);
                if (theField instanceof JahiaAllowApplyChangeToAllLangField) { %>
            <% if (localeList.size() > 1) { %> <br/>
            <input type="checkbox" name="apply_change_to_all_lang_<%=theField.getID()%>"
                          value="<%=theField.getID()%>"
            <% if (theField.isShared()) { %>
                          disabled="disabled" checked="checked"
            <% } else if (applyChangeToAllLang) {%>
                          checked="checked"<% } %> >&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.applyToAllLanguages.label"/>
            <% } %>
            <% } %>
            <%=fieldForm%>
        </div>
    </div>
    <div class="clearing">&nbsp;</div>
</div>
<% } else if (! theScreen.equals("notools")) {%>
    <%=fieldForm%>
<% } %>
<% } %>

 <script type="text/javascript">
     function setfocus() {
        if (document.mainForm.elements && document.mainForm.elements["_<%=theField.getID()%>"]) {
            document.mainForm.elements["_<%=theField.getID()%>"].select();
        }
    }
    setfocus();
    // Edit In Word fix, so the popup does not close itself
    function checkParent() {}
 </script>
 <!-- End updateField.jsp -->