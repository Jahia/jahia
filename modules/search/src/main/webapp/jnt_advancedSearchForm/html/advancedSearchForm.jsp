<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="jquery.js"/>
<template:addResources type="css" resources="advancedsearchform.css"/>

<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<template:addResources>
    <script type="text/javascript">
        function toggleSearchMode(field) {
            document.getElementById('search-pages-criteria').style.display = field == 'siteContent' ? '' : 'none';
            document.getElementById('search-documents-criteria').style.display = field == 'siteContent' ? 'none' : '';
        }
        $(document).ready(function() {
            $('#advancedSearch').hide();

            $(".BtToggleSearch").toggle(function() {
                $(this).addClass("active");
            }, function () {
                $(this).removeClass("active");
            });

            $('.BtToggleSearch').click(function() {
                $('#advancedSearch').slideToggle("slow");
            });
        })
    </script>
</template:addResources>
	<c:if test="${not empty title.string}">
        <h3 class="inline-advancedSearchForm-title"> ${fn:escapeXml(title.string)} </h3>
	</c:if>
	<a class="BtToggleSearch" href="#"></a>
    <div class="clear"></div>
<div id="advancedSearch">
    <div>
        <c:url value='${url.base}${renderContext.mainResource.node.path}.html' var="searchUrl"/>
        <s:form name="advancedSearchForm" class="Form advancedSearchForm" method="post" action="${searchUrl}">
            <fieldset>
                <legend><fmt:message key="search.advancedSearch.criteria.text.title"/></legend>
                <p><label class="left" for="searchTerm"><fmt:message key="search"/></label>&nbsp;<s:termMatch selectionOptions="all_words,exact_phrase,any_word,as_is"/>&nbsp;<s:term id="searchTerm"/></p>
                <p><label class="left" for="searchFields"><fmt:message key="searchForm.term.searchIn"/></label>&nbsp;<s:termFields id="searchFields" appearance="select" onchange="toggleSearchMode(this.options[this.selectedIndex].value)"/></p>
            </fieldset>
            <fieldset>
                <legend><fmt:message key="search.advancedSearch.criteria.authorAndDate.title"/></legend>
                <p><label class="left" for="searchCreatedBy"><fmt:message key="search.advancedSearch.criteria.authorAndDate.createdBy"/></label><s:createdBy id="searchCreatedBy"/></p>
                <p><label class="left" for="searchCreated"><fmt:message key="search.advancedSearch.criteria.authorAndDate.created"/></label><s:created id="searchCreated"/></p>
                <p><label class="left" for="searchLastModifiedBy"><fmt:message key="search.advancedSearch.criteria.authorAndDate.modifiedBy"/></label><s:lastModifiedBy id="searchLastModifiedBy"/></p>
                <p><label class="left" for="searchLastModified"><fmt:message key="search.advancedSearch.criteria.authorAndDate.modified"/></label><s:lastModified id="searchLastModified"/></p>
            </fieldset>
            <fieldset>
                <legend><fmt:message key="search.advancedSearch.criteria.miscellanea.title"/></legend>
                <p><label class="left" for="searchSite"><fmt:message key="search.advancedSearch.criteria.miscellanea.site"/></label><s:site id="searchSite"/></p>
                <p><label class="left" for="searchLanguage"><fmt:message key="search.advancedSearch.criteria.miscellanea.language"/></label><s:language id="searchLanguage"/></p>
                <c:set var="searchInFieldkey" value="src_terms[0].fields.custom"/>
                <div id="search-pages-criteria" ${empty paramValues[searchInFieldkey] || fn:contains(fn:join(paramValues[searchInFieldkey], ','), 'siteContent') ? '' : 'style="display:none"'}>
                    <p><label class="left" for="searchPagePath"><fmt:message key="search.advancedSearch.criteria.miscellanea.pagePath"/></label><s:pagePath id="searchPagePath"/></p>
                </div>
                <div id="search-documents-criteria" ${fn:contains(fn:join(paramValues[searchInFieldkey], ','), 'fileContent') ? '' : 'style="display:none"'}>
                    <p><label class="left" for="searchFileType"><fmt:message key="search.advancedSearch.criteria.miscellanea.fileType"/></label><s:fileType id="searchFileType"/></p>
                    <p><label class="left" for="searchFilePath"><fmt:message key="search.advancedSearch.criteria.miscellanea.location"/></label><s:filePath id="searchFilePath"/></p>
                </div>
                <p><label class="left" for="searchResultsPerPage"><fmt:message key="search.advancedSearch.criteria.miscellanea.itemsPerPage"/></label><s:itemsPerPage id="searchResultsPerPage"/></p>
            </fieldset>
            <div class="divButton">
                <input type="submit" name="search" class="button" value="<fmt:message key='search.advancedSearch.submit'/>"/>
            </div>
        </s:form>
    </div>
</div>
