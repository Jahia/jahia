<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:if test="${not renderContext.loggedIn}">
    <form method="post" action="${url.base}${currentNode.path}.newUser.do" name="newDocspace">
        <input type="hidden" name="userprofilepage" value="${currentNode.properties['userProfilePage'].node.path}"/>
        <h3 class="boxdocspacetitleh3"><fmt:message key="userregistration.label.form.name"/></h3>
        <fieldset>
            <legend><fmt:message key="userregistration.label.form.name"/></legend>

            <p><label for="login"><fmt:message key="userregistration.label.form.login"/></label>
                <input type="text" name="desired_login" id="login" value="" tabindex="20"/></p>


            <p><label for="password"><fmt:message
                    key="userregistration.label.form.password"/></label><input type="password" name="desired_password" id="password"/></p>
            <p><label for="email"><fmt:message
                    key="userregistration.label.form.email"/></label><input type="text" name="desired_email" id="email"/></p>
            <p><label for="firstname"><fmt:message
                    key="userregistration.label.form.firstname"/></label><input type="text" name="desired_firstname" id="firstname"/></p>
            <p><label for="lastname"><fmt:message
                    key="userregistration.label.form.lastname"/></label><input type="text" name="desired_lastname" id="lastname"/></p>

            <div>
                <input type="submit" class="button"
                       value="<fmt:message key="docspace.label.docspace.create"/>"  />
            </div>
        </fieldset>
    </form>
</c:if>