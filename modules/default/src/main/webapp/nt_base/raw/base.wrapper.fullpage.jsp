<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><c:set target="${renderContext}" property="contentType" value="text/html;charset=UTF-8"/><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<head>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
<title>${fn:escapeXml(currentNode.name)}</title>
</head>
<body style="color:#36393D; font-family:Arial,Helvetica,sans-serif; font-size:80%; line-height:160%;">
${wrappedContent}
</body>
</html>