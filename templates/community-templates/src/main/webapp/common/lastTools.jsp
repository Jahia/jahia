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
<%@ include file="declarations.jspf" %>
<div class="box lastTools"><!--start box lastTools -->

    <div class="colA"><!--start colA lastTools -->
        <h3><fmt:message key='lastSubscribers'/></h3>

        <ul class="lastSubscribers"><!--start lastSubscribers -->
            <ui:userList displayLimit="10" styleClass="lastSubscribers"/>

        </ul>
        <!--stop lastSubscribers -->
    </div>
    <!--stop colA lastTools -->
    <div class="colB"><!--start colB lastTools -->
        <h3><fmt:message key='availableGroups'/></h3>

        <ul class="lastSubscribersGroup"><!--start lastSubscribersGroup -->
            <ui:groupList displayLimit="10" styleClass="lastSubscribersGroup" viewMembers="false"/>
        </ul>
        <!--stop lastSubscribersGroup -->
    </div>
    <!--stop colB lastTools -->

    <div class="clear"></div>
</div>
<!--stop box lastTools-->