<%@ tag import="org.jahia.params.ProcessingContext" %>
<%@ tag import="org.jahia.registries.ServicesRegistry" %>
<%@ tag import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ tag import="org.jahia.services.content.JCRStoreService" %>
<%@ tag import="javax.jcr.RepositoryException" %>
<%@ tag import="java.util.ArrayList" %>
<%@ tag import="java.util.Iterator" %>
<%@ tag import="org.jahia.services.content.JCRSessionFactory" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ attribute name="jahiaServletPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="jahiaContextPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
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
<template:gwtJahiaModule id="contentpicker" jahiaType="contentpicker"  jahiaServletPath="${fn:escapeXml(jahiaServletPath)}" jahiaContextPath="${fn:escapeXml(jahiaContextPath)}" rootPath="<%=rootPath%>"
                         startPath="<%=startPath%>"
                         filters="<%=filters%>"
                         mimeTypes="${fn:escapeXml(mimeTypes)}" callback="${fn:escapeXml(callback)}" config="${fn:escapeXml(conf)}"
                         embedded="<%=embedded%>" multiple="<%=multiple%>"/>

