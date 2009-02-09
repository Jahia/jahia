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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ tag body-content="empty" description="Renders search term input control." %>
<%@include file="declaration.tagf" %>
<%@ attribute name="value" required="false" type="java.lang.String" description="The initial value." %>
<%@ attribute name="match" required="false" type="java.lang.String" description="The match type for search terms." %>
<%@ attribute name="searchIn" required="false" type="java.lang.String"
              description="Comma separated list of fields to search in." %>
<c:set var="formId" value="<%=this.getParent().toString() %>"/>
<c:set var="termIndex" value="${searchTermIndexes[formId]}"/>
<c:set target="${attributes}" property="type" value="${display ? 'text' : 'hidden'}"/>
<c:set var="key" value="src_terms[${termIndex}].term"/>
<c:set target="${attributes}" property="name" value="${key}"/>
<c:set var="value" value="${h:default(param[key], value)}"/>
<c:set var="key" value="src_terms[${termIndex}].fields"/>
<c:set var="searchIn" value="${h:default(param[key], h:default(searchIn, 'content'))}"/>
<input ${h:attributes(attributes)} value="${fn:escapeXml(value)}"/>
<c:if test="${not empty match && match != 'as_is'}">
    <c:set var="key" value="src_terms[${termIndex}].match"/>
    <input type="hidden" name="${key}" value="${h:default(param[key], match)}"/>
</c:if>
<c:forTokens items="${searchIn}" delims="," var="field">
    <input type="hidden" name="src_terms[${termIndex}].fields.${field}" value="true"/>
</c:forTokens>
<c:set target="${searchTermIndexes}" property="${formId}" value="${searchTermIndexes[formId] + 1}"/>