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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ include file="../../common/declarations.jspf" %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions" %>
<script type="text/javascript">
function toggleSearchMode(field) {
    var mode = field.indexOf('pages_') == 0 ? 'pages' : 'files';
    document.advancedSearchForm.src_mode.value = mode;
    document.getElementById('searchPage').style.display = mode == 'pages' ? '' : 'none';
    document.getElementById('searchFileType').style.display = mode == 'pages' ? 'none' : '';
}
</script>
<div>
    <c:set var="searchMode" value="${h:default(param.src_mode, 'pages')}"/>
    <s:form searchFor="${searchMode}" name="advancedSearchForm" class="advancedSearchForm">
        <fieldset>
            <legend><fmt:message key="search.advancedSearch.criteria.text.title"/></legend>
            <label for="term"><fmt:message key="search"/></label>&nbsp;<s:termMatch selectionOptions="all_words,exact_phrase,any_word"/>&nbsp;<s:term id="term"/><br/>
            <label for="searchFields"><fmt:message key="searchForm.term.searchIn"/></label>&nbsp;<s:termFields id="searchFields" appearance="select" selectionOptions="pages_content,pages_all,documents_content,documents_all" value="pages_content" onChange="toggleSearchMode(this.options[this.selectedIndex].value)"/>
        </fieldset>
        <fieldset>
            <legend><fmt:message key="search.advancedSearch.criteria.authorAndDate.title"/></legend>
            <label for="createdBy"><fmt:message key="search.advancedSearch.criteria.authorAndDate.createdBy"/></label><s:createdBy id="createdBy"/><br/>
            <label for="created"><fmt:message key="search.advancedSearch.criteria.authorAndDate.created"/></label><s:created id="created"/><br/>
            <label for="lastModifiedBy"><fmt:message key="search.advancedSearch.criteria.authorAndDate.modifiedBy"/></label><s:lastModifiedBy id="lastModifiedBy"/><br/>
            <label for="lastModified"><fmt:message key="search.advancedSearch.criteria.authorAndDate.modified"/></label><s:lastModified id="lastModified"/>
        </fieldset>
        <fieldset>
            <legend><fmt:message key="search.advancedSearch.criteria.miscellanea.title"/></legend>
            <div id="searchPage" ${searchMode != 'pages' ? 'style="display:none"' : ''}><label for="pagePath"><fmt:message key="search.advancedSearch.criteria.miscellanea.pagePath"/></label><s:pagePath id="pagePath"/></div>
            <div id="searchFileType" ${searchMode != 'files' ? 'style="display:none"' : ''}><label for="fileType"><fmt:message key="search.advancedSearch.criteria.miscellanea.fileType"/></label><s:fileType id="fileType"/></div>
            <s:documentType value="jnt:file" display="false"/>
        </fieldset>
        <input type="submit" name="search" class="button" value="<fmt:message key='search.advancedSearch.submit'/>"/>
    </s:form>
</div>