<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<template:addResources type="css" resources="960-fluid-admin-jahia.css"/>
<template:addResources type="css" resources="jahia-admin.css"/>
<div id="bodywrapper">
<div class="container container_16">
    <div id="topTitle">
        <div class="grid_16">
                  <h1 class="hide"><fmt:message key="administration"/> Jahia</h1>
                  <h2 class="edit"><fmt:message key="administration"/></h2>
        </div>
    </div>
</div>


<!--stop topheader-->




<div class="container container_16"><!--start container_16-->
	<div class='grid_16'><!--start grid_16-->
<div id="content"><!--start content-->
<div class="headtop">
    <div class="object-title"><fmt:message key="content.folder"/>
    </div>
</div>
    ${wrappedContent}
       <div class="clear"></div></div>
	<div class='clear'></div></div><!--stop grid_16-->
<div class='clear'></div></div><!--start container_16-->


<!--stop content-->
<div id="copyright">
  &copy; Copyright 2002-2010  <a target="newJahia" href="http://www.jahia.com">Jahia Solutions Group SA</a> -&nbsp;Tous droits réservés.&nbsp;Jahia 6.5.0 r31312</div>


<div class="clear"></div></div><!--stop bodywrapper-->