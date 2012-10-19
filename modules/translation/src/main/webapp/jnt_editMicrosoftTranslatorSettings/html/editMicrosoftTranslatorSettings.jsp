<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<fmt:message key="label.changeSaved" var="i18nSaved"/><c:set var="i18nSaved" value="${functions:escapeJavaScript(i18nSaved)}"/>

<template:addResources type="javascript" resources="jquery.min.js,jquery.form.min.js"/>
<template:addResources>
    <script type="text/javascript">
        $(document).ready(function () {
            $("#updateSiteForm").submit(function(event) {
                $("#updateSiteButton").attr('disabled', 'disabled');
                event.preventDefault();
                var $form = $(this);
                var url = $form.attr('action');
                var type = $form.attr('method');
                var values = $form.serializeArray();
                values.push({name:'j:microsoftTranslationActivated', value:$('#activateProvider').is(':checked')});
                $.ajax({
                    type : type,
                    url: url,
                    data: values,
                    dataType: "json",
                    success: function(data, textStatus, jqXHR) {
                        alert('${i18nSaved}');
                        $("#updateSiteButton").removeAttr('disabled');
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        if (jqXHR.status == 400) {
                            var json = $.parseJSON(jqXHR.responseText);
                            if (typeof(json.validationError) !== "undefined") {
                                var message = "";
                                for (var i = 0; i < json.validationError.length; i++) {
                                    message += json.validationError[i].message + "\n";
                                }
                                alert(message);
                            }
                        }
                        $("#updateSiteButton").removeAttr('disabled');
                    }
                });
                return false;
            });
        });
    </script>
</template:addResources>
<c:set var="site" value="${renderContext.mainResource.node.resolveSite}"/>
<c:set var="providerActivated" value="${site.properties['j:microsoftTranslationActivated']}"/>

<h2><fmt:message key="siteSettings.label.translation"/> - ${fn:escapeXml(site.displayableName)}</h2>
<h3><fmt:message key="jmix_microsoftTranslatorSettings"/></h3>

<form id="updateSiteForm" action="<c:url value='${url.base}${site.path}'/>" method="post">
    <input type="hidden" name="jcrMethodToCall" value="put"/>
    <input type="hidden" name="jcr:mixinTypes" value="jmix:microsoftTranslatorSettings"/>
    <p><input type="checkbox" name="activateProvider" id="activateProvider" ${not empty providerActivated && providerActivated.boolean ? 'checked="checked"' : ''}/>&nbsp;<label for="activateProvider"><fmt:message key="jmix_microsoftTranslatorSettings.j_microsoftTranslationActivated"/></label></p>
    <p>
        <label for="microsoftClientId"><fmt:message key="jmix_microsoftTranslatorSettings.j_microsoftClientId"/></label>
        <input type="text" name="j:microsoftClientId" id="microsoftClientId" class="field" value="${site.properties['j:microsoftClientId'].string}" />
    </p>
    <p>
        <label for="microsoftClientSecret"><fmt:message key="jmix_microsoftTranslatorSettings.j_microsoftClientSecret"/></label>
        <input type="text" name="j:microsoftClientSecret" id="microsoftClientSecret" class="field" value="${site.properties['j:microsoftClientSecret'].string}" />
    </p>
    <p>
        <input type="submit" name="updateSiteButton" id="updateSiteButton" class="button" value="<fmt:message key='label.save'/>" />
    </p>
</form>
