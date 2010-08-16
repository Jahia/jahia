/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.ModalPanel;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 2, 2010
 * Time: 4:04:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnginePanel extends ContentPanel implements EngineContainer {
    private AbstractContentEngine engine;
    private ModalPanel modalPanel;

    public EnginePanel() {
        setBodyBorder(false);
        setLayout(new FitLayout());
    }

    public ContentPanel getPanel() {
        return this;
    }

    public void setEngine(AbstractContentEngine engine) {
        this.engine = engine;
        add(engine);
    }

    public void showEngine() {

        ((EditLinker)engine.getLinker()).replaceMainAreaComponent(this);

    }

    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);

        setZIndex(1040);
        modalPanel = ModalPanel.pop();
        modalPanel.setBlink(false);
        modalPanel.show(this);

    }

    public void closeEngine() {
        ModalPanel.push(modalPanel);
        modalPanel = null;

        ((EditLinker)engine.getLinker()).restoreMainArea();
    }

}
