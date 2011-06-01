<%@ tag body-content="scriptless" description="Initializes the CKEditor configuration and replaces all elements, matching the specified selector (jQuery selector syntax) with CKEditor instances. The body of the tag contains the CKEditor configuration options to override the defaults in the form {option1: value, option2: value}. By default the CKEditor will use a simplified toolbar and no Jahia-specific content (file, image, link, etc.) pickers. The tag is supposed to be used in the content of Jahia rendering templates as it expects the renderContext bean being available." %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.

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

<%@ attribute name="selector" required="true" type="java.lang.String"
              description="The jQuery syntax based selector to find HTML elements that will be replaced with instances of the CKEditor when the page is loaded." %>
<%@ attribute name="includeJQuery" required="false" type="java.lang.Boolean"
              description="The functionality of this tag depends on the jQuery. If this attribute is set to false the jquery.min.js won't be automatically included by this tag and it is up to the template developer to include a proper jQuery version for this tag to work properly. This attribute is mainly provided to override the jQuery version included if needed. [true]" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="${empty includeJQuery || includeJQuery ? 'jquery.js,' : ''}ckeditor/ckeditor.js,ckeditor/adapters/jquery.js"/>
<jsp:doBody var="config"/>
<template:addResources>
    <script type="text/javascript">
        $(document).ready( function()
        {
            CKEDITOR.replace( '${selector}',
            $.extend({ toolbar: 'Mini',
                filebrowserBrowseUrl: null,
                filebrowserFlashBrowseUrl: null,
                filebrowserImageBrowseUrl: null,
                filebrowserLinkBrowseUrl: null
            }, ${not empty config ? config : '{}'
			}) );
        });
    </script>
</template:addResources>