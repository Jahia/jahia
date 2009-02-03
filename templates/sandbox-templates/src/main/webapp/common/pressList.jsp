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
<c:set var="params" value="${paramValues.listCategories}" scope="request"/>
<c:set var="listCategories" value="${fn:join(params, ',')}" scope="request"/>
<c:set var="dateFilter" value="${param.dateFilter}" scope="request"/>
<template:jahiaPageForm name="pressPageForm" method="get">
    <p>
        <span><utility:resourceBundle resourceName="search" defaultValue="Search"/></span>
        <input type="text" name="searchString" class="field" value="${param.searchString}"/>
    </p>

    <p>
        - <utility:resourceBundle resourceName="display" defaultValue="Display"/>
        <ui:displayWindowSizeComboBox form="pressPageForm" listName="press${param.id}"/>
        <utility:resourceBundle resourceName="itemsPerPage"
                                defaultValue="(Items / Page)"/>
    </p>

    <p>

        <input type="submit" name="submitbutton" class="button"
               value="<utility:resourceBundle resourceName='submit' defaultValue='Submit'/>"/>

    </p>

    <p>
        <utility:resourceBundle resourceName="categoryFilter"
                                defaultValue="Filter by categories"/>:
        <!-- displayCategorySelection (start) -->
        <query:categoryFilter startingCategoryKey="root"
                              display="selectBoxSingle"
                              name="listCategories"
                              level="1"
                              messageKey="noCategories"
                              selected="${listCategories}"
                              displayRootCategory="false"
                              cssClassName="categorySelection"/>
        <!-- displayCategorySelection (end) -->
    </p>

    <p>
        <utility:resourceBundle resourceName="dateFilter" defaultValue="Date filter"/>:
        <ui:dateSelector cssClassName="field" fieldName="dateFilter" value="${dateFilter}"/>
        <c:if test="$(empty listCategories)">
            <c:set var="listCategories" value="${requestScope.listCategories}" scope="request"/>
        </c:if>
    </p>

    <p>
                <span><utility:resourceBundle resourceName="currentFilters"
                                              defaultValue="Category filters currently applied"/>:
                    <c:choose>
                        <c:when test="${empty listCategories}">
                            <utility:resourceBundle resourceName="none" defaultValue="None"/>
                        </c:when>
                        <c:otherwise>
                            <ui:displayCategoryTitle categoryKeys="${listCategories}"/>
                        </c:otherwise>
                    </c:choose>
                </span>
    </p>

    <div class="maincontentList">
    <template:containerList name="pressRelease" id="pressRelease" displayActionMenu="false" windowSize="2">
        <query:containerQuery>
            <query:selector nodeTypeName="sandbox_templates:pressreleaseContainer" selectorName="pressList"/>
            <query:descendantNode selectorName="pressList" path="${currentSite.JCRPath}"/>
            <c:if test="${!empty listCategories}">
                <query:equalTo propertyName="${queryConstants.CATEGORY_LINKS}" value="${listCategories}"
                               metadata="true" multiValue="true"/>
            </c:if>
            <c:if test="${!empty dateFilter}">
                <utility:dateUtil currentDate="${dateFilter}" valueID="today" hours="0" minutes="0"
                                    seconds="0"/>
                <utility:dateUtil currentDate="${dateFilter}" valueID="tomorrow" days="1" hours="0" minutes="0"
                                    seconds="0"/>
                <query:greaterThan numberValue="true" propertyName="newsDate" value="${today.time}"/>
                <query:lessThanOrEqualTo numberValue="true" propertyName="newsDate" value="${tomorrow.time}"/>
            </c:if>
            <c:if test="${!empty param.searchString}">
                <query:fullTextSearch searchExpression="${param.searchString}"/>
            </c:if>
            <query:sortBy propertyName="newsDate" order="${queryConstants.ORDER_DESCENDING}"/>
        </query:containerQuery>
        <table class="tab" width="100%">
            <colgroup>
                <col span="1" width="25%" class="col1"/>
                <col span="1" width="25%" class="col2"/>
                <col span="1" width="25%" class="col3"/>
                <c:if test="${requestScope.currentRequest.editMode}">
                    <col span="1" width="25%" class="col4"/>
                </c:if>
            </colgroup>
            <thead>
            <tr>
                <th id="datePress" scope="col"><utility:resourceBundle resourceName="date"
                                                                       defaultValue="Date"/></th>
                <th id="titlePress" scope="col"><utility:resourceBundle resourceName="title"
                                                                        defaultValue="Title"/></th>
                <th id="introPress" scope="col"><utility:resourceBundle resourceName="intro"
                                                                        defaultValue="Intro"/></th>
                <c:if test="${requestScope.currentRequest.editMode}">
                    <th id="actionPress" scope="col"><utility:resourceBundle resourceName="action"
                                                                             defaultValue="Action"/></th>
                </c:if>
            </tr>
            </thead>
            <tbody>
            <template:container id="pressReleaseContainer" displayActionMenu="false">
                <tr>
                    <td headers="datePress">&nbsp;<template:field name='pressReleaseDate'/></td>
                    <td headers="titlePress"><a
                            href="template/pressDetail?id=${pressReleaseContainer.ID}"><template:field
                            name='pressReleaseTitle'/></a></td>
                    <td headers="datePress">&nbsp;<template:field name='pressReleaseIntro'/></td>
                    <c:if test="${requestScope.currentRequest.editMode}">
                        <td><ui:actionMenu contentObjectName="pressReleaseContainer"
                                           namePostFix="pressRelease"
                                           labelKey="pressRelease.update"/></td>
                    </c:if>
                </tr>
            </template:container>
            </tbody>
            <c:if test="${requestScope.currentRequest.editMode}">
                <tfoot>
                <tr>
                    <td colspan="4"><ui:actionMenu contentObjectName="pressRelease"
                                                   namePostFix="pressRelease"
                                                   labelKey="pressRelease.add"/></td>
                </tr>
                </tfoot>
            </c:if>
        </table>
    </template:containerList>
</template:jahiaPageForm>
</div>