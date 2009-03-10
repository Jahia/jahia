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
<%@ include file="../../common/declarations.jspf" %>
<div id="openclose">
<div class="clasp"><a href="advancedSearchForm.jsp#toggle" onclick="return toggleSearch('pages');"><fmt:message key="search.advancedSearch.portalContent"/></a></div>
    <div id="searchTypePages" class="lunchbox" style="display: ${param['src_mode'] == 'pages' ? 'block' : 'none'}">
        <s:form searchFor="pages" class="advancedSearchForm">
            <fieldset>
                <legend><fmt:message key="search.advancedSearch.criteria.text.title"/></legend>
                <label for="allWords"><fmt:message key="search.advancedSearch.criteria.text.allWords"/></label><s:term id="allWords" match="all_words"/><br/>
                <label for="exactPhrase"><fmt:message key="search.advancedSearch.criteria.text.exactPhrase"/></label><s:term id="exactPhrase" match="exact_phrase"/><br/>
                <label for="anyWord"><fmt:message key="search.advancedSearch.criteria.text.anyWord"/></label><s:term id="anyWord" match="any_word"/><br/>
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
                <label for="language"><fmt:message key="search.advancedSearch.criteria.miscellanea.language"/></label><s:language id="language"/><br/>
                <label for="pagePath"><fmt:message key="search.advancedSearch.criteria.miscellanea.pagePath"/></label><s:pagePath id="pagePath"/><br/>
                <label for="itemsPerPage"><fmt:message key="search.advancedSearch.criteria.miscellanea.itemsPerPage"/></label><s:itemsPerPage id="itemsPerPage"/>
            </fieldset>
            <input type="submit" name="search" class="button" value="<fmt:message key='search.advancedSearch.submit'/>"/>
        </s:form>
    <div class="clear"> </div>
</div>
<div class="clasp"><a href="advancedSearchForm.jsp#toggle" onclick="return toggleSearch('files');"><fmt:message key="search.advancedSearch.fileRepository"/></a></div>
    <div id="searchTypeFiles" class="lunchbox advancedSearchForm" style="display: ${param['src_mode'] == 'files' ? 'block' : 'none'}">
        <s:form searchFor="files">
            <fieldset>
                <legend><fmt:message key="search.advancedSearch.criteria.text.title"/></legend>
                <label for="fileAllWords"><fmt:message key="search.advancedSearch.criteria.text.allWords"/></label><s:term id="fileAllWords" match="all_words"/><br/>
                <label for="fileExactPhrase"><fmt:message key="search.advancedSearch.criteria.text.exactPhrase"/></label><s:term id="fileExactPhrase" match="exact_phrase"/><br/>
                <label for="fileAnyWord"><fmt:message key="search.advancedSearch.criteria.text.anyWord"/></label><s:term id="fileAnyWord" match="any_word"/><br/>
            </fieldset>
            <fieldset>
                <legend><fmt:message key="search.advancedSearch.criteria.authorAndDate.title"/></legend>
                <label for="fileCreatedBy"><fmt:message key="search.advancedSearch.criteria.authorAndDate.createdBy"/></label><s:createdBy id="fileCreatedBy"/><br/>
                <label for="fileCreated"><fmt:message key="search.advancedSearch.criteria.authorAndDate.created"/></label><s:created id="fileCreated"/><br/>
                <label for="fileLastModifiedBy"><fmt:message key="search.advancedSearch.criteria.authorAndDate.modifiedBy"/></label><s:lastModifiedBy id="fileLastModifiedBy"/><br/>
                <label for="fileLastModified"><fmt:message key="search.advancedSearch.criteria.authorAndDate.modified"/></label><s:lastModified id="fileLastModified"/>
            </fieldset>
            <fieldset>
                <legend><fmt:message key="search.advancedSearch.criteria.miscellanea.title"/></legend>
                <label for="fileType"><fmt:message key="search.advancedSearch.criteria.miscellanea.fileType"/></label><s:fileType id="fileType"/><br/>
                <label for="documentType"><fmt:message key="search.advancedSearch.criteria.miscellanea.documentType"/></label><s:documentType id="documentType" value="nt:file"/><br/>
                <label for="documentProperty"><fmt:message key="search.advancedSearch.criteria.miscellanea.categories"/></label><s:documentProperty id="documentProperty" documentType="jmix:categorized" name="j:defaultCategory"/><br/>
                <label for="fileLocation"><fmt:message key="search.advancedSearch.criteria.miscellanea.location"/></label><s:fileLocation id="fileLocation"/><br/>
                <label for="fileItemsPerPage"><fmt:message key="search.advancedSearch.criteria.miscellanea.itemsPerPage"/></label><s:itemsPerPage id="fileItemsPerPage"/>
            </fieldset>
                <input type="submit" name="search" class="button" value="<fmt:message key='search.advancedSearch.submit'/>"/>
        </s:form>
    <div class="clear"> </div>
    </div>
</div>