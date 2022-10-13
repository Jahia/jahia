package org.jahia.taglibs.contentsecurity;

import org.jahia.services.render.RenderContext;
import org.jahia.settings.SettingsBean;

/**
 * Generates nonce placeholder for inline scripts
 */
public class ContentSecurityPolicyNonce {

    public static String noncePlaceholder(RenderContext renderContext) {
        if (renderContext.getSite().getInstalledModules().contains("content-security-policy")) {
            String placeHolderName= SettingsBean.getInstance().getPropertiesFile().getProperty("contentSecurityPolicy.nonce.placeHolder", "XXXXX");
            return String.format("nonce=\"%s\"", placeHolderName);
        }

        return "";
    }
}
