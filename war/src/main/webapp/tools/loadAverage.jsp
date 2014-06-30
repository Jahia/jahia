<%@page import="org.jahia.exceptions.JahiaBadRequestException"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><% response.setContentType("json".equals(request.getParameter("output")) ? "application/json; charset=UTF-8" : "text/plain; charset=UTF-8"); 
%><%@page import="org.jahia.utils.*"
%><%
String average = request.getParameter("type");
LoadAverage load = null;
if ("request".equalsIgnoreCase(average)) {
    load = RequestLoadAverage.getInstance();
} else if ("jcrsession".equalsIgnoreCase(average)) {
    load = JCRSessionLoadAverage.getInstance();
} else if (average == null) {
    load = RequestLoadAverage.getInstance();
} else {
    throw new JahiaBadRequestException("Unknown load average type: " + average);
}
pageContext.setAttribute("load", load);
%><c:choose><c:when test="${param.output == 'json'}"
>{"oneMinuteLoad": "${load.oneMinuteLoad}", "fiveMinuteLoad": "${load.fiveMinuteLoad}", "fifteenMinuteLoad": "${load.fifteenMinuteLoad}"}</c:when><c:otherwise
>${load.oneMinuteLoad}|${load.fiveMinuteLoad}|${load.fifteenMinuteLoad}</c:otherwise></c:choose>