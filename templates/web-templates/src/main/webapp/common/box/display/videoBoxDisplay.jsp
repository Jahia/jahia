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

<%@ include file="../../declarations.jspf" %>


<template:containerList name="videoContainer" id="video"
                       actionMenuNamePostFix="video" actionMenuNameLabelKey="video.add">
    <template:container id="videoContainer" displayActionMenu="false">
        <ui:actionMenu contentObjectName="videoContainer" namePostFix="video" labelKey="video">
        <template:field name="videoSource" valueBeanID="mySourceField" display="false"/>
            <embed
                    name='<template:field name="videoName"/>'
                    pluginspage="http://www.microsoft.com/Windows/MediaPlayer/"
                    src="${pag
                    eContext.request.scheme}://${pageContext.request.localAddr}:${pageContext.request.localPort}${mySourceField.downloadUrl}"
                    width='<template:field name="videoWidth"/>'
                    height='<template:field name="videoHeight"/>'
                    type="application/x-mplayer2"
                    autostart='<template:field name="videoAutostart"/>'
                    invokeURLs='<template:field name="videoInvokeURLs"/>'
                    enablecontextmenu='<template:field name="videoEnablecontextmenu"/>'
                    showstatusbar='<template:field name="videoShowstatusbar"/>'
                    showcontrols='<template:field name="videoShowcontrols"/>'
                    AutoSize='<template:field name="videoAutosize"/>'
                    displaysize='<template:field name="videoDisplaysize"/>'>
            </embed>
        </ui:actionMenu>
    </template:container>

</template:containerList>