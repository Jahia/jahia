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
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="settings" value="${requestScope['org.jahia.engines.EngineHashMap'].savedSearchView.settings}"/>
<h3 class="updateelement">
    <internal:engineResourceBundle
            resourceName="org.jahia.engines.search.customizeSaveSearchView.label" defaultValue="Customize view"/>
</h3>

<jsp:include page="../buttons.jsp" flush="true" />

<script type="text/javascript" src="${pageContext.request.contextPath}/jsp/jahia/javascript/scriptaculous/scriptaculous-17-compressed.js"></script>
<script type="text/javascript">
    // <![CDATA[
    Event.observe(window, 'load', doOnLoad);
    function doOnLoad() {
        Sortable.create('columns');
    }
    function check() {
        $('fieldsOrder').value = Sortable.sequence('columns').join(',');
        return true;
    }
    
    // ]]>
</script>
<style type="text/css">
<!--
#columns {
    border: 1px solid black;
	list-style-type: none;
    padding: 10px 5px;
	margin: 0;
}
#columns li {
    border: 1px dotted #398EC3;
    margin: 3px;
    padding: 1px;
    background: url("${pageContext.request.contextPath}/jsp/jahia/engines/images/arrow_off.png") no-repeat 95%;
}
-->
</style>

<input type="hidden" id="fieldsOrder" name="view_fieldsOrder" value="${settings.fieldsOrder}"/>
<div class="clearing">&nbsp;</div>
<div class="menuwrapper">
    <div class="content">
        <div id="editor">
            <table cellpadding="0" cellspacing="5" width="400px">
            <c:if test="${jahia.requestInfo.admin}">
	            <tr>
	                <td align="left" valign="top" class="warning">
	                    <internal:engineResourceBundle resourceName="org.jahia.engines.search.customizeSaveSearchView.sharedSettings.label" defaultValue="This profile is shared."/>
	                </td>
	            </tr>
            </c:if>
            <tr>
                <td align="left" valign="top">
                <h2><internal:engineResourceBundle resourceName="org.jahia.engines.search.customizeSaveSearchView.fieldsToDisplay.label" defaultValue="Field to display"/>:</h2>
                <span style="clear: both">&nbsp;</span>
                <ul id="columns">
                <c:forEach items="${settings.fields}" var="field">
                    <li id="column_${field.name}"><input type="checkbox" name="view_fieldMap.${field.name}.selected" id="${field.name}" value="true" ${field.selected ? 'checked="checked"' : ''}/>&nbsp;<label for="${field.name}"><internal:engineResourceBundle resourceName="${field.resourceKey}" defaultValue="${field.label}"/></label></li>
                </c:forEach>
                </ul>
                </td>
            <tr>
            <tr>
                <td align="left" valign="top">
                <h2><internal:engineResourceBundle resourceName="org.jahia.engines.search.customizeSaveSearchView.fieldsForSort.label" defaultValue="Sort on field"/>:</h2>
                <select name="view_sortBy">
                    <option value=""></option>
                    <c:forEach items="${settings.fields}" var="field">
                        <option value="${field.name}"${settings.sortBy == field.name ? 'selected="selected"' : ''}><internal:engineResourceBundle resourceName="${field.resourceKey}" defaultValue="${field.label}"/></option>
                    </c:forEach>
                </select>
                <input type="radio" id="view_ascending_asc" name="view_ascending" value="true" ${settings.ascending ? 'checked="checked"' : ''}/>&nbsp;<label for="view_ascending_asc"><internal:engineResourceBundle resourceName="org.jahia.engines.search.customizeSaveSearchView.ascending.label" defaultValue="Ascending"/></label>
                <input type="radio" id="view_ascending_desc" name="view_ascending" value="false" ${not settings.ascending ? 'checked="checked"' : ''}/>&nbsp;<label for="view_ascending_desc"><internal:engineResourceBundle resourceName="org.jahia.engines.search.customizeSaveSearchView.descending.label" defaultValue="Descending"/></label>
                </td>
            </tr>
        </table>														          	
    </div>
</div>
<div class="clearing">&nbsp;</div>
</div>