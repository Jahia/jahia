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
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<div id="openclose">
<div class="clasp"><a href="#toggle" onclick="return toggleSearch('pages');"><fmt:message key="search.advancedSearch.portalContent"/></a></div>
    <div id="searchTypePages" class="lunchbox" style="display: ${param['src_mode'] == 'pages' ? 'block' : 'none'}">
        <s:form searchFor="pages" class="advancedSearchForm">
            <fieldset>
                <legend><fmt:message key="search.advancedSearch.criteria.text.title"/></legend>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.text.allWords"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.text.exactPhrase"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.text.anyWord"/>
            </fieldset>
            <fieldset>
                <legend><fmt:message key="search.advancedSearch.criteria.authorAndDate.title"/></legend>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.authorAndDate.createdBy"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.authorAndDate.created"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.authorAndDate.modifiedBy"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.authorAndDate.modified"/>
            </fieldset>
            <fieldset>
                <legend><fmt:message key="search.advancedSearch.criteria.miscellanea.title"/></legend>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.miscellanea.language"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.miscellanea.pagePath"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.miscellanea.itemsPerPage"/>
            </fieldset>
            <input type="submit" name="search" class="button" value="<fmt:message key='search.advancedSearch.submit'/>"/>
        </s:form>
    <div class="clear"></div>
</div>
<div class="clasp"><a href="#toggle" onclick="return toggleSearch('files');"><fmt:message key="search.advancedSearch.fileRepository"/></a></div>
    <div id="searchTypeFiles" class="lunchbox advancedSearchForm" style="display: ${param['src_mode'] == 'files' ? 'block' : 'none'}">
        <s:form searchFor="files">
            <fieldset>
                <legend><fmt:message key="search.advancedSearch.criteria.text.title"/></legend>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.text.allWords"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.text.exactPhrase"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.text.anyWord"/>
            </fieldset>
            <fieldset>
                <legend><fmt:message key="search.advancedSearch.criteria.authorAndDate.title"/></legend>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.authorAndDate.createdBy"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.authorAndDate.created"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.authorAndDate.modifiedBy"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.authorAndDate.modified"/>
            </fieldset>
            <fieldset>
                <legend><fmt:message key="search.advancedSearch.criteria.miscellanea.title"/></legend>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.miscellanea.fileType"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.miscellanea.documentType"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.miscellanea.location"/>
                <span class="label"><fmt:message key="search.advancedSearch.criteria.miscellanea.itemsPerPage"/>
            </fieldset>
                <input type="submit" name="search" class="button" value="<fmt:message key='search.advancedSearch.submit'/>"/>
        </s:form>
    <div class="clear"></div>
    </div>
</div>