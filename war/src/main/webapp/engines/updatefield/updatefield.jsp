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
<%@ page import="java.util.*" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.data.fields.JahiaAllowApplyChangeToAllLangField" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.data.fields.FieldTypes"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
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
    <fmt:message key="org.jahia.engines.updatefiled.UpdateField_Engine.updateField.label"/>
    <span>
        [ID = <%=theField.getID()%>]
    </span>
</h3>

<%
    if (theField.getPageID() != jParams.getPageID()) {
%>
<p class="error">
    <fmt:message key="org.jahia.engines.updatefiled.UpdateField_Engine.rememberValidate.label"/>
    <%=theField.getPageID()%>
    <fmt:message key="org.jahia.engines.updatefiled.UpdateField_Engine.toPublishContent.label"/>
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
            <h4 class="<%=getIconClassName(theField.getType(), false)%>"><fmt:message key="org.jahia.engines.updatefiled.UpdateField_Engine.fieldName.label"/>:
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
                          checked="checked"<% } %> >&nbsp;<fmt:message key="org.jahia.applyToAllLanguages.label"/>
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