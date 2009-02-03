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

package org.jahia.params;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.bin.Jahia;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 5 ao�t 2008
 * Time: 12:56:39
 * To change this template use File | Settings | File Templates.
 */
public class AdvPreviewSettings implements Cloneable {

    static private ThreadLocal instance = new ThreadLocal();

    private boolean enabled = true;
    private long previewDate;

    private JahiaUser mainUser;
    private JahiaUser aliasedUser;

    public AdvPreviewSettings() {
    }

    public AdvPreviewSettings(boolean enabled, long previewDate) {
        this.enabled = enabled;
        this.previewDate = previewDate;
    }

    public static AdvPreviewSettings getThreadLocaleInstance(){
        return (AdvPreviewSettings)instance.get();
    }

    public static void clearAdvPreviewSettings(){
        instance.remove();
    }

    public static void setThreadLocalAdvPreviewSettings(AdvPreviewSettings settings){
        if (settings != null){
            instance.set(settings);
        }
    }

    public static void clearAdvPreviewSettings(SessionState session) throws JahiaException {
        if ( session != null ){
            session.removeAttribute(ProcessingContext.SESSION_ADV_PREVIEW_SETTINGS);
        }
        instance.remove();
    }

    /**
     * Set advanced preview settings by taking settings from session
     *
     * @param session
     * @throws Exception
     */
    public static void setThreadLocalAdvPreviewSettings(SessionState session) throws Exception {
        if ( session != null ){
            AdvPreviewSettings sessionSettings = (AdvPreviewSettings)session
                    .getAttribute(ProcessingContext.SESSION_ADV_PREVIEW_SETTINGS);
            if (sessionSettings != null){
                AdvPreviewSettings advPrevSettings = getThreadLocaleInstance();
                if (advPrevSettings == null){
                    advPrevSettings = new AdvPreviewSettings();
                    instance.set(advPrevSettings);
                }
                advPrevSettings.setEnabled(sessionSettings.isEnabled());
                advPrevSettings.setPreviewDate(sessionSettings.getPreviewDate());
                advPrevSettings.setMainUser(sessionSettings.getMainUser());
                advPrevSettings.setAliasedUser(sessionSettings.getAliasedUser());
            }
        }
    }

    /**
     * Returns true if the mode returned by <code>ProcessingContext.PREVIEW.equals(context.getOperationMode())</code> is
     * true and the JahiaUserAliasing thread local instance is not null and is enabled and the aliasedUser is not null.
     *
     * @return
     */
    public static boolean isInUserAliasingMode(){
        ProcessingContext context = Jahia.getThreadParamBean();
        if (context != null){
            AdvPreviewSettings settings = AdvPreviewSettings.getThreadLocaleInstance();
            return (ProcessingContext.PREVIEW.equals(context.getOperationMode()) && settings != null
                    && settings.isEnabled() && settings.getAliasedUser() != null);
        }
        return false;
    }

    public static boolean isPreviewingAtDefinedDateMode(){
        ProcessingContext context = Jahia.getThreadParamBean();
        if (context != null){
            AdvPreviewSettings settings = AdvPreviewSettings.getThreadLocaleInstance();
            return (ProcessingContext.PREVIEW.equals(context.getOperationMode()) && settings != null
                    && settings.isEnabled() && settings.getPreviewDate() != 0);
        }
        return false;
    }

    /**
     * Return the aliasedUser if in AliasingMode, else return the defaultUser.
     * @param defaultUser
     * @return
     */
    public static JahiaUser getAliasedUser(JahiaUser defaultUser){
        ProcessingContext context = Jahia.getThreadParamBean();
        if (context != null){
            AdvPreviewSettings settings = AdvPreviewSettings.getThreadLocaleInstance();
            if (ProcessingContext.PREVIEW.equals(context.getOperationMode()) && settings != null
                    && settings.isEnabled() && settings.getAliasedUser() != null){
                return settings.getAliasedUser();
            }
        }
        return defaultUser;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getPreviewDate() {
        return previewDate;
    }

    public void setPreviewDate(long previewDate) {
        this.previewDate = previewDate;
    }

    public JahiaUser getMainUser() {
        return mainUser;
    }

    public void setMainUser(JahiaUser mainUser) {
        this.mainUser = mainUser;
    }

    public JahiaUser getAliasedUser() {
        return aliasedUser;
    }

    public void setAliasedUser(JahiaUser aliasedUser) {
        this.aliasedUser = aliasedUser;
    }

    public String toString(){
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("enabled", enabled)
                .append("previewDate", previewDate)
                .append("mainUser",  mainUser != null ? mainUser.getUsername() : null)
                .append("aliasedUser",  aliasedUser != null ? aliasedUser.getUsername() : null)
                .toString();
    }

    public Object clone(){
        AdvPreviewSettings copy = new AdvPreviewSettings();
        copy.setEnabled(this.isEnabled());
        copy.setMainUser(this.getMainUser());
        copy.setAliasedUser(this.getAliasedUser());
        copy.setPreviewDate(this.getPreviewDate());
        return copy;
    }
}
