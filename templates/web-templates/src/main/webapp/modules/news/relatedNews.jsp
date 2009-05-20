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
<c:if test="${!empty param.categories}">
<div class="relatedNews"><!--start relatedNews -->
    <template:containerList id="newsRelatedList" windowSize="5" displayActionMenu="false">
        <query:containerQuery>
            <query:selector nodeTypeName="web_templates:newsContainer" selectorName="newsRelated"/>
            <query:descendantNode selectorName="newsRelated" path="${currentSite.JCRPath}"/>
            <query:equalTo propertyName="${queryConstants.CATEGORY_LINKS}" value="${param.categories}"
                           multiValue="true" metadata="true"/>
             <query:setProperty name="${queryConstants.SEARCH_MAX_HITS}" value="5" />
            <query:sortBy propertyName="newsDate" order="${queryConstants.ORDER_DESCENDING}"/>
        </query:containerQuery>
        <div class="box2 box2-style1"><!--start box 2 style1 -->

            <div class="box2-topright"></div>
            <div class="box2-topleft"></div>
            <h3 class="box2-header"><span><fmt:message key='relatedNews'/></span></h3>

            <div class="box2-text">
                <ul class="summary"><!--start summary -->
                    <template:container id="newsRelatedContainer" displayActionMenu="false"
                                        cacheKey="newsRelatedList${param.containerid}" displayExtensions="false" displayContainerAnchor="false">
                        <c:if test="${param.containerid != newsRelatedContainer.ID}">
                            <li class="summary">
                                <h4>
                                    <a href="${currentPage.url}/template/tpl.newsDetail?queryPath=${newsRelatedContainer.JCRPath}"><template:field
                                            name='newsTitle'/></a></h4>

                                <p class="summaryresume"><template:field name="newsDesc" maxChar="150"/></p>
                            </li>
                        </c:if>
                    </template:container>
                </ul>
                <!--stop summary -->
            </div>
            <div class="box2-bottomright"></div>
            <div class="box2-bottomleft"></div>
            <div class="clear"> </div>
        </div>
        <!--stop box 2 style 1-->
    </template:containerList>
</div>
</c:if>
<!--stop relatedNews -->