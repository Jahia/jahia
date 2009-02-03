<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page language="java" %>
<html>
  <head>
    <title>Add Jahia Open Search plugins to your browser</title>
  	<link rel="search" type="application/opensearchdescription+xml" title="Jahia CMS" href="${pageContext.request.contextPath}/opensearch/opensearch-descriptor.xml">
  	<link rel="search" type="application/opensearchdescription+xml" title="Jahia CMS RSS SEARCH" href="${pageContext.request.contextPath}/opensearch/opensearch-descriptor-rss.xml">
  </head>
  <body bgcolor="#FFFFFF">
    <script language="javascript">
      window.external.AddSearchProvider('${pageContext.request.contextPath}/opensearch/opensearch-descriptor.xml');
      window.external.AddSearchProvider('${pageContext.request.contextPath}/opensearch/opensearch-descriptor-rss.xml');
    </script>
    <div align="center">
		<h2>If ok, You should be able to see Jahia Open Search Icons in the Browser's search box.</h2>
		<img src="jahia_opensearch_plugins.gif">
		</div>
  </body>
</html>