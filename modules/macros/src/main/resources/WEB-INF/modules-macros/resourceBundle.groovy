import org.jahia.utils.i18n.JahiaResourceBundle

if(binding.variables.containsKey("param1")){
    try{
        print JahiaResourceBundle.getString(binding.variables.containsKey("param2") ? param2 : null, param1, renderContext.getMainResourceLocale(), renderContext.getSite().getTemplatePackageName());
    }catch(java.util.MissingResourceException e){
        print param1;
    }
}else{
    print "<p>This macro require one or two parameter like : <br />" +
            "## resourceBundle(parameter) ##  or ## resourceBundle(parameter1, parameter2) ##</p>";
}