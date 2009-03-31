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
<%@ include file="../common/declarations.jspf" %>

<template:containerList name="owner" id="ownerContainer" actionMenuNamePostFix="owner" actionMenuNameLabelKey="owner">
    <template:container id="ownerContent">
<h3><fmt:message key="aboutMe"/> </h3>

                        <div class="aboutMeListItem"><!--start aboutMeListItem -->
                            <div class="aboutMePhoto">
                                <template:image file="picture"/>
                            </div>
                            <div class="aboutMeBody"><!--start aboutMeBody -->
                                <h5><template:field name="firstname" inlineEditingActivated="false"/>&nbsp;<template:field name="lastname" inlineEditingActivated="false"/></h5>
                                <template:field name="birthdate" var="birthdate" display="false"/>
                                <fmt:formatDate pattern="yyyy" value="${birthdate.date}" var="birthyear"/>
                                <jsp:useBean id="now" class="java.util.Date" />
                                <fmt:formatDate pattern="yyyy" value="${now}" var="actualyear"/>
                                <p class="aboutMeAge"><fmt:message key="age">
                                        <fmt:param value="${actualyear - birthyear }"/>
                                </fmt:message> </p>

                                <div class="clear"></div>
                                <p class="aboutMeResume"><template:field name="description"/></p>
                            </div>
                            <!--stop aboutMeBody -->
                            <div class="clear"></div>
                        </div>                        <!--stop aboutMeListItem -->
    </template:container>
</template:containerList>