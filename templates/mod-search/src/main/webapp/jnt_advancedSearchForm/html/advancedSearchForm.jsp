<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>

<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<c:if test="${not empty title.string}">
	<h3>${fn:escapeXml(title.string)}</h3>
</c:if>

<div>
    <s:form name="advancedSearchForm" class="advancedSearchForm" method="get">
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
        	<label for="language">!!!Language</label><s:language id="language"/><br/>
        	<label for="resultsPerPage">!!!Results per page</label><s:itemsPerPage id="resultsPerPage"/><br/>
        </fieldset>
        <input type="submit" name="search" class="button" value="<fmt:message key='search.advancedSearch.submit'/>"/>
    </s:form>
</div>