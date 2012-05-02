import javax.servlet.jsp.jstl.core.Config
import javax.servlet.jsp.jstl.fmt.LocalizationContext
import org.jahia.utils.i18n.JahiaResourceBundle

LocalizationContext locCtxt = renderContext.getRequest().getAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".request")
// ResourceBundle bundle = null
// if (locCtxt != null) 
//    bundle = locCtxt.getResourceBundle()
// if (bundle != null) 
//    print bundle.getString(param1)
print JahiaResourceBundle.getString(binding.variables.containsKey("param2") ? param2 : null, param1, locCtxt != null ? locCtxt.getLocale() : renderContext.getMainResourceLocale(), renderContext.getSite().getTemplatePackageName())
