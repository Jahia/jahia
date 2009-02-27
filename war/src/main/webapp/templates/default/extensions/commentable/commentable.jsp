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

<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.apache.commons.id.IdentifierUtils" %>

<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:useBean id="now" class="java.util.Date"/>

<template:getContainer containerID="${containerID}" valueID="container"/>
<template:getContainerField fieldName="isCommentable" containerBean="${container}" valueID="isCommentable"/>
<c:if test="${isCommentable.value}">
    <div class="comments">
        <template:containerList name="comment" id="comment" displayActionMenu="false"
                                actionMenuNameLabelKey="comment.add" actionMenuNamePostFix="comment">
            <!-- <h4><utility:resourceBundle resourceName="comments" defaultValue="Comments"/>:</h4> -->
            <ul>
                <template:container id="commentContainer" displayActionMenu="false">
                    <li>
                        <template:field name="commentTitle"/><br/>
                        <template:field name="commentBody"/><br/>
                        <utility:resourceBundle resourceName="postedOn" defaultValue="Posted on"/>
                        <template:field name="commentDate"/>
                        <utility:resourceBundle resourceName="by" defaultValue="by"/>
                        <template:field name="commentAuthor"/>
                    </li>
                </template:container>
            </ul>
        </template:containerList>

        <template:getContainerField fieldName="newsTitle" containerBean="${container}" valueID="newsTitle"/>

        <h5><utility:resourceBundle resourceName="postYourComment" defaultValue="Post your comment"/>:</h5>

         <template:gwtJahiaModule isTemplate="true" jahiaType="form" id='<%= "form" + IdentifierUtils.nextStringNumericIdentifier() %>' nodeType="jnt:comment" captcha="${pageContext.request.contextPath}/jcaptcha"
                         action="createNode" target="${comment.JCRPath}" cssClassName="comment" />
    </div>
</c:if>
