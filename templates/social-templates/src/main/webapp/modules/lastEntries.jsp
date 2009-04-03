
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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../common/declarations.jspf" %>

<div class="column-item"><!--start column-item -->
    <div class="columnspace"><!--start columnspace -->
        <div class="mapshortcuts"><!--start bottomshortcuts-->
            <h4><fmt:message key="social_templates_lastEntries.recent"/></h4>
            <template:containerList name="lastEntries" id="lastEntries"
                                    actionMenuNamePostFix="lastentriess" actionMenuNameLabelKey="lastentriess">
                <template:container id="lastEntry" cache="off" actionMenuNamePostFix="lastentries"
                                    actionMenuNameLabelKey="lastentries.update">
                    <template:field name="maxEntries" var="maxEntries" defaultValue="10" display="false"/>
                    <c:if test="${requestScope.currentRequest.editMode}">
                        <div class="preferences">
                            <h5><fmt:message key="social_templates_lastEntries.preferences"/></h5>

                            <p class="preference-item"><span class="preference-label"><fmt:message
                                    key="social_templates_lastEntries.rowsDisplay"/>: </span><span
                                    class="preference-value">${maxEntries.integer}</span></p>
                        </div>
                    </c:if>
                    <c:set var="maxEntries" value="${maxEntries}"/>
                </template:container>
            </template:containerList>
            <template:containerList maxSize="${maxEntries.integer}" id="newsList" displayActionMenu="false">
                <query:containerQuery>
                    <query:selector nodeTypeName="social_templates:blogEntry" selectorName="blogEntry"/>
                    <query:descendantNode selectorName="blogEntry" path="${currentSite.JCRPath}"/>
                    <query:setProperty name="${queryConstants.SEARCH_MAX_HITS}" value="${maxEntries.integer}"/>
                    <query:sortBy propertyName="date" order="${queryConstants.ORDER_DESCENDING}"/>
                </query:containerQuery>
                <ul>
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

