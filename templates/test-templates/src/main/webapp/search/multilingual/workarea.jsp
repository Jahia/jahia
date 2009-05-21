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