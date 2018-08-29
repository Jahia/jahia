/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.data.GWTJahiaChannel;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Display untranslated contents action
 */
public class ShowUntranslatedContentsActionItem extends BaseActionItem {

    public static final String SHOW_UNTRANSLATED_CONTENTS_PARAM = "showUntranslatedContents";

    private boolean checked;

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override public MenuItem createMenuItem() {
        return new CheckMenuItem();
    }

    @Override public void onComponentSelection() {
        MainModule mainModule = ((EditLinker)linker).getMainModule();
        GWTJahiaChannel activeChannel = mainModule.getActiveChannel();
        StringBuilder url;

        if (((CheckMenuItem)getMenuItem()).isChecked()) {
            url = new StringBuilder(mainModule.getUrl(mainModule.getPath(), mainModule.getTemplate(),
                    activeChannel != null ? activeChannel.getValue() : null, mainModule.getActiveChannelVariant(), true));

            List<String[]> params = new ArrayList<String[]>();
            params.add(new String[]{SHOW_UNTRANSLATED_CONTENTS_PARAM, "true"});

            MainModule.appendParamsToUrl(url, params);
        } else {
            url = new StringBuilder(mainModule.getUrl(mainModule.getPath(), mainModule.getTemplate(),
                    activeChannel != null ? activeChannel.getValue() : null, mainModule.getActiveChannelVariant(), true,
                    new String[] {SHOW_UNTRANSLATED_CONTENTS_PARAM}));
        }

        mainModule.goToUrl(url.toString(), false, false, false);
    }

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        super.init(gwtToolbarItem, linker);
        if (checked) {
            ((CheckMenuItem)getMenuItem()).setChecked(true);
        }
    }
}
