<%@page import="java.io.File"%>
<%@page import="java.io.FileFilter"%>
<%@page import="org.apache.commons.io.FileUtils"%>
<%@page import="org.apache.commons.io.filefilter.DirectoryFileFilter"%>
<%@page import="org.jahia.settings.SettingsBean"%>
<%@page import="org.jahia.services.search.spell.CompositeSpellChecker"%>
<%@page import="org.jahia.utils.SearchIndexUtils"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%
File indexRoot = new File(SettingsBean.getInstance().getRepositoryHome(), request.getParameter("indexPath"));
int hash = indexRoot.getPath().hashCode();
%>
<%= indexRoot.getPath() %> (<%= org.jahia.utils.FileUtils.humanReadableByteCount(FileUtils.sizeOfDirectory(indexRoot)) %>)<br/>
<table cellpadding="0" cellspacing="0" border="0">
<%  int count = 0;
    for (File folder : indexRoot.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)) {
        count++;
    %>
    <tr>
        <td width="100" style="padding-left: 10px"><%= folder.getName() %></td>
        <td width="125"><%= org.jahia.utils.FileUtils.humanReadableByteCount(FileUtils.sizeOfDirectory(folder)) %></td>
        <%
        StringBuilder buf = new StringBuilder();
        pageContext.setAttribute("ok", Boolean.valueOf(SearchIndexUtils.checkIndex(folder, buf)));
        pageContext.setAttribute("result", buf.toString().replace("\n", "<br/>"));
        pageContext.setAttribute("obsolete", buf.toString().contains("could not read any segments file in directory"));
        %>
        <td width="25" align="right">
        <span style="color: ${ok ? 'green' : (obsolete ? 'brown' : 'red')}">${ok ? 'OK' : (obsolete ? 'Obsolete' : '<strong>Problem</strong>')}</span>
        </td>
        <td>
<<<<<<< .working
        <a class="detailsLink" title="Show details" href="#details-${param.indexId}-<%=count%>"><img src="<c:url value='/css/images/andromeda/icons/help.png'/>" width="16" height="16" alt="?" title="Show details"/></a>
=======
        <a class="detailsLink" title="Show details" href="#details-<%=hash%>-<%=count%>"><img src="<c:url value='/css/images/andromeda/icons/help.png'/>" width="16" height="16" alt="?" title="Show details"/></a>
>>>>>>> .merge-right.r42665
        <div style="display: none;">
<<<<<<< .working
            <div id="details-${param.indexId}-<%=count%>">
=======
            <div id="details-<%=hash%>-<%=count%>">
>>>>>>> .merge-right.r42665
                <h3>Index <%= folder.getPath() %></h3>
                <pre>${result}</pre>
            </div>
        </div>
        </td>
    </tr>
<% } %>
</table>