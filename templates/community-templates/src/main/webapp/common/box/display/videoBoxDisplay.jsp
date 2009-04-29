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


<template:containerList name="videoContainer" id="video"
                       actionMenuNamePostFix="video" actionMenuNameLabelKey="video.add">
    <template:container id="videoContainer" displayActionMenu="false">
        <ui:actionMenu contentObjectName="videoContainer" namePostFix="video" labelKey="video.update">
        <template:field name="videoSource" var="mySourceField" display="false"/>
            <embed
                    name="<template:field name='videoName'/>"
                    pluginspage="http://www.microsoft.com/Windows/MediaPlayer/"
                    src="${pageContext.request.scheme}://${pageContext.request.localAddr}:${pageContext.request.localPort}${mySourceField.file.downloadUrl}"
                    width="<template:field name='videoWidth'/>"
                    height="<template:field name='videoHeight'/>"
                    type="application/x-mplayer2"
                    autostart="<template:field name='videoAutostart'/>"
                    invokeURLs="<template:field name='videoInvokeURLs'/>"
                    enablecontextmenu="<template:field name='videoEnablecontextmenu'/>"
                    showstatusbar="<template:field name='videoShowstatusbar'/>"
                    showcontrols="<template:field name='videoShowcontrols'/>"
                    AutoSize="<template:field name='videoAutosize'/>"
                    displaysize="<template:field name='videoDisplaysize'/>">
            </embed>
        </ui:actionMenu>
    </template:container>

</template:containerList>
