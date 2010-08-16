<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<jcr:nodeProperty node="${currentNode}" name="j:nbOfVotes" var="nbVotes"/>
<jcr:nodeProperty node="${currentNode}" name="j:sumOfVotes" var="sumVotes"/>
<c:set var="id" value="${currentNode.identifier}"/>
<c:if test="${nbVotes.long > 0}">
    <c:set var="avg" value="${sumVotes.long / nbVotes.long}"/>
</c:if>
<c:if test="${nbVotes.long == 0}">
    <c:set var="avg" value="0.0"/>
</c:if>
<template:addResources type="css" resources="uni-form.css,ui.stars.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.core.min.js,ui.stars.js"/>
<script type="text/javascript">
    $(document).ready(function() {
        $("#avg${id}").children().not(":input").hide();
        // Create stars for: Average rating
        $("#avg${id}").stars();
    });
</script>
<div style="display:none;">${fn:substring(avg,0,3)}</div>
<form id="avg${id}" action="">
    <input type="radio" name="rate_avg" value="1" title="Poor"
           disabled="disabled"
           <c:if test="${avg >= 1.0}">checked="checked"</c:if> />
    <input type="radio" name="rate_avg" value="2" title="Fair"
           disabled="disabled"
           <c:if test="${avg >= 2.0}">checked="checked"</c:if> />
    <input type="radio" name="rate_avg" value="3" title="Average"
           disabled="disabled"
           <c:if test="${avg >= 3.0}">checked="checked"</c:if> />
    <input type="radio" name="rate_avg" value="4" title="Good"
           disabled="disabled"
           <c:if test="${avg >= 4.0}">checked="checked"</c:if> />
    <input type="radio" name="rate_avg" value="5" title="Excellent"
           disabled="disabled"
           <c:if test="${avg >= 5.0}">checked="checked"</c:if> />
</form>
