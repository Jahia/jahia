<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

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