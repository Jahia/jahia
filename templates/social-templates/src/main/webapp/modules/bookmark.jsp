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
<div class="bookmarksList">
    <h3><fmt:message key="social_templates.bookmark"/></h3>
    <ul>
        <template:containerList name="bookmarks" id="bookmarks"
                                actionMenuNamePostFix="bookmarkss" actionMenuNameLabelKey="bookmarkss">
            <template:container id="lastEntry" cache="off" actionMenuNamePostFix="bookmarks"
                                actionMenuNameLabelKey="bookmarks.update">
                <template:field name="url" var="urlBookmark" display="false"/>
                <template:field name="name" var="nameBookmark" display="false"/>

                <li><a class="bookmarksListTitle" title="${nameBookmark}"
                       href="${urlBookmark}">${nameBookmark}</a>
                    <div class="bookmarksListDes">
                        <template:field name="note"/>
                    </div>
                </li>
            </template:container>
     </ul>
    <div class="bookmarksForm">
            <template:containerForm var="bookmarkform">
                <h3><fmt:message key="bookmark.add"/></h3>
                    <p class="field"><label><fmt:message key="bookmark.url"/></label><input type="text" name="${bookmarkform['url'].name}"></p>
                    <p class="field"><label><fmt:message key="bookmark.name"/></label><input type="text" name="${bookmarkform['name'].name}"></p>
                    <p class="field">
                        <label><fmt:message key="bookmark.note"/></label>
                        <textarea id="bookmarks_content" tabindex="14" name="${bookmarkform['note'].name}" cols="15" rows="4"></textarea>
                    </p>
                    <p class="bookmarks_button"><input class="button" type="submit" value="<fmt:message key='bookmark.add'/>"/></p>
            </template:containerForm>

        </template:containerList>
    </div>
</div>