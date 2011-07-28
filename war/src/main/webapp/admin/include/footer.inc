<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div id="copyright">
  <%=copyright %>&nbsp;<%=Jahia.getFullProductVersion() %>
</div>
</div>
<br/>
<br/>
 <c:if test="${empty requestScope['jahia.engines.gwtModuleIncluded']}">
<internal:gwtGenerateDictionary/>        
<internal:gwtImport module="org.jahia.ajax.gwt.module.admin.Admin"/>
</c:if>
</body>
</html>
<% out.flush(); %>