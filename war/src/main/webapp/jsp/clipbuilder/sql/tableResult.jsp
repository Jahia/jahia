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

<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@taglib uri="http://displaytag.sf.net" prefix="display"%>
<%@page language="java" import="java.util.*"%>
<%
  // Columns name
  String[] queryColumns = (String[]) request.getSession().getAttribute("queryColumns");

  //Display the table data:
  List queryData = (List) request.getSession().getAttribute("queryData");
  int resultSize = queryData.size();

  //max rows
  int maxRows = resultSize;
  String maxRowsStrg = (String) request.getSession().getAttribute("tableSize");
  if (maxRowsStrg != null) {
    maxRows = Integer.parseInt(maxRowsStrg);
  }

%>
<display:table id="sql_clip_portlet" name="sessionScope.resultSet.rows" defaultsort="1" pagesize="<%=maxRows%>">
  <display:setProperty name="paging.banner.placement" value="bottom"/>
<%for (int i = 0; i < queryColumns.length; i++) {%>
  <display:column property="<%=queryColumns[i]%>" title='<%=queryColumns[i]+"       "%>' sortable="true" headerClass="sortable"/>
<%}%>
</display:table>
