/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 * <p/>
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 * <p/>
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 * <p/>
 *     1/ GPL
 *     ======================================================================================
 * <p/>
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p/>
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 * <p/>
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 * <p/>
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * <p/>
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
package org.jahia.services.content.nodetypes.initializers;

import org.jahia.bin.Jahia;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.renderer.ChoiceListRenderer;
import org.jahia.services.render.RenderContext;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.io.File;
import java.util.*;

/**
 * An implementation for choice list initializer and renderer that displays a list of available flags for countries.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 * Created : 18 nov. 2009
 */
public class CountryFlagChoiceListInitializerAndRendererImpl implements ChoiceListInitializer, ChoiceListRenderer {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(CountryFlagChoiceListInitializerAndRendererImpl.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        if (values != null) {
            for (ChoiceListValue value : values) {
                try {
                    String flagPath = "/css/images/flags/shadow/flag_" + Patterns.SPACE.matcher(new Locale("en",
                            value.getValue().getString()).getDisplayCountry(
                            Locale.ENGLISH).toLowerCase()).replaceAll("_") + ".png";
                    flagPath = checkFlagPath(flagPath);
                    value.addProperty("image", Jahia.getContextPath() + flagPath);
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            return values;
        }
        return new ArrayList<ChoiceListValue>();
    }

    public Map<String, Object> getObjectRendering(RenderContext context,
                                                  JCRPropertyWrapper propertyWrapper) throws RepositoryException {
        return getObjectRendering(context, null, propertyWrapper.getValue().getString());
    }

    public Map<String, Object> getObjectRendering(RenderContext context,
                                                  ExtendedPropertyDefinition propDef, Object propertyValue) throws RepositoryException {
        Map<String, Object> map = new HashMap<String, Object>(2);
        final String displayName = new Locale("en", propertyValue.toString())
                .getDisplayCountry(context.getMainResource().getLocale());
        final String enDisplayName = new Locale("en", propertyValue.toString())
                .getDisplayCountry(Locale.ENGLISH);
        String flagPath = "/css/images/flags/shadow/flag_"
                + Patterns.SPACE.matcher(enDisplayName.toLowerCase()).replaceAll("_") + ".png";
        flagPath = checkFlagPath(flagPath);
        map.put("displayName", displayName);
        map.put("flag", context.getRequest().getContextPath() + flagPath);
        return map;
    }

    public String getStringRendering(RenderContext context, JCRPropertyWrapper propertyWrapper)
            throws RepositoryException {
        String value;
        if (propertyWrapper.isMultiple()) {
            value = propertyWrapper.getValues()[0].getString();
        } else {
            value = propertyWrapper.getValue().getString();
        }
        return getStringRendering(context, null, value);
    }

    public String getStringRendering(RenderContext context, ExtendedPropertyDefinition propDef,
                                     Object propertyValue) throws RepositoryException {
        String value = propertyValue.toString();
        final String displayName = new Locale("en", value).getDisplayCountry(context
                .getMainResource().getLocale());
        final String enDisplayName = new Locale("en", value).getDisplayCountry(Locale.ENGLISH);
        String flagPath = "/css/images/flags/shadow/flag_"
                + Patterns.SPACE.matcher(enDisplayName.toLowerCase()).replaceAll("_") + ".png";
        flagPath = checkFlagPath(flagPath);
        return "<img src=\"" + context.getRequest().getContextPath() + flagPath + "\">&nbsp;<span>"
                + displayName + "</span>";
    }

    public Map<String, Object> getObjectRendering(Locale locale,
                                                  ExtendedPropertyDefinition propDef, Object propertyValue)
            throws RepositoryException {
        throw new UnsupportedOperationException("This renderer does not work without RenderContext");
    }

    public String getStringRendering(Locale locale,
                                     ExtendedPropertyDefinition propDef, Object propertyValue)
            throws RepositoryException {
        throw new UnsupportedOperationException("This renderer does not work without RenderContext");
    }

    private String checkFlagPath(String flagPath) {
        String realFlagPath = JahiaContextLoaderListener.getServletContext().getRealPath(flagPath);
        boolean hasFlag = realFlagPath != null;
        if (hasFlag) {
            try {
                File f = new File(realFlagPath);
                hasFlag = f.exists();
            } catch (Exception e) {
                hasFlag = false;
            }
        }
        if (!hasFlag) {
            flagPath = "/css/blank.gif";
        }
        return flagPath;
    }

}
