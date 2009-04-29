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

<template:containerList name="banner" id="banner" actionMenuNamePostFix="banner" actionMenuNameLabelKey="banner">
    <template:container id="bannerContainer"  emptyContainerDivCssClassName="mockup-banner">
    <template:field name='background' var="background" display="false"/>

    <div id="illustration2" style="background:transparent url(${background.file.downloadUrl}) no-repeat top left;">
        <div class="illustration2-text" style='margin-top:<template:field name="positionTop"/>px; margin-left:<template:field name="positionLeft"/>px'>
            <h2><template:field name="title"/></h2>
            <p><template:field name="cast"/></p>
        <div class="clear"> </div></div>
    </div>
    </template:container>
</template:containerList>