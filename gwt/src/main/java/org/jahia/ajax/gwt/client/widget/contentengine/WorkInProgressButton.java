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
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.Iterator;

/**
 * Button used for work in progress button item.
 */
public class WorkInProgressButton extends HorizontalPanel {

    private AbstractContentEngine engine;
    private Button button = new Button();
    private Html title = new Html();

    public WorkInProgressButton(AbstractContentEngine engine) {
        this.engine = engine;
        title.addStyleName("button-label");
        add(title);
        add(button);
        addStyleName("button-label-group");
    }

    /**
     * update the title of the button according to the engine value
     */
    public void updateButtonTitle() {
        switch (engine.getWipStatus()) {
            case ALL_CONTENT:
                title.setHtml(Messages.get("label.wip.engine.title", "Work in progress:"));
                button.setHtml(Messages.get("label.wip.engine.title.all", "All Content <span>(localized & non-localized)</span>"));
                button.removeStyleName("button-work-in-progress-off");
                button.addStyleName("button-work-in-progress-on");
                break;
            case LANGUAGES:
                title.setHtml(Messages.get("label.wip.engine.title", "Work in progress:"));
                button.removeStyleName("button-work-in-progress-off");
                button.addStyleName("button-work-in-progress-on");

                Iterator<String> languagesIt = engine.getWorkInProgressLanguagesSorted().iterator();
                if (engine.getWorkInProgressLanguagesSorted().size() == 1) {
                    String[] language = {resolveLanguageDisplayName(languagesIt.next())};
                    button.setHtml(Messages.getWithArgs("label.wip.engine.title.one", "{0} <span>(excluding non-localized content)</span>", language));
                } else if (engine.getWorkInProgressLanguagesSorted().size() == 2) {
                    String[] languages = {resolveLanguageDisplayName(languagesIt.next()), resolveLanguageDisplayName(languagesIt.next())};
                    button.setHtml(Messages.getWithArgs("label.wip.engine.title.two", "{0} and {1} <span>(excluding non-localized content)</span>", languages));

                } else if (engine.getWorkInProgressLanguagesSorted().size() > 2) {
                    String[] params = {resolveLanguageDisplayName(languagesIt.next()), Integer.valueOf(engine.getWorkInProgressLanguagesSorted().size() - 1).toString()};
                    button.setHtml(Messages.getWithArgs("label.wip.engine.title.more", "{0} and {1} more languages <span>(excluding non-localized content)</span>", params));
                }
                break;
            case DISABLED:
            default:
                title.setHtml("");
                button.removeStyleName("button-work-in-progress-on");
                button.addStyleName("button-work-in-progress-off");
                button.setHtml(Messages.get("label.saveAsWIP", "Save as work in progress"));
        }
    }

    public void addSelectionListener(SelectionListener<ButtonEvent> listener) {
        button.addSelectionListener(listener);
    }

    private static String resolveLanguageDisplayName(String lang) {
        for (GWTJahiaLanguage language : JahiaGWTParameters.getSiteLanguages()) {
            if (lang.equals(language.getLanguage())) {
                return language.getDisplayName();
            }
        }
        return lang;
    }
}