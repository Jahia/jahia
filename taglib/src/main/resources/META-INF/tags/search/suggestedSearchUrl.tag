<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
<%@ tag body-content="empty"
        description="Renders the search URL built from current search request parameters, considering the specified suggestion search term. The suggested term can be specified explicitly by providing suggestion attribute or by nesting this tag inside &lt;s:suggestions/&gt; tag."
        import="org.jahia.taglibs.search.SuggestionsTag" 
%><%@ attribute name="suggestion" required="false" type="java.lang.String"
              description="The search term suggestion to generate the URL for."
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><%@ taglib prefix="search" uri="http://www.jahia.org/tags/search" 
%><c:if test="${empty suggestion}"><%
SuggestionsTag parent = (SuggestionsTag) findAncestorWithClass(this, SuggestionsTag.class);
if (parent == null) {
     throw new JspTagException("Suggestion tern must be provided either through the 'suggestion' attribute" + 
             " or by nesting this tag into <s:suggestions/> tag");
}
jspContext.setAttribute("suggestion", parent.getSuggestion().getSuggestedQuery());
%></c:if><c:set var="searchUrl"><search:searchUrl exclude='src_terms[0].term'/></c:set><c:url value="${searchUrl}">
	<c:param name="src_terms[0].term" value="${suggestion}"/>
</c:url>