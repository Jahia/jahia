id = renderContext.getRequest().getAttribute("form-" + param1)
if (id == null) {
    Map<String, List<String>> formValues = renderContext.getRequest().getAttribute("form-parameter").get(param1);
    if (formValues != null) {
        id = java.util.UUID.randomUUID().toString();
        Map<String, Map<String, List<String>>> toks = (Map<String, Map<String, List<String>>>) renderContext.getRequest().getSession().getAttribute("form-tokens");
        if (toks == null) {
            toks = new HashMap<String, Map<String, List<String>>>();
            renderContext.getRequest().getSession().setAttribute("form-tokens", toks);
        }
        toks.put(id, formValues);
        renderContext.getRequest().setAttribute("form-" + param1, id)
    }
}
if(id!=null)
print id;
else
print "error in token"
