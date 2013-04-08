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
package org.jahia.modules.serversettings.flow;

import org.apache.commons.io.FilenameUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.modules.serversettings.moduleManagement.ModuleFile;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.TemplatePackageDeployer;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.DefaultMessageResolver;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 08/04/13
 */
public class ModuleManagementFlowHandler implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(ModuleManagementFlowHandler.class);

    @Autowired
    private transient JahiaTemplateManagerService templateManagerService;

    @Autowired
    private transient JCRTemplate jcrTemplate;


    public ModuleFile initModuleFile() {
        return new ModuleFile();
    }

    public boolean uploadModule(ModuleFile moduleFile, MessageContext context) {
        String originalFilename = moduleFile.getModuleFile().getOriginalFilename();
        if (!FilenameUtils.isExtension(originalFilename, Arrays.<String>asList("war","jar","WAR","JAR"))) {
            context.addMessage(new MessageBuilder().error().source("moduleFile").defaultText("File should be a jar or a war file").build());
            return false;
        }
        try {
            final File file = new File(SettingsBean.getInstance().getJahiaModulesDiskPath(),originalFilename);
            moduleFile.getModuleFile().transferTo(file);

            context.addMessage(new MessageBuilder().source("moduleFile").defaultText("Module has been successfully uploaded. Check status in the list").build());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return true;
    }
}
