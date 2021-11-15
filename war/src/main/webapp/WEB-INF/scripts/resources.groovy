import org.jahia.ajax.gwt.utils.GWTInitializer
import org.jahia.taglibs.internal.gwt.GWTIncluder
import org.jahia.taglibs.AbstractJahiaTag

/**
 * @author rincevent* @since JAHIA 6.5
 */
if (renderContext.mainResource.contextConfiguration == 'page' && targetTag == 'HEAD') {
    if (renderContext.editMode) {
        if (!renderContext.getServletPath().endsWith("frame")) {
            println GWTInitializer.generateInitializerStructure(renderContext.request, renderContext.request.session)
            println GWTIncluder.generateGWTImport(renderContext.request, renderContext.response, "edit")
            println AbstractJahiaTag.getGwtDictionnaryInclude(renderContext.request, AbstractJahiaTag.getUILocale(renderContext, renderContext.request.session, renderContext.request))
        } else {
            println GWTInitializer.generateInitializerStructureForFrame(renderContext)
        }
    }

    def slangmap = [en: 'en_US', da: 'da_DK', nl: 'nl_NL', fi: 'fi_FI', fr: 'fr_FR', de: 'de_DE', el: 'el_GR', it: 'it_IT', nb: 'nb_NO', pt: 'pt_PT', es: 'es_ES', sv: 'sv_SE']
    print "<script type=\"application/json\" id=\"jahia-data-ctx\">"
    if (contextJsParameters == null) {
        contextJsParameters = "{\"contextPath\":\"${contextPath}\",\"lang\":\"${renderContext.mainResourceLocale}\",\"uilang\":\"${renderContext.UILocale}\",\"siteUuid\":\"${renderContext.site.identifier}\",\"wcag\":${renderContext.siteInfo.WCAGComplianceCheckEnabled}}"
    }
    print contextJsParameters
    println "</script>"

    print "<script type=\"application/json\" id=\"jahia-data-ck\">"
    if (slangmap[renderContext.mainResource.locale.language] != null) {
        print "{\"path\":\"${contextPath}/modules/ckeditor/javascript/\",\"lng\":\"${slangmap[renderContext.mainResource.locale.language]}\"}";
    } else {
        print "{\"path\":\"${contextPath}/modules/ckeditor/javascript/\"}";
    }
    println "</script>"
}
println "<script type=\"text/javascript\" src=\"${contextPath}/javascript/initJahiaContext.js\"></script>";

renderContext.request.getAttribute("staticAssets").each { resource ->
    resource.each { type ->
        switch (type.key) {
            case "css":
                type.value.eachWithIndex { css, i ->
                    condition = css.value.get("condition")
                    if (condition != null) println("<!--[" + condition + "]>")
                    def rel = css.value.get("rel")
                    def media = css.value.get("media")
                    def url = renderContext.response.encodeURL(css.key)
                    println "<link id=\"staticAssetCSS${targetTag == 'HEAD' ? '' : targetTag}${i}\" rel=\"${rel != null ? rel : "stylesheet"}\" href=\"${url}\" media=\"${media != null ? media : "screen"}\" type=\"text/css\"/>"
                    if (condition != null) println("<![endif]-->")
                }
                break
            case "javascript":
                type.value.eachWithIndex { javascript, i ->
                    def condition = javascript.value != null ? javascript.value.get("condition") : null
                    def async = javascript.value != null ? javascript.value.get("async") : null
                    def defer = javascript.value != null ? javascript.value.get("defer") : null
                    if (condition != null) println("<!--[" + condition + "]>")
                    def url = renderContext.response.encodeURL(javascript.key)
                    println "<script id=\"staticAssetJavascript${targetTag == 'HEAD' ? '' : targetTag}${i}\" type=\"text/javascript\" src=\"${url}\" ${async != null ? "async" : ""} ${defer != null ? "defer" : ""}></script>"
                    if (condition != null) println("<![endif]-->")
                }
                break
            case "aggregatedjavascript":
                if (type.getValue().size() > 0) {
                    println "<script type=\"application/json\" id=\"jahia-data-aggregatedjs${targetTag == 'HEAD' ? '' : targetTag}\">{\"scripts\":[" + type.value.collect { javascript -> '"' + javascript.key + '"' }.join(',') + "]}</script>"
                }

                break
            case "html":
                type.value.eachWithIndex { html, i ->
                    def condition = html.value.get("condition")
                    if (condition != null) println("<!--[" + condition + "]>")
                    def rel = html.value.get("rel")
                    def url = renderContext.response.encodeURL(html.key)
                    println "<link id=\"staticAssetHTML${targetTag == 'HEAD' ? '' : targetTag}${i}\" rel=\"${rel != null ? rel : "import"}\" href=\"${url}\" />";
                    if (condition != null) println("<![endif]-->")
                }
                break
            default:
                type.value.each { inline ->
                    def condition = inline.value != null ? inline.value.get("condition") : null
                    if (condition != null) println("<!--[" + condition + "]>")
                    println "${inline.key}"
                    if (condition != null) println("<![endif]-->")
                }
        }
    }
}
