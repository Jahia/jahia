<%@ page import="java.util.Iterator" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/queryLib" prefix="query" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>


<template:containerList name="fileContainer" id="files"
                        actionMenuNamePostFix="files" actionMenuNameLabelKey="files.add">
    <ul>
        <template:container id="fileContainer" displayActionMenu="false">

            <template:field name="fileDisplayDetails" var="displayDetails" display="false"/>
            <li class="document">
                <ui:actionMenu contentObjectName="fileContainer" namePostFix="file" labelKey="file.update">
                    <template:field name="file"/>
                    <template:field name="fileDesc"/>
                </ui:actionMenu>
            </li>
        </template:container>
    </ul>
</template:containerList>