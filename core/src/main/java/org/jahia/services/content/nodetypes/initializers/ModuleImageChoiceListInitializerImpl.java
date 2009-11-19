/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes.initializers;

import org.apache.log4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.RepositoryException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 18 nov. 2009
 */
public class ModuleImageChoiceListInitializerImpl implements ChoiceListInitializer {
    private transient static Logger logger = Logger.getLogger(ModuleImageChoiceListInitializerImpl.class);

    /**
     * This Initializer allows to add an dimage to a dropdown list. It tries to find an image having the name of
     * the options inside the img folder of the module.
     * <p/>
     * - column (string,choicelist[resourceBundle,moduleImage='png']) = '1col16' < '1col16','2col412','2col124','2col106'
     * <p/>
     * This will search for images named img/1col16.png inside the module containing the definition.
     *
     * @param context
     * @param epd
     * @param param
     * @param realNodeType
     * @param values
     * @return
     */
    public List<ChoiceListValue> getChoiceListValues(ProcessingContext context, ExtendedPropertyDefinition epd,
                                                     String param, String realNodeType, List<ChoiceListValue> values) {
        if (values != null && values.size() > 0) {
            final JahiaTemplatesPackage template = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(
                    epd.getDeclaringNodeType().getSystemId());
            for (ChoiceListValue value : values) {
                try {
                    final File imagePath = new File(
                            template.getFilePath() + File.separator + "img" + File.separator + value.getValue().getString() + "." + param);
                    if (imagePath.exists()) {
                        value.addProperty("image",
                                          context.getContextPath() + "/" + template.getRootFolderPath() + "/img/" + value.getValue().getString() + "." + param);
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
