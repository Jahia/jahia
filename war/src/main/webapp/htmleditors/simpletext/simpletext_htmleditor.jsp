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