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

<%@ include file="../common/declarations.jspf" %>
    <template:containerList name="pictures" id="pictFolderContainerList" actionMenuNamePostFix="pictFolder"
                           actionMenuNameLabelKey="pictFolder">
        <template:container id="pictFolderContainer" emptyContainerDivCssClassName="mockup-pictFolder">
            <template:field name='pictFolder' var="pictures" display="false"/>
            <c:set var="picts" value="${pictures.file.realName}" scope="request"/>
            <ui:thumbView path="${picts}" cssClassName="thumbView"/>
        </template:container>
    </template:containerList>