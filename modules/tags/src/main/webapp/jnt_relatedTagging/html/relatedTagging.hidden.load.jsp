<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>

    <c:set var="statement" value="${' '}" />
    <c:set var="sign" value="'" />	
    <c:set var="operator" value="" />
    <c:set var="startQuery" value="0" />	
     
	<jcr:nodeProperty node="${currentNode}" name='j:type' var="type"/>		
	<jcr:nodeProperty node="${currentNode}" name="j:tags" var="assignedTags"/>
	<jcr:nodeProperty node="${currentNode}" name='j:nbOfResult' var="nbOfResult"/>
	   	
	<c:forEach items="${assignedTags}" var="tag" varStatus="status">
            <c:if test="${not empty tag.node}">
            	  <c:set var="statement" value="${statement}${operator}${'tags.[j:tags]= '}${sign}${tag.string}${sign}${' '}" />	
            	  <c:set var="operator" value="${'or '}" />
            	  <c:set var="startQuery" value="1" />			
            </c:if>
    </c:forEach>
    
    <c:choose> 
    <c:when test="${startQuery == '1' && not empty type && not empty nbOfResult}">
        <query:definition var="listQuery"
           statement="select * from [${type.string}] as tags where isdescendantnode(tags, '${renderContext.site.path}') and ${statement} order by tags.[jcr:lastModified] desc"
           limit="${nbOfResult.long}"/>
    </c:when>
    <c:otherwise>
        <c:remove var="listQuery"/>
    </c:otherwise>
    </c:choose>     
             
    <%-- Set variables to store the result --%>
    <c:set target="${moduleMap}" property="editable" value="false" />
    <c:set target="${moduleMap}" property="subNodesView" value="link" />
    <c:set target="${moduleMap}" property="listQuery" value="${listQuery}" /> 