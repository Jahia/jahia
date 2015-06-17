/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.bin.filters;

import com.phloc.commons.io.IInputStreamProvider;
import com.phloc.css.ECSSVersion;
import com.phloc.css.decl.*;
import com.phloc.css.reader.CSSReader;
import com.phloc.css.writer.CSSWriter;
<<<<<<< .working

import org.apache.commons.io.Charsets;
=======

>>>>>>> .merge-right.r52715
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            InputStream readStream = servletContext.getResourceAsStream(uri.replace(Jahia.getContextPath(), ""));
            if(readStream == null) {
            	//Check if it is a JCR resource (because it cannot read as resource from servletContext)
            	try {
            		if(uri.contains("/sites/")) {
            			JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(uri.substring(uri.indexOf("/sites/")));
            			if(node.hasNode("jcr:content")) {
            				readStream = node.getNode("jcr:content").getProperty("jcr:data").getBinary().getStream();
            			}
            		}
            	} catch(RepositoryException ex) {
            		logger.warn("cannot parse css file in JCR: " + uri, ex);
            	}
            }
            final InputStream stream = readStream;

            CascadingStyleSheet css = CSSReader.readFromStream(new IInputStreamProvider() {
                public InputStream getInputStream() {
                    return stream;
                }
            }, Charsets.UTF_8, ECSSVersion.CSS30);
            if (css != null) {
                List<CSSMediaRule> filteredOutRules = new ArrayList<CSSMediaRule>();
                for (CSSMediaRule mediaRule : css.getAllMediaRules()) {
                    if (!evalMediaRule(channel, mediaRule.getAllMediaQueries(), channelVariant)) {
                        filteredOutRules.add(mediaRule);
                    } else {
                        for (CSSMediaQuery mediaQuery : mediaRule.getAllMediaQueries()) {
                            mediaQuery.getAllMediaExpressions().clear();
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

                    final List<ICSSTopLevelRule> allRules = css.getAllRules();
                    for (ICSSTopLevelRule rule : allRules) {
                        if (rule instanceof CSSMediaRule) {
                            final List<ICSSTopLevelRule> allRulesInMediaRule = ((CSSMediaRule)rule).getAllRules();
                            for (ICSSTopLevelRule icssTopLevelRule : allRulesInMediaRule) {
                                sheet.addRule(icssTopLevelRule);
                            }
                        } else {
                            sheet.addRule(rule);
                        }
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
            for (CSSMediaExpression mediaExpr : mediaQuery.getAllMediaExpressions()) {
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
