<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<%@ include file="../../getUser.jspf" %>

<form id="changePassword" method="post" action="">
    <c:forEach items="${param}" var="p">
        <c:if test="${not empty ps}">
            <c:set var="ps" value="${ps}&${p.key}=${p.value}"/>
        </c:if>
        <c:if test="${empty ps}">
            <c:set var="ps" value="?${p.key}=${p.value}"/>
        </c:if>
    </c:forEach>
    <fieldset>
        <p><label for="password" class="left"><fmt:message key="label.password"/></label>
            <input type="password" id="password" name="password"/></p>

        <p><label for="passwordconfirm" class="left"><fmt:message key="label.comfirmPassword"/></label>
            <input type="password" id="passwordconfirm" name="passwordconfirm"/></p>
    </fieldset>
    <input class="button" type="button" value="<fmt:message key="label.submit"/>"
           tabindex="28"
           id="messagesendbutton" onclick="$('#changePassword').submit();">
</form>

<script type="text/javascript">
    $.ready(
        $("#changePassword").submit(function() {
            if ($("#password").val() == "") {
                alert("<fmt:message key="org.jahia.admin.userMessage.specifyPassword.label"/>");
                return false;
            }

            if ($("#password").val() != $("#passwordconfirm").val()) {
                alert("<fmt:message key="org.jahia.admin.userMessage.passwdNotMatch.label"/>");
                return false;
            }

            $.post('${url.base}${user.path}.changePassword.do',
                $(this).serializeArray(),function(data) {
                    alert(data['errorMessage']);
                }, 'json');

            return false;
        })
    );
</script>