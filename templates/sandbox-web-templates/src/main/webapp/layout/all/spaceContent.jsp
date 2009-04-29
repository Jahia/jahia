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

<div id="content2">
    <!--start content  #content2=InsetB/content #content3=content/InsetA   content4=alone #content5=50%InsetB/50%content-->
    <div class="spaceContent"><!--start spaceContent -->
        <template:include page="containers/banner.jsp"/>
        <template:include page="containers/maincontent/mainContentAdvanced.jsp"/>
        <template:include page="containers/newsContent/newsDisplay.jsp"/>
        <template:include page="containers/peopleContent/peopleDisplay.jsp"/>
        <template:include page="containers/pressContent/pressDisplay.jsp"/>
    </div>
    <!--stop space content-->
</div>
<!--stopContent-->
