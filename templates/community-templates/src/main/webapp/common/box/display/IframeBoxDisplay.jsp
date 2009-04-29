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
                       actionMenuNamePostFix="Iframe" actionMenuNameLabelKey="Iframe.add">
    <template:container id="IframeContainer" displayActionMenu="false">
        <ui:actionMenu contentObjectName="IframeContainer" namePostFix="Iframe" labelKey="Iframe.update">
            <iframe
                    src="<template:field name="IframeSource"/>"
                    name="<template:field name="IframeName"/>"
                    width="<template:field name="IframeWidth"/>"
                    height="<template:field name="IframeHeight"/>"
                    frameborder="<template:field name="IframeFrameborder"/>"
                    marginheight="<template:field name="IframeMarginheight"/>"
                    marginwidth="<template:field name="IframeMarginwidth"/>"
                    scrolling="<template:field name="IframeScrolling"/>"
                    ><template:field name="IframeAlt"/></iframe>
        </ui:actionMenu>
    </template:container>

</template:containerList>
