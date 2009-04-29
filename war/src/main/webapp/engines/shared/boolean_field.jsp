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
<%@ page language="java" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="org.jahia.utils.JahiaTools" %>
<%@ page import="java.util.*" %>
<%@page import="org.jahia.data.fields.ExpressionMarker"%>
<%@page import="org.jahia.utils.i18n.ResourceBundleMarker"%>
<%@page import="org.jahia.params.ProcessingContext"%>
<%@page import="org.jahia.data.JahiaData"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");

    final JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + "." + "theField");
    final String contextID = JahiaTools.replacePattern(String.valueOf(theField.getID()), "-", "_");
    
    final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
    final ProcessingContext jParams = (jData != null) ? jData.getProcessingContext() : null;

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
    function changeBoolean_<%=contextID%>(what) {
        if (document.mainForm.elements["_<%=theField.getID()%>checkBoolean"].checked) {
            what.value = "true";
        } else {
            what.value = "false";
        }
    }
</script>

<input type="checkbox" name="_<%=theField.getID()%>checkBoolean" id="_<%=theField.getID()%>checkBoolean"
       onclick="changeBoolean_<%=contextID%>(document.mainForm.elements['_<%=theField.getID()%>']);"
<%

if (val.equals("true")) { %>
       checked="checked"
<% } %>>&nbsp;<label for="_<%=theField.getID()%>checkBoolean"><fmt:message key="org.jahia.engines.shared.Boolean_Field.checkForTrue.label"/></label>
<input type="hidden" name="_<%=theField.getID()%>" value="<%=val%>">

<script type="text/javascript">
    <!--
   function check() {

         return true;
   }
   // -->
</script>