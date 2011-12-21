<%@ page contentType="text/html;charset=UTF-8" language="java" 
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.util.Locale"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="javax.jcr.Node"%>
<%@page import="javax.jcr.ItemNotFoundException"%>
<%@page import="javax.jcr.NodeIterator"%>
<%@page import="javax.jcr.query.Query"%>
<%@page import="javax.jcr.query.QueryManager"%>
<%@page import="javax.jcr.query.QueryResult"%>
<%@page import="org.jahia.services.usermanager.jcr.JCRUserManagerProvider"%>
<%@page import="org.jahia.services.content.JCRContentUtils"%>
<%@page import="org.jahia.services.content.JCRNodeWrapper"%>
<%@page import="org.jahia.services.content.JCRSessionFactory"%>
<%@page import="org.jahia.services.content.JCRSessionWrapper"%>
<%@page import="org.jahia.utils.LanguageCodeConverters"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr"%>
<%@taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>JCR Query Tool</title>
<link rel="stylesheet" href="tools.css" type="text/css" />
<link type="text/css" href="resources/jquery.fancybox-1.3.4.css" rel="stylesheet"/>
<script type="text/javascript" src="resources/jquery.min.js"></script>
<script type="text/javascript" src="resources/jquery.fancybox-1.3.4.js"></script>
<script type="text/javascript">
    $(document).ready(function() {
        $('#helpLink').fancybox({
                    'hideOnContentClick': false,
                    'titleShow' : false,
                    'transitionOut' : 'none'
                });
    });
    function go(id1, value1, id2, value2, id3, value3) {
    	document.getElementById(id1).value=value1;
    	if (id2) {
    		document.getElementById(id2).value=value2;
    	}
    	if (id3) {
    		document.getElementById(id3).value=value3;
    	}
    	document.getElementById('navigateForm').submit();
    }
</script>
</head>
<c:set var="workspace" value="${functions:default(fn:escapeXml(param.workspace), 'default')}"/>
<c:set var="locale" value="${functions:default(fn:escapeXml(param.locale), 'en')}"/>
<c:set var="lang" value="${functions:default(fn:escapeXml(param.lang), 'JCR-SQL2')}"/>
<c:set var="limit" value="${functions:default(fn:escapeXml(param.limit), '20')}"/>
<c:set var="offset" value="${functions:default(fn:escapeXml(param.offset), '0')}"/>
<c:set var="showActions" value="${functions:default(fn:escapeXml(param.showActions), 'false')}"/>
<%
Locale currentLocale = LanguageCodeConverters.languageCodeToLocale((String) pageContext.getAttribute("locale"));
pageContext.setAttribute("locales", LanguageCodeConverters.getSortedLocaleList(Locale.ENGLISH));
%>
<body>
<c:set var="switchToWorkspace" value="${workspace == 'default' ? 'live' : 'default'}"/>
<fieldset>
    <legend>
        <strong>${workspace}</strong>&nbsp;workspace&nbsp;(<a href="#switchWorkspace" onclick="document.getElementById('workspace').value='${switchToWorkspace}'; document.getElementById('navigateForm').submit(); return false;">switch to ${switchToWorkspace}</a>)
        <select name="localeSelector" onchange="document.getElementById('locale').value=this.value;">
            <c:forEach items="${locales}" var="loc">
                <% pageContext.setAttribute("localeLabel", ((Locale) pageContext.getAttribute("loc")).getDisplayName(Locale.ENGLISH)); %>
                <option value="${loc}"${loc == locale ? 'selected="selected"' : ''}>${fn:escapeXml(localeLabel)}</option>
            </c:forEach>
        </select>
    </legend>
    <fieldset style="position: absolute; right: 20px;">
        <legend><strong>Settings</strong></legend>
            <input id="cbActions" type="checkbox" ${showActions ? 'checked="checked"' : ''}
                onchange="go('showActions', '${!showActions}')"/>&nbsp;<label for="cbActions">Show actions</label>
    </fieldset>
    <form id="navigateForm" action="?" method="get">
        <input type="hidden" name="workspace" id="workspace" value="${workspace}"/>
        <input type="hidden" name="locale" id="locale" value="${locale}"/>
        <input type="hidden" name="showActions" id="showActions" value="${showActions}"/>
        <input type="hidden" name="action" id="action" value=""/>
        <input type="hidden" name="target" id="target" value=""/>
        <textarea rows="3" cols="75" name="query" id="query"
            onkeyup="if ((event || window.event).keyCode == 13 && (event || window.event).ctrlKey) document.getElementById('navigateForm').submit();"
        >${not empty param.query ? param.query : 'SELECT * FROM [nt:file]'}</textarea>
        <span>
        <span style="position: absolute;"><a id="helpLink" title="Help" href="#helpArea"><img src="<c:url value='/css/images/andromeda/icons/help.png'/>" width="16" height="16" alt="help" title="Help"></a></span>
        <select name="lang" id="lang">
            <option value="JCR-SQL2"${lang == 'JCR-SQL2' ? 'selected="selected"' : ''}>JCR-SQL2</option>
            <option value="xpath"${lang == 'xpath' ? 'selected="selected"' : ''}>XPath</option>
            <option value="sql"${lang == 'sql' ? 'selected="selected"' : ''}>SQL</option>
        </select>
        Limit:
        <select name="limit" id="limit">
            <option value="10"${limit == '10' ? 'selected="selected"' : ''}>10</option>
            <option value="20"${limit == '20' ? 'selected="selected"' : ''}>20</option>
            <option value="50"${limit == '50' ? 'selected="selected"' : ''}>50</option>
            <option value="100"${limit == '100' ? 'selected="selected"' : ''}>100</option>
            <option value="0"${limit == '0' ? 'selected="selected"' : ''}>all</option>
        </select>
        &nbsp;Offset:
        <input type="text" size="2" name="offset" id="offset" value="${offset}"/>
        <input type="submit" value="Execute query ([Ctrl+Enter])" />
        </span>
    </form> 
</fieldset>
<%
JCRSessionFactory.getInstance().setCurrentUser(JCRUserManagerProvider.getInstance().lookupRootUser());
JCRSessionWrapper jcrSession = JCRSessionFactory.getInstance().getCurrentUserSession((String) pageContext.getAttribute("workspace"), currentLocale, Locale.ENGLISH);
%>
<c:if test="${param.action == 'delete' && not empty param.target}">
	<% 
       try {
           JCRNodeWrapper target = jcrSession.getNodeByIdentifier(request.getParameter("target"));
           pageContext.setAttribute("target", target);
           jcrSession.checkout(target.getParent());    
           target.remove();
           jcrSession.save();
       } catch (ItemNotFoundException e) {
           // not found
       }
    %>
    <c:if test="${not empty target}">
   	    <p style="color: blue">Node <strong>${fn:escapeXml(target.path)}</strong> deleted successfully</p>
    </c:if>
    <c:if test="${empty target}">
        <p style="color: red">Node with identifier <strong>${fn:escapeXml(param.target)}</strong> cannot be found</p>
    </c:if>
</c:if>
<c:if test="${param.action == 'deleteAll' && not empty param.query}">
    <% 
       try {
           Query q = jcrSession.getWorkspace().getQueryManager().createQuery(request.getParameter("query"), (String) pageContext.getAttribute("lang"));
           QueryResult result = q.execute();
           int count = 0;
           for (NodeIterator it = result.getNodes(); it.hasNext();) {
               Node target = it.nextNode();
               try {
                   jcrSession.checkout(target.getParent());    
                   target.remove();
                   count++;
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
           jcrSession.save();
           pageContext.setAttribute("deletedCount", Integer.valueOf(count));
       } catch (ItemNotFoundException e) {
           // not found
       }
    %>
    <p style="color: blue">${deletedCount} nodes were deleted</p>
</c:if>
<%
try {
    String query = request.getParameter("query");
    Long limit = Long.valueOf(StringUtils.defaultIfEmpty(request.getParameter("limit"), "20"));
    Long offset = Long.valueOf(StringUtils.defaultIfEmpty(request.getParameter("offset"), "0"));
    if (StringUtils.isNotEmpty(query)) {
        long actionTime = System.currentTimeMillis();
        Query q = jcrSession.getWorkspace().getQueryManager().createQuery(query, (String) pageContext.getAttribute("lang"));
        q.setLimit(Long.valueOf((String) pageContext.getAttribute("limit")));
        q.setOffset(Long.valueOf((String) pageContext.getAttribute("offset")));
        QueryResult result = q.execute();
        pageContext.setAttribute("count", JCRContentUtils.size(result.getNodes()));
        pageContext.setAttribute("nodes", result.getNodes());
        pageContext.setAttribute("took", Long.valueOf(System.currentTimeMillis() - actionTime));
    }
%>
<c:if test="${not empty param.query}">
<fieldset>
    <legend>Displaying <strong>${count} nodes</strong> (query took ${took} ms)</legend>
    <c:if test="${showActions}">
        <div style="position: absolute; right: 20px;">
            <a href="#delete" onclick='if (!confirm("You are about to permanently delete ALL the nodes this query is matching. Continue?")) return false; go("action", "deleteAll"); return false;' title="Delete All"><img src="<c:url value='/icons/delete.png'/>" height="16" width="16" title="Delete All" border="0"/> Delete ALL</a>
        </div>
    </c:if>
<ol start="${offset + 1}">
<c:forEach var="node" items="${nodes}" varStatus="status">
    <li>
        <a title="Open in JCR Browser" href="<c:url value='/tools/jcrBrowser.jsp?uuid=${node.identifier}&workspace=${workspace}&showProperties=true'/>" target="_blank"><strong>${fn:escapeXml(not empty node.displayableName ? node.name : '<root>')}</strong></a> (${fn:escapeXml(node.nodeTypes)})
        <a title="Open in Repository Explorer" href="<c:url value='/engines/manager.jsp?selectedPaths=${node.path}&workspace=${workspace}'/>" target="_blank"><img src="<c:url value='/icons/fileManager.png'/>" width="16" height="16" alt="open" title="Open in Repository Explorer"></a>
        <c:if test="${showActions}">
        	&nbsp;<a href="#delete" onclick='var nodeName="${node.name}"; if (!confirm("You are about to delete the node \"" + nodeName + "\" with all child nodes. Continue?")) return false; go("action", "delete", "target", "${node.identifier}"); return false;' title="Delete"><img src="<c:url value='/icons/delete.png'/>" height="16" width="16" title="Delete" border="0"/></a>
        </c:if>
        <br/>
        <strong>Path: </strong>${fn:escapeXml(node.path)}<br/>
        <strong>ID: </strong>${fn:escapeXml(node.identifier)}<br/>
		<jcr:nodeProperty node="${node}" name="jcr:created" var="created"/>
		<jcr:nodeProperty node="${node}" name="jcr:createdBy" var="createdBy"/>
		<jcr:nodeProperty node="${node}" name="jcr:lastModified" var="lastModified"/>
		<jcr:nodeProperty node="${node}" name="jcr:lastModifiedBy" var="lastModifiedBy"/>
        <c:if test="${not empty created}">
        <strong>created on </strong><fmt:formatDate value="${created.time}" pattern="yyyy-MM-dd HH:mm" /><strong> by </strong>${not empty createdBy.string ? fn:escapeXml(createdBy.string) : 'n.a.'},
        </c:if>
        <c:if test="${not empty lastModified}">
        <strong> last modified on </strong><fmt:formatDate value="${lastModified.time}" pattern="yyyy-MM-dd HH:mm" /><strong> by </strong>${not empty lastModifiedBy.string ? fn:escapeXml(lastModifiedBy.string) : 'n.a.'}
        </c:if>
    </li>
</c:forEach>
</ol>
</fieldset>
</c:if>
<%} catch (Exception e) {
    pageContext.setAttribute("error", e);
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    pageContext.setAttribute("errorTrace", sw.toString());
%>
<fieldset>
    <legend><strong>Error</strong></legend>
    <pre>${errorTrace}</pre>
</fieldset>
<%} finally {
    JCRSessionFactory.getInstance().setCurrentUser(null);
}%>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
<div style="display: none;">
    <div id="helpArea">
        <h3>Query examples</h3>
        <h4>1. Select pages in ACME site (site key: 'ACME'), created by user 'john', sorted by creation date descending (newest pages first)</h4>
        <ul>
            <li><strong>JCR-SQL2</strong>:<br/>
                <c:set var="q">SELECT * FROM [jnt:page] WHERE ISDESCENDANTNODE('/sites/ACME') AND [jcr:createdBy]='john' ORDER BY [jcr:created] DESC</c:set>
                <code>${fn:escapeXml(q)}</code><br/>
                <c:set var="jsQ" value="${functions:escapeJavaScript(q)}"/>
                <button onclick="document.getElementById('query').value='${jsQ}'; document.getElementById('lang').value='JCR-SQL2'; $.fancybox.close();">Use it in the query area</button>
            </li>
            <li><strong>XPath</strong>:<br/>
                <c:set var="q">/jcr:root/sites/ACME//element(*,jnt:page)[@jcr:createdBy='john'] order by @jcr:created descending</c:set>
                <code>${fn:escapeXml(q)}</code><br/>
                <c:set var="jsQ" value="${functions:escapeJavaScript(q)}"/>
                <button onclick="document.getElementById('query').value='${jsQ}'; document.getElementById('lang').value='xpath'; $.fancybox.close();">Use it in the query area</button>
            </li>
        </ul>
        <h4>2. Select all news items that were created after 1st of June 2011, ordered by creation date descending</h4>
        <ul>
            <li><strong>JCR-SQL2</strong>:<br/>
                <c:set var="q">SELECT * FROM [jnt:news] WHERE [jcr:created] > '2011-06-01T00:00:00.000Z' ORDER BY [jcr:created] DESC</c:set>
                <code>${fn:escapeXml(q)}</code><br/>
                <c:set var="jsQ" value="${functions:escapeJavaScript(q)}"/>
                <button onclick="document.getElementById('query').value='${jsQ}'; document.getElementById('lang').value='JCR-SQL2'; $.fancybox.close();">Use it in the query area</button>
            </li>
            <li><strong>XPath</strong>:<br/>
                <c:set var="q">//element(*,jnt:news)[@jcr:created > xs:dateTime('2011-06-01T00:00:00.000Z')] order by @jcr:created descending</c:set>
                <code>${fn:escapeXml(q)}</code><br/>
                <c:set var="jsQ" value="${functions:escapeJavaScript(q)}"/>
                <button onclick="document.getElementById('query').value='${jsQ}'; document.getElementById('lang').value='xpath'; $.fancybox.close();">Use it in the query area</button>
            </li>
        </ul>
        <h4>3. Select all PDF files in the ACME Web site (site key: 'ACME')</h4>
        <ul>
            <li><strong>XPath</strong>:<br/>
                <c:set var="q">/jcr:root/sites/ACME//element(*,nt:file)[jcr:content/@jcr:mimeType='application/pdf' or jcr:content/@jcr:mimeType='application/x-pdf']</c:set>
                <code>${fn:escapeXml(q)}</code><br/>
                <c:set var="jsQ" value="${functions:escapeJavaScript(q)}"/>
                <button onclick="document.getElementById('query').value='${jsQ}'; document.getElementById('lang').value='xpath'; $.fancybox.close();">Use it in the query area</button>
            </li>
        </ul>
    </div>
</div>
</body>
</html>