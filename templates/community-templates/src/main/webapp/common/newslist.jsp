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

<div class="newsList"><!--start newslist -->
    <h3><utility:resourceBundle resourceName='news' defaultValue="News"/></h3>
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
                <template:field name="newsTitle" valueBeanID="newsTitle" display="false"/>
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
        	<span class="categoryLabel"><utility:resourceBundle
                    resourceName='category' defaultValue='category'/>  :</span>
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

