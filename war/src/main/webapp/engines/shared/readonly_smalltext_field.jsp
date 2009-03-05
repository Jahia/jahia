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
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.exceptions.JahiaException"%>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%!
    final static private org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger("jsp.jahia.engines.shared.readonly_smalltext_field");  %>

<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
    final JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + "." + "theField");
    final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
    EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }
    final int pageID = theField.getPageID();
    String defValue = null;
    try {
        if (pageID > 0) {
            final ContentPage contentPage = ContentPage.getPage(pageID);
            defValue = theField.getDefinition().getDefaultValue();
        } else {
            // global definition
            defValue = theField.getDefinition().getDefaultValue();
        }
    } catch (final JahiaException e) {
        logger.error("Error setting defValue", e);
    }

%>
<utility:setBundle basename="JahiaInternalResources"/>

<% if (theField.getValue().indexOf("#") != -1 && theField.getValue().length() > 6 && theField.getValue().length() < 10) { %>
<fmt:message key="org.jahia.engines.shared.SmallText_Field.colorValue.label"/>
: <%=theField.getValue()%>
<% } else if (defValue != null && !defValue.equals("") && defValue.toUpperCase().indexOf("JAHIA_MULTIVALUE") != -1) { %>
<%
    String theSelectedField = theField.getValue();
    String strToRemove[] = {"&LT;JAHIA", "_MULTIVALUE"};

    for (int i = 0; i < strToRemove.length; i++) {
        String upperCaseField = theSelectedField.toUpperCase();
        int index = upperCaseField.indexOf(strToRemove[i]);
        if (index != -1) {
            theSelectedField = theSelectedField.substring(0, index) +
                    theSelectedField.substring(index + strToRemove[i].length(), theSelectedField.length());
        }
    }

    if (theSelectedField.indexOf("[") != -1) {
        int startStr = theSelectedField.indexOf("[");
        int endStr = theSelectedField.indexOf("]>") + 2;
        theSelectedField = theSelectedField.substring(0, startStr) + theSelectedField.substring(endStr, theSelectedField.length());
    }
%>

<%
    if (defValue.indexOf("[") != -1) {
        int startStr = defValue.indexOf("[") + 1;
        int endStr = defValue.indexOf("]");
        String theList = defValue.substring(startStr, endStr);
        while (true) {
            final String theSelection;
            if (theList.indexOf(":") != -1) {
                endStr = theList.indexOf(":");
                theSelection = theList.substring(0, endStr);
                theList = theList.substring(endStr + 1, theList.length());
                if (theSelection.equals(theSelectedField)) { %>
<fmt:message key="org.jahia.engines.value.label"/> : <%=theSelection%>
<% }
} else {
    theSelection = theList.substring(0, theList.length());
    if (theSelection.equals(theSelectedField)) { %>
<fmt:message key="org.jahia.engines.value.label"/> : <%=theSelection%>
<% }
    break;
}
}
}

} else {
    String value = theField.getValue();
    if (value == null)
        value = "";

    if (value.length() > 100) {
%>
<fmt:message key="org.jahia.engines.value.label"/>
: <%=value.substring(0, 100) + "..."%>
<% } else {
%>
<fmt:message key="org.jahia.engines.value.label"/> : <%=value%>
<% }
}
%>
