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
<%@ page import="org.jahia.engines.applications.*" %>
<%@ page import="org.jahia.data.applications.EntryPointDefinition" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.resourcebundle.*" %>
<%@ page import="org.displaytag.tags.*" %>
<%@ page import="org.displaytag.util.*" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<script type="text/javascript">
  // OpenJahiaScrollableWindow
  function OpenJahiaScrollableWindow(url, name, width, height) {
    // check for veryvery small screens
    if (screen.availHeight <= 720 || screen.availWidth <= 950) {
      width = screen.availWidth;
      height = screen.availHeight;
    }
    var params = "width=" + width + ",height=" + height + ",left=10,top=10,resizable=yes,scrollbars=yes,status=no";
    // Check if the popup is alread opened, if so, give it the focus
    if (myEngineWin != null) {
      try {
        if (myEngineWin.closed) { // need to test it first...
          myEngineWin = null;
          myEngineWin = window.open(url, name, params);
        } else {
          if (myEngineWin.name != name) {
            myEngineWin.close();
            myEngineWin = null;
            myEngineWin = window.open(url, name, params);
          } else {
            myEngineWin.focus();
          }
        }
      } catch (ex) {
          // suppress exception
      }
    } else {
        myEngineWin = window.open(url, name, params);
    }
  }

</script>
<%
//get resource bundle
//JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
String titleNameColumn = JahiaResourceBundle.getEngineResource("org.jahia.engines.shared.Application_Field_displyTag.portletNameColumnTitle.label", jData.getProcessingContext(), jData.getProcessingContext().getLocale());
String titleDescriptionColumn = JahiaResourceBundle.getEngineResource("org.jahia.engines.shared.Application_Field_displyTag.portletDescriptionColumnTitle.label", jData.getProcessingContext(), jData.getProcessingContext().getLocale());
String defaultDescription = JahiaResourceBundle.getEngineResource("org.jahia.engines.shared.Application_Field_displyTag.portletNoDescription.label", jData.getProcessingContext(), jData.getProcessingContext().getLocale());

// portlet data
List definitions = theTempoApplicationBean.getEntryPointDefinitions();
String appName = theTempoApplicationBean.getName();
// data for display tag
String tableId = "porltet_webapps";
request.setAttribute("resultRows", definitions);
ParamEncoder encoder = new ParamEncoder(tableId);
String paramPageName = encoder.encodeParameterName(TableTagParameters.PARAMETER_PAGE);
String paramPageValue = request.getParameter(paramPageName);
String paramSort = encoder.encodeParameterName(TableTagParameters.PARAMETER_SORT);
String paramSortValue = request.getParameter(paramSort);
String paramOrder = encoder.encodeParameterName(TableTagParameters.PARAMETER_ORDER);
String paramOrderValue = request.getParameter(paramOrder);
%>
<%/* hidden */%>
<input type="hidden" name="<%=paramPageName%>" value="<%=paramPageValue%>"/>
<input type="hidden" name="<%=paramSort%>" value="<%=paramSortValue%>"/>
<input type="hidden" name="<%=paramOrder%>" value="<%=paramOrderValue%>"/>
<br/>

<display:table style="width:100%" class="evenOddTable" id="<%=tableId%>" requestURI="<%=engineUrl%>" name="resultRows" export="false" defaultsort="1" pagesize="5">
  <%
    EntryPointDefinition definition = (EntryPointDefinition) pageContext.getAttribute(tableId);
    // get name and id of the current portlet Definition
    String definitionName = "";
    String appID = "";
    String uniqueID = "";

    // get description
    String description = defaultDescription;
      if (definition != null) {
          definitionName = definition.getName();
          appID = "" + definition.getApplicationID();
          uniqueID = appID + "::" + definitionName;
          if (isPortlet) {
              description = definition.getDescription();
          }
      }


  %>
  <display:column title="<%=titleNameColumn%>" sortable="true" sortProperty="name" headerClass="sortable" sortName="page">
    <%=definitionName %>
  </display:column>
  <display:column title="<%=titleDescriptionColumn%>" sortable="true" sortProperty="name" headerClass="sortable" sortName="page">
    <%=description %>
  </display:column>
  <display:setProperty name="paging.banner.placement" value="bottom"/>
</display:table>

<script type="text/javascript">
  // add flag submit when a displayTag link is clicked
  function addFlagSubmitToDisplayTagLinks(tableId, itemListingClass) {
    var table = document.getElementById(tableId);
    var thead = table.getElementsByTagName("thead")[0];
    var hrefs = thead.getElementsByTagName("a");
   // add event handlers so rows light up and are clickable
    for (var i = 0; i < hrefs.length; i++) {
      hrefs[i].onclick = function() {
         flagSubmit();
      };
    }

    var span = getElementsByClassName(document, "pagelinks")[0];
    if (span) {
      hrefs = span.getElementsByTagName("a");
  // add event handlers so rows light up and are clickable
      for (var i = 0; i < hrefs.length; i++) {
        hrefs[i].onclick = function() {
           flagSubmit();
        };
      }
    }

  }

  function flagSubmit() {
    if (submittedCount == 0) {
      submittedCount++;
    }
  }

  function getElementsByClassName(el, clsName)
  {
    var arr = new Array();
    var elems = el.getElementsByTagName("*");
    for (var cls, i = 0; ( elem = elems[i] ); i++){
      if (elem.className == clsName){
        arr[arr.length] = elem;
      }
    }
    return arr;
  }

  addFlagSubmitToDisplayTagLinks('<%=tableId%>', 'evenOddTable');
</script>