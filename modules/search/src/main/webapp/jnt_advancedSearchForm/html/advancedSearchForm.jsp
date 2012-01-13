<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="css" resources="advancedsearchform.css"/>

<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<template:addResources>
    <script type="text/javascript">
        function toggleSearchMode(field) {
        	if (field.type == 'checkbox') {
        		if (field.name.indexOf('siteContent') != -1) {
                    document.getElementById('search-pages-criteria').style.display = field.checked ? '' : 'none';
        		}
        		if (field.name.indexOf('files') != -1) {
                    document.getElementById('search-documents-criteria').style.display = field.checked ? '' : 'none';
        		}
        	} else {
             	var o,i=0;
            	while(o=field.options[i++]){
            		if (o.value == 'siteContent') {
            			document.getElementById('search-pages-criteria').style.display = o.selected ? '' : 'none';
            		}
            		if (o.value == 'files') {
            			document.getElementById('search-documents-criteria').style.display = o.selected ? '' : 'none';
            		}
                }  
        	}
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
                <p><label class="left" for="searchFields"><fmt:message key="searchForm.term.searchIn"/></label>&nbsp;<s:termFields id="searchFields" onchange="toggleSearchMode(this)"/></p>
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
                <p><label class="left" for="searchSite"><fmt:message key="search.advancedSearch.criteria.miscellanea.site"/></label><s:site id="searchSite" includeReferencesFrom="systemsite"/></p>
                <p><label class="left" for="searchLanguage"><fmt:message key="search.advancedSearch.criteria.miscellanea.language"/></label><s:language id="searchLanguage"/></p>
                <c:set var="searchInFieldkey" value="src_terms[0].fields.custom"/>
                <c:set var="searchInFilesKey" value="src_terms[0].fields.files"/>
                <c:set var="searchInSiteContentKey" value="src_terms[0].fields.siteContent"/>
                <c:set var="pValues" value="${fn:join(paramValues[searchInFieldkey], ',')}"/>
                <c:set var="pFilesValue" value="${param[searchInFilesKey]}"/>
                <c:set var="pSiteContentValue" value="${param[searchInSiteContentKey]}"/>
                <div id="search-pages-criteria" ${fn:contains(pValues, 'siteContent') || pSiteContentValue == 'true' ? '' : 'style="display:none"'}>
                    <p><label class="left" for="searchPagePath"><fmt:message key="search.advancedSearch.criteria.miscellanea.pagePath"/></label><s:pagePath id="searchPagePath"/></p>
                </div>
                <div id="search-documents-criteria" ${fn:contains(pValues, 'fileContent') or fn:contains(pValues, 'files') or pFilesValue == 'true' ? '' : 'style="display:none"'}>
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
