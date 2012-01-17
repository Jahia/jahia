/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.content.nodetypes.initializers;

import org.slf4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.renderer.ChoiceListRenderer;
import org.jahia.services.render.RenderContext;

import javax.jcr.RepositoryException;
import java.io.File;
import java.util.*;

/**
 * An implementation for choice list initializer and renderer that displays a list of available flags for countries.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 18 nov. 2009
 */
public class CountryFlagChoiceListInitializerAndRendererImpl implements ChoiceListInitializer, ChoiceListRenderer {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(CountryFlagChoiceListInitializerAndRendererImpl.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        if (values != null) {
            for (ChoiceListValue value : values) {
                try {
                    String flagPath = "/css/images/flags/shadow/flag_" + new Locale("en",
                                                                                    value.getValue().getString()).getDisplayCountry(
                            Locale.ENGLISH).toLowerCase().replaceAll(" ", "_") + ".png";
                    File f = new File(JahiaContextLoaderListener.getServletContext().getRealPath(flagPath));
                    if (!f.exists()) {
                        flagPath = "/css/blank.gif";
                    }
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
                + enDisplayName.toLowerCase().replaceAll(" ", "_") + ".png";
        File f = new File(JahiaContextLoaderListener.getServletContext().getRealPath(flagPath));
        if (!f.exists()) {
            flagPath = "/css/blank.gif";
        }
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
                + enDisplayName.toLowerCase().replaceAll(" ", "_") + ".png";
        File f = new File(JahiaContextLoaderListener.getServletContext().getRealPath(flagPath));
        if (!f.exists()) {
            flagPath = "/css/blank.gif";
        }
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
}
