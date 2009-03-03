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

            <template:field name='articleLevel' display="false" valueBeanID="artLevel"/>
            <c:forEach var="i" begin="2" end="${artLevel.integer}">
                &nbsp;&nbsp;&nbsp;&nbsp;
            </c:forEach>
            <a href="#<template:field name='articleTitle'/>"><template:field name='articleTitle'/></a>
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

        <a name="<template:field name='articleTitle' />"></a>

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