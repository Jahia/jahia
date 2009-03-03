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

<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../../common/declarations.jspf" %>
<c:set var="boxID" scope="request" value="columnB_box"/>
<p>
    <%
        //temporary code to get the PageID from the first "using Boxes" template
        org.jahia.data.JahiaData jData = (org.jahia.data.JahiaData) pageContext.getRequest().getAttribute("org.jahia.data.JahiaData");
        org.jahia.services.pages.JahiaPageDefinition theTemplate = org.jahia.registries.ServicesRegistry.getInstance().getJahiaPageTemplateService().lookupPageTemplateByName(
                "Using-Boxes", jData.getProcessingContext().getSiteID());
        java.util.List pageIds = org.jahia.registries.ServicesRegistry.getInstance().getJahiaPageService().getPageIDsWithTemplate(theTemplate.getID());
        int paId = jData.page().getID();
        if (pageIds != null && pageIds.size() > 0)
            paId = ((Integer) pageIds.get(0)).intValue();
    %>

    <b><fmt:message key='boxtags.display'/>
        (pageID="<%=paId%>"):</b>
    <template:boxList name="columnB_box" id="columnB_box" pageID="<%=paId%>" displayActionMenu="false">
        <template:container id="boxContainer" actionMenuNamePostFix="box" actionMenuNameLabelKey="box.update"
                            cache="no">
            <template:box displayTitle="true" id="boxCont" onError="full" surroundingDivCssClass="jahiabox"/>
        </template:container>
    </template:boxList>
</p>

<p>
    <b><fmt:message key='boxtags.pagelevel'/>:</b>
    <template:boxList name="columnB_box" id="columnB_box" pageLevel="2" displayActionMenu="false">
        <template:container id="boxContainer" actionMenuNamePostFix="box" actionMenuNameLabelKey="box.update"
                            cache="no">
            <template:box displayTitle="true" id="boxCont" onError="full" surroundingDivCssClass="jahiabox"/>
        </template:container>
    </template:boxList>
</p>

<p>

    <b><fmt:message key='boxtags.errorfull'/>:</b>
    <template:boxList name="columnB_box" id="columnB_box" >
        <template:container id="boxContainer" actionMenuNamePostFix="box" actionMenuNameLabelKey="box.update"
                            cache="no">
            <template:box displayTitle="true" id="boxCont" onError="full" surroundingDivCssClass="jahiabox"/>
        </template:container>
    </template:boxList>
</p>

<p>

    <b><fmt:message key='boxtags.errorhide'/>:</b>
    <template:boxList name="columnB_box" id="columnB_box" >
        <template:container id="boxContainer" actionMenuNamePostFix="box" actionMenuNameLabelKey="box.update"
                            cache="no">
            <template:box displayTitle="true" id="boxCont" onError="hide" surroundingDivCssClass="jahiabox"/>
        </template:container>
    </template:boxList>
</p>

<p>

    <b><fmt:message key='boxtags.errorcompact'/>:</b>
    <template:boxList name="columnB_box" id="columnB_box" >
        <template:container id="boxContainer" actionMenuNamePostFix="box" actionMenuNameLabelKey="box.update"
                            cache="no">
            <template:box id="boxCont" onError="COMPACT" surroundingDivCssClass="jahiabox"/>
        </template:container>
    </template:boxList>
</p>