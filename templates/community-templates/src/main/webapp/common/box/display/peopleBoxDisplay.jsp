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
<%@ include file="../../declarations.jspf" %>
<template:containerList name="peopleContainer" id="people"
                       actionMenuNamePostFix="people" actionMenuNameLabelKey="people.add">
        <template:container id="peopleContainer" displayActionMenu="false">
            <ui:actionMenu contentObjectName="peopleContainer" namePostFix="people" labelKey="people.update">
            <ul>
                <li>name : <template:field name="peopleFirstname"/></li>
                <li>lastname : <template:field name="peopleLastname"/></li>
                <li>title : <template:field name="peopleTitle"/></li>
                <li>birthdate : <template:field name="peopleBirthdate"/></li>
                <li>gender : <template:field name="peopleGender"/></li>
                <li>civility : <template:field name="peopleCivility"/></li>
                <li>nationality : <template:field name="peopleNationality"/></li>
                <li>picture : <template:image file="peoplePicture"/></li>
            </ul>
            </ui:actionMenu>
        </template:container>
</template:containerList>
