<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>

<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<c:if test="${not empty title.string}">
	<h3>${fn:escapeXml(title.string)}</h3>
</c:if>
<script type="text/javascript">
function toggleSearchMode(field) {
    document.getElementById('search-pages-criteria').style.display = field == 'siteContent' ? '' : 'none';
    document.getElementById('search-documents-criteria').style.display = field == 'siteContent' ? 'none' : '';
}
</script>

<div>
    <s:form name="advancedSearchForm" class="advancedSearchForm" method="get">
        <fieldset>
            <legend><fmt:message key="search.advancedSearch.criteria.text.title"/></legend>
            <label for="searchTerm"><fmt:message key="search"/></label>&nbsp;<s:termMatch selectionOptions="all_words,exact_phrase,any_word,as_is"/>&nbsp;<s:term id="searchTerm"/><br/>
            <label for="searchFields"><fmt:message key="searchForm.term.searchIn"/></label>&nbsp;<s:termFields id="searchFields" appearance="select" onchange="toggleSearchMode(this.options[this.selectedIndex].value)"/>
        </fieldset>
        <fieldset>
            <legend><fmt:message key="search.advancedSearch.criteria.authorAndDate.title"/></legend>
            <label for="searchCreatedBy"><fmt:message key="search.advancedSearch.criteria.authorAndDate.createdBy"/></label><s:createdBy id="searchCreatedBy"/><br/>
            <label for="searchCreated"><fmt:message key="search.advancedSearch.criteria.authorAndDate.created"/></label><s:created id="searchCreated"/><br/>
            <label for="searchLastModifiedBy"><fmt:message key="search.advancedSearch.criteria.authorAndDate.modifiedBy"/></label><s:lastModifiedBy id="searchLastModifiedBy"/><br/>
            <label for="searchLastModified"><fmt:message key="search.advancedSearch.criteria.authorAndDate.modified"/></label><s:lastModified id="searchLastModified"/>
        </fieldset>
        <fieldset>
            <legend><fmt:message key="search.advancedSearch.criteria.miscellanea.title"/></legend>
        	<label for="searchSite"><fmt:message key="search.advancedSearch.criteria.miscellanea.site"/></label><s:site id="searchSite"/><br/>
        	<label for="searchLanguage"><fmt:message key="search.advancedSearch.criteria.miscellanea.language"/></label><s:language id="searchLanguage"/><br/>
        	<c:set var="searchInFieldkey" value="src_terms[0].fields.custom"/>
        	<div id="search-pages-criteria" ${empty paramValues[searchInFieldkey] || fn:contains(fn:join(paramValues[searchInFieldkey], ','), 'siteContent') ? '' : 'style="display:none"'}><label for="searchPagePath"><fmt:message key="search.advancedSearch.criteria.miscellanea.pagePath"/></label><s:pagePath id="searchPagePath"/><br/></div>
        	<div id="search-documents-criteria" ${fn:contains(fn:join(paramValues[searchInFieldkey], ','), 'fileContent') ? '' : 'style="display:none"'}>
        	<label for="searchFileType"><fmt:message key="search.advancedSearch.criteria.miscellanea.fileType"/></label><s:fileType id="searchFileType"/><br/>
        	<label for="searchFilePath"><fmt:message key="search.advancedSearch.criteria.miscellanea.location"/></label><s:filePath id="searchFilePath"/><br/>
        	</div>
        	<label for="searchResultsPerPage"><fmt:message key="search.advancedSearch.criteria.miscellanea.itemsPerPage"/></label><s:itemsPerPage id="searchResultsPerPage"/><br/>
        </fieldset>
        <input type="submit" name="search" class="button" value="<fmt:message key='search.advancedSearch.submit'/>"/>
    </s:form>
</div>