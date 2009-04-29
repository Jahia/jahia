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
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>

<%@ page language="java" %>
<%@ page import="org.jahia.data.fields.JahiaDateFieldUtil" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="org.jahia.data.fields.JahiaFieldDefinition" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="java.util.*" %>
<%@page import="org.jahia.data.fields.ExpressionMarker"%>
<%@page import="org.jahia.utils.i18n.ResourceBundleMarker"%>
<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final JahiaField theField = (JahiaField) engineMap.get(engineMap.get("fieldsEditCallingEngineName") + ".theField");
    final JahiaFieldDefinition def = theField.getDefinition();

    final String format = JahiaDateFieldUtil.getDateFormat(def.getDefaultValue(
    ), jParams.getLocale()).getPattern();

    final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }
    
    String val;
    if (theField.getRawValue().startsWith("<jahia-expression")) {
        val = ExpressionMarker.getValue(theField.getRawValue(), jParams);
    } else if (theField.getRawValue().startsWith("<jahia-resource")) {
        val = ResourceBundleMarker.getValue(theField.getRawValue(), jParams.getLocale());
    } else {
        val = theField.getValue();
    }    
%>

<script type="text/javascript">
    function check() {
        return true;
    }
</script>

<% final boolean readOnly = def.getItemDefinition().isProtected(); %>

<ui:dateSelector fieldName='<%= "_Str" + theField.getID() %>'
                           value='<%= val != null ? val : "" %>'
                           datePattern='<%= format %>'
                           displayTime='true'
                           templateUsage='false'
                           readOnly='<%= readOnly %>'/>
<%=format%>