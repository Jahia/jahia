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
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<div id="openclose">
<div class="clasp"><a href="#toggle" onclick="return toggleSearch('pages');"><utility:resourceBundle resourceName="search.advancedSearch.portalContent" escape="true"/></a></div>
    <div id="searchTypePages" class="lunchbox" style="display: ${param['src_mode'] == 'pages' ? 'block' : 'none'}">
        <s:form searchFor="pages" class="advancedSearchForm">
            <fieldset>
                <legend><utility:resourceBundle resourceName="search.advancedSearch.criteria.text.title" escape="true"/></legend>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.text.allWords" escape="true"/></span><s:term match="all_words"/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.text.exactPhrase" escape="true"/></span><s:term match="exact_phrase"/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.text.anyWord" escape="true"/></span><s:term match="any_word"/><br/>
            </fieldset>
            <fieldset>
                <legend><utility:resourceBundle resourceName="search.advancedSearch.criteria.authorAndDate.title" escape="true"/></legend>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.authorAndDate.createdBy" escape="true"/></span><s:createdBy/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.authorAndDate.created" escape="true"/></span><s:created/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.authorAndDate.modifiedBy" escape="true"/></span><s:lastModifiedBy/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.authorAndDate.modified" escape="true"/></span><s:lastModified/>
            </fieldset>
            <fieldset>
                <legend><utility:resourceBundle resourceName="search.advancedSearch.criteria.miscellanea.title" escape="true"/></legend>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.miscellanea.language" escape="true"/></span><s:language/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.miscellanea.pagePath" escape="true"/></span><s:pagePath/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.miscellanea.itemsPerPage" escape="true"/></span><s:itemsPerPage/>
            </fieldset>
            <input type="submit" name="search" class="button" value="<utility:resourceBundle resourceName='search.advancedSearch.submit' escape='true'/>"/>
        </s:form>
    <div class="clear"></div>
</div>
<div class="clasp"><a href="#toggle" onclick="return toggleSearch('files');"><utility:resourceBundle resourceName="search.advancedSearch.fileRepository" escape="true"/></a></div>
    <div id="searchTypeFiles" class="lunchbox advancedSearchForm" style="display: ${param['src_mode'] == 'files' ? 'block' : 'none'}">
        <s:form searchFor="files">
            <fieldset>
                <legend><utility:resourceBundle resourceName="search.advancedSearch.criteria.text.title" escape="true"/></legend>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.text.allWords" escape="true"/></span><s:term match="all_words"/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.text.exactPhrase" escape="true"/></span><s:term match="exact_phrase"/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.text.anyWord" escape="true"/></span><s:term match="any_word"/><br/>
            </fieldset>
            <fieldset>
                <legend><utility:resourceBundle resourceName="search.advancedSearch.criteria.authorAndDate.title" escape="true"/></legend>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.authorAndDate.createdBy" escape="true"/></span><s:createdBy/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.authorAndDate.created" escape="true"/></span><s:created/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.authorAndDate.modifiedBy" escape="true"/></span><s:lastModifiedBy/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.authorAndDate.modified" escape="true"/></span><s:lastModified/>
            </fieldset>
            <fieldset>
                <legend><utility:resourceBundle resourceName="search.advancedSearch.criteria.miscellanea.title" escape="true"/></legend>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.miscellanea.fileType" escape="true"/></span><s:fileType/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.miscellanea.documentType" escape="true"/></span><s:documentType value="nt:file"/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.miscellanea.categories" escape="true"/></span><s:documentProperty documentType="jmix:categorized" name="j:defaultCategory"/><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.miscellanea.location" escape="true"/></span><s:fileLocation /><br/>
                <span class="label"><utility:resourceBundle resourceName="search.advancedSearch.criteria.miscellanea.itemsPerPage" escape="true"/></span><s:itemsPerPage/>
            </fieldset>
                <input type="submit" name="search" class="button" value="<utility:resourceBundle resourceName='search.advancedSearch.submit' escape='true'/>"/>
        </s:form>
    <div class="clear"></div>
    </div>
</div>
<%-- 
<div class="clasp"><a href="#toggle" onclick="return toggleSearch('files');">${fn:escapeXml(jahia.i18n['search.advancedSearch.fileRepository'])}</a></div>
    <div id="searchTypeFiles" class="lunchbox advancedSearchForm" style="display: ${param['src_mode'] == 'files' ? 'block' : 'none'}">
        <s:form searchFor="pages">
            <fieldset>
                <legend>Text search</legend>
                <span class="label">Raw:</span><s:rawQuery size="100"/><br/>
            </fieldset>
                <input type="submit" name="search" class="button" value="${jahia.i18n['submit']}"/>
        </s:form>
    <div class="clear"></div>
    </div>
</div>
--%>