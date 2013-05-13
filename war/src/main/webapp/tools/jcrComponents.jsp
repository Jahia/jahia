<%@ page import="org.jahia.ajax.gwt.helper.CacheHelper" %>
<%@ page import="org.jahia.api.Constants" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.services.SpringContextSingleton" %>
<%@ page import="org.jahia.services.content.JCRCallback" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="org.jahia.services.content.JCRSessionWrapper" %>
<%@ page import="org.jahia.services.content.JCRTemplate" %>
<%@ page import="org.jahia.utils.RequestLoadAverage" %>
<%@ page import="org.quartz.JobDetail" %>
<%@ page import="org.quartz.SchedulerException" %>
<%@ page import="javax.jcr.*" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="javax.jcr.query.Query" %>
<%@ page import="javax.jcr.query.QueryResult" %>
<%@ page import="org.jahia.services.content.nodetypes.NodeTypeRegistry" %>
<%@ page import="javax.jcr.nodetype.NoSuchNodeTypeException" %>
<%@ page import="org.jahia.services.content.nodetypes.ExtendedNodeType" %>
<%@ page import="javax.jcr.nodetype.NodeType" %>
<%@ page import="java.util.*" %>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<c:set var="workspace" value="${functions:default(param.workspace, 'default')}"/>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" href="tools.css" type="text/css"/>
    <title>JCR Components check</title>
    <style type="text/css">
        div.hiddenDetails {
            margin: 0px 20px 0px 20px;
            display: none;
        }

        .error {
            color: #FF0000;
        }

        .warning {
            color: brown;
        }
    </style>
</head>
<body>
<h1>JCR Components Tools</h1>

<%!

    private List<String> getMissingNodeTypes() throws Exception {
        final List<String> nt = new ArrayList<String>();
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) {

                try {
                    JCRNodeWrapper node = session.getNode("/jcr:system/jcr:nodeTypes");
                    NodeIterator ni = node.getNodes();

                    while (ni.hasNext()) {
                        Node next = (Node) ni.next();
                        try {
                            NodeTypeRegistry.getInstance().getNodeType(next.getName());
                        } catch (NoSuchNodeTypeException e) {
                            nt.add(next.getName());
                        }
                    }
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        return nt;
    }

    private List<String> getNodes(final String nodeType, final boolean delete) throws Exception {
        final List<String> nt = new ArrayList<String>();
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) {
                try {
                    Session jrSession = session.getNode("/").getRealNode().getSession();
                    Query query = jrSession.getWorkspace().getQueryManager().createQuery("//element(*, "+nodeType+")", Query.XPATH);
                    NodeIterator ni = query.execute().getNodes();

                    while (ni.hasNext()) {
                        Node next = (Node) ni.next();
                        nt.add(next.getPath());
                    }
                    if (delete) {
                        for (String s : nt) {
                            try {
                                jrSession.getNode(s).remove();
                                jrSession.save();
                            } catch (PathNotFoundException e) {
                                //
                            }
                        }
                    }

                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        return nt;
    }

    private void purgeComponents(final String nodeType) throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) {
                try {
                    Query query = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:component] where name()='"+nodeType+"'", Query.JCR_SQL2);
                    NodeIterator ni = query.execute().getNodes();

                    while (ni.hasNext()) {
                        Node next = (Node) ni.next();
                        next.remove();
                        session.save();
                    }
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }


%>
<%
    List<String> missingNodeTypes = getMissingNodeTypes();
    pageContext.setAttribute("missingNodeTypes", missingNodeTypes);

    if (request.getParameter("viewNodes") != null) {
        List<String> l = getNodes(request.getParameter("viewNodes"), false);
        pageContext.setAttribute("nodes", l);
    }
    if (request.getParameter("purgeNodes") != null) {
        getNodes(request.getParameter("purgeNodes"), true);
    }
    if (request.getParameter("purgeComponents") != null) {
        purgeComponents(request.getParameter("purgeComponents"));
    }
    if ("true".equals(request.getParameter("purgeAllNodes"))) {
        for (String nodeType : missingNodeTypes) {
            getNodes(nodeType, true);
        }

    }
    if ("true".equals(request.getParameter("purgeAllComponents"))) {
        for (String nodeType : missingNodeTypes) {
            purgeComponents(nodeType);
        }
    }
%>

<table border="1">
<c:forEach items="${missingNodeTypes}" var="nodeType">
    <tr>
        <td>${nodeType}</td><td><a href="jcrComponents.jsp?viewNodes=${nodeType}">View nodes</a></td><td><a href="jcrComponents.jsp?purgeNodes=${nodeType}">Purge nodes</a></td><td><a href="jcrComponents.jsp?purgeComponents=${nodeType}">Purge components</a></td>
    </tr>

    <c:if test="${nodeType eq param['viewNodes']}">
        <tr>
            <td colspan="4">
                <c:forEach var="node" items="${nodes}">
                    ${node}<br/>
                </c:forEach>
            </td>
        </tr>
    </c:if>
</c:forEach>
</table>
<a href="jcrComponents.jsp?purgeAllNodes=true">Purge all nodes</a>
<a href="jcrComponents.jsp?purgeAllComponents=true">Purge all missing components</a>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>