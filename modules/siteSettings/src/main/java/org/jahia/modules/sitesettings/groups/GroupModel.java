/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.modules.sitesettings.groups;

import java.io.Serializable;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.Messages;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Represents a model for a new group.
 * 
 * @author Sergiy Shyrkov
 */
public class GroupModel implements Serializable {

    private static final long serialVersionUID = 4835608458510366337L;

    private static void addError(MessageContext context, String errorText) {
        context.addMessage(new MessageBuilder().error().source("groupname").defaultText(errorText).build());
    }

    private static void addErrorI18n(MessageContext context, String errorKey) {
        addError(context, i18n(errorKey));
    }

    private static String i18n(String label) {
        return Messages.get("resources.JahiaSiteSettings", label, LocaleContextHolder.getLocale());
    }

    @SuppressWarnings("deprecation")
    static boolean validateGroupName(String name, int siteId, MessageContext context) {
        boolean valid = false;
        if (StringUtils.isBlank(name)) {
            addErrorI18n(context, "siteSettings.groups.errors.groupname.mandatory");
        } else if (!ServicesRegistry.getInstance().getJahiaGroupManagerService().isGroupNameSyntaxCorrect(name)) {
            addErrorI18n(context, "siteSettings.groups.errors.groupname.syntax");
        } else if (ServicesRegistry.getInstance().getJahiaGroupManagerService().groupExists(siteId, name)) {
            Locale locale = LocaleContextHolder.getLocale();
            addError(
                    context,
                    i18n("siteSettings.groups.errors.groupname.unique") + " "
                            + Messages.getInternal("label.group", locale) + " '" + name + "' "
                            + i18n("siteSettings.groups.errors.groupname.exists"));
        } else {
            valid = true;
        }
        return valid;
    }

    private String groupname;

    private int siteId;

    public GroupModel() {
        super();
    }

    public GroupModel(int siteId) {
        this();
        this.siteId = siteId;
    }

    public String getGroupname() {
        return groupname;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    /**
     * Performs validation of the group name for syntax and also the group for existence.
     * 
     * @param context
     *            the current validation context object
     */
    public void validateCopyGroup(ValidationContext context) {
        validateGroupName(groupname, siteId, context.getMessageContext());
    }

    /**
     * Performs validation of the group name for syntax and also the group for existence.
     * 
     * @param context
     *            the current validation context object
     */
    public void validateCreateGroup(ValidationContext context) {
        validateGroupName(groupname, siteId, context.getMessageContext());
    }

}
