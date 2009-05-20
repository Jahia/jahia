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
<%@taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<div class="searchform">
<s:form>
    <fieldset>
        <legend>Text search</legend>
        Search term:&nbsp;<s:term  searchIn="content,metadata" searchInAllowSelection="true" searchInSelectionOptions="content,metadata"/><br/>
    </fieldset>
    <fieldset>
        <legend>Author and date</legend>
	    Author:&nbsp;<s:createdBy/><br/>
	    Created:&nbsp;<s:created/><br/>
	    Last editor:&nbsp;<s:lastModifiedBy/><br/>
        Modified:&nbsp;<s:lastModified/><br/>
    </fieldset>
    <fieldset>
        <legend>More...</legend>
        Language:&nbsp;<s:language/><br/>
        Page: <s:pagePath/><br/>
        Results per page:&nbsp;<s:itemsPerPage/><br/>
    </fieldset>
    <input type="submit" name="search" value="Search"/>
</s:form>
</div>