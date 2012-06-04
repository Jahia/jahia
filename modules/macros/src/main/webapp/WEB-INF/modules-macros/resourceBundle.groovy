import java.util.Locale
import org.jahia.utils.i18n.JahiaResourceBundle
try {
    Locale loc = renderContext.getRequest().getAttribute(org.jahia.services.render.filter.ForceUILocaleFilter.RENDERING_FORCED_LOCALE)
    print JahiaResourceBundle.getString(binding.variables.containsKey("param2") ? param2 : null, param1, loc != null ? loc : renderContext.getMainResourceLocale(), renderContext.getSite().getTemplatePackageName())
} catch (java.util.MissingResourceException e) {
    print param1
}