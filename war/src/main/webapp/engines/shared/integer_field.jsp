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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle"%>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="org.jahia.utils.JahiaTools" %>
<%@ page import="java.util.*" %>
<%@page import="org.jahia.data.fields.ExpressionMarker"%>
<%@page import="org.jahia.utils.i18n.ResourceBundleMarker"%>

<%!
    private String contextID(int id) {
        return JahiaTools.replacePattern(String.valueOf(id), "-", "_");
    }
%>
<%
    final Boolean isIE = (Boolean) request.getAttribute("isIE");
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");

    final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");

    final JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + "." + "theField");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final int pageID = theField.getPageID();
    final ContentPage contentPage = ContentPage.getPage(pageID);
    final String defValue = theField.getDefinition().getDefaultValue();
    final Boolean isSelectedField = (Boolean) engineMap.get(fieldsEditCallingEngineName + "." + "isSelectedField");

    final String contextID = contextID(theField.getID());

%>

<% if (defValue != null && !defValue.equals("") && defValue.toUpperCase().indexOf("JAHIA_MULTIVALUE") != -1) { %>

<%
    String theSelectedField = theField.getValue();
    final String strToRemove[] = {"&LT;JAHIA", "_MULTIVALUE"};

    for (int i = 0; i < strToRemove.length; i++) {
        final String upperCaseField = theSelectedField.toUpperCase();
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
%>
<select name="listSelection_<%=contextID%>"
        onChange="document.mainForm.elements['_<%=theField.getID()%>'].value = document.mainForm.listSelection_<%=contextID%>.options[document.mainForm.listSelection_<%=contextID%>.selectedIndex].value;">
    <%
        while (true) {
            String theSelection = "";
            if (theList.indexOf(":") != -1) {
                endStr = theList.indexOf(":");
                theSelection = theList.substring(0, endStr);
                theList = theList.substring(endStr + 1, theList.length());
                if (theSelection.equals(theSelectedField)) { %>
    <option value="<%=theSelection%>" selected><%=theSelection%></option>
    <% } else { %>
    <option value="<%=theSelection%>"><%=theSelection%></option>
    <% }
    } else {
        theSelection = theList.substring(0, theList.length());
        if (theSelection.equals(theSelectedField)) { %>
    <option value="<%=theSelection%>" selected><%=theSelection%></option>
    <% } else { %>
    <option value="<%=theSelection%>"><%=theSelection%></option>
    <% } %>
</select>
<%
                break;
            }
        }
    }
%>
<input name="_<%=theField.getID()%>" type="hidden" value="<%=theField.getValue()%>">
<SCRIPT type="text/javascript">
    <!--
    document.mainForm.elements['_<%=theField.getID()%>'].value = document.mainForm.listSelection_<%=contextID%>.options[document.mainForm.listSelection_<%=contextID%>.selectedIndex].value;
    // -->
</SCRIPT>

<% if (isSelectedField.booleanValue() && isIE.booleanValue()) { %>
<SCRIPT type="text/javascript">
    <!--
    function setfocus() {
        document.mainForm.listSelection_<%=contextID%>.focus();
    }
    setfocus();
    // -->
</SCRIPT>
<% }
} else {
    String val;
    if (theField.getRawValue().startsWith("<jahia-expression")) {
        val = ExpressionMarker.getValue(theField.getRawValue(), jParams);
    } else if (theField.getRawValue().startsWith("<jahia-resource")) {
        val = ResourceBundleMarker.getValue(theField.getRawValue(), jParams.getLocale());
    } else {
        val = theField.getValue();
    } 
    
    int columns = 30;
    String userAgent = request.getHeader("user-agent");
    if (userAgent != null) {
        if (userAgent.indexOf("MSIE") != -1) {
            columns = 40;
        }
    }
%>
<input id="field_<%=theField.getID()%>" type="text" name="_<%=theField.getID()%>" size="<%=columns%>" maxlength="250"
       value="<%=val%>" onBlur="checkInteger_<%=contextID(theField.getID())%>()">

<% if (isSelectedField.booleanValue()) { %>
<SCRIPT type="text/javascript">
    <!--
    function setfocus() {
        document.mainForm.elements["_<%=theField.getID()%>"].focus();
    }
    setfocus();

    // -->
</SCRIPT>
<% }
}
%>
<SCRIPT type="text/javascript">
    <!--
    function checkInteger_<%=contextID(theField.getID())%>() {
        if (( (!isNaN(document.mainForm.elements["_<%=theField.getID()%>"].value)) &&
              (parseFloat(document.mainForm.elements["_<%=theField.getID()%>"].value) ==
               parseInt(document.mainForm.elements["_<%=theField.getID()%>"].value)) ) || (document.mainForm.elements["_<%=theField.getID()%>"].value == ""))
        {
            document.mainForm.elements["_<%=theField.getID()%>"].value = (document.mainForm.elements["_<%=theField.getID()%>"].value).replace(" ", "");
            return true;
        }
        else
        {
            document.mainForm.elements["_<%=theField.getID()%>"].value = 0;
            document.mainForm.elements["_<%=theField.getID()%>"].focus();
            alert("<%=JahiaTools.html2text(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.shared.Integer_Field.valueMustBeInteger.label",
                jParams.getLocale()))%>");
            return false;
        }
    }
    // -->
</SCRIPT>