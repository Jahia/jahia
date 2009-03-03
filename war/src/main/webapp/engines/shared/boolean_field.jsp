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