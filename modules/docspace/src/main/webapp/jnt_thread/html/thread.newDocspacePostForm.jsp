<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<script type="text/javascript">
    function jahiaForumQuote(targetId, quotedText) {
        var targetArea = document.getElementById(targetId);
        if (targetArea) {
            targetArea.value = targetArea.value + '\n<blockquote>\n' + quotedText + '\n</blockquote>\n';
        }
        return false;
    }
</script>
<a name="threadPost"></a>

<div class="boxdocspace"><!--start boxdocspace -->
    <div class="boxdocspacegrey boxdocspacepadding10">
        <div class="boxdocspace-inner">
            <div class="boxdocspace-inner-border">
                <a id="formdocspacecomment" name="formdocspacecomment"></a>

                <div class="formDocspace formdocspacecomment"><!--start formdocspacecomment-->
                    <form action="${url.base}${currentNode.path}/*" method="post">
                        <input type="hidden" name="nodeType" value="jnt:post"/>
                        <input type="hidden" name="redirectTo"
                               value="${url.base}${renderContext.mainResource.node.path}.docspace"/>
                        <%-- Define the output format for the newly created node by default html or by redirectTo--%>
                        <input type="hidden" name="newNodeOutputFormat" value="html"/>

                        <p>
                            <label for="commenttitle"><fmt:message key="docspace.label.title"/> </label>
                            <input value="<c:if test="${not empty currentNode.nodes}"> Re:</c:if>${currentNode.propertiesAsString['threadSubject']}"
                                   type="text" size="35" id="forum_site" name="jcr:title" id="commenttitle"
                                   tabindex="1"/>
                        </p>

                        <p>
                            <label for="jahia-forum-thread-${currentNode.UUID}"><fmt:message key="docspace.label.comments"/> </label>
                            <textarea rows="7" cols="35" id="jahia-forum-thread-${currentNode.UUID}" name="content"
                                      tabindex="2"></textarea>
                        </p>

                        <div>
                            <input type="reset" value="Reset" class="button" tabindex="3"/>
                            <input type="submit" value="Submit" class="button" tabindex="4"/>
                        </div>
                    </form>


                </div>
            </div>
        </div>
    </div>
</div>