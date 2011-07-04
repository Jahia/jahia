<%@ tag body-content="scriptless" description="Renders a date picker control using corresponding jQuery UI component (http://docs.jquery.com/UI/Datepicker). Datepicker options (see http://docs.jquery.com/UI/Datepicker#options) can be specified as a body of this tag in form {option1: value1, option2: value2 ...}. The template module that will use this tag should have Default Jahia Templates module as a dependency." %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field ID to bind the date picker to." %>
<%@ attribute name="theme" required="false" type="java.lang.String"
              description="The name of the CSS file with corresponding jQuery theme. [jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css]" %>
<%@ attribute name="time" required="false" type="java.lang.Boolean"
              description="True if you want the time selector" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty requestScope['org.jahia.tags.dateSelector.resources']}">
    <template:addResources type="css" resources="${not empty theme ? theme : 'jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css'}"/>
    <template:addResources type="javascript"
                           resources="jquery.min.js,jquery-ui.min.js"/>
    <c:set var="locale" value="${renderContext.mainResource.locale}"/>
    <c:if test="${locale != 'en_US'}">
        <template:addResources type="javascript" resources="i18n/jquery.ui.datepicker-${locale.language}.js"/>
        <c:if test="${not empty locale.country}">
            <template:addResources type="javascript"
                                   resources="i18n/jquery.ui.datepicker-${locale.language}-${locale.country}.js"/>
        </c:if>
    </c:if>
    <c:if test="${not empty time and time eq true}">
        <template:addResources type="javascript" resources="timepicker.js"/>
        <template:addResources type="css" resources="timepicker.css"/>
    </c:if>
    <c:set var="org.jahia.tags.dateSelector.resources" value="true" scope="request"/>
</c:if>
<jsp:doBody var="options"/>
<c:if test="${empty options}">
    <c:set var="options" value="{dateFormat: $.datepicker.ISO_8601, showButtonPanel: true, showOn: 'both'}"/>
</c:if>
<script type="text/javascript">
    /* <![CDATA[ */
    <c:if test="${empty time or time eq false}">
    $(document).ready(function() {
        $('#${fieldId}').datepicker(${options});
    });
    </c:if>
    <c:if test="${not empty time and time eq true}">
    $(document).ready(function() {
        $('#${fieldId}').datetime(${options});
    });
    </c:if>
    /* ]]> */
</script>