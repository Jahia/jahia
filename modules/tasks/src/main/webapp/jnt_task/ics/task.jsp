<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%@ page import="org.jahia.registries.ServicesRegistry"%>
<%@ page import="org.jahia.services.content.JCRNodeWrapper"%>
<%@ page import="org.jahia.services.usermanager.JahiaUser"%>
<%@ page import="org.jahia.services.usermanager.JahiaUserManagerService"%>
<%@ page import="java.util.HashMap" %>
<%@ page import="javax.jcr.RepositoryException" %>
<%!
    String getUserContentLine(String userKey, String contentLineName) throws RepositoryException {
        if (userKey != null && !"".equals(userKey)) {
            JahiaUserManagerService userService = ServicesRegistry.getInstance().getJahiaUserManagerService();
            JahiaUser user = userService.lookupUserByKey(userKey);
            String email = user.getProperty("j:email");
            if (email != null && !"".equals(email)) {
                String contentLine = contentLineName;
                String firstName = user.getProperty("j:firstName");
                String lastName = user.getProperty("j:lastName");
                boolean hasFirstName = firstName != null && !"".equals(firstName);
                boolean hasLastName = lastName != null && !"".equals(lastName);
                if (hasFirstName || hasLastName) {
                    contentLine += ";CN=";
                    if (hasFirstName) {
                        contentLine += firstName;
                        if (hasLastName) {
                            contentLine += " ";
                        }
                    }
                    if (hasLastName) {
                        contentLine += lastName;
                    }
                }
                contentLine += ":MAILTO:" + email + "\n";
                return contentLine;
            }

        }
        return "";
    }
%>
<%
    HashMap<String, Integer> priorities = new HashMap<String, Integer>();
    priorities.put("very_low", 9);
    priorities.put("low", 7);
    priorities.put("medium", 5);
    priorities.put("high", 3);
    priorities.put("very_high", 1);
    pageContext.setAttribute("priorities", priorities);
%>
<c:set target="${renderContext}" property="contentType" value="text/calendar;charset=UTF-8" />BEGIN:VCALENDAR
VERSION:2.0
BEGIN:VTODO
<c:set var="assignee" value="${currentNode.properties['assigneeUserKey'].string}" /><%= getUserContentLine((String)pageContext.getAttribute("assignee"), "ATTENDEE")%><c:if test="${not empty currentNode.properties['priority'].string}">PRIORITY:${priorities[currentNode.properties['priority'].string]}
</c:if><c:set var="creator" value="${currentNode.properties['jcr:createdBy'].string}" /><%= getUserContentLine((String)pageContext.getAttribute("creator"), "ORGANIZER")%>DTSTAMP:<fmt:formatDate value="${currentNode.properties['jcr:created'].date.time}" pattern="yyyyMMdd'T'HHmmss'Z'" timeZone="GMT" />
URL;VALUE=URI:<c:url value="${url.server}${url.baseLive}${renderContext.user.localPath}.user-tasks.html"/>
SUMMARY:${currentNode.properties['jcr:title'].string}
<c:if test="${not empty currentNode.properties['description'].string}">DESCRIPTION:${currentNode.properties['description'].string}
</c:if>DUE:<fmt:formatDate value="${currentNode.properties['dueDate'].date.time}" pattern="yyyyMMdd'T'HHmmss'Z'" timeZone="GMT" />
<c:choose><c:when test="${empty currentNode.properties['assigneeUserKey'].string}">STATUS:CANCELLED</c:when><c:when test="${currentNode.properties['state'].string eq 'active'}">STATUS:NEEDS-ACTION</c:when><c:when test="${currentNode.properties['state'].string eq 'started'}">STATUS:IN-PROCESS</c:when><c:when test="${currentNode.properties['state'].string eq 'finished'}">STATUS:COMPLETED</c:when></c:choose>
END:VTODO
END:VCALENDAR
