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
            <label for="term"><fmt:message key="search"/></label>&nbsp;<s:termMatch selectionOptions="all_words,exact_phrase,any_word,as_is"/>&nbsp;<s:term id="term"/><br/>
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