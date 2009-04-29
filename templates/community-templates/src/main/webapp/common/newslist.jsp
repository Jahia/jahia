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

<div class="newsList"><!--start newslist -->
    <h3><fmt:message key='news'/></h3>
    <template:containerList id="newsList" windowSize="2" displayActionMenu="false">
        <query:containerQuery>
            <query:selector nodeTypeName="community_templates:communityNews" selectorName="news"/>
            <query:descendantNode selectorName="newsList" path="${currentSite.JCRPath}"/>
            <query:sortBy propertyName="newsDate" order="${queryConstants.ORDER_DESCENDING}"/>
        </query:containerQuery>
        <template:container id="newsContainer" encapsulatingDivCssClassName="news" displayActionMenu="false"
                            cacheKey="newslist">
            <div class="newsListItem"><!--start newsListItem -->
                <div class="newsImg"><a href="#"><template:image file="newsImage"/></a></div>
                <template:field name="newsTitle" var="newsTitle" display="false"/>
                <h4><template:link page="newsLink" linkBody="${newsTitle}"/>
                </h4>

                <p class="newsInfo">
                    <span class="newsLabelName">Auteur :</span>
                    <span class="newsName"></span>
                    <span class="newsLabelDate">Date :</span>
                    <span class="newsDate"><template:field name='newsDate'/></span>
                </p>

                <p class="newsResume">
                    <template:field name="newsDesc"/>
                </p>

                <div class="newsMeta">
        	<span class="categoryLabel"><fmt:message key='category'/>  :</span>
                    <template:getContentObjectCategories valueID="newsContainerCatKeys"
                                                         objectKey="contentContainer_${pageScope.newsContainer.ID}"/>
                    <ui:displayCategoryTitle categoryKeys="${newsContainerCatKeys}"/>
                </div>
                <div class="clear"></div>

            </div>
            <!--stop newsListItem -->
        </template:container>
    </template:containerList>


    <div class="clear"></div>
</div>
<!--stop newslist -->

