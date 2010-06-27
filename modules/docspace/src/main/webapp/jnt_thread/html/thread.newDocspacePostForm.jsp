<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
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