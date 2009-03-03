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

import java.util.Locale;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaRevision;
import org.jahia.ajax.gwt.client.data.GWTJahiaVersion;
import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.impl.AdvCompareModeAjaxActionImpl;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 5 ao�t 2008
 * Time: 12:56:39
 * To change this template use File | Settings | File Templates.
 */
public class AdvCompareModeSettings implements Cloneable {

    private static Logger logger = Logger
            .getLogger(AdvCompareModeSettings.class);
    static private ThreadLocal instance = new ThreadLocal();

    private boolean enabled;
    private VersionSetting version1;
    private VersionSetting version2;

    public AdvCompareModeSettings() {
        version1 = new VersionSetting(0,null,false);
        version2 = new VersionSetting(0,null,false);
    }

    public AdvCompareModeSettings(boolean enabled, VersionSetting version1, VersionSetting version2) {
        this.enabled = enabled;
        this.version1 = version1;
        this.version2 = version2;
    }

    public static AdvCompareModeSettings getThreadLocaleInstance(){
        return (AdvCompareModeSettings)instance.get();
    }

    public static void clearAdvCompareModeSettings(){
        instance.remove();
    }

    public static void setThreadLocalAdvCompareModeSettings(AdvCompareModeSettings settings){
        if (settings != null){
            instance.set(settings);
        }
    }

    public static void clearAdvCompareModeSettings(SessionState session) throws JahiaException {
        if ( session != null ){
            session.removeAttribute(ProcessingContext.SESSION_ADV_COMPARE_MODE_SETTINGS);
        }
        instance.remove();
    }

    /**
     * Set advanced compare mode settings by taking settings from session
     *
     * @param session
     * @throws Exception
     */
    public static void setThreadLocalAdvCompareModeSettings(SessionState session) throws Exception {
        if ( session != null ){
            AdvCompareModeSettings sessionSettings = (AdvCompareModeSettings)session
                    .getAttribute(ProcessingContext.SESSION_ADV_COMPARE_MODE_SETTINGS);
            if (sessionSettings != null){
                AdvCompareModeSettings advCompareModeSettings = getThreadLocaleInstance();
                if (advCompareModeSettings == null){
                    advCompareModeSettings = (AdvCompareModeSettings)sessionSettings.clone();
                    AdvCompareModeSettings.setThreadLocalAdvCompareModeSettings(advCompareModeSettings);
                }
            }
        }
    }

    /**
     * Returns true if the mode returned by <code>ProcessingContext.COMPARE.equals(context.getOperationMode())</code> is
     * true and the AdvCompareModeSettings thread local instance is not null and is enabled.
     *
     * @return
     */
    public static boolean isInAdvCompareMode(){
        ProcessingContext context = Jahia.getThreadParamBean();
        if (context != null){
            return (ProcessingContext.COMPARE.equals(context.getOperationMode())
                    && AdvCompareModeSettings.threadLocalAdvCompareModeSettinsIsEnabled());
        }
        return false;
    }

    /**
     * Return true only if the comparison is not between live and staging but at least one archived version involved
     *
     * @return
     */
    public static boolean isComparingUsingArchivedRevision(){
        ProcessingContext context = Jahia.getThreadParamBean();
        if (context != null){
            AdvCompareModeSettings settings = AdvCompareModeSettings.getThreadLocaleInstance();
            if (ProcessingContext.COMPARE.equals(context.getOperationMode()) && settings != null
                    && settings.isEnabled()){
                int version1 = 0, version2 = 0;
                if (settings.getVersion1() != null){
                    version1 = AdvCompareModeAjaxActionImpl.resolveVersionForComparison(settings.getVersion1());
                }
                if (settings.getVersion2() != null){
                    version2 = AdvCompareModeAjaxActionImpl.resolveVersionForComparison(settings.getVersion2());
                }
                if (version1 == 0 || version2 == 0){
                    return false;
                } else if (version1 == version2) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static ThreadLocal getInstance() {
        return instance;
    }

    public static void setInstance(ThreadLocal instance) {
        AdvCompareModeSettings.instance = instance;
    }

    public VersionSetting getVersion1() {
        return version1;
    }

    public void setVersion1(VersionSetting version1) {
        this.version1 = version1;
    }

    public VersionSetting getVersion2() {
        return version2;
    }

    public void setVersion2(VersionSetting version2) {
        this.version2 = version2;
    }

    public static boolean threadLocalAdvCompareModeSettinsIsEnabled(){
        AdvCompareModeSettings settings = getThreadLocaleInstance();
        return (settings != null && settings.isEnabled());
    }

    public GWTJahiaRevision getGWTRevision1(){
        return getGWTRevision(this.getVersion1());
    }

    public GWTJahiaRevision getGWTRevision2(){
        return getGWTRevision(this.getVersion2());
    }

    /**
     *
     * @param setting
     * @param contentPage
     * @param locale
     * @return
     */
    public ContentObjectEntryState getContentObjectEntryState(VersionSetting setting,
                                                              ContentPage contentPage,
                                                              Locale locale){
        ContentObjectEntryState entryState = null;
        if (setting == null){
            return null;
        }
        if (setting.isUseVersion() && setting.getVersion() != null){
            if (setting.getVersion().getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE){
                entryState = new ContentObjectEntryState(setting.getVersion().getWorkflowState(),2,locale.toString());
            } else {
                entryState = new ContentObjectEntryState(setting.getVersion().getWorkflowState(),
                    Integer.parseInt(String.valueOf(setting.getVersion().getDate()/1000)),locale.toString());
            }
        } else if (!setting.isUseVersion() && setting.getDate()>0){
            entryState = new ContentObjectEntryState(0,Integer.parseInt(String.valueOf(setting.getDate()/1000)),
                    locale.toString());
        }
        if (entryState==null){
            return null;
        }
        try {
            entryState = contentPage.getEntryState(entryState,false,true);
        } catch ( Throwable t ){
            logger.debug(t);
        }
        return entryState;
    }

    private GWTJahiaRevision getGWTRevision(VersionSetting setting){
        if (setting==null){
            return null;
        }
        return new GWTJahiaRevision(setting.getDate(),setting.getVersion(),setting.isUseVersion());
    }

    public Object clone() throws CloneNotSupportedException {
        VersionSetting setting1 = null;
        VersionSetting setting2 = null;
        if (this.version1 != null){
            setting1 = (VersionSetting)this.version1.clone();
        }
        if (this.version2 != null){
            setting2 = (VersionSetting)this.version2.clone();
        }
        AdvCompareModeSettings settings = (AdvCompareModeSettings) super.clone();
        settings.setEnabled(enabled);
        settings.setVersion1(setting1);
        settings.setVersion2(setting2);
        return settings;
    }

    public String toString(){
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("enabled", enabled)
                .append("version1", version1)
                .append("version2", version2)
                .toString();
    }

    public static class VersionSetting implements Cloneable {
        private long date;
        private GWTJahiaVersion version;
        private boolean useVersion;

        public VersionSetting(long date, GWTJahiaVersion version, boolean useVersion) {
            this.date = date;
            this.version = version;
            this.useVersion = useVersion;
        }

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public GWTJahiaVersion getVersion() {
            return version;
        }

        public void setVersion(GWTJahiaVersion version) {
            this.version = version;
        }

        public boolean isUseVersion() {
            return useVersion;
        }

        public void setUseVersion(boolean useVersion) {
            this.useVersion = useVersion;
        }

        /**
         * set selected version to null
         *
         */
        public void invalidateVersion(){
            this.version = null;
            if (this.useVersion){
                this.useVersion = false;
                this.date = 0;
            }
        }

        /**
         *
         * @param checkStagingVersion
         * @return
         */
        public long getDateOrVersionValue(boolean checkStagingVersion){
            if (this.isUseVersion()){
                return this.date;
            } else if (this.getVersion()!= null){
                if (checkStagingVersion){
                    if (this.getVersion().getWorkflowState()> EntryLoadRequest.ACTIVE_WORKFLOW_STATE){
                        return EntryLoadRequest.STAGING_WORKFLOW_STATE;
                    } else {
                        return this.getVersion().getDate();
                    }
                }
            }
            return 0;
        }

        public String toString(){
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("date",this.date).append("version",this.version.getReadableName())
                    .append("useVersion",this.useVersion).toString();
        }

        public Object clone() throws CloneNotSupportedException {
            VersionSetting setting = (VersionSetting) super.clone();
            setting.setDate(date);
            setting.setVersion(version);
            setting.setUseVersion(useVersion);
            return setting;
        }
    }
}