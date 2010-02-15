<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="searchresults.css"/>

<c:if test="${renderContext.editMode}">
	<fieldset>
		<legend>${fn:escapeXml(jcr:label(currentNode.primaryNodeType))}</legend>
</c:if>
<s:results>
	<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
	<jcr:nodeProperty name="autoSuggest" node="${currentNode}" var="autoSuggest"/>
	<c:if test="${not empty title.string}">
		<h3>${fn:escapeXml(title.string)}</h3>
	</c:if>
	<div class="resultsList">
        <c:if test="${autoSuggest.boolean}">
        	<%-- spelling auto suggestions are enabled --%>
        	<jcr:nodeProperty name="autoSuggestMinimumHitCount" node="${currentNode}" var="autoSuggestMinimumHitCount"/>
        	<jcr:nodeProperty name="autoSuggestHitCount" node="${currentNode}" var="autoSuggestHitCount"/>
        	<c:if test="${count <= functions:default(autoSuggestMinimumHitCount.long, 2)}">
        		<%-- the number of original results is less than the configured threshold, we can start auto-suggest  --%>
	        	<s:suggestions>
	        		<%-- we have a suggestion --%>
		        	<c:if test="${suggestedCount > count}">
		        		<%-- found more hits for the suggestion than the original query brings --%>
						<h4>
							<fmt:message key="search.results.didYouMean" />:&nbsp;<a href="<s:suggestedSearchUrl/>"><em>${fn:escapeXml(suggestion.suggestedQuery)}</em></a>.&nbsp;
							<fmt:message key="search.results.didYouMean.topResults"><fmt:param value="${functions:min(functions:default(autoSuggestHitCount.long, 2), suggestedCount)}" /></fmt:message>
						</h4>
						<ol>
							<s:resultIterator begin="0" end="${functions:default(autoSuggestHitCount.long, 2) - 1}">
								<li><%@ include file="searchHit.jspf" %></li>
							</s:resultIterator>
						</ol>
						<hr/>
						<h4><fmt:message key="search.results.didYouMean.resultsFor"/>:&nbsp;<strong>${fn:escapeXml(suggestion.originalQuery)}</strong></h4>
			        </c:if>
	        	</s:suggestions>
        	</c:if>
        </c:if>
		<c:if test="${count > 0}">
	    	<h4><fmt:message key="search.results.found"><fmt:param value="${count}"/></fmt:message></h4>
        	<ol>
				<s:resultIterator>
					<li><%@ include file="searchHit.jspf" %></li>
				</s:resultIterator>
	        </ol>
		</c:if>
        <c:if test="${count == 0}">
        	<h4><fmt:message key="search.results.no.results"/></h4>
		</c:if>
    </div>
</s:results>
<c:if test="${renderContext.editMode}">
	</fieldset>
</c:if>
