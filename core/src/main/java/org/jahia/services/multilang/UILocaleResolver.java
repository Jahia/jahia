package org.jahia.services.multilang;

import org.jahia.params.ProcessingContext;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Locale;

public class UILocaleResolver implements LocaleResolver {
    public Locale resolveLocale(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session != null) {
            Locale uiLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_UI_LOCALE);
            if (uiLocale != null) {
                return uiLocale;
            }
        }
        return request.getLocale();
    }

    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
    }

}
