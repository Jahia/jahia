/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes.initializers;

import org.slf4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.springframework.core.io.Resource;

import javax.jcr.RepositoryException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 18 nov. 2009
 */
public class ModuleImageChoiceListInitializerImpl implements ChoiceListInitializer {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(ModuleImageChoiceListInitializerImpl.class);

    /**
     * This Initializer allows to add an dimage to a dropdown list. It tries to find an image having the name of
     * the options inside the img folder of the module.
     * <p/>
     * - column (string,choicelist[resourceBundle,moduleImage='png']) = '1col16' < '1col16','2col412','2col124','2col106'
     * <p/>
     * This will search for images named img/1col16.png inside the module containing the definition.
     *
     * @param epd
     * @param param
     * @param values
     * @param locale
     * @param context  @return
     */
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        if (values != null && values.size() > 0) {
            final JahiaTemplatesPackage template = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(
                    epd.getDeclaringNodeType().getSystemId());
            for (ChoiceListValue value : values) {
                try {
                    final Resource imagePath = template.getResource("/img/" + value.getValue().getString() + "." + param);
                    if (imagePath != null && imagePath.exists()) {
                        String s = Jahia.getContextPath();
                        if (s.equals("/")) {
                            s = "";
                        }
                        value.addProperty("image", s + (template.getRootFolderPath().startsWith("/")?"":"/")+template.getRootFolderPath() + "/img/" + value.getValue().getString() + "." + param);
                    } else {
                        logger.debug("ModuleImageChoiceListInitializerImpl : unable to find image /img/" + value.getValue().getString() + "."  + param
                                + " in module " + template.getName()
                                + " for property " + epd.getName()
                                + " for type " + epd.getDeclaringNodeType().getName() );
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            return values;
        }
        return new ArrayList<ChoiceListValue>();
    }
}
