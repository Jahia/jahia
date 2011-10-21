/**
 * @author rincevent
 * @since JAHIA 6.5
 */
renderContext.request.getAttribute("staticAssets").css.eachWithIndex { resource, i ->
    println "<link id=\"staticAssetCSS${i}\" rel=\"stylesheet\" href=\"${resource.key}\" media=\"screen\" type=\"text/css\"/>";
}
renderContext.request.getAttribute("staticAssets").inline.eachWithIndex { resource, i ->
    println "${resource.key}";
}