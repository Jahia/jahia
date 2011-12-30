<%@ page contentType="text/html; charset=UTF-8" language="java"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="java.util.*"%>
<%@page import="javax.jcr.*"%>
<%@page import="javax.jcr.query.*"%>
<%@page import="org.jahia.services.content.*"%>
<%@page import="org.jahia.services.history.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>JCR Version History Management</title>
</head>
<body>
<h1>JCR Version History Management</h1>
<p>This tool aims to perform cleanup tasks on the version store, e.g. find version history for nodes that no longer exists and purge them or purge old versions for existing nodes.</p>
<c:if test="${param.action == 'orphanedReport' || param.action == 'orphanedDelete'}">
<c:set var="maxLimit" value="${functions:default(fn:escapeXml(param.maxLimit), '10000')}"/>
<%!
private static final boolean nodeExists(String id, JCRSessionWrapper session) throws RepositoryException {
    try {
        session.getNodeByIdentifier(id);
        return true;
    } catch (ItemNotFoundException e) {
        return false;
    }
}
private static final int checkOrhpaned(NodeIterator it, JCRSessionWrapper session, Set<String> ids) throws RepositoryException {
    int checkedCount = 0;
    while (it.hasNext()) {
        Node vhNode = it.nextNode();
        checkedCount++;
        String targetId = vhNode.hasProperty("jcr:versionableUuid") ? vhNode.getProperty("jcr:versionableUuid").getString() : null;
        if (targetId == null) {
            continue;
        }
        if (!vhNode.getReferences().hasNext()) {
            ids.add(targetId);
        }
    }
    return checkedCount;
}
%>
<%
long timer = System.currentTimeMillis();
final Set<String> ids = new HashSet<String>();
final int maxLimit = Integer.parseInt((String) pageContext.getAttribute("maxLimit")); 

try {
    int total = JCRTemplate.getInstance().doExecuteWithSystemSession(
            new JCRCallback<Integer>() {
                public Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    int step = maxLimit < 1000 ? maxLimit : 1000;
                    int checkedCount = 0;
                    Query q = session.getWorkspace().getQueryManager().createQuery("/jcr:root/jcr:system/jcr:versionStorage//element(*,nt:versionHistory)", Query.XPATH);
                    q.setLimit(step);
                    
                    int offset = 0;
                    boolean stop = false;
                    while (!stop) {
                        q.setOffset(offset);
                        NodeIterator nodes = q.execute().getNodes();
                        if (nodes.hasNext()) {
                            checkedCount = checkedCount + checkOrhpaned(nodes, session, ids);
                        } else {
                            stop = true;
                        }
                        offset+=step;
                        if (ids.size() >= maxLimit) {
                            stop = true;
                        }
                    }
                    
                    return checkedCount;
                }
            });
    pageContext.setAttribute("total", String.valueOf(total));
    pageContext.setAttribute("orphaned", String.valueOf(ids.size()));
    pageContext.setAttribute("maxLimitReached", Boolean.valueOf(ids.size() >= maxLimit));
    
    if ("orphanedDelete".equals(request.getParameter("action"))) {
        pageContext.setAttribute("deleted", String.valueOf(NodeVersionHistoryHelper.purgeVersionHistoryForNodes(ids)));
    }
} catch (Exception e) {
    e.printStackTrace();
} finally {
    pageContext.setAttribute("took", System.currentTimeMillis() - timer);
}
%>
<fieldset>
<legend style="color: blue">Successfully executed in <strong>${took}</strong> ms</legend>
<p>Checked <strong>${total}</strong> items. Found <strong>${orphaned}</strong> orphaned version histories.
<c:if test="${param.action == 'orphanedDelete'}">
<strong>${deleted}</strong> single version items (can be multiple in each version history) successfully deleted.
</c:if>
</p>
<c:if test="${maxLimitReached}">
<p>Please, note, that the check was stopped when more than <strong>${maxLimit}</strong> orphaned histories were found (maximum limit).</p> 
</c:if> 

</fieldset>
</c:if>
<p>Available actions:</p>
<ul>
    <li><a href="?action=orphanedReport" onclick="return confirm('Start checking for the orhpaned version history?');">Check for orphaned version history</a> - searches for version history of already deleted nodes and prints a report</li>
    <li><a href="?action=orphanedDelete" onclick="return confirm('The orhpaned version history for no longer existing nodes will be permanently deleted. Do you want to continue?');">Delete orphaned version history</a> - searches for version history of already deleted nodes and deleted version items</li>
</ul>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>