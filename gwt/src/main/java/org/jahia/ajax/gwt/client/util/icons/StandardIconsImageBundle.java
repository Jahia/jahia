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
package org.jahia.ajax.gwt.client.util.icons;

import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.AbstractImagePrototype;


public interface StandardIconsImageBundle extends ImageBundle {

    @Resource("about.png")
    public AbstractImagePrototype about();

    @Resource("delete.png")
    public AbstractImagePrototype delete();

    @Resource("engine-button-cancel.png")
    public AbstractImagePrototype engineButtonCancel();

    @Resource("engine-button-ok.png")
    public AbstractImagePrototype engineButtonOK();

    @Resource("error.png")
    public AbstractImagePrototype error();

    @Resource("google-docs-32x32.png")
    public AbstractImagePrototype googleDocsLarge();

    @Resource("group.png")
    public AbstractImagePrototype group();

    @Resource("information.png")
    public AbstractImagePrototype information();

    @Resource("lock.png")
    public AbstractImagePrototype lock();

    @Resource("lock-language.png")
    public AbstractImagePrototype lockLanguage();

    @Resource("minus.png")
    public AbstractImagePrototype minusRound();

    @Resource("navigate_down.png")
    public AbstractImagePrototype moveDown();

    @Resource("navigate_up2.png")
    public AbstractImagePrototype moveFirst();

    @Resource("navigate_down2.png")
    public AbstractImagePrototype moveLast();

    @Resource("navigate_up.png")
    public AbstractImagePrototype moveUp();

    @Resource("text_tree.png")
    public AbstractImagePrototype navMenu();

    @Resource("plus.png")    
    public AbstractImagePrototype plusRound();

    @Resource("refresh.png")
    public AbstractImagePrototype refresh();

    @Resource("restore.png")
    public AbstractImagePrototype restore();

    @Resource("role.png")
    public AbstractImagePrototype role();

    @Resource("saved-search.png")
    public AbstractImagePrototype savedSearch();

    @Resource("search.png")
    public AbstractImagePrototype search();

    @Resource("user.png")
    public AbstractImagePrototype user();
    
    @Resource("warning.png")
    public AbstractImagePrototype warning();
    
    @Resource("workflow.png")
    public AbstractImagePrototype workflow();
    
    @Resource("workflow_task.png")
    public AbstractImagePrototype workflowTask();
    
}