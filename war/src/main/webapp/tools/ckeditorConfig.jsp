<%@page import="org.apache.commons.io.*,java.io.*,java.util.jar.*,org.jahia.settings.SettingsBean"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%!
public void createJar(String cfg, OutputStream os) throws IOException {
    Manifest manifest = new Manifest();
    Attributes attrs = manifest.getMainAttributes();
    attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    attrs.putValue("Bundle-ManifestVersion", "2");
    attrs.putValue("Bundle-Name", "CKEditor Custom Configuration");
    attrs.putValue("Bundle-SymbolicName", "ckeditor-config");
    attrs.putValue("Bundle-Version", "1.0");
    attrs.putValue("Fragment-Host", "ckeditor");
    JarOutputStream jarOutputStream = new JarOutputStream(os, manifest);

    jarOutputStream.putNextEntry(new JarEntry("javascript/config.js"));
    jarOutputStream.write(cfg.getBytes(Charsets.UTF_8));
    jarOutputStream.closeEntry();
    jarOutputStream.close();
}
%><c:if test="${not empty param.config && param.action=='Create and download configuration'}" var="download"><%
response.setContentType("application/java-archive; charset=UTF-8");
response.setHeader("Content-Disposition", "attachment; filename=\"ckeditor-config-1.0.jar");
createJar(request.getParameter("config"), response.getOutputStream());
%></c:if><c:if test="${!download}"
><%@ page contentType="text/html; charset=UTF-8" language="java"%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/tools/tools.css" type="text/css" />
<title>CKEditor Custom Configuration</title>
</head>
<body>
<h1>CKEditor Custom Configuration</h1>
<c:if test="${not empty param.config}">
<%
File cfgFile = new File(FileUtils.getTempDirectory(), "ckeditor-config-1.0.jar");
FileOutputStream fos = new FileOutputStream(cfgFile);
createJar(request.getParameter("config"), fos);
File targetFile = new File(SettingsBean.getInstance().getJahiaModulesDiskPath(), cfgFile.getName());
FileUtils.copyFile(cfgFile, targetFile);
FileUtils.deleteQuietly(cfgFile);
%>
<p style="color: blue;">
CKEditor configuration bundle created and deployed to: <%= targetFile %><br/>
Please, wait for the Digital Factory server to deploy the bundle for changes to be effective. 
</p>
</c:if>
<form id="cke" action="ckeditorConfig.jsp" method="post">
<p>Paste here your custom CKEditor configuration:</p>
<p><textarea rows="20" cols="120" id="config" name="config"><c:if test="${empty param.config}">
CKEDITOR.editorConfig = function( config ) {
    config.extraPlugins='mathjax';
    config.toolbar_Full[8]=['Image','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak','Mathjax'];
}
</c:if><c:if test="${not empty param.config}">${param.config}</c:if></textarea></p>
<p><input type="submit" name="action" value="Create and download configuration" /><input type="submit" name="action" value="Create and deploy configuration" /></p>
</form>
<%@ include file="gotoIndex.jspf" %>
</body>
</html></c:if>