<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="portal.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.core.min.js,inettuts.js"/>
<jcr:node path="/shared/portalComponents" var="widgets"/>
<div class="content clearfix">
<div class="left">
<h3>Corporate Portal</h3>
<p class="grey">Jahia offers the ability to place portlets or social gadgets on any page of your site as easily as if you were adding a piece of text or a picture. Thanks to its built-in Mashup Center, empowered end users can manage, categorize or instantiate the hundreds of possible micro-applications through a unified and centralized interface, regardless of the underlying technology.</p>

</div>
<div class="left">
<h3>Add Portal Components</h3>
<ul class="panellist">
    <c:forEach items="${widgets.nodes}" var="node" varStatus="status">
        <li>
            <div onclick="addWidget('${node.path}','${node.name}')">
                <span><jcr:nodeProperty node="${node}" name="jcr:title" var="title"/><c:if
                        test="${not empty title}">${title.string}</c:if><c:if test="${empty title}">${node.name}</c:if></span>
            </div>
        </li>
    </c:forEach>
</ul>
</div>
<div class="left right">
	<h3>Add RSS</h3>
        <form  class="Form" action="" method="post">
            <p>
            <label>Rss feed URL :</label>
            <input type="text" name="feedUrl" id="feedUrl" maxlength="256"/>
            </p>
            <p>
            <label>Number of feeds :</label>
            <input type="text" name="nbFeeds" id="nbFeeds" maxlength="2" value="5"/>
            </p>
        </form>
        <button name="addRss" type="button" value="Add Rss" onclick="addRSSWidget()">Add Rss Widget</button>

</div>
</div>