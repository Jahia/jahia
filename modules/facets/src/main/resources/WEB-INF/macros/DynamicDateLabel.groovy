import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import org.jahia.services.search.facets.JahiaQueryParser;

String dateString = param1;
Date date = JahiaQueryParser.DATE_TYPE.parseMath(null, param1); 
if (date != null) {
   MessageFormat mf = new MessageFormat("{0" + (binding.variables.containsKey("param2") ? "," + param2 : "") + (binding.variables.containsKey("param3") ? "," + param3 : "") + "}", renderContext.getMainResourceLocale());
   TimeZone tz = TimeZone.getTimeZone("UTC");
   Object [] formats = mf.getFormats();
   for (int i = 0; i < formats.length; i++) {
       if (formats[i] instanceof SimpleDateFormat) {
           ((SimpleDateFormat)formats[i]).setTimeZone(tz);
       }
   }
   dateString = mf.format([date] as Object[]);
}
print dateString;