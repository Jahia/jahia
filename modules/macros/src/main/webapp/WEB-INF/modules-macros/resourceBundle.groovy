import org.jahia.utils.i18n.JahiaResourceBundle

try {
    print JahiaResourceBundle.getString(binding.variables.containsKey("param2") ? param2 : null, param1, renderContext.getMainResourceLocale(), renderContext.getSite().getTemplatePackageName())
} catch (java.util.MissingResourceException e) {
    print param1
}