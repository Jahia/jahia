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

<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../declarations.jspf" %>
<c:set var="params" value="${paramValues.listCategories}" scope="request"/>
<c:set var="listCategories" value="${fn:join(params, ',')}" scope="request"/>
<c:set var="dateFilter" value="${param.dateFilter}" scope="request"/>

<div class="newslist">
    <template:jahiaPageForm name="newsPageForm" method="get">
        <fieldset>
            <legend>&nbsp;<fmt:message key='filtersAndSearchers'/>&nbsp;</legend>
            <div class="search">
                <span><fmt:message key="search"/></span>
                <input type="text" name="searchString" value="${param.searchString}"/>
                - <fmt:message key="display"/>
            </div>
            <div class="windowSizeComboBox">
                <ui:displayWindowSizeComboBox form="newsPageForm" listName="core_templates_news_news${param.id}"/>
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
        <template:containerList name="news${param.id}" id="newsList" windowSize="10" displayActionMenu="false">
            <query:containerQuery>
                <query:selector nodeTypeName="core_template:commentableNews" selectorName="newsList"/>
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