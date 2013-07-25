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
    def slangmap = [en:'en_US', da:'da_DK', nl:'nl_NL', fi:'fi_FI', fr:'fr_FR', de:'de_DE', el:'el_GR', it:'it_IT', nb:'nb_NO', pt:'pt_PT', es:'es_ES', sv:'sv_SE']
    println "<script type=\"text/javascript\">"
    print "var contextJsParameters={contextPath:\"${contextPath}\",lang:\"${renderContext.mainResourceLocale}\",uilang:\"${renderContext.UILocale}\",siteUuid:\"${renderContext.site.identifier}\",wcag:${renderContext.site.WCAGComplianceCheckEnabled}}; ";
    print "var CKEDITOR_BASEPATH=\"${contextPath}/modules/assets/javascript/ckeditor/\"; "
    print "var scayt_custom_params=new Array(); "
    if (slangmap[renderContext.mainResource.locale.language] != null) {
        print "scayt_custom_params['sLang']='"+slangmap[renderContext.mainResource.locale.language]+"';"
    }
    println "";
    println "</script>";
}
renderContext.request.getAttribute("staticAssets").each { resource ->
  resource.each { type ->
    switch ( type.key ) {
      case "css" :
        type.value.eachWithIndex { css, i ->
          condition = css.value.get("condition");
          if (condition != null) println("<!--["+condition+"]>");
          media = css.value.get("media");
          println "<link id=\"staticAssetCSS${i}\" rel=\"stylesheet\" href=\"${css.key}\" media=\"${media!=null?media:"screen"}\" type=\"text/css\"/>";
          if (condition != null) println("<![endif]-->");
        }
        break;
      case "javascript" :
        type.value.eachWithIndex { javascript, i ->
          condition = javascript.value != null ? javascript.value.get("condition") : null;
          if (condition != null) println("<!--["+condition+"]>");
          println "<script id=\"staticAssetJavascript${i}\" type=\"text/javascript\" src=\"${javascript.key}\"></script>";
          if (condition != null) println("<![endif]-->");
        }
        break;
      case "aggregatedjavascript" :
      	if (type.getValue().size() > 0) {
      		println "<script id=\"staticAssetAggregatedJavascriptList0\" type=\"text/javascript\">";
      		println "if (typeof jAggregatedStaticAssetsJavascript == 'undefined') { var jAggregatedStaticAssetsJavascript = new Array(); }";
	        type.value.eachWithIndex { javascript, i ->
	          condition = javascript.value != null ? javascript.value.get("condition") : null;
	          if (condition != null) println("<!--["+condition+"]>");
	          println "jAggregatedStaticAssetsJavascript.push('${javascript.key}');";
	          if (condition != null) println("<![endif]-->");
	        }
      		println "</script>";
        }
        break;
      default:
        type.value.each { inline ->  
          condition = inline.value != null ? inline.value.get("condition") : null;
          if (condition != null) println("<!--["+condition+"]>");
          println "${inline.key}";
          if (condition != null) println("<![endif]-->");
       }
    }
  }
}