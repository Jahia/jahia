String dateString = param1;
java.util.Date date = org.jahia.services.search.facets.JahiaQueryParser.DATE_TYPE.parseMath(null, param1); 
if (date != null) {
   dateString = new java.text.MessageFormat("{0" + (binding.variables.containsKey("param2") ? "," + param2 : "") + (binding.variables.containsKey("param3") ? "," + param3 : "") + "}", renderContext.getMainResourceLocale()).format([date] as Object[]);
}
print dateString;