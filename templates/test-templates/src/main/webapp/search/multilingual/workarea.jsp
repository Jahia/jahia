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
<%@ include file="../../common/declarations.jspf" %>

<div class="expectedResultTitle">
    <fmt:message key="label.expected.result"/>:
</div>
<div class="expectedResult">
    <fmt:message key="description.template.multilingualSearch.expectedResult"/>
</div>

<utility:useConstants var="queryConstants1" className="org.jahia.query.qom.JahiaQueryObjectModelConstants"
                      scope="application"/>
<template:jahiaPageForm name="searchMultiForm">
    Search:<br/>
    Text: <input type="text" value="${param.texttext}" name="texttext"/><br/>
    Search in all Languages (it,en,de,fr): <input type="checkbox" name="allLang" value="true"
                                                  <c:if test="${param.allLang == 'true'}">checked</c:if>/><br/>
    <input type="submit" name="searchtext"/>
</template:jahiaPageForm>

<template:containerList name="multiSearchLangFields" id="multiSearchLangFieldsCl" actionMenuNamePostFix="testContainers"
                        actionMenuNameLabelKey="testContainers.add">
    <query:containerQuery>
        <c:if test="${!empty param.texttext}">
            <query:fullTextSearch searchExpression="${param.texttext}"/>
        </c:if>
        <c:if test="${param.allLang == 'true'}">
            <query:setProperty name="${queryConstants.LANGUAGE_CODES}" value="it,en,de,fr"/>
        </c:if>
    </query:containerQuery>
    <template:container id="testContainer" cacheKey="cont1">
        <div>
            <br/>
            <template:field name='Title' containerName="testContainer"/>

            <template:field name='Text' containerName="testContainer"/>

            <template:field name='file' var='fileMultSearch' display="false" containerName="testContainer"/>
            <c:if test="${!empty fileMultSearch && fileMultSearch.file.downloadable}">
                <c:choose>
                    <c:when test="${fileMultSearch.file.image}">
                        <img border="0" width="112" height="83"
                             src="${fileMultSearch.file.downloadUrl}"
                             alt="${fileMultSearch.file.fileFieldTitle}"/>
                    </c:when>
                    <c:otherwise>
                        <a href="${fileMultSearch.file.downloadUrl}">
                            <c:out value="${fileMultSearch.file.fileFieldTitle}"/>
                        </a>
                    </c:otherwise>
                </c:choose>
            </c:if>
        </div>
    </template:container>

</template:containerList>