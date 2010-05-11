/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.util.icons;

import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.AbstractImagePrototype;


public interface StandardIconsImageBundle extends ImageBundle {


    @Resource("tab_addContent.png")
    public AbstractImagePrototype tabAddContent();

    @Resource("tab_browseContent.png")
    public AbstractImagePrototype tabBrowseContent();

    @Resource("tab_documents.png")
    public AbstractImagePrototype tabDocuments();


     @Resource("icon-query.png")
    public AbstractImagePrototype tabQuery();

    @Resource("icon-img.png")
    public AbstractImagePrototype img();
    

    @Resource("icon-mashup.png")
    public AbstractImagePrototype mashup();

    @Resource("tab_pages.png")
    public AbstractImagePrototype tabPages();

    @Resource("lock.png")
    public AbstractImagePrototype lock();

    @Resource("icon-query.png")
    public AbstractImagePrototype query();

    @Resource("minus.png")
    public AbstractImagePrototype minusRound();

    @Resource("plus.png")    
    public AbstractImagePrototype plusRound();

    @Resource("engine-button-cancel.png")
    public AbstractImagePrototype engineButtonCancel();

    @Resource("engine-button-ok.png")
    public AbstractImagePrototype engineButtonOK();

    @Resource("logo-jahia.gif")
    public AbstractImagePrototype engineLogoJahia();

    @Resource("engine-tab-content.png")
    public AbstractImagePrototype engineTabContent();

    @Resource("engine-tab-metadata.png")
    public AbstractImagePrototype engineTabMetadata();

    @Resource("engine-tab-layout.png")
    public AbstractImagePrototype engineTabLayout();

    @Resource("engine-tab-classification.png")
    public AbstractImagePrototype engineTabClassification();

    @Resource("engine-tab-options.png")
    public AbstractImagePrototype engineTabOption();

    @Resource("engine-tab-publication.png")
    public AbstractImagePrototype engineTabPublication();
    
    @Resource("text_tree.png")
    public AbstractImagePrototype navMenu();

    @Resource("navigate_up2.png")
    public AbstractImagePrototype moveFirst();

    @Resource("navigate_up.png")
    public AbstractImagePrototype moveUp();

    @Resource("navigate_down2.png")
    public AbstractImagePrototype moveLast();

    @Resource("navigate_down.png")
    public AbstractImagePrototype moveDown();

    @Resource("workflow.png")
    public AbstractImagePrototype workflow();

    @Resource("workflow_task.png")
    public AbstractImagePrototype workflowTask();

}