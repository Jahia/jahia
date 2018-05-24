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

import com.extjs.gxt.ui.client.widget.button.Button;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Button used for work in progress button item.
 */

public class WorkInProgressButton extends Button {

    private AbstractContentEngine engine;

    public WorkInProgressButton(AbstractContentEngine engine) {
        this.engine = engine;
    }

    /**
     * update the title of the button according to the engine value
     */
    public void updateButtonTitle() {
        switch (engine.getWipStatus()) {
            case ALL_CONTENTS:
                setHtml(Messages.get("label.wip.engine.title.all", "All Content ( localised & non-localised )"));
                break;
            case LANGUAGES:
                String title = Messages.get("label.wip.engine.title", "Work in progress:");
                if (engine.getWorkInProgressByLanguages().size() == 1) {
                    String[] language = {resolveLanguageDisplayName(engine.getWorkInProgressByLanguages().get(0))};
                    setHtml(title + Messages.getWithArgs("label.wip.engine.title.one", "{0} ( excluding non-localised content )", language));
                } else if (engine.getWorkInProgressByLanguages().size() == 2) {
                    String[] languages = {resolveLanguageDisplayName(engine.getWorkInProgressByLanguages().get(0)), resolveLanguageDisplayName(engine.getWorkInProgressByLanguages().get(1))};
                    setHtml(title + Messages.getWithArgs("label.wip.engine.title.two", "{0} and {1} ( excluding non-localised content )", languages));

                } else if (engine.getWorkInProgressByLanguages().size() > 2) {
                    String[] params = {resolveLanguageDisplayName(engine.getWorkInProgressByLanguages().get(0)), Integer.valueOf(engine.getWorkInProgressByLanguages().size()).toString()};
                    setHtml(title + Messages.getWithArgs("label.wip.engine.title.more", "{0} and {1} more languages ( excluding non-localised content )", params));
                }
                break;
            case DISABLED:
            default:
                setHtml(Messages.get("label.saveAsWIP", "Save as work in progress"));
        }
    }

    private String resolveLanguageDisplayName(String lang) {
        for (GWTJahiaLanguage language : JahiaGWTParameters.getSiteLanguages()) {
            if (lang.equals(language.getLanguage())) {
                return language.getDisplayName();
            }
        }
        return lang;
    }
}