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
<template:containerList name="blogEntries" id="blogEntriesPagination" displayActionMenu="false">
<template:containerForm ignoreAcl="true" var="inputs" action="${currentPage.url}">
    <div id="comment-form">
            <div id="commentsForm">
             <fieldset>
                 <p class="field">
                     <label for="c_name"><fmt:message key="article.date"/> :</label>
                     <input type="text" size="30" id="c_date" name="${inputs['date'].name}"
                            value="${inputs['date'].defaultValue}" tabindex="20"/>
                 </p>
                    <p class="field">
                        <label for="c_name"><fmt:message key="article.title"/> :</label>
                        <input type="text" size="30" id="c_name" name="${inputs['title'].name}"
                               value="${inputs['title'].defaultValue}" tabindex="20"/>
                    </p>
                 <p class="field">
                     <label for="c_name"><fmt:message key="article.content"/> :</label>
                     <input type="text" size="30" id="c_content" name="${inputs['content'].name}"
                            value="${inputs['content'].defaultValue}" tabindex="20"/>
                 </p>
                 <p class="c_button">
                     <input type="submit" value="envoyer" class="button" tabindex="11"/>
                 </p>
             </fieldset>
            </div>
    </div>
</template:containerForm>
</template:containerList>