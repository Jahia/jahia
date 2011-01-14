Map<String,String> formValues = renderContext.getRequest().getAttribute("form-parameter").get(param1);
String id = java.util.UUID.randomUUID().toString();
Map<String,Map<String,String>> toks = (Map<String, Map<String, String>>) renderContext.getRequest().getSession().getAttribute("form-tokens");
if (toks == null) {
    toks = new HashMap<String,Map<String,String>>();
    renderContext.getRequest().getSession().setAttribute("form-tokens", toks);
}
toks.put(id, formValues);
print id;
