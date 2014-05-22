println("<script type=\"text/javascript\">");
println("JCRRestUtils = new Jahia.JCRRestUtils({");
options.eachWithIndex() {
    obj, i -> println "${obj.key}: ${obj.value}${i+1 < options.size() ? ',' : ''}"
};
println("});")
println("</script>")