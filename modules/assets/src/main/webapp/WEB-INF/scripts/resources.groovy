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

if (renderContext.mainResource.contextConfiguration == 'page') {
  println "<script type=\"text/javascript\">"
  println "\tvar jahiaJsParameters={contextPath:\"${url.context}\",uilang:\"${renderContext.mainResourceLocale}\",siteUuid:\"${renderContext.site.identifier}\",wcag:${renderContext.site.WCAGComplianceCheckEnabled}}";
  println "</script>";
}


renderContext.staticAssets.each { resource ->
  resource.each { type ->
    switch ( type.key ) {
      case "css" :
        type.value.eachWithIndex { css, i -> println "<link id=\"staticAssetCSS${i}\" rel=\"stylesheet\" href=\"${css.value}\" media=\"screen\" type=\"text/css\"/>";  }
        break;
      case "opensearch" :
        type.value.each { opensearch -> println "<link rel=\"search\" type=\"application/opensearchdescription+xml\" href=\"${opensearch.value}\" title=\"" + (renderContext.staticAssetOptions[opensearch.value].title != null ? renderContext.staticAssetOptions[opensearch.value].title : 'Jahia search') + "\" />"; }
        break;
      case "javascript" :
        type.value.eachWithIndex { javascript, i -> println "<script id=\"staticAssetJavascript${i}\" type=\"text/javascript\" src=\"${javascript.value}\"></script>"; }
        break;
      default:
       type.value.each { inline ->  println "${inline}"; }
    }
  }
}