package org.jahia.bin.filters;

import com.phloc.commons.io.IInputStreamProvider;
import com.phloc.css.ECSSVersion;
import com.phloc.css.decl.*;
import com.phloc.css.handler.CSSHandler;
import com.phloc.css.writer.CSSWriter;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class CSSChannelFilter implements Filter {
    private ServletContext servletContext;

    private enum Modifier { EQUALS , MIN , MAX }

    public void init(FilterConfig filterConfig) throws ServletException {
        servletContext = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String channelId = request.getParameter("channel");
        if (channelId != null) {
            ChannelService service = (ChannelService) SpringContextSingleton.getInstance().getContext().getBean("ChannelService");
            Channel channel = service.getChannel(channelId);

            String uri  = ((HttpServletRequest)request).getRequestURI();
            final InputStream stream = servletContext.getResourceAsStream(uri);

            CascadingStyleSheet css = CSSHandler.readFromStream(new IInputStreamProvider() {
                public InputStream getInputStream() {
                    return stream;
                }
            }, Charset.forName("UTF-8"),ECSSVersion.CSS30);
            if (css != null) {
                List<CSSMediaRule> filteredOutRules = new ArrayList<CSSMediaRule>();
                for (CSSMediaRule mediaRule : css.getAllMediaRules()) {
                    if (!evalMediaRule(channel, mediaRule.getAllMediaQueries())) {
                        filteredOutRules.add(mediaRule);
                    } else {
                        for (CSSMediaQuery mediaQuery : mediaRule.getAllMediaQueries()) {
                            mediaQuery.getExpressions().clear();
                        }
                    }
                }

                List<CSSImportRule> filteredOutImports = new ArrayList<CSSImportRule>();
                List<CSSImportRule> imports = css.getAllImportRules();
                for (CSSImportRule anImport : imports) {
                    if (!evalMediaRule(channel, anImport.getAllMediaQueries())) {
                        filteredOutImports.add(anImport);
                    }
                }

                if (!filteredOutRules.isEmpty() || !filteredOutImports.isEmpty()) {
                    for (CSSMediaRule filteredOutRule : filteredOutRules) {
                        css.removeRule(filteredOutRule);
                    }

                    for (CSSImportRule filteredOutImport : filteredOutImports) {
                        css.removeImportRule(filteredOutImport);
                    }

                    CSSWriter w = new CSSWriter(ECSSVersion.CSS30);
                    w.writeCSS(css, response.getWriter());
                    return;
                }
                
                
            }
        }

        chain.doFilter(request, response);
    }

    private boolean evalMediaRule(Channel channel, List<CSSMediaQuery> mediaQueries) {
        for (CSSMediaQuery mediaQuery : mediaQueries) {
            for (CSSMediaExpr mediaExpr : mediaQuery.getExpressions()) {
                if (!evalFeature(mediaExpr, channel)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean evalFeature(CSSMediaExpr mediaExpr, Channel channel) {
        Modifier modifier = Modifier.EQUALS;
        String feature = mediaExpr.getFeature().toLowerCase();
        if (feature.startsWith("max-")) {
            modifier = Modifier.MAX;
            feature = StringUtils.substringAfter(feature, "max-");
        } else if (feature.startsWith("min-")) {
            modifier = Modifier.MIN;
            feature = StringUtils.substringAfter(feature,"min-");
        }

        if (feature.equals("width")) {
            return evalLength(modifier, channel.getCapability("resolution_width"), mediaExpr.getValue());
        } else if (feature.equals("height")) {
            return evalLength(modifier, channel.getCapability("resolution_height"), mediaExpr.getValue());
        } else if (feature.equals("device-width")) {
            return evalLength(modifier, channel.getCapability("resolution_width"), mediaExpr.getValue());
        } else if (feature.equals("device-height")) {
            return evalLength(modifier, channel.getCapability("resolution_height"), mediaExpr.getValue());
        } else if (feature.equals("orientation")) {

        } else if (feature.equals("aspect-ratio")) {

        } else if (feature.equals("device-aspect-ratio")) {

        } else if (feature.equals("color")) {

        } else if (feature.equals("monochrome")) {

        } else if (feature.equals("resolution")) {

        } else if (feature.equals("scan")) {

        } else if (feature.equals("grid")) {

        }
        return true;
    }

    public boolean evalLength(Modifier modifier, String realValue, String value) {
        if (realValue == null) {
            return true;
        }
        int rv = Integer.parseInt(realValue);
        if (value.endsWith("px")) {
            value = StringUtils.substringBeforeLast(value,"px");
            int v = Integer.parseInt(value);
            switch (modifier) {
                case EQUALS:
                    return v == rv;
                case MIN:
                    return v < rv;
                case MAX:
                    return v > rv;
            }
        }
        return true;
    }

    public void destroy() {

    }
}
