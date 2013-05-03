/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.bin.filters;

import com.phloc.commons.io.IInputStreamProvider;
import com.phloc.css.ECSSVersion;
import com.phloc.css.decl.*;
import com.phloc.css.handler.CSSHandler;
import com.phloc.css.writer.CSSWriter;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */
public class CSSChannelFilter implements Filter {
    private static Logger logger = LoggerFactory.getLogger(CSSChannelFilter.class);
    private ServletContext servletContext;

    private enum Modifier {EQUALS, MIN, MAX}

    public void init(FilterConfig filterConfig) throws ServletException {
        servletContext = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String channelId = request.getParameter("channel");
        if (channelId != null) {
            String channelVariant = request.getParameter("variant");
            ChannelService service = (ChannelService) SpringContextSingleton.getInstance().getContext().getBean(
                    "ChannelService");
            Channel channel = service.getChannel(channelId);

            String uri = ((HttpServletRequest) request).getRequestURI();
            final InputStream stream = servletContext.getResourceAsStream(uri.replace(Jahia.getContextPath(), ""));

            CascadingStyleSheet css = CSSHandler.readFromStream(new IInputStreamProvider() {
                public InputStream getInputStream() {
                    return stream;
                }
            }, Charset.forName("UTF-8"), ECSSVersion.CSS30);
            if (css != null) {
                List<CSSMediaRule> filteredOutRules = new ArrayList<CSSMediaRule>();
                for (CSSMediaRule mediaRule : css.getAllMediaRules()) {
                    if (!evalMediaRule(channel, mediaRule.getAllMediaQueries(), channelVariant)) {
                        filteredOutRules.add(mediaRule);
                    } else {
                        for (CSSMediaQuery mediaQuery : mediaRule.getAllMediaQueries()) {
                            mediaQuery.getMediaExpressions().clear();
                        }
                    }
                }

                List<CSSImportRule> filteredOutImports = new ArrayList<CSSImportRule>();
                List<CSSImportRule> imports = css.getAllImportRules();
                for (CSSImportRule anImport : imports) {
                    if (!evalMediaRule(channel, anImport.getAllMediaQueries(), channelVariant)) {
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

                    CascadingStyleSheet sheet = new CascadingStyleSheet();
                    final List<CSSMediaRule> allMediaRules = css.getAllMediaRules();
                    for (CSSMediaRule rule : allMediaRules) {
                        final List<ICSSTopLevelRule> allRules = rule.getAllRules();
                        for (ICSSTopLevelRule icssTopLevelRule : allRules) {
                            sheet.addRule(icssTopLevelRule);
                        }

                    }
                    final List<CSSImportRule> allImportRules = css.getAllImportRules();
                    for (CSSImportRule importRule : allImportRules) {
                        sheet.addImportRule(importRule);
                    }
                    CSSWriter w = new CSSWriter(ECSSVersion.CSS30);
                    w.writeCSS(sheet, response.getWriter());
                    return;
                }


            } else {
                logger.warn("Cannot parse CSS " + uri);
            }
        }

        chain.doFilter(request, response);
    }

    private boolean evalMediaRule(Channel channel, List<CSSMediaQuery> mediaQueries, String channelVariant) {
        for (CSSMediaQuery mediaQuery : mediaQueries) {
            for (CSSMediaExpression mediaExpr : mediaQuery.getMediaExpressions()) {
                if (!evalFeature(mediaExpr, channel, channelVariant)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean evalFeature(CSSMediaExpression mediaExpr, Channel channel, String channelVariant) {
        Modifier modifier = Modifier.EQUALS;
        String feature = mediaExpr.getFeature().toLowerCase();
        if (feature.startsWith("max-")) {
            modifier = Modifier.MAX;
            feature = StringUtils.substringAfter(feature, "max-");
        } else if (feature.startsWith("min-")) {
            modifier = Modifier.MIN;
            feature = StringUtils.substringAfter(feature, "min-");
        }
        int channelIndex = 0;
        if (channelVariant != null) {
            final List<String> variants = Arrays.asList(channel.getCapability("variants").split(","));
            if(variants.contains(channelVariant))
            channelIndex = variants.indexOf(channelVariant);
        }
        String capability = channel.getCapability("usable-resolutions");
        capability = capability.split(",")[channelIndex];
        if (feature.equals("width")) {
            List<CSSExpressionMemberTermSimple> allSimpleMembers = mediaExpr.getValue().getAllSimpleMembers();
            if (allSimpleMembers != null && !allSimpleMembers.isEmpty()) {
                return evalLength(modifier, capability, allSimpleMembers.get(
                        0).getValue(), 0, "x");
            }
        } else if (feature.equals("height")) {
            List<CSSExpressionMemberTermSimple> allSimpleMembers = mediaExpr.getValue().getAllSimpleMembers();
            if (allSimpleMembers != null && !allSimpleMembers.isEmpty()) {
                return evalLength(modifier, capability, allSimpleMembers.get(
                        0).getValue(), 1, "x");
            }
        } else if (feature.equals("device-width")) {
            List<CSSExpressionMemberTermSimple> allSimpleMembers = mediaExpr.getValue().getAllSimpleMembers();
            if (allSimpleMembers != null && !allSimpleMembers.isEmpty()) {
                return evalLength(modifier, capability, allSimpleMembers.get(
                        0).getValue(), 0, "x");
            }
        } else if (feature.equals("device-height")) {
            List<CSSExpressionMemberTermSimple> allSimpleMembers = mediaExpr.getValue().getAllSimpleMembers();
            if (allSimpleMembers != null && !allSimpleMembers.isEmpty()) {
                return evalLength(modifier, capability, allSimpleMembers.get(
                        0).getValue(), 1, "x");
            }
        } else if (feature.equals("orientation")) {
            List<CSSExpressionMemberTermSimple> allSimpleMembers = mediaExpr.getValue().getAllSimpleMembers();
            if (allSimpleMembers != null && !allSimpleMembers.isEmpty()) {
                final String value = allSimpleMembers.get(0).getValue();
                return (channelVariant == null && channel.getCapability("variants").split(",")[0].equals(value)) ||
                       value.equals(channelVariant);
            }
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

    public boolean evalLength(Modifier modifier, String realValue, String value,
                              int indexOfRealValueInChannel, String channelValueSeparator) {
        if (realValue == null) {
            return true;
        }
        int rv;
        if (channelValueSeparator != null) {
            rv = Integer.parseInt(realValue.split(channelValueSeparator)[indexOfRealValueInChannel]);
        } else {
            rv = Integer.parseInt(realValue);
        }
        if (value.endsWith("px")) {
            value = StringUtils.substringBeforeLast(value, "px");
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
