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
 