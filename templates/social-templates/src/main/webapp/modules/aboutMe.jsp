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

<h3><fmt:message key="aboutMe"/> </h3>

                        <div class="aboutMeListItem"><!--start aboutMeListItem -->
                            <div class="aboutMePhoto">
                                <img src="${picture.file.downloadUrl}" alt=""/>
                            </div>
                            <div class="aboutMeBody"><!--start aboutMeBody -->
                                <h5>${firstname}&nbsp;${lastname}</h5>
                                <%--
                                use this method when form handler manage dates
                                <fmt:formatDate pattern="yyyy" value="${birthdate.date}" var="birthyear"/>
                                <jsp:useBean id="now" class="java.util.Date" />
                                <fmt:formatDate pattern="yyyy" value="${now}" var="actualyear"/>--%>
                                <p class="aboutMeAge">
                                    ${birthdate}
                                    <%--<fmt:message key="age">
                                        <fmt:param value="${actualyear - birthyear}"/>
                                    </fmt:message>--%>
                                </p>

                                <div class="clear"></div>
                                <p class="aboutMeResume">${description}</p>
                            </div>
                            <!--stop aboutMeBody -->
                            <div class="clear"></div>
                        </div>                        <!--stop aboutMeListItem -->