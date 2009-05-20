/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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

    public Object clone() throws CloneNotSupportedException {        
        AdvPreviewSettings copy = (AdvPreviewSettings) super.clone();
        copy.setEnabled(this.isEnabled());
        copy.setMainUser(this.getMainUser());
        copy.setAliasedUser(this.getAliasedUser());
        copy.setPreviewDate(this.getPreviewDate());
        return copy;
    }
}
