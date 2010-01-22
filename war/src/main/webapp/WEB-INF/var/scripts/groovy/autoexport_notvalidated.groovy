import org.jahia.engines.EngineMessage
import org.jahia.content.NodeOperationResult
import java.text.MessageFormat

textBody = ""
htmlBody = ""
subject = "[JAHIA] Auto export has encountered an error"
htmlBody += """\
<h2>Auto export has encountered an error</h2>
"""
textBody += """\
Auto export has encountered an error
"""


if(results.getWarnings().size()>0){
    htmlBody += "<br/><h3>Warnings</h3>"
    textBody += "Warnings : \n";
}

for (NodeOperationResult thenode : (List<NodeOperationResult>)results.getWarnings()) {
    EngineMessage msg = thenode.getMsg();
    String keyValue = msg != null ? getRessource(msg.getKey(), locale) : null;
    if (keyValue != null) {
        MessageFormat msgFormat = new MessageFormat(keyValue);
        msgFormat.setLocale(locale);
        htmlBody += msgFormat.format(msg.getValues())+ "<br/>";
        textBody += msgFormat.format(msg.getValues())+ "\n";
    } else if (thenode.getComment() != null) {
        htmlBody += thenode.getComment() + "<br/>";
        textBody += thenode.getComment() + "\n";
    }
}

if(results.getErrors().size()>0){
    htmlBody += "<br/><h3>Errors</h3>"
    textBody += "Errors : \n";
}
// error messages
for (NodeOperationResult thenode : (List<NodeOperationResult>)results.getErrors()) {
    EngineMessage msg = thenode.getMsg();
    String keyValue = msg != null ? getRessource(msg.getKey(), locale) : null;
    if (keyValue != null) {
        MessageFormat msgFormat = new MessageFormat(keyValue);
        msgFormat.setLocale(locale);
        htmlBody += msgFormat.format(msg.getValues())+ "<br/>";
        textBody += msgFormat.format(msg.getValues())+ "\n";
    } else if (thenode.getComment() != null) {
        htmlBody += thenode.getComment() + "<br/>";
        textBody += thenode.getComment() + "\n";
    }
}


private String getRessource(String label, Locale l) {
    try {
        return ResourceBundle.getBundle("JahiaInternalResources", l).getString(label);
    } catch (Exception e) {
        return "";
    }
}
