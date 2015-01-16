<%@ page contentType="text/html; charset=UTF-8" language="java"%>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="java.io.*" %>
<%@page import="java.util.*"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="org.jahia.services.content.JCRContentUtils" %>
<%@page import="org.jahia.services.textextraction.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/tools/tools.css" type="text/css" />
<title>Jahia Text Extraction Service</title>
<script type="text/javascript">
    function go(form, id1, value1, id2, value2, id3, value3) {
        document.getElementById(id1).value=value1;
        if (id2) {
            document.getElementById(id2).value=value2;
        }
        if (id3) {
            document.getElementById(id3).value=value3;
        }
        document.getElementById(form).submit();
    }
</script>
</head>
<%
if ("stopExtractionCheck".equals(request.getParameter("action"))) {
    TextExtractionHelper.forceStopExtractionCheck();
}
pageContext.setAttribute("extractionCheckRunning", TextExtractionHelper.isCheckingExtractions());
%>
<body>
<h1>Jahia Text Extraction Service</h1>
<p>This tool aims to perform text extractions on documents in the repository and store it in the j:extractedText property. Another option allows to extract the text from a local file and immediately show the results.</p>

<c:if test="${param.action == 'reportMissingExtraction' || param.action == 'fixMissingExtraction' || param.action == 'reportExtractionByFilter' || param.action == 'redoExtractionByFilter'}">
<pre>
<%
String action = request.getParameter("action");
long timer = System.currentTimeMillis();
try {
    ExtractionCheckStatus status;
    if (action.contains("MissingExtraction")) {
        status = TextExtractionHelper.checkMissingExtraction("fixMissingExtraction".equals(action), out);
    } else {
        Set<String> mimeTypes = new HashSet<String>();
        List<String> mappedMimeTypes = JCRContentUtils.getInstance().getMimeTypes().get(request.getParameter("fileType"));
        if (mappedMimeTypes != null) {
            mimeTypes.addAll(mappedMimeTypes);
        }
        String mimeType = request.getParameter("mimeType");
        if (StringUtils.isNotBlank(mimeType)) {
            mimeTypes.addAll(Arrays.asList(mimeType.split("\\s*(,|\\s)\\s*")));
        }
        RepositoryFileFilter filter = new RepositoryFileFilter(request.getParameter("workspace"), mimeTypes, 
                StringEscapeUtils.unescapeHtml(request.getParameter("path")), request.getParameter("includeSubnodes") != null, 
                StringEscapeUtils.unescapeHtml(request.getParameter("filenamePattern")));
        status = TextExtractionHelper.checkExtractionByFilter("redoExtractionByFilter".equals(action), filter, out);
    }

    pageContext.setAttribute("status", status);
} catch (Exception e) {
    e.printStackTrace();
    pageContext.setAttribute("error", e);
} finally {
    pageContext.setAttribute("took", System.currentTimeMillis() - timer);
}
%>
</pre>
<fieldset>
<legend style="color: blue">Executed in <strong>${took}</strong> ms</legend>
<p>${status}</p>
<c:if test="${not empty error}">
    <pre style="color: red">${error}</pre>
</c:if>
</fieldset>
</c:if>
<form id="navigateForm" action="${pageContext.request.contextPath}/tools/textExtractor.jsp" method="get">
    <input type="hidden" name="action" id="action" value=""/>
<c:choose>
  <c:when test="${extractionCheckRunning}">
    <fieldset>
      <legend>Check extractions</legend>    
      <p>An extraction check is currently already running.</p>
      <p><input type="submit" name="stopExtractionCheck" onclick="if (confirm('Do you want to stop the process running the extraction check?')) { go('navigateForm','action', 'stopExtractionCheck'); } return false;" value="Stop running extraction check process"/></p>
    </fieldset>            
  </c:when>
  <c:otherwise>
    <fieldset>
        <legend>Check missing extractions</legend>
        <p><input type="submit" name="reportMissingExtraction" onclick="if (confirm('Start checking for the missing extractions?')) { go('navigateForm','action', 'reportMissingExtraction'); } return false;" value="Check for missing text extractions"/> - searches for missing text extractions of documents and prints a report</p>
        <p><input type="submit" name="fixMissingExtraction" onclick="if (confirm('Now we will try to fix the missing extractions. Do you want to continue?')) { go('navigateForm','action', 'fixMissingExtraction'); } return false;" value="Fix missing text extractions"/> - searches for missing text extractions of documents and tries to extract the text now</p>
    </fieldset>
    
    <fieldset>
        <legend>Redo text extractions by filter</legend>
        <label for="workspaceSelector">Choose workspace:</label>
        <select id="workspaceSelector" name="workspace">
            <option value="">All Workspaces</option>
            <option value="default" ${param.workspace == 'default' ? 'selected="selected"' : ''}>default</option>
            <option value="live" ${param.workspace == 'live' ? 'selected="selected"' : ''}>live</option>
        </select>
        <c:set var="fileTypes" value="<%= JCRContentUtils.getInstance().getMimeTypes() %>"/>
        <p><label class="left" for="fileType">File type: </label>
        <select name="fileType"}>
          <option value="">any</option>
          <c:forEach items="${fileTypes}" var="type">
            <c:if test="${type.key != 'image' && type.key != 'video' && type.key != 'archive'}">
              <option value="${type.key}" ${param.fileType == type.key ? 'selected="selected"' : ''}>${type.key}</option>
            </c:if>              
          </c:forEach>
        </select>
        <label class="left" for="mimeType">Mime type(s):</label><input name="mimeType" id="mimeType" value="${param.mimeType}" size="100"/>
        </p>
        <p><label class="left" for="path">Path:</label><input name="path" id="path" value="${param.path}" size="120"/><input type="checkbox" name="includeSubnodes" id="includeSubnodes" ${param.includeSubnodes != null ? 'checked' : ''}/><label for="includeSubnodes">Include subnodes</label>
        </p>
        <p></p><label class="left" for="filenamePattern">Filename (pattern):</label><input name="filenamePattern" id="filenamePattern" value="${param.filenamePattern}"/> (? is wildcard for single and * for multiple characters)</p>  
        <p><input type="submit" name="reportExtractionByFilter" onclick="if (confirm('Start checking for extractable documents by filter?')) { go('navigateForm','action', 'reportExtractionByFilter'); } return false;" value="Check matching documents"/> - searches for extractable files matching the chosen filter</p>
        <p><input type="submit" name="redoExtractionByFilter" onclick="if (confirm('Redo text extractions for documents by filter. Do you want to continue?')) { go('navigateForm','action', 'redoExtractionByFilter'); } return false;" value="Redo text extractions"/> - extracts the text of files matching the chosen filter</p>
     </fieldset>
</c:otherwise>
</c:choose>
</form>
<p>
<form id="extraction" action="${pageContext.request.contextPath}/cms/text-extract" enctype="multipart/form-data" method="post">
<fieldset>
<legend>Check text extraction of a local file</legend>
<label for="file">Choose a file to upload:&nbsp;</label><input name="file" id="file" type="file" />
</p>
<p><input type="submit" value="Extract content" /></p>
</fieldset>
</form>
<c:if test="${extracted}">
<hr/>
<h2>Content extracted in ${extractionTime} ms</h2>
<fieldset>
    <legend><strong>Metadata</strong></legend>
    <c:forEach items="${metadata}" var="item">
        <p>
            <strong><c:out value="${item.key}"/>:&nbsp;</strong>
            <c:out value="${item.value}"/>
        </p>
    </c:forEach>
</fieldset>
<fieldset>
    <legend><strong>Content (${fn:length(content)} characters)</strong></legend>
    <p><c:out value="${content}"/></p>
</fieldset>
</c:if>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>