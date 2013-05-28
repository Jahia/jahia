<%@page contentType="text/html;charset=UTF-8" language="java" %>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.apache.commons.lang.StringUtils" %>
<%@page import="org.apache.jackrabbit.core.query.lucene.join.JahiaQueryEngine" %>
<%@page import="org.jahia.services.content.JCRContentUtils" %>
<%@page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@page import="org.jahia.services.content.JCRSessionFactory" %>
<%@page import="org.jahia.services.content.JCRSessionWrapper" %>
<%@page import="org.jahia.services.history.NodeVersionHistoryHelper" %>
<%@page import="org.jahia.services.usermanager.jcr.JCRUserManagerProvider" %>
<%@page import="org.jahia.utils.LanguageCodeConverters" %>
<%@page import="javax.jcr.ItemNotFoundException" %>
<%@page import="javax.jcr.Node" %>
<%@page import="javax.jcr.NodeIterator" %>
<%@page import="javax.jcr.Session" %>
<%@page import="javax.jcr.query.Query" %>
<%@page import="javax.jcr.query.QueryResult" %>
<%@page import="java.io.PrintWriter" %>
<%@page import="java.io.StringWriter" %>
<%@page import="java.util.Locale" %>
<%@ page import="org.apache.jackrabbit.core.query.lucene.join.JoinRow" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>JCR Query Tool</title>
    <link rel="stylesheet" href="tools.css" type="text/css"/>
    <link type="text/css" href="<c:url value='/modules/assets/css/jquery.fancybox.css'/>" rel="stylesheet"/>
    <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.min.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.fancybox.pack.js'/>"></script>
    <script type="text/javascript">
        $(document).ready(function () {
            $('#helpLink').fancybox({
                'hideOnContentClick': false,
                'titleShow': false,
                'transitionOut': 'none'
            });
        });
        function go(id1, value1, id2, value2, id3, value3) {
            document.getElementById(id1).value = value1;
            if (id2) {
                document.getElementById(id2).value = value2;
            }
            if (id3) {
                document.getElementById(id3).value = value3;
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
<c:set var="displayLimit" value="${functions:default(fn:escapeXml(param.displayLimit), '100')}"/>
<c:set var="showActions" value="${functions:default(fn:escapeXml(param.showActions), 'false')}"/>
<c:set var="useJackrabbitSession" value="${functions:default(fn:escapeXml(param.useJackrabbitSession), 'false')}"/>
<c:set var="useNativeSort" value="<%= JahiaQueryEngine.nativeSort %>"/>
<c:set var="allowChangingNativeSort" value="false"/>
<c:if test="${allowChangingNativeSort && not empty param.useNativeSort}">
    <c:set var="useNativeSort" value="${fn:escapeXml(param.useNativeSort)}"/>
    <% JahiaQueryEngine.nativeSort = Boolean.valueOf(request.getParameter("useNativeSort")); %>
</c:if>
<%
    Locale currentLocale = LanguageCodeConverters.languageCodeToLocale((String) pageContext.getAttribute("locale"));
    pageContext.setAttribute("locales", LanguageCodeConverters.getSortedLocaleList(Locale.ENGLISH));
%>
<body>
<c:set var="switchToWorkspace" value="${workspace == 'default' ? 'live' : 'default'}"/>
<fieldset>
    <legend>
        <strong>${workspace}</strong>&nbsp;workspace&nbsp;(<a href="#switchWorkspace"
                                                              onclick="document.getElementById('workspace').value='${switchToWorkspace}'; document.getElementById('navigateForm').submit(); return false;">switch
        to ${switchToWorkspace}</a>)
        <select name="localeSelector" onchange="document.getElementById('locale').value=this.value;">
            <c:forEach items="${locales}" var="loc">
                <% pageContext.setAttribute("localeLabel", ((Locale) pageContext.getAttribute("loc")).getDisplayName(Locale.ENGLISH)); %>
                <option value="${loc}"${loc == locale ? 'selected="selected"' : ''}>${fn:escapeXml(localeLabel)}</option>
            </c:forEach>
        </select>
    </legend>
    <fieldset style="position: absolute; right: 20px;">
        <legend><strong>Settings</strong></legend>
        <c:if test="${allowChangingNativeSort}">
            <input id="cbNative" type="checkbox" ${useNativeSort ? 'checked="checked"' : ''}
                   onchange="go('useNativeSort', '${!useNativeSort}')"/>&nbsp;<label for="cbNative">Use native sort</label><br/>
        </c:if>
        <input id="cbActions" type="checkbox" ${showActions ? 'checked="checked"' : ''}
               onchange="go('showActions', '${!showActions}')"/>&nbsp;<label for="cbActions">Show actions</label>
        <input id="cbUseJackrabbitSession" type="checkbox" ${useJackrabbitSession ? 'checked="checked"' : ''}
               onchange="go('useJackrabbitSession', '${!useJackrabbitSession}')"/>&nbsp;<label for="cbUseJackrabbitSession">Use jackrabbit session</label>
    </fieldset>
    <form id="navigateForm" action="?" method="get">
        <input type="hidden" name="workspace" id="workspace" value="${workspace}"/>
        <input type="hidden" name="locale" id="locale" value="${locale}"/>
        <input type="hidden" name="showActions" id="showActions" value="${showActions}"/>
        <input type="hidden" name="useNativeSort" id="useNativeSort" value="${useNativeSort}"/>
        <input type="hidden" name="useJackrabbitSession" id="useJackrabbitSession" value="${useJackrabbitSession}"/>
        <input type="hidden" name="action" id="action" value=""/>
        <input type="hidden" name="target" id="target" value=""/>
        <textarea rows="3" cols="75" name="query" id="query"
                  onkeyup="if ((event || window.event).keyCode == 13 && (event || window.event).ctrlKey) document.getElementById('navigateForm').submit();"
                >${not empty param.query ? param.query : 'SELECT * FROM [nt:file]'}</textarea>
        <span>
        <span style="position: absolute;"><a id="helpLink" title="Help" href="#helpArea"><img
                src="<c:url value='/icons/help.png'/>" width="16" height="16" alt="help" title="Help"></a></span>
        <br/>
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
            <option value="1000"${limit == '1000' ? 'selected="selected"' : ''}>1000</option>
            <option value="10000"${limit == '10000' ? 'selected="selected"' : ''}>10000</option>
            <option value="-1"${limit == '-1' ? 'selected="selected"' : ''}>all</option>
        </select>
        &nbsp;Offset:
        <input type="text" size="2" name="offset" id="offset" value="${offset}"/>
        Display limit:
        <select name="displayLimit" id="displayLimit">
            <option value="0"${displayLimit == '0' ? 'selected="selected"' : ''}>none</option>
            <option value="100"${displayLimit == '100' ? 'selected="selected"' : ''}>100</option>
            <option value="200"${displayLimit == '200' ? 'selected="selected"' : ''}>200</option>
            <option value="500"${displayLimit == '500' ? 'selected="selected"' : ''}>500</option>
            <option value="1000"${displayLimit == '1000' ? 'selected="selected"' : ''}>1000</option>
            <option value="10000"${displayLimit == '10000' ? 'selected="selected"' : ''}>10000</option>
            <option value="-1"${displayLimit == '-1' ? 'selected="selected"' : ''}>all</option>
        </select>
        <input type="submit" value="Execute query ([Ctrl+Enter])"/>
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
            long limit = Long.valueOf((String) pageContext.getAttribute("limit"));
            if (limit >= 0) {
                q.setLimit(limit);
            }
            q.setOffset(Long.valueOf((String) pageContext.getAttribute("offset")));
            QueryResult result = q.execute();
            int count = 0;
            for (NodeIterator it = result.getNodes(); it.hasNext(); ) {
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
<c:if test="${param.action == 'purgeHistory' && not empty param.query}">
    <pre><%
        out.flush();
        long actionTime = System.currentTimeMillis();
        Query q = jcrSession.getWorkspace().getQueryManager().createQuery(request.getParameter("query"), (String) pageContext.getAttribute("lang"));
        long limit = Long.valueOf((String) pageContext.getAttribute("limit"));
        if (limit >= 0) {
            q.setLimit(limit);
        }
        q.setOffset(Long.valueOf((String) pageContext.getAttribute("offset")));
        pageContext.setAttribute("purgeStatus", NodeVersionHistoryHelper.purgeVersionHistoryForNodes(q.execute().getNodes(), out));
        pageContext.setAttribute("took", Long.valueOf(System.currentTimeMillis() - actionTime));
    %>
    </pre>
    <p style="color: blue">Finished purging unused versions for nodes in ${took} ms: ${purgeStatus}</p>
</c:if>
<%
    try {
        String query = request.getParameter("query");
        if (StringUtils.isNotEmpty(query)) {
            long actionTime = System.currentTimeMillis();
            Query q;
            if ("true".equals(request.getParameter("useJackrabbitSession"))) {
                Session jrSession = jcrSession.getProviderSession(JCRSessionFactory.getInstance().getProvider("/"));
                pageContext.setAttribute("jrSession", jrSession);
                q = jrSession.getWorkspace().getQueryManager().createQuery(query, (String) pageContext.getAttribute("lang"));
            } else {
                q = jcrSession.getWorkspace().getQueryManager().createQuery(query, (String) pageContext.getAttribute("lang"));
            }

            long limit = Long.valueOf((String) pageContext.getAttribute("limit"));
            if (limit >= 0) {
                q.setLimit(limit);
            }
            q.setOffset(Long.valueOf((String) pageContext.getAttribute("offset")));
            QueryResult result = q.execute();
            pageContext.setAttribute("count", JCRContentUtils.size(result.getRows()));
            String countColumnName = null;

            for (String columnName : result.getColumnNames()) {
                if (columnName.startsWith("rep:count(")) {
                    countColumnName = columnName;
                    break;
                } else if (columnName.startsWith("rep:facet(")) {
                    pageContext.setAttribute("hasFacets", true);
                    pageContext.setAttribute("result", result);
                    break;
                }
            }
            if (countColumnName != null) {
                pageContext.setAttribute("countColumnName", countColumnName);
                pageContext.setAttribute("countResult", result.getRows().nextRow().getValue(countColumnName).getLong());
            } else if (result.getSelectorNames().length == 1) {
                pageContext.setAttribute("nodes", result.getNodes());
            } else {
                pageContext.setAttribute("selectorNames", result.getSelectorNames());
                pageContext.setAttribute("rows", result.getRows());
            }
            pageContext.setAttribute("took", Long.valueOf(System.currentTimeMillis() - actionTime));
        }

%>
<c:if test="${not empty param.query}">
    <fieldset>
        <legend>Found ${count} results.
            Displaying ${displayLimit == 0 ? 'none' : (displayLimit == -1 || displayLimit > count ? 'all' : displayLimit)}.
            Query took ${took} ms.
        </legend>
        <c:if test="${showActions}">
            <div style="position: absolute; right: 20px;">
                <a href="#delete"
                   onclick='if (!confirm("You are about to permanently delete all the nodes this query is matching (considering limit and offset). Continue?")) return false; go("action", "deleteAll"); return false;'
                   title="Permanently delete all the nodes this query is matching (considering limit and offset)"><img
                        src="<c:url value='/icons/delete.png'/>" height="16" width="16"
                        title="Permanently delete all the nodes this query is matching (considering limit and offset)"
                        border="0"/> Delete all found</a><br/>
                <a href="#history"
                   onclick='if (!confirm("You are about to purge unused versions for all nodes this query is matching (considering limit and offset). Continue?")) return false; go("action", "purgeHistory"); return false;'
                   title="Purge unused versions for all nodes (considering limit and offset)"><img
                        src="<c:url value='/icons/tab-templates.png'/>" height="16" width="16"
                        title="Purge unused versions for all nodes (considering limit and offset)" border="0"/> Purge
                    unused versions for all found nodes</a>
            </div>
        </c:if>
        <c:if test="${displayLimit != 0}">
            <% pageContext.setAttribute("maxIntValue", Integer.MAX_VALUE); %>
            <c:if test="${hasFacets}">
                <div class="facets">Facets:
                    <c:forEach items="${result.facetFields}" var="currentFacet">
                        <h5><facet:facetLabel currentFacetField="${currentFacet}"/></h5>
                        <ul>
                            <c:forEach items="${currentFacet.values}" var="facetValue">
                                <li><facet:facetValueLabel currentFacetField="${currentFacet}"
                                                           facetValueCount="${facetValue}"/> (${facetValue.count})<br/>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:forEach>
                    <c:forEach items="${result.facetDates}" var="currentFacet">
                        <h5><facet:facetLabel currentFacetField="${currentFacet}"/></h5>
                        <ul>
                            <c:forEach items="${currentFacet.values}" var="facetValue">
                                <li><facet:facetValueLabel currentFacetField="${currentFacet}"
                                                           facetValueCount="${facetValue}"/> (${facetValue.count})<br/>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:forEach>
                    <c:forEach items="${result.facetQuery}" var="facetValue" varStatus="iterationStatus">
                        <c:if test="${iterationStatus.first}">
                            <ul>
                        </c:if>
                        <li><facet:facetValueLabel currentActiveFacetValue="${facetValue}"/> (${facetValue.value})<br/>
                        </li>
                        <c:if test="${iterationStatus.last}">
                            </ul>
                        </c:if>
                    </c:forEach>
                </div>
            </c:if>
            <ol start="${offset + 1}">
                <c:choose>
                    <c:when test="${not empty countResult}">
                        <li><strong>${countColumnName}: </strong>${countResult}</li>
                    </c:when>
                    <c:when test="${not empty jrSession and empty rows}">
                        <c:forEach var="node" items="${nodes}" varStatus="status"
                                   end="${displayLimit != -1 ? displayLimit - 1 : maxIntValue}">
                            <li>
                                <a title="Open in JCR Browser"
                                   href="<c:url value='/tools/jcrBrowser.jsp?uuid=${node.identifier}&workspace=${workspace}&showProperties=true&showJCRNodes=true'/>"
                                   target="_blank"><strong>${node.name}</strong></a>
                                <strong>${node.name}</strong>
                                <c:forEach items="${node.mixinNodeTypes}" var="mixin">
                                    ${mixin.name}
                                </c:forEach>
                                <br/>
                                <strong>Path: </strong>${fn:escapeXml(node.path)}<br/>
                                <strong>ID: </strong>${fn:escapeXml(node.identifier)}<br/>
                            </li>
                        </c:forEach>
                    </c:when>
                    <c:when test="${not empty jrSession and not empty rows}">
                        <c:forEach var="row" items="${rows}" varStatus="status"
                                   end="${displayLimit != -1 ? displayLimit - 1 : maxIntValue}">
                            <li>
                                <c:forEach var="selectorName" items="${selectorNames}" varStatus="nodestatus">
                                    <%
                                        pageContext.setAttribute("node",((JoinRow)pageContext.getAttribute("row")).getNode(((String)pageContext.getAttribute("selectorName"))));
                                    %>
                                    <a title="Open in JCR Browser"
                                       href="<c:url value='/tools/jcrBrowser.jsp?uuid=${node.identifier}&workspace=${workspace}&showProperties=true&showJCRNodes=true'/>"
                                       target="_blank"><strong>${node.name}</strong></a>
                                    <c:forEach items="${node.mixinNodeTypes}" var="mixin">
                                        ${mixin.name}
                                    </c:forEach>
                                    <br/>
                                    <strong>Path: </strong>${fn:escapeXml(node.path)}<br/>
                                    <strong>ID: </strong>${fn:escapeXml(node.identifier)}<br/>
                                    <c:if test="${not nodestatus.last}">
                                        <br/>
                                    </c:if>
                                </c:forEach>
                            </li>
                        </c:forEach>
                    </c:when>
                    <c:when test="${empty rows}">
                        <c:forEach var="node" items="${nodes}" varStatus="status"
                                   end="${displayLimit != -1 ? displayLimit - 1 : maxIntValue}">
                            <li>
                                <a title="Open in JCR Browser"
                                   href="<c:url value='/tools/jcrBrowser.jsp?uuid=${node.identifier}&workspace=${workspace}&showProperties=true'/>"
                                   target="_blank"><strong>${fn:escapeXml(not empty node.displayableName ? node.name : '<root>')}</strong></a>
                                (${fn:escapeXml(node.nodeTypes)})
                                <a title="Open in Repository Explorer"
                                   href="<c:url value='/engines/manager.jsp?selectedPaths=${node.path}&workspace=${workspace}'/>"
                                   target="_blank"><img src="<c:url value='/icons/fileManager.png'/>" width="16"
                                                        height="16" alt="open" title="Open in Repository Explorer"></a>
                                <c:if test="${showActions}">
                                    &nbsp;<a href="#delete" onclick='var nodeName="${node.name}"; if (!confirm("You are about to delete the node \"" + nodeName + "\" with all child nodes. Continue?")) return false; go("action", "delete", "target", "${node.identifier}"); return false;' title="Delete"><img src="<c:url
                                        value='/icons/delete.png'/>" height="16" width="16" title="Delete" border="0"/></a>
                                </c:if>
                                <br/>
                                <strong>Path: </strong>${fn:escapeXml(node.path)}<br/>
                                <strong>ID: </strong>${fn:escapeXml(node.identifier)}<br/>
                                <jcr:nodeProperty node="${node}" name="jcr:created" var="created"/>
                                <jcr:nodeProperty node="${node}" name="jcr:createdBy" var="createdBy"/>
                                <jcr:nodeProperty node="${node}" name="jcr:lastModified" var="lastModified"/>
                                <jcr:nodeProperty node="${node}" name="jcr:lastModifiedBy" var="lastModifiedBy"/>
                                <c:if test="${not empty created}">
                                    <strong>created on </strong><fmt:formatDate value="${created.time}"
                                                                                pattern="yyyy-MM-dd HH:mm"/><strong>
                                    by </strong>${not empty createdBy.string ? fn:escapeXml(createdBy.string) : 'n.a.'},
                                </c:if>
                                <c:if test="${not empty lastModified}">
                                    <strong> last modified on </strong><fmt:formatDate value="${lastModified.time}"
                                                                                       pattern="yyyy-MM-dd HH:mm"/><strong>
                                    by </strong>${not empty lastModifiedBy.string ? fn:escapeXml(lastModifiedBy.string) : 'n.a.'}
                                </c:if>
                            </li>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="row" items="${rows}" varStatus="status"
                                   end="${displayLimit != -1 ? displayLimit - 1 : maxIntValue}">
                            <li>
                                <c:forEach var="selectorName" items="${selectorNames}" varStatus="nodestatus">
                                    <c:set var="node" value="${row.nodes[selectorName]}"/>
                                    <a title="Open in JCR Browser"
                                       href="<c:url value='/tools/jcrBrowser.jsp?uuid=${node.identifier}&workspace=${workspace}&showProperties=true'/>"
                                       target="_blank"><strong>${fn:escapeXml(not empty node.displayableName ? node.name : '<root>')}</strong></a> (${fn:escapeXml(node.nodeTypes)})
                                    <a title="Open in Repository Explorer"
                                       href="<c:url value='/engines/manager.jsp?selectedPaths=${node.path}&workspace=${workspace}'/>"
                                       target="_blank"><img src="<c:url value='/icons/fileManager.png'/>" width="16"
                                                            height="16" alt="open" title="Open in Repository Explorer"></a>
                                    <c:if test="${showActions}">
                                        &nbsp;<a href="#delete" onclick='var nodeName="${node.name}"; if (!confirm("You are about to delete the node \"" + nodeName + "\" with all child nodes. Continue?")) return false; go("action", "delete", "target", "${node.identifier}"); return false;' title="Delete"><img src="<c:url
                                            value='/icons/delete.png'/>" height="16" width="16" title="Delete" border="0"/></a>
                                    </c:if>
                                    <br/>
                                    <strong>Path: </strong>${fn:escapeXml(node.path)}<br/>
                                    <strong>ID: </strong>${fn:escapeXml(node.identifier)}<br/>
                                    <jcr:nodeProperty node="${node}" name="jcr:created" var="created"/>
                                    <jcr:nodeProperty node="${node}" name="jcr:createdBy" var="createdBy"/>
                                    <jcr:nodeProperty node="${node}" name="jcr:lastModified" var="lastModified"/>
                                    <jcr:nodeProperty node="${node}" name="jcr:lastModifiedBy" var="lastModifiedBy"/>
                                    <c:if test="${not empty created}">
                                        <strong>created on </strong><fmt:formatDate value="${created.time}"
                                                                                    pattern="yyyy-MM-dd HH:mm"/><strong>
                                        by </strong>${not empty createdBy.string ? fn:escapeXml(createdBy.string) : 'n.a.'},
                                    </c:if>
                                    <c:if test="${not empty lastModified}">
                                        <strong> last modified on </strong><fmt:formatDate value="${lastModified.time}"
                                                                                           pattern="yyyy-MM-dd HH:mm"/><strong>
                                        by </strong>${not empty lastModifiedBy.string ? fn:escapeXml(lastModifiedBy.string) : 'n.a.'}
                                    </c:if>
                                    <c:if test="${not nodestatus.last}">
                                        <br/>
                                    </c:if>
                                </c:forEach>
                            </li>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </ol>
        </c:if>
        <c:if test="${displayLimit == 0}">
            <br/><br/>
        </c:if>
    </fieldset>
</c:if>
<%
} catch (Exception e) {
    pageContext.setAttribute("error", e);
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    pageContext.setAttribute("errorTrace", sw.toString());
%>
<fieldset>
    <legend><strong>Error</strong></legend>
    <pre>${errorTrace}</pre>
</fieldset>
<%
    } finally {
        JCRSessionFactory.getInstance().setCurrentUser(null);
    }
%>
<%@ include file="gotoIndex.jspf" %>
<div style="display: none;">
    <div id="helpArea">
        <h3>Query examples</h3>
        <h4>1. Select pages in ACME site (site key: 'ACME'), created by user 'john', sorted by creation date descending
            (newest pages first)</h4>
        <ul>
            <li><strong>JCR-SQL2</strong>:<br/>
                <c:set var="q">SELECT * FROM [jnt:page] WHERE ISDESCENDANTNODE('/sites/ACME') AND [jcr:createdBy]='john' ORDER BY [jcr:created] DESC</c:set>
                <code>${fn:escapeXml(q)}</code><br/>
                <c:set var="jsQ" value="${functions:escapeJavaScript(q)}"/>
                <button onclick="document.getElementById('query').value='${jsQ}'; document.getElementById('lang').value='JCR-SQL2'; $.fancybox.close();">
                    Use it in the query area
                </button>
            </li>
            <li><strong>XPath</strong>:<br/>
                <c:set var="q">/jcr:root/sites/ACME//element(*,jnt:page)[@jcr:createdBy='john'] order by @jcr:created descending</c:set>
                <code>${fn:escapeXml(q)}</code><br/>
                <c:set var="jsQ" value="${functions:escapeJavaScript(q)}"/>
                <button onclick="document.getElementById('query').value='${jsQ}'; document.getElementById('lang').value='xpath'; $.fancybox.close();">
                    Use it in the query area
                </button>
            </li>
        </ul>
        <h4>2. Select all news items that were created after 1st of June 2011, ordered by creation date descending</h4>
        <ul>
            <li><strong>JCR-SQL2</strong>:<br/>
                <c:set var="q">SELECT * FROM [jnt:news] WHERE [jcr:created] > '2011-06-01T00:00:00.000Z' ORDER BY [jcr:created] DESC</c:set>
                <code>${fn:escapeXml(q)}</code><br/>
                <c:set var="jsQ" value="${functions:escapeJavaScript(q)}"/>
                <button onclick="document.getElementById('query').value='${jsQ}'; document.getElementById('lang').value='JCR-SQL2'; $.fancybox.close();">
                    Use it in the query area
                </button>
            </li>
            <li><strong>XPath</strong>:<br/>
                <c:set var="q">//element(*,jnt:news)[@jcr:created > xs:dateTime('2011-06-01T00:00:00.000Z')] order by @jcr:created descending</c:set>
                <code>${fn:escapeXml(q)}</code><br/>
                <c:set var="jsQ" value="${functions:escapeJavaScript(q)}"/>
                <button onclick="document.getElementById('query').value='${jsQ}'; document.getElementById('lang').value='xpath'; $.fancybox.close();">
                    Use it in the query area
                </button>
            </li>
        </ul>
        <h4>3. Select all PDF files in the ACME Web site (site key: 'ACME')</h4>
        <ul>
            <li><strong>XPath</strong>:<br/>
                <c:set var="q">/jcr:root/sites/ACME//element(*,nt:file)[jcr:content/@jcr:mimeType='application/pdf' or jcr:content/@jcr:mimeType='application/x-pdf']</c:set>
                <code>${fn:escapeXml(q)}</code><br/>
                <c:set var="jsQ" value="${functions:escapeJavaScript(q)}"/>
                <button onclick="document.getElementById('query').value='${jsQ}'; document.getElementById('lang').value='xpath'; $.fancybox.close();">
                    Use it in the query area
                </button>
            </li>
        </ul>
    </div>
</div>
</body>
</html>