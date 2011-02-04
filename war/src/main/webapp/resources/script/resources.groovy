import org.jahia.ajax.gwt.utils.GWTInitializer
import org.jahia.taglibs.internal.gwt.GWTIncluder
import org.jahia.taglibs.AbstractJahiaTag

/**
 * @author rincevent
 * @since JAHIA 6.5
 */
if (renderContext.editMode && renderContext.mainResource.contextConfiguration == 'page') {
    println GWTInitializer.generateInitializerStructure(renderContext.request,renderContext.request.session);
    println GWTIncluder.generateGWTImport(renderContext.request,renderContext.response, "org.jahia.ajax.gwt.module.edit.Edit");
    println AbstractJahiaTag.getGwtDictionnaryInclude(renderContext.request,AbstractJahiaTag.getUILocale(renderContext,renderContext.request.session,renderContext.request));
}

if (renderContext.contributionMode && renderContext.mainResource.contextConfiguration == 'page') {
    println "<script type=\"text/javascript\">"
    println "\tvar jahiaGWTParameters={contextPath:\"${url.context}\",uilang:\"${renderContext.mainResourceLocale}\",siteUuid:\"${renderContext.site.identifier}\",wcag:${renderContext.site.WCAGComplianceCheckEnabled}}";
    println "</script>";
}

renderContext.staticAssets.css.eachWithIndex { resource, i ->
    println "<link id=\"staticAssetCSS${i}\" rel=\"stylesheet\" href=\"${resource}\" media=\"screen\" type=\"text/css\"/>";
}

renderContext.staticAssets.opensearch.eachWithIndex { resource, i ->
    println "<link rel=\"search\" type=\"application/opensearchdescription+xml\" href=\"${resource}\" title=\"" + (renderContext.staticAssetOptions[resource].title != null ? renderContext.staticAssetOptions[resource].title : 'Jahia search') + "\" />";
}

renderContext.staticAssets.javascript.eachWithIndex { resource, i ->
    println "<script id=\"staticAssetJavascript${i}\" type=\"text/javascript\" src=\"${resource}\"></script>";
}

renderContext.staticAssets.inline.eachWithIndex { resource, i ->
    println "${resource}";
}