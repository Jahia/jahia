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
--%><%@ page language="java" %>
<%@ page import="org.displaytag.tags.TableTagParameters" %>
<%@ page import="org.displaytag.util.ParamEncoder" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.data.applications.ApplicationBean" %>
<%@ page import="org.jahia.data.applications.EntryPointDefinition" %>
<%@ page import="org.jahia.data.applications.PortletEntryPointDefinition" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle" %>
<%@taglib uri="http://displaytag.sf.net" prefix="display"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
		//get resource bundle
		JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
 		String titleNameColumn = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.shared.Application_Field_displyTag.portletNameColumnTitle.label",jData.getProcessingContext().getLocale());
 		String titleContextColumn = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.shared.Application_Field_displyTag.portletContextColumnTitle.label",jData.getProcessingContext().getLocale());
 		String titleDescriptionColumn = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.shared.Application_Field_displyTag.portletDescriptionColumnTitle.label",jData.getProcessingContext().getLocale());

 		// data for display tag
 		String tableId = "porltet_webapps";

 		//search
 		String portletSearchName = "portlet_search";
 		String portletSearchValue = request.getParameter(portletSearchName);
 		if(portletSearchValue == null || portletSearchValue.equals("*")){
 			portletSearchValue = "";
 		}
 		String portletSearchCastedValue = new String(portletSearchValue);
 		portletSearchValue = portletSearchValue.toLowerCase();

 		String portletSearchType = "portlet_type";
 		String portletSearchTypeValue = request.getParameter(portletSearchType);
 		if(portletSearchTypeValue == null){
 			portletSearchTypeValue = "all";
 		}


   	List resultRows = new ArrayList();
   	Map appPropsMap = new HashMap();
   	boolean containSelectedApp = false;
   	String currentApplicationContext = "";
    while (appList.hasNext()) {
    		ApplicationBean appBean = (ApplicationBean) appList.next();
        Iterator epDefIter = appBean.getEntryPointDefinitions().iterator();
        while (epDefIter.hasNext()) {
        	EntryPointDefinition entryPointDef = (EntryPointDefinition) epDefIter.next();
        	String name = entryPointDef.getName();
            if(name != null){
                name = entryPointDef.getName().toLowerCase();
            }else{
                name = "";
            }
            String desc = appBean.getdesc();
            if(desc != null){
                desc = appBean.getdesc().toLowerCase();
            } else {
                desc = "";
            }
            String ctx = appBean.getName();
            if(ctx != null){
                ctx = appBean.getName().toLowerCase();
            } else{
                ctx = "";
            }
            //no search
        	if(portletSearchValue == null ||portletSearchValue.equals("") ){
        		resultRows.add(entryPointDef);
        		appPropsMap.put(""+entryPointDef.getApplicationID(),appBean);
        		if(appID == entryPointDef.getApplicationID() ){
        			containSelectedApp = true;
        		}
        	}

        	// search by name
       		else if(portletSearchTypeValue.equalsIgnoreCase("name")){
        		if(name.indexOf(portletSearchValue) > -1){
        			resultRows.add(entryPointDef);
        			appPropsMap.put(""+entryPointDef.getApplicationID(),appBean);
        			if(appID == entryPointDef.getApplicationID() ){
        				containSelectedApp = true;
        			}
        		}
        	}
        	//search by description
        	else if(portletSearchTypeValue.equalsIgnoreCase("description")){
        		if(desc.indexOf(portletSearchValue) > -1){
        			resultRows.add(entryPointDef);
        			appPropsMap.put(""+entryPointDef.getApplicationID(),appBean);
        			if(appID == entryPointDef.getApplicationID() ){
        				containSelectedApp = true;
        			}
         		}
         	}
         	// search by context
         	else if(portletSearchTypeValue.equalsIgnoreCase("context")){
        		if(ctx.indexOf(portletSearchValue) > -1){
        			resultRows.add(entryPointDef);
        			appPropsMap.put(""+entryPointDef.getApplicationID(),appBean);
        			if(appID == entryPointDef.getApplicationID() ){
        				containSelectedApp = true;
        			}
         		}
         	}
         	//search All
          else if(portletSearchTypeValue.equalsIgnoreCase("all")){
        			if(name.indexOf(portletSearchValue) >-1 || desc.indexOf(portletSearchValue) >-1 || ctx.indexOf(portletSearchValue) > -1){
        				resultRows.add(entryPointDef);
        				appPropsMap.put(""+entryPointDef.getApplicationID(),appBean);
        				if(appID == entryPointDef.getApplicationID() ){
        					containSelectedApp = true;
        				}
        			}
        	}

        	// get current context
        	if(appID == entryPointDef.getApplicationID() ){
        		currentApplicationContext = appBean.getName();
        	}

        }
   	}

   	// set counter value
   	counter = resultRows.size();


    request.setAttribute( "resultRows",resultRows);
    ParamEncoder encoder = new ParamEncoder(tableId);
    String paramPageName = encoder.encodeParameterName(TableTagParameters.PARAMETER_PAGE);
  	String paramPageValue = request.getParameter(paramPageName);

  	String paramSort = encoder.encodeParameterName(TableTagParameters.PARAMETER_SORT);
  	String paramSortValue = request.getParameter(paramSort);

  	String paramOrder = encoder.encodeParameterName(TableTagParameters.PARAMETER_ORDER);
  	String paramOrderValue = request.getParameter(paramOrder);

    String selectedFieldId = null;
    if(jParams !=null){
        selectedFieldId = jParams.getParameter("_" + Integer.toString(theField.getID()));
    }
    if(selectedFieldId == null){
    	selectedFieldId = ""+appID;
    	if(selectedEntryPointDefName != null){
        selectedFieldId = appID+"_"+selectedEntryPointDefName;
      }
    }
    if(selectedFieldId == null){
    	selectedFieldId = "-1";
    }


%>

<b>
    <%if(selectedEntryPointDefName!=null && !selectedEntryPointDefName.equalsIgnoreCase("")){%>
        <fmt:message key="org.jahia.engines.shared.Application_Field_displyTag.selectedPortlet.label"/>
        <a href="javascript:searchSelectedWebapp();"><%=selectedEntryPointDefName%> </a> .
        <a title="<fmt:message key="org.jahia.engines.shared.Application_Field_displyTag.unselectedPortlet.label"/>" href="javascript:changeSelectedWebapp('no_webapp');">
            <img src="${pageContext.request.contextPath}/engines/images/icons/delete3.gif" alt="search"  border="0"/>
        </a>
    <%}else{%>
        <fmt:message key="org.jahia.engines.shared.Application_Field_displyTag.noselectedPortlet"/>
    <%}%>

    <input style="display:none;" id="no_webapp" name="_<%=theField.getID()%>"  value="-1" type="radio" onchange="javascript:check();javascript:handleActionChange('edit');" <%if(appID == -1){%> checked <%}%>/>

</b>

<br/><br/>

<%/* hidden */%>
<input type="hidden" name="<%=paramPageName%>" value="<%=paramPageValue%>"/>
<input type="hidden" name="<%=paramSort%>" value="<%=paramSortValue%>"/>
<input type="hidden" name="<%=paramOrder%>" value="<%=paramOrderValue%>"/>
<% if(!appListIsEmpty) {%>
<fmt:message key="org.jahia.engines.shared.Application_Field_displyTag.search.label"/>
<input onkeyup="javascript:doSeachOnEnterClick(event);" id="<%=portletSearchName%>" name="<%=portletSearchName%>" value="<%=portletSearchCastedValue%>"/>
<fmt:message key="org.jahia.engines.shared.Application_Field_displyTag.searchBy.label"/>
<select id="<%=portletSearchType%>" name="<%=portletSearchType%>" value="<%=portletSearchTypeValue%>">
	<option value="name" <%if(portletSearchTypeValue.equalsIgnoreCase("name")){%>selected <%}%>><%=titleNameColumn%></option>
	<option value="context"<%if(portletSearchTypeValue.equalsIgnoreCase("context")){%>selected <%}%>><%=titleContextColumn%></option>
	<option value="description"<%if(portletSearchTypeValue.equalsIgnoreCase("description")){%>selected <%}%>><%=titleDescriptionColumn%></option>
	<option value="all"<%if(portletSearchTypeValue.equalsIgnoreCase("all")){%>selected <%}%>><fmt:message key="org.jahia.engines.shared.Application_Field_displyTag.all.label"/></option>
</select>
<a href="javascript:check();javascript:handleActionChange('edit');">
<img src="${pageContext.request.contextPath}/css/images/icones/find.gif" alt="search" width="24" height="24" border="0"/>
</a>
<br/>
<display:table style="width:600px" class="evenOddTable" id="<%=tableId%>" name="resultRows" export="false" defaultsort="1" pagesize="10">
	<% 	EntryPointDefinition epDef = (EntryPointDefinition)pageContext.getAttribute(tableId);
	   	String name = "";
		 	String id = "";
		 	if(epDef != null){
 				name = epDef.getName();
				id = ""+epDef.getApplicationID();

				String currentFieldId = id+"_"+name;
				ApplicationBean currentAppBean = (ApplicationBean)appPropsMap.get(id);

				// get description
				String description = currentAppBean.getdesc();

				// check if portlet has a defnition. If it's the case, replace webapp definition by portlet definition
				if(epDef instanceof PortletEntryPointDefinition){
                    description = epDef.getDescription();
				}

				if(description == null || description.equals("")){
						description = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.shared.Application_Field_displyTag.portletNoDescription.label",jData.getProcessingContext().getLocale());;
				}
				String contextName = currentAppBean.getName();
				boolean isSelected = (appID == currentAppBean.getID() && name.equals(selectedEntryPointDefName)) || (appID == -1 && counter == 0);
				String selectedStyle = "";

				if(isSelected){
					selectedStyle = "color:#5F77B0; font-weight: bold;";
					selectedFieldId	= currentFieldId;
				}
   %>
  	<%/* name  */%>
 		<display:column  style="<%=selectedStyle%>"  title="<%=titleNameColumn%>" sortable="true" sortProperty="name" headerClass="sortable" sortName="page">
 			<div id="name_<%=currentFieldId%>" onmouseover="onmouseoverWebapp('<%=currentFieldId%>');" onmouseout="onmouseoutWebapp('<%=currentFieldId%>')" style="cursor: pointer;"onclick="javascript:changeSelectedWebapp('<%=currentFieldId%>');">
 				<%=name%>
 			</div>
 		</display:column>

 		<display:column style="<%=selectedStyle%>"  title="<%=titleContextColumn%>" sortable="true" sortProperty="name" headerClass="sortable" sortName="page">
 			<div id="contextName_<%=currentFieldId%>" onmouseover="onmouseoverWebapp('<%=currentFieldId%>');" onmouseout="onmouseoutWebapp('<%=currentFieldId%>')"  style="cursor: pointer;" onclick="javascript:changeSelectedWebapp('<%=currentFieldId%>');">
 				<%=contextName%>
 			</div>
 			<input  style="display:none;" id="<%=currentFieldId%>" name="_<%=theField.getID()%>"  value="<%=currentFieldId%>" type="radio" onchange="javascript:check();javascript:handleActionChange('edit');" <%if(isSelected){%> checked <%}%>/>
 		</display:column>

    <%/* description  */%>
    <display:column style="<%=selectedStyle%>" title="<%=titleDescriptionColumn%>" sortable="true" headerClass="sortable" maxLength="25"><div id="description_<%=currentFieldId%>" onmouseover="onmouseoverWebapp('<%=currentFieldId%>');" onmouseout="onmouseoutWebapp('<%=currentFieldId%>')"  style="cursor: pointer;"onclick="javascript:changeSelectedWebapp('<%=currentFieldId%>');"><%=description%></div></display:column>

		<display:footer>
  		<tr>
            <td><input style="display:none;" id="<%=selectedFieldId%>" name="_<%=theField.getID()%>"  value="<%=selectedFieldId%>" type="radio" onchange="javascript:check();javascript:handleActionChange('edit');" checked />	 </td>
  			<td></td>
  			<td></td>
  		<tr>
  	</display:footer>

		<%/* porperties */%>
		<display:setProperty name="paging.banner.placement" value="bottom"/>
		<%}%>
</display:table>
<%}%>
<br/>

<script type="text/javascript">
    <!--
function changeSelectedWebapp(fieldId) {
	document.getElementById(fieldId).click();
	check();
	handleActionChange('edit');
	return true;

}

function unselectedWebapp() {
	document.getElementById('<%=appID%>').click();
	check();
	handleActionChange('edit');
	return true;

}

function searchSelectedWebapp() {
	document.getElementById('<%=portletSearchName%>').value='<%=selectedEntryPointDefName%>' ;
	document.getElementById('<%=portletSearchType%>').value='name' ;
	check();
	handleActionChange('edit');
	return true;

}

function onmouseoverWebapp(fieldId) {
	changeElementsColorValue(fieldId,'#4E77B0','white');
}

function onmouseoutWebapp(fieldId) {
	changeElementsColorValue(fieldId,'','');
}

function changeElementsColorValue(fieldId,colorB,colorT){
	eleNameStyle = document.getElementById('name_'.concat(fieldId)).style;
	contextNameStyle = document.getElementById('contextName_'.concat(fieldId)).style;
	descNameStyle = document.getElementById('description_'.concat(fieldId)).style;

	eleNameStyle.background = colorB;
	contextNameStyle.background = colorB;
	descNameStyle.background = colorB;

	eleNameStyle.color = colorT;
	contextNameStyle.color = colorT;
	descNameStyle.color = colorT;
}

function doSeachOnEnterClick(e) {
        var code;
        if (window.event)
            code = window.event.keyCode;
        else if (e)
            code = e.which;
        else
            code = null;

        if (code!=null && code == 13) {
            check();
            handleActionChange('edit');
        }
}

// add flag submit when a displayTag link is clicked
function addFlagSubmitToDisplayTagLinks(tableId, itemListingClass) {
    var table = document.getElementById(tableId);
    var thead = table.getElementsByTagName("thead")[0];
    var hrefs = thead.getElementsByTagName("a");
    // add event handlers so rows light up and are clickable
    for (i = 0; i < hrefs.length; i++) {
        hrefs[i].onclick = function() {
            flagSubmit();
        };
    }

    var span = getElementsByClassName(document, "pagelinks")[0];
    if (span) {
        hrefs = span.getElementsByTagName("a");
        // add event handlers so rows light up and are clickable
        for (i = 0; i < hrefs.length; i++) {
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
    for (var cls, i = 0; ( elem = elems[i] ); i++)
    {
        if (elem.className == clsName)
        {
            arr[arr.length] = elem;
        }
    }
    return arr;
}

addFlagSubmitToDisplayTagLinks('<%=tableId%>', 'evenOddTable');



// -->
</script>