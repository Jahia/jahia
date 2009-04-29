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
<%@ include file="../declarations.jspf" %>

<template:containerList name="pediaMainCL" id="pediaMainCL">
    <template:container id="main" cacheKey="mainContent">
        <h1><template:field name='articleHeader'/></h1>
        <h5><template:field name='articleHeaderNote'/></h5>
    </template:container>
</template:containerList>
<div>
    Contents: <br/>
    <template:containerList name="articles" id="pediaarticle" displayActionMenu="false">
        <template:container id="article" displayActionMenu="false" cacheKey="contents">

            <template:field name='articleLevel' display="false" var="artLevel"/>
            <c:forEach var="i" begin="2" end="${not empty artLevel ? artLevel.integer : 0}">
                &nbsp;&nbsp;&nbsp;&nbsp;
            </c:forEach>
            <a href="#<template:field name='articleTitle' inlineEditingActivated='false' diffActive='false'/>"><template:field name='articleTitle'/></a>
            <br class="clear"/>
        </template:container>
    </template:containerList>

</div>
<br/>
<br/>

<template:containerList name="articles" id="pediaarticle" actionMenuNamePostFix="mainContents"
                        actionMenuNameLabelKey="mainContents.add">
    <template:container id="article" actionMenuNamePostFix="mainContent" actionMenuNameLabelKey="mainContent.update"
                        cacheKey="display">

        <a name="<template:field name='articleTitle' inlineEditingActivated='false' diffActive='false'/>"></a>

        <h3><template:field name='articleTitle'/></h3>
        <h5><template:field name='articleNote'/></h5>

        <p>
            <template:field name='articleBody'/>
        </p>
        <br class="clear"/>
    </template:container>
</template:containerList>

<template:containerList name="pediaComment" id="pediaCommentCL">
    <template:container id="comment" cacheKey="mainComment">
        &nbsp;
    </template:container>
</template:containerList>