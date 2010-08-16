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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>

<jcr:nodeProperty node="${currentNode}" name="name" var="name"/>
<jcr:nodeProperty node="${currentNode}" name="source" var="source"/>
<jcr:nodeProperty node="${currentNode}" name="width" var="width"/>
<jcr:nodeProperty node="${currentNode}" name="height" var="height"/>
<jcr:nodeProperty node="${currentNode}" name="hspace" var="hspace"/>
<jcr:nodeProperty node="${currentNode}" name="vspace" var="vspace"/>
<jcr:nodeProperty node="${currentNode}" name="autostart" var="autostart"/>
<jcr:nodeProperty node="${currentNode}" name="enablecontextmenu" var="enablecontextmenu"/>
<jcr:nodeProperty node="${currentNode}" name="showstatusbar" var="showstatusbar"/>
<jcr:nodeProperty node="${currentNode}" name="showcontrols" var="showcontrols"/>
<jcr:nodeProperty node="${currentNode}" name="autosize" var="autosize"/>
<jcr:nodeProperty node="${currentNode}" name="displaysize" var="displaysize"/>
<jcr:nodeProperty node="${currentNode}" name="loop" var="loop"/>
<jcr:nodeProperty node="${currentNode}" name="invokeURLs" var="invokeURLs"/>


<!-- we should write dynamically the type depending on the selected file -->


<!-- avi : video/x-msvideo -->
<!-- wmv : video/x-ms-wmv -->
<!-- mpeg : video/mpeg -->
<!-- mov : video/quicktime -->

<object data="${source.node.url}" type="video/mpeg" width="${width.long}" height="${height.long}">
  <param name="src" value="${source.node.url}">
  <param name="autoplay" value="false">
  <param name="autoStart" value="${autostart.string}">
    <param name="controller" value="${showcontrols.string}" >
  alt : <a href="${source.node.url}">${name.string}</a>
</object>



