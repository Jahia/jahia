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

<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<utility:setBundle basename="jahiatemplates.Community_templates"/>
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