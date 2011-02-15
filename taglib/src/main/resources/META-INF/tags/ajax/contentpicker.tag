<%@ tag import="org.jahia.params.ProcessingContext" description="Displays a content picker" %>
<%@ tag import="org.jahia.registries.ServicesRegistry" %>
<%@ tag import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ tag import="org.jahia.services.content.JCRStoreService" %>
<%@ tag import="javax.jcr.RepositoryException" %>
<%@ tag import="java.util.ArrayList" %>
<%@ tag import="java.util.Iterator" %>
<%@ tag import="org.jahia.services.content.JCRSessionFactory" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ attribute name="jahiaServletPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="jahiaContextPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="filesServletPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="rootPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="startPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="filters" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="mimeTypes" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="conf" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="embedded" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="callback" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="selectedNodeUUIds" required="false" rtexprvalue="true" type="java.util.List" description="text" %>
<%@ attribute name="multiple" required="false" rtexprvalue="true" type="java.lang.Boolean" description="text" %>


<%
    final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
    final JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();

%>

<script type="text/javascript">
    var crop = 0;
    var sContentNodes = [
        <%
if (multiple == null) {
    multiple = false;
}
if(selectedNodeUUIds == null){
 // case of single selection
 selectedNodeUUIds = new ArrayList<String>();
}

final Iterator selectedNodeIter = selectedNodeUUIds.iterator();
while (selectedNodeIter.hasNext()) {
   final String uuid = (String) selectedNodeIter.next();
   JCRNodeWrapper jcrNodeWrapper = null ;
   try {
        jcrNodeWrapper = sessionFactory.getCurrentUserSession().getNodeByUUID(uuid);
   } catch (RepositoryException e) {
        jcrNodeWrapper = null;
   }
   if (jcrNodeWrapper != null) {
        %>
        {
            uuid:"<%=jcrNodeWrapper.getUUID()%>",
            name:"<%=jcrNodeWrapper.getName()%>",
            displayName:"<%=jcrNodeWrapper.getName()%>",
            path:"<%=jcrNodeWrapper.getPath()%>"
        }<%if(selectedNodeIter.hasNext()){%>,
        <%}%>
        <%
     }

}%>
    ];
    var sLocale = '${locale}';

    var sAutoSelectParent = '${autoSelectParent}';

</script>
<template:gwtJahiaModule id="contentpicker" jahiaType="contentpicker"  jahiaServletPath="${fn:escapeXml(jahiaServletPath)}"
                         jahiaContextPath="${fn:escapeXml(jahiaContextPath)}" filesServletPath="${fn:escapeXml(filesServletPath)}"  rootPath="<%=rootPath%>"
                         startPath="<%=startPath%>"
                         filters="<%=filters%>"
                         mimeTypes="${fn:escapeXml(mimeTypes)}" callback="${fn:escapeXml(callback)}" config="${fn:escapeXml(conf)}"
                         embedded="<%=embedded%>" multiple="<%=multiple%>"/>

