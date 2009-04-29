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
<c:set var="params" value="${paramValues.listCategories}" scope="request"/>
<c:set var="listCategories" value="${fn:join(params, ',')}" scope="request"/>
<c:set var="dateFilter" value="${param.dateFilter}" scope="request"/>
<template:jahiaPageForm name="pressPageForm" method="get">
    <p>
        <span><fmt:message key="search"/></span>
        <input type="text" name="searchString" class="field" value="${param.searchString}"/>
    </p>

    <p>
        - <fmt:message key="display"/>
        <ui:displayWindowSizeComboBox form="pressPageForm" listName="press${param.id}"/>
        <fmt:message key="itemsPerPage"/>
    </p>

    <p>

        <input type="submit" name="submitbutton" class="button"
               value="<fmt:message key='submit'/>"/>

    </p>

    <p>
        <fmt:message key="categoryFilter"/>:
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
        <fmt:message key="dateFilter"/>:
        <ui:dateSelector cssClassName="field" fieldName="dateFilter" value="${dateFilter}"/>
        <c:if test="$(empty listCategories)">
            <c:set var="listCategories" value="${requestScope.listCategories}" scope="request"/>
        </c:if>
    </p>

    <p>
                <span><fmt:message key="currentFilters"/>:
                    <c:choose>
                        <c:when test="${empty listCategories}">
                            <fmt:message key="none"/>
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
                <th id="datePress" scope="col"><fmt:message key="date"/></th>
                <th id="titlePress" scope="col"><fmt:message key="title"/></th>
                <th id="introPress" scope="col"><fmt:message key="intro"/></th>
                <c:if test="${requestScope.currentRequest.editMode}">
                    <th id="actionPress" scope="col"><fmt:message key="action"/></th>
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