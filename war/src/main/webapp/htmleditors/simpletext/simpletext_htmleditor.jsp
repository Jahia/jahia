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
<%@ page import="org.jahia.data.FormDataManager" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="org.jahia.utils.JahiaTools" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final JahiaField theField = (JahiaField) engineMap.get("theField");

    // FIXME : Have these things really something to do here ???
    String theOldField = theField.getValue();
    String theNewField;
    /*
    if (!theOldField.toLowerCase().startsWith("<html>")
            && !theOldField.toLowerCase().startsWith("&lt;html>")) {
        theOldField = JahiaTools.replacePattern(theOldField, "\r\n", "<br/>");
    }*/

    final String strToRemove[] = {"&lt;jahia", "_htmleditor>"};
    for (int i = 0; i < strToRemove.length; i++) {
        final String lowerCaseField = theOldField.toLowerCase();
        int index = lowerCaseField.indexOf(strToRemove[i]);
        if (index != -1) {
            theNewField = theOldField.substring(0, index) +
                    theOldField.substring(index + strToRemove[i].length(), theOldField.length());
            theOldField = theNewField;
        }
    }
    if (theOldField == null || theOldField.length() == 0) {
        theOldField = "<html></html>";
    }
%>
<p>
    <div>
    <input class="simpleTextEditorButton" type="button" value="<internal:message key="org.jahia.engines.simpleText.replaceCRWithBR.label"/>" onmousedown="replaceLineBreakByBr(document.getElementById('jahiaEditor'))">
    <input class="simpleTextEditorButton" type="button" value='<internal:message key="org.jahia.engines.simpleText.selectAll.label"/>' onClick="selectAll(document.getElementById('jahiaEditor'))">
    <textarea id="jahiaEditor" name="_<%=theField.getID()%>" cols="75" rows="20"><%=JahiaTools.replacePattern(FormDataManager.formEncode(theOldField),"<","&lt;")%></textarea>
    </div>
</p>

<script type="text/javascript">
    /*
    function saveContent() {
        var area = document.getElementById("jahiaEditor");
        var value = area.value.toLowerCase();
        if ( value.indexOf("<html") == -1 && value.indexOf("&lt;html") == -1 ){
          	value = area.value.replace(/\n/g,'<br/>');
            area.value = value;  
        }
    }*/

    function replaceLineBreakByBr(oTextbox) {
    
       if (document.selection) {
           var oRange = document.selection.createRange();
           oRange.text = unescape(oRange.text.replace(/\n/g,'<br/>'));
           oRange.collapse(true);
           oRange.select();                                
       } else if (window.getSelection) {
           var selection = "";
           var iStart = oTextbox.selectionStart;
           var iStop = oTextbox.selectionEnd;
           if (iStart != iStop) {
             selection = oTextbox.value.substring(iStart, iStop);
           }
           selection = selection.replace(/\r/g,'')
           selection = unescape(selection.replace(/\n/g,'<br/>%0D%0A'));
           oTextbox.value = oTextbox.value.substring(0, iStart) + selection 
            + oTextbox.value.substring(oTextbox.selectionEnd, oTextbox.value.length);
           oTextbox.setSelectionRange(iStart + selection.length, iStart + selection.length);
       }
    
       oTextbox.focus();
    }

    function selectAll(what){
      what.focus();
      what.select();
    }
</script>