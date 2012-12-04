<%@ page contentType="text/html;charset=UTF-8" language="java"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.apache.commons.io.FileUtils" %>
<%@page import="org.jahia.data.templates.JahiaTemplatesPackage" %>
<%@ page import="org.jahia.services.SpringContextSingleton" %>
<%@ page import="org.jahia.services.templates.JahiaTemplateManagerService" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.services.content.nodetypes.NodeTypeRegistry" %>
<%@ page import="javax.jcr.nodetype.NodeTypeIterator" %>
<%@ page import="org.jahia.services.content.nodetypes.ExtendedNodeType" %>
<%@ page import="javax.jcr.RepositoryException" %>
<%@ page import="javax.jcr.query.Query" %>
<%@ page import="javax.jcr.query.QueryResult" %>
<%@ page import="javax.jcr.NodeIterator" %>
<%@ page import="org.jahia.services.content.*" %>
<%@ page import="javax.jcr.Workspace" %>
<%@ page import="javax.jcr.nodetype.NodeTypeManager" %>
<%@ page import="ij.plugin.NextImageOpener" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Installed Modules Browser</title>
    <link type="text/css" href="resources/jquery.fancybox-1.3.4.css" rel="stylesheet"/>
    <style type="text/css" title="currentStyle">
        @import "resources/css/demo_page.css";
        @import "resources/css/demo_table_jui.css";
        @import "resources/css/le-frog/jquery-ui-1.8.13.custom.css";
    </style>
    <script type="text/javascript" src="resources/jquery.min.js"></script>
    <script type="text/javascript" src="resources/jquery.dataTables.min.js"></script>
    <script type="text/javascript" src="resources/jquery.fancybox-1.3.4.js"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            $('.defFileLink').fancybox({
                        'hideOnContentClick': false,
                        'scrolling' : 'yes',
                        'width' : 640,
                        'height' : 480,
                        'autoDimensions' : false
                    });
            $('#moduleTable').dataTable({
                        "bLengthChange": true,
                        "iDisplayLength":50,
                        "aLengthMenu": [
                            [50, 100, 150, -1],
                            [50, 100, 150, "All"]
                        ],
                        "bFilter": true,
                        "bSort": true,
                        "bInfo": false,
                        "bAutoWidth": true,
                        "bStateSave" : false,
                        "sPaginationType": "full_numbers",
                        "bJQueryUI": true
                    });
        });
    </script>
</head>
<%
    JahiaTemplateManagerService jahiaTemplateManagerService = (JahiaTemplateManagerService) SpringContextSingleton.getInstance().getContext().getBean(
            "JahiaTemplateManagerService");
    List<JahiaTemplatesPackage> availableTemplatePackages = new ArrayList<JahiaTemplatesPackage>(
            jahiaTemplateManagerService.getAvailableTemplatePackages());
    Collections.sort(availableTemplatePackages, new Comparator<JahiaTemplatesPackage>() {
        public int compare(JahiaTemplatesPackage o1, JahiaTemplatesPackage o2) {
            return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
        }
    });
    pageContext.setAttribute("availablePackages", availableTemplatePackages);

    if (request.getParameter("delete") != null) {
        final String packName = request.getParameter("delete");
        final JahiaTemplatesPackage pack = jahiaTemplateManagerService.getTemplatePackage(packName);
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes(pack.getRootFolder());
                while (nti.hasNext()) {
                    ExtendedNodeType next = (ExtendedNodeType) nti.next();
                    System.out.println(next.getName());
                    Query q = session.getWorkspace().getQueryManager().createQuery("select * from ["+next.getName()+"]", Query.JCR_SQL2);
                    QueryResult result = q.execute();
                    NodeIterator ni = result.getNodes();
                    while (ni.hasNext()) {
                        JCRNodeWrapper o = (JCRNodeWrapper) ni.next();
                        if (next.isMixin()) {
//                            o.removeMixin(next.getName());
                        }
                        if (o.isNodeType(next.getName())) {
                            o.remove();
                        }
                        System.out.println(o.getPath());
                    }
                }
                session.save();
                NodeTypeManager ntm = session.getProviderSession(session.getNode("/").getProvider()).getWorkspace().getNodeTypeManager();
                while (nti.hasNext()) {
                    ExtendedNodeType next = (ExtendedNodeType) nti.next();
                    ntm.unregisterNodeType(next.getName());
                }
                session.getNode("/modules/"+pack.getRootFolder()).remove();
                session.save();
                return null;
            }
        });
    }
%>
<body id="dt_example">
<%@ include file="gotoIndex.jspf" %>
<table id="moduleTable" class="display">
    <thead>
    <tr>
        <th>N°</th>
        <th>Name</th>
        <th>Description</th>
        <th>Root Folder</th>
        <th>Dependencies</th>
        <th>Definitions</th>
        <th>Rules</th>
        <th>Import Files</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${availablePackages}" var="package" varStatus="pstatus">
        <tr class="gradeA">
            <td align="center">${pstatus.count}</td>
            <td><a name="${package.name}" href="definitionsBrowser.jsp?#${package.name}">${package.name}</a>
            <br>
                <a href="modulesBrowser.jsp?delete=${package.name}">Delete</a>

            </td>
            <td>${package.description}</td>
            <td>${package.rootFolderPath}</td>
            <td>
                <ol>
                    <c:forEach items="${package.dependencies}" var="dep">
                        <li><a href="#${dep.name}">${dep.name}</a></li>
                    </c:forEach>
                </ol>
            </td>
            <td>
                <ol>
                    <c:forEach items="${package.definitionsFiles}" var="defFile">
                        <%
                            JahiaTemplatesPackage aPackage = (JahiaTemplatesPackage) pageContext.getAttribute(
                                    "package");
                            String file = aPackage.getFilePath() + File.separator + pageContext.getAttribute("defFile");
                            String s = FileUtils.readFileToString(new File(file));
                            pageContext.setAttribute("defFileName", UUID.randomUUID());
                            pageContext.setAttribute("defFileContent", s);
                        %>
                        <li><a href="#${defFileName}" class="defFileLink">${defFile}</a>

                            <div style="display: none;">
                                <div id="${defFileName}">
                                    <pre>${fn:escapeXml(defFileContent)}</pre>
                                </div>
                            </div>
                        </li>
                    </c:forEach>
                </ol>
            </td>
            <td>
                <ol>
                    <c:forEach items="${package.rulesFiles}" var="defFile">
                        <%
                            JahiaTemplatesPackage aPackage = (JahiaTemplatesPackage) pageContext.getAttribute(
                                    "package");
                            String file = aPackage.getFilePath() + File.separator + pageContext.getAttribute("defFile");
                            String s = FileUtils.readFileToString(new File(file));
                            pageContext.setAttribute("defFileName", UUID.randomUUID());
                            pageContext.setAttribute("defFileContent", s);
                        %>
                        <li><a href="#${defFileName}" class="defFileLink">${defFile}</a>

                            <div style="display: none;">
                                <div id="${defFileName}">
                                    <pre>${fn:escapeXml(defFileContent)}</pre>
                                </div>
                            </div>
                        </li>
                    </c:forEach>
                </ol>
            </td>
            <td>
                <ol>
                    <c:forEach items="${package.initialImports}" var="defFile">
                        <li>${defFile}</li>
                    </c:forEach>
                </ol>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>