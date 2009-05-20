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
<%@ include file="../../common/declarations.jspf"%>

<template:containerList name="displayCL" id="displayContainerList" actionMenuNamePostFix="testContainers"
    actionMenuNameLabelKey="testContainers.add">
	<template:container id="display">
		<b><fmt:message key='display.image'/>:</b><br/>
		<template:image file="Image"/>
    <br/>
    <b><fmt:message key='display.imagelink'/>:</b>
    <template:link page="" image="Image" />
    
    <br/>
    
    <b><fmt:message key='display.file'/>:</b>
    <template:file file="File" />
    <br/>
    <b><fmt:message key='display.filedetails'/>:</b>
    
    <br/>
    <b><fmt:message key='display.link'/>:</b>
    <template:link page="page" openExternalLinkInNewWindow="true"/>
    
    <br/>
    <b><fmt:message key='display.randomimage'/> (<template:field name="RandomPath" var="RandomPath" />):</b>
    <ui:displayRandomImage path="RandomPath" fileTypes="jpg,gif,png"/>
    
	</template:container>
</template:containerList>