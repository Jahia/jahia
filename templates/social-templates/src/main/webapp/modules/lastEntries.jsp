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

<%@ include file="../common/declarations.jspf" %>

<div class="column-item"><!--start column-item -->
    <div class="columnspace"><!--start columnspace -->
        <div class="mapshortcuts"><!--start bottomshortcuts-->
            <h4><fmt:message key="social_templates_lastEntries.recent"/></h4>
            <template:containerList id="newsList" displayActionMenu="false">
                <query:containerQuery>
                    <query:setProperty name="${queryConstants.FILTER_CREATORS}" value="JahiaDBFilterCreator"/>
                    <query:selector nodeTypeName="social_templates:blogEntry" selectorName="blogEntry"/>
                    <query:descendantNode selectorName="blogEntry" path="${currentSite.JCRPath}"/>
                    <query:setProperty name="${queryConstants.DB_MAX_RESULT}" value="${lastEntriesMaxEntries.integer}"/>
                    <query:sortBy propertyName="date" order="${queryConstants.ORDER_DESCENDING}"/>
                </query:containerQuery>
                <ul class="footer-recent-posts">
                    <template:container id="blogLastEntry" cacheKey="lastEntry" displayExtensions="false" displayActionMenu="false">
                        <li class="">
                            <a href="?article=${blogLastEntry.ID}"><template:field name='title'/></a>
                            <p class="small"><template:field name='date'/></p>
                        </li>
                    </template:container>
                </ul>
            </template:containerList>
        </div>
    </div>
    <!--stop columnspace -->
    <div class="clear"></div>
</div>
<!--stop column-item -->

