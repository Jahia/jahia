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
/* <![CDATA[ */
function toggleSearchMode(field) {
    document.getElementById('search-pages-criteria').style.display = field == 'siteContent' ? '' : 'none';
    document.getElementById('search-documents-criteria').style.display = field == 'siteContent' ? 'none' : '';
}
/* ]]> */
</script>

<div>
    <s:form name="advancedSearchForm" class="Form advancedSearchForm" method="get">
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
        	<div id="search-pages-criteria" ${empty paramValues[searchInFieldkey] || fn:contains(fn:join(paramValues[searchInFieldkey], ','), 'siteContent') ? '' : 'style="display:none"'}><p><label class="left" for="searchPagePath"><fmt:message key="search.advancedSearch.criteria.miscellanea.pagePath"/></label><s:pagePath id="searchPagePath"/></p></div>
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