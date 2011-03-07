/**
 * @author rincevent
 * @since JAHIA 6.5
 */
renderContext.staticAssets.css.eachWithIndex { resource, i ->
    println "<link id=\"staticAssetCSS${i}\" rel=\"stylesheet\" href=\"${resource}\" media=\"screen\" type=\"text/css\"/>";
}

renderContext.staticAssets.opensearch.eachWithIndex { resource, i ->
    println "<link rel=\"search\" type=\"application/opensearchdescription+xml\" href=\"${resource}\" title=\"" + (renderContext.staticAssetOptions[resource].title != null ? renderContext.staticAssetOptions[resource].title : 'Jahia search') + "\" />";
}

renderContext.staticAssets.inline.eachWithIndex { resource, i ->
    println "${resource}";
}