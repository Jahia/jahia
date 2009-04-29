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
<%@include file="../declarations.jspf" %>

<template:absoluteContainerList name="bottomLinks" id="bottomLinks" actionMenuNameLabelKey="bottomLinks.add" pageLevel="1">
    <!--start  5 columns -->
    <div class="columns5">
        <template:container id="bottomLinkContainer" displayActionMenu="false">
            <div class="column-item">
                <div class="spacer"><!--start mapshortcuts-->
                    <div class="mapshortcuts">
                        <ui:actionMenu contentObjectName="bottomLinkContainer" namePostFix="bottomLink"
                                                       labelKey="bottomLink.update">
                        <template:field name="link" var="links" display="false"/>
                        <h4><template:field name="link"/></h4>
                        <ui:sitemap enableDescription="true" startPid="${links.page.linkID}" maxDepth="1"/>
                        </ui:actionMenu>
                    </div>
                </div>
                <div class="clear"> </div>
            </div>
        </template:container>
        <div class="clear"> </div>
    </div>
    </template:absoluteContainerList>
<!--stop 5 columns -->
<div class="clear"> </div>
