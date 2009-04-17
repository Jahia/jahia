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

package org.jahia.data.beans;

import org.jahia.gui.GuiBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;

import java.util.Locale;

/**
 * <p>Title: This bean contains request contextual attributes</p>
 * <p>Description: This is mostly a wrapper for GuiBean methods, and offers
 * functionality close to the what the ProcessingContext offers, but in a 100% JavaBean
 * compliant API.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class RequestBean {
    private GuiBean guiBean;
    private ProcessingContext processingContext;

    public RequestBean() {
    }

    public RequestBean(final GuiBean guiBean, final ProcessingContext processingContext) {
        this.guiBean = guiBean;
        this.processingContext = processingContext;
    }

    public boolean isEditMode() {
        return guiBean.isEditMode();
    }

    public boolean isNormalMode() {
        return guiBean.isNormalMode();
    }

    public boolean isCompareMode() {
        return guiBean.isCompareMode();
    }

    public boolean isPreviewMode() {
        return guiBean.isPreviewMode();
    }

    public boolean isLogged() {
        return guiBean.isLogged();
    }

    public boolean isNS() {
        return guiBean.isNS();
    }

    public boolean isNS4() {
        return guiBean.isNS4();
    }

    public boolean isNS6() {
        return guiBean.isNS6();
    }

    public boolean isIE() {
        return guiBean.isIE();
    }

    public boolean isIE4() {
        return guiBean.isIE4();
    }

    public boolean isIE5() {
        return guiBean.isIE5();
    }

    public boolean isIE6() {
        return guiBean.isIE6();
    }

    public boolean isIE7() {
        return guiBean.isIE7();
    }

    public boolean isWindows() {
        return guiBean.isWindow();
    }

    public boolean isUnix() {
        return guiBean.isUnix();
    }

    public boolean isMac() {
        return guiBean.isMac();
    }

    /**
     * @deprecated use getProcessingContext() instead
     */
    public ProcessingContext getParamBean() {
        return processingContext;
    }

    public ProcessingContext getProcessingContext() {
        return processingContext;
    }

    public boolean isAdmin() {
        return processingContext.getUser().isAdminMember(processingContext.getSiteID());
    }

    public boolean isRoot() {
        return processingContext.getUser().isRoot();
    }

    public boolean hasWriteAccess() {
        return guiBean.checkWriteAccess();
    }

    public boolean isHasWriteAccess() {
        return guiBean.checkWriteAccess();
    }

    public boolean hasAdminAccess() {
        return processingContext.getPage().checkAdminAccess(processingContext.getUser());
    }

    public Locale getLocale() {
        return processingContext.getLocale();
    }

    public JahiaUser getCurrentUser() {
        return processingContext.getUser();
    }

    public String getCurrentUserName() {
        return processingContext.getUser().getUsername();
    }
}