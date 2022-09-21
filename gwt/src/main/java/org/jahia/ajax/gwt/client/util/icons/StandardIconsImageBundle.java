/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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