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

<%@ include file="declarations.jspf" %>
<c:set var="letters">A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z</c:set>
<c:set value="0" var="i"/>
<c:set value="0" var="next"/>
<c:set value="A" var="curLetter"/>
<c:set value="%" var="writLetter"/>
<c:forTokens items="${letters}" delims="," var="let">
    <a href='#<c:out value="${let}" />'><c:out value="${let}"/></a>&nbsp;
    <c:set value="${i+1}" var="i"/>
    <c:set scope="request" value="${let}" var="abc[i]"/>
</c:forTokens>

<br/>
<template:containerList name="pediaGlossaryCL" id="pediaGlossaryCL">
    <query:containerQuery>
        <query:selector nodeTypeName="sandbox_templates:pediaGlossaryCL" selectorName="pediaGlossaryCL"/>
        <query:sortBy propertyName="glossaryTerm" order="${queryConstants.ORDER_ASCENDING}"/>
    </query:containerQuery>
    <template:container id="pediaGlossary" cacheKey="pediaGlossary">
        <template:field name="glossaryTerm" display="false" var="term"/>

        <c:set value="${fn:substring(not empty term ? term.text : '', 0, 1)}" var="term1"/>
        <c:set value="${fn:toUpperCase(term1)}" var="term1"/>
        <c:set value="1" var="end"/>
        <c:set value="1" var="start"/>
        <c:forTokens items="${letters}" delims="," var="let">
            <c:if test="${!(writLetter != let && writLetter != '%') && start == 1}">
                <c:set value="0" var="end"/>
                <c:set value="0" var="start"/>
            </c:if>
            <c:if test="${end == 0 && let != writLetter && term1 != writLetter}">
                <c:if test="${next == 1}">
                    <c:set value="${let}" var="curLetter"/>
                    <c:set value="0" var="next"/>
                </c:if>
                <c:if test="${let == curLetter && let != writLetter}">
                    <c:if test='${term1 == curLetter}'>
                        <a name='<c:out value="${fn:trim(let)}" />'></a><c:out value="${let}"/>
                        <hr/>
                        <c:set value="${let}" var="writLetter"/>
                        <c:set value="1" var="next"/>
                        <c:set value="1" var="end"/>
                    </c:if>
                    <c:if test="${term1 != curLetter}">
                        <a name='<c:out value="${fn:trim(let)}" />'></a><c:out value="${let}"/>
                        <hr/>
                        <c:set value="${let}" var="writLetter"/>
                        <c:set value="1" var="next"/>
                    </c:if>
                </c:if>
            </c:if>
        </c:forTokens>
        <h3><template:field name='glossaryTerm'/></h3>
        <template:field name='glossaryDescription'/>
    </template:container>
    <c:set value="0" var="next"/>
    <c:forTokens items="${letters}" delims="," var="let">
        <c:if test="${next == '1' || writLetter == '%'}">
            <a name='<c:out value="${let}" />'></a><c:out value="${let}"/>
            <hr/>
        </c:if>
        <c:if test="${let == curLetter}">
            <c:set value="1" var="next"/>
        </c:if>
    </c:forTokens>
</template:containerList>
 