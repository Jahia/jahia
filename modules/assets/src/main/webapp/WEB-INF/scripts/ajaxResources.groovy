/**
 * @author rincevent
 * @since JAHIA 6.5
 */
renderContext.staticAssets.css.eachWithIndex { resource, i ->
    println "<link id=\"staticAssetCSS${i}\" rel=\"stylesheet\" href=\"${resource}\" media=\"screen\" type=\"text/css\"/>";
}
renderContext.staticAssets.inline.eachWithIndex { resource, i ->
    println "${resource}";
}