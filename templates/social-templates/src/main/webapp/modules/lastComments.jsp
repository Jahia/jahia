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
            <h4><fmt:message key="social_templates_lastComments.recent"/></h4>
            <ul class="footer-recent-comments">
            <c:set var="isComment" value="false"/>
            <template:containerList maxSize="${lastCommentsMaxEntries.integer}" id="newsList" displayActionMenu="false">
                <query:containerQuery>
                    <query:selector nodeTypeName="jnt:comment" selectorName="comments"/>
                    <query:descendantNode selectorName="comments" path="${currentSite.JCRPath}"/>
                    <query:setProperty name="${queryConstants.FILTER_CREATORS}" value="JahiaDBFilterCreator"/>
                    <query:setProperty name="${queryConstants.DB_MAX_RESULT}" value="${lastCommentsMaxEntries.integer}"/>
                    <query:sortBy propertyName="commentDate" order="${queryConstants.ORDER_DESCENDING}"/>
                </query:containerQuery>
                    <template:container id="comment" cacheKey="lastcomment" displayExtensions="false" displayActionMenu="false">
                        <c:set var="isComment" value="true"/>
                        <li class="">
                            <a href="?article=${comment.parent.pageID}"><template:field name='commentTitle'/></a>
                        </li>
                    </template:container>
            </template:containerList>
                <c:if test="${isComment eq 'false'}">
                    <li class=""><fmt:message key="no.comment"/></li>
                </c:if>
                </ul>
        </div>
    </div>
    <!--stop columnspace -->
    <div class="clear"></div>
</div>
<!--stop column-item -->