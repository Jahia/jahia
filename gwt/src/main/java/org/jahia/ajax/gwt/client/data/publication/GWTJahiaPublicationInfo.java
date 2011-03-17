/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.data.publication;

import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.data.SerializableBaseModel;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 4, 2009
 * Time: 12:00:28 PM
 * 
 */
public class GWTJahiaPublicationInfo extends SerializableBaseModel {

    public static final int PUBLISHED = 1;
    public static final int MODIFIED = 3;
    public static final int NOT_PUBLISHED = 4;
    public static final int UNPUBLISHED = 5;
    public static final int MANDATORY_LANGUAGE_UNPUBLISHABLE = 6;
    public static final int LIVE_MODIFIED = 7;
    public static final int LIVE_ONLY = 8;
    public static final int CONFLICT = 9;
    public static final int MANDATORY_LANGUAGE_VALID = 10;
    
    public static Map<Integer,String> statusToLabel = new HashMap<Integer, String>();

    static {
        statusToLabel.put(GWTJahiaPublicationInfo.PUBLISHED,"published");
        statusToLabel.put(GWTJahiaPublicationInfo.MODIFIED,"modified");
        statusToLabel.put(GWTJahiaPublicationInfo.NOT_PUBLISHED,"notpublished");
        statusToLabel.put(GWTJahiaPublicationInfo.UNPUBLISHED,"unpublished");
        statusToLabel.put(GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE,"mandatorylanguageunpublishable");
        statusToLabel.put(GWTJahiaPublicationInfo.LIVE_MODIFIED,"livemodified");
        statusToLabel.put(GWTJahiaPublicationInfo.LIVE_ONLY,"liveonly");
        statusToLabel.put(GWTJahiaPublicationInfo.CONFLICT,"conflict");
        statusToLabel.put(GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_VALID,"mandatorylanguagevalid");
    }

    public GWTJahiaPublicationInfo() {
    }

    public GWTJahiaPublicationInfo(String uuid, int status, boolean canPublish) {
        setUuid(uuid);
        setStatus(status);
        setCanPublish(canPublish);
        setLocked(false);
    }

    public String getTitle() {
        return get("title");
    }

    public void setTitle(String path) {
        set("title", path);
    }

    public String getNodetype() {
        return get("nodetype");
    }

    public void setNodetype(String nodetype) {
        set("nodetype", nodetype);
    }

    public String getUuid() {
        return get("uuid");
    }

    public void setUuid(String path) {
        set("uuid", path);
    }

    public String getI18nUuid() {
        return get("i18nUuid");
    }

    public void setI18NUuid(String path) {
        set("i18nUuid", path);
    }

    public Integer getStatus() {
        return get("status");
    }

    public void setStatus(Integer status) {
        set("status", status);
    }

    public Boolean isCanPublish() {
        return get("canPublish");
    }

    public void setCanPublish(Boolean canPublish) {
        set("canPublish", canPublish);
    }

    public Boolean isLocked() {
        return get("locked");
    }

    public void setLocked(Boolean locked) {
        set("locked", locked);
    }

    public String getMainPath() {
        return get("mainPath");
    }

    public void setMainPath(String mainTitle) {
        set("mainPath", mainTitle);
    }

    public Integer getMainPathIndex() {
        return get("mainPathIndex");
    }

    public void setMainPathIndex(Integer mainTitle) {
        set("mainPathIndex", mainTitle);
    }

    public String getWorkflowGroup() {
        return get("workflowGroup");
    }

    public void setWorkflowGroup(String workflowGroup) {
        set("workflowGroup", workflowGroup);
    }

    public String getWorkflowTitle() {
        return get("workflowTitle");
    }

    public void setWorkflowTitle(String workflowTitle) {
        set("workflowTitle", workflowTitle);
    }

    public String getWorkflowDefinition() {
        return get("workflowDefinition");
    }

    public void setWorkflowDefinition(String workflowDefinition) {
        set("workflowDefinition", workflowDefinition);
    }

    public String getLanguage() {
        return get("language");
    }

    public void setLanguage(String language) {
        set("language", language);
    }

    public static Object renderPublicationStatusImage(GWTJahiaPublicationInfo info) {
        if (info != null) {
            String label= statusToLabel.get(info.getStatus());

            String title = Messages.get("label.publication." + label, label);
            final Image image = ToolbarIconProvider.getInstance().getIcon("publication/" + label).createImage();
            image.setTitle(title);
            return image;
        }

        return "";
    }

    public static boolean canPublish(GWTJahiaNode node, GWTJahiaPublicationInfo info, final String language) {

        return !node.isLanguageLocked(language) && info.isCanPublish() &&
                info.getStatus() > GWTJahiaPublicationInfo.PUBLISHED &&
                info.getStatus() != GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE &&
                info.getStatus() != GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_VALID;
    }


    public void setMainUUID(String uuid) {
        set("mainUUID", uuid);
    }

    public String getMainUUID() {
        return get("mainUUID");
    }
}
