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


<template:containerList name="IframeContainer" id="iframe"
                       actionMenuNamePostFix="Iframe" actionMenuNameLabelKey="Iframe">
    <template:container id="IframeContainer" displayActionMenu="false">
        <ui:actionMenu contentObjectName="IframeContainer" namePostFix="Iframe" labelKey="Iframe.update">
            <iframe
                    src='<template:field name="IframeSource" inlineEditingActivated="false"/>'
                    name='<template:field name="IframeName" inlineEditingActivated="false"/>'
                    width='<template:field name="IframeWidth" inlineEditingActivated="false"/>'
                    height='<template:field name="IframeHeight" inlineEditingActivated="false"/>'
                    frameborder='<template:field name="IframeFrameborder" inlineEditingActivated="false"/>'
                    marginheight='<template:field name="IframeMarginheight" inlineEditingActivated="false"/>'
                    marginwidth='<template:field name="IframeMarginwidth" inlineEditingActivated="false"/>'
                    scrolling='<template:field name="IframeScrolling" inlineEditingActivated="false"/>'
                    ><template:field name="IframeAlt" inlineEditingActivated="false"/></iframe>
        </ui:actionMenu>
    </template:container>

</template:containerList>