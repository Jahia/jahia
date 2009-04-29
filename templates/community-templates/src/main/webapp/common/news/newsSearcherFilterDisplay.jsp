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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%--
Copyright 2002-2008 Jahia Ltd

Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL),
Version 1.0 (the "License"), or (at your option) any later version; you may
not use this file except in compliance with the License. You should have
received a copy of the License along with this program; if not, you may obtain
a copy of the License at

 http://www.jahia.org/license/

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ include file="../declarations.jspf" %>
<c:set var="params" value="${paramValues.listCategories}" scope="request"/>
<c:set var="listCategories" value="${fn:join(params, ',')}" scope="request"/>
<c:set var="dateFilter" value="${param.dateFilter}" scope="request"/>
<div class="newslist">
    <template:jahiaPageForm name="newsPageForm" method="get">
        <fieldset>
            <legend>&nbsp;<fmt:message key='filtersAndSearchers'/>&nbsp;</legend>
            <div class="search">
                <span><fmt:message key="search" /></span>
                <input type="text" name="searchString" value="${param.searchString}"/>
                - <fmt:message key="display"/>
            </div>
            <div class="windowSizeComboBox">
                <ui:displayWindowSizeComboBox form="newsPageForm" listName="community_templates_news_news${param.id}"/>
            </div>
            <div class="itemsPerPage"><fmt:message key="itemsPerPage"/></div>
            <input type="submit" name="submitbutton" class="button"
                   value="<fmt:message key='submit'/>"/>

            <div class="categorySelectionDiv">
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
            </div>
            <div class="dateSelectionDiv">
                <fmt:message key="dateFilter"/>:
                <ui:dateSelector cssClassName="dateSelection" fieldName="dateFilter" value="${dateFilter}"/>
                <c:if test="$(empty listCategories)">
                    <c:set var="listCategories" value="${requestScope.listCategories}" scope="request"/>
                </c:if>
            </div>
            <div class="currentFilters">
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
            </div>
        </fieldset>
        <template:containerList name="news${param.id}" id="newsList" windowSize="10" actionMenuNamePostFix="newss"
                                actionMenuNameLabelKey="newss.add">
            <query:containerQuery>
                <query:selector nodeTypeName="community_templates:communityNews" selectorName="newsList"/>
                <query:descendantNode selectorName="newsList" path="${currentSite.JCRPath}"/>
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

            <%@ include file="newsDisplay.jspf" %>
        </template:containerList>
    </template:jahiaPageForm>
</div>