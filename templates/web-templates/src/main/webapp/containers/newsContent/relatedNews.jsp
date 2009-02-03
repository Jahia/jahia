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

<%@ include file="../../common/declarations.jspf" %>

<div class="relatedNews"><!--start relatedNews -->
    <template:containerList id="newsRelatedList" windowSize="5" displayActionMenu="false">
        <query:containerQuery>
            <query:selector nodeTypeName="web_templates:newsContainer" selectorName="newsRelated"/>
            <query:descendantNode selectorName="newsRelated" path="${currentSite.JCRPath}"/>
            <query:equalTo propertyName="${queryConstants.CATEGORY_LINKS}" value="${param.categories}"
                           multiValue="true" metadata="true"/>
            <query:sortBy propertyName="newsDate" order="${queryConstants.ORDER_DESCENDING}"/>
        </query:containerQuery>
        <div class="box2 box2-style1"><!--start box 2 style1 -->

            <div class="box2-topright"></div>
            <div class="box2-topleft"></div>
            <h3 class="box2-header"><span><utility:resourceBundle
                    resourceName='relatedNews' defaultValue='Related News'/></span></h3>

            <div class="box2-text">
                <ul class="summary"><!--start summary -->
                    <template:container id="newsRelatedContainer" displayActionMenu="false"
                                        cacheKey="newsRelatedList${param.containerid}" displayExtensions="false">
                        <c:if test="${param.containerid != newsRelatedContainer.ID}">
                            <li class="summary">
                                <h4>
                                    <a href="${currentPage.url}/template/newsDetail?queryPath=${newsRelatedContainer.JCRPath}"><template:field
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
            <div class="clear"></div>
        </div>
        <!--stop box 2 style 1-->
    </template:containerList>
</div>
<!--stop relatedNews -->