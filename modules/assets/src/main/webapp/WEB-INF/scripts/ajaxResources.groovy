/**
 * @author rincevent
 * @since JAHIA 6.5
 */
renderContext.request.getAttribute("staticAssets").css.eachWithIndex { resource, i ->
    condition = resource.value.get("condition");
    if (condition != null) println("<!--["+condition+"]>");
    media = resource.value.get("media");
    println "<link id=\"staticAssetCSS${i}\" rel=\"stylesheet\" href=\"${resource.key}\" media=\"${media!=null?media:"screen"}\" type=\"text/css\"/>";
    if (condition != null) println("<![endif]-->");
}
if (renderContext.request.getParameter("includeJavascripts")) {
    renderContext.request.getAttribute("staticAssets").javascript.eachWithIndex { resource, i ->
        condition = resource.value != null ? resource.value.get("condition") : null;
        if (condition != null) println("<!--["+condition+"]>");
        println "<script id=\"staticAssetJavascript${i}\" type=\"text/javascript\" src=\"${resource.key}\"></script>";
        if (condition != null) println("<![endif]-->");
    }
}

renderContext.request.getAttribute("staticAssets").inline.eachWithIndex { resource, i ->
    condition = resource.value != null ? resource.value.get("condition") : null;
    if (condition != null) println("<!--["+condition+"]>");
    println "${resource.key}";
    if (condition != null) println("<![endif]-->");
}
