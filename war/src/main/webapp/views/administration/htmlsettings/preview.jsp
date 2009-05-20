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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>FCKeditor - Toolbar - Preview</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="robots" content="noindex, nofollow" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/htmleditors/fckeditor/fckeditor.js"></script>
</head>

<body>
        <script type="text/javascript">
<!--
var oFCKeditor = new FCKeditor( 'FCKeditor1' ) ;
oFCKeditor.BasePath = '${pageContext.request.contextPath}/htmleditors/fckeditor/' ;
oFCKeditor.Height   = 150 ;
oFCKeditor.Value    = '<p>This is some <strong>sample text<\/strong>.<\/p>' ;
oFCKeditor.Config["CustomConfigurationsPath"] = "${pageContext.request.contextPath}/htmleditors/fckeditor/fckconfig_jahia.js"
oFCKeditor.ToolbarSet = "${not empty param.toolbar ? param.toolbar : 'Basic'}";
oFCKeditor.Create() ;
//-->
        </script>
</body>
</html>
