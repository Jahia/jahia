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