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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<html>
<head>
    <title><internal:message key="button.mashupmanager"/></title>


    <link rel="stylesheet" type="text/css" media="screen"
          href="<%= request.getContextPath() %>/engines/gwtfilemanager/javascript/uvumi-crop.css"/>
    <style type="text/css">
        .yellowSelection {
            border: 2px dotted #FFB82F;
        }

        .blueMask {
            background-color: #00f;
            cursor: pointer;
        }
    </style>
    <internal:gwtInit standalone="true"/>
    <internal:gwtImport module="org.jahia.ajax.gwt.module.filemanager.FileManager"/>
</head>
<body>
<internal:fileManager enginemode="true"
                      rootPath="/content/mashups"
                      startPath="/content/mashups"
                      nodeTypes="jnt:portlet"
                      conf="mashupmanager"/>
<internal:gwtGenerateDictionary/>
</body>
</html>