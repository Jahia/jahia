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

<%@ include file="../../declarations.jspf" %>
<div class="links">
    <template:containerList name="${boxID}_linkContainer" id="links"
                            actionMenuNamePostFix="links" actionMenuNameLabelKey="links.add">
        <ul>
            <template:container id="linkContainer" displayActionMenu="false">
                <li>
                    <ui:actionMenu contentObjectName="linkContainer" namePostFix="link" labelKey="link.update">
                        <template:field name="link" maxChar="35"/>
                        <template:field name="linkDesc"/>
                    </ui:actionMenu>
                </li>
            </template:container>
        </ul>
    </template:containerList>
</div>
