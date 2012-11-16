<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<span><fmt:message key="label.source"/></span>
<code>
<pre>
    <c:out value="${currentNode.properties.sourceCode.string}" escapeXml="true"/>
</pre>
</code>
