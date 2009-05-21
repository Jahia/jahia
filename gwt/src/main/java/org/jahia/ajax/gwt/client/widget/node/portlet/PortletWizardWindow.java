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
package org.jahia.ajax.gwt.client.widget.node.portlet;

import org.jahia.ajax.gwt.client.widget.wizard.WizardWindow;
import org.jahia.ajax.gwt.client.widget.wizard.WizardCard;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNewPortletInstance;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.button.Button;

/**
 * User: ktlili
 * Date: 25 nov. 2008
 * Time: 10:28:28
 */
public class PortletWizardWindow extends WizardWindow {
    private GWTJahiaNode parentNode;
    private BrowserLinker linker;
    private GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance = new GWTJahiaNewPortletInstance();

    public PortletWizardWindow(BrowserLinker linker, GWTJahiaNode parentNode) {
        super(createCards());
        this.parentNode = parentNode;
        this.linker = linker;
        setSize(650, 460);

    }

    public PortletWizardWindow() {
        this(null, null);
    }

    private static List<WizardCard> createCards() {
        // setup an array of WizardCards
        ArrayList<WizardCard> cards = new ArrayList<WizardCard>();

        // 1st card
        MashupWizardCard wc1 = new PortletDefinitionCard();
        cards.add(wc1);

        // 2nd card
        MashupWizardCard wc2 = new PortletFormCard();
        cards.add(wc2);
        wc1.setNextWizardCard(wc2);

        // 3rd card
        MashupWizardCard wc3 = new PortletRoleCard();
        cards.add(wc3);
        wc2.setNextWizardCard(wc3);

        // 4th card
        MashupWizardCard wc4 = new PortletModesCard();
        cards.add(wc4);
        wc3.setNextWizardCard(wc4);

        // 5th card
        MashupWizardCard wc5 = new PortletSaveAsCard();
        cards.add(wc5);
        wc4.setNextWizardCard(wc5);


        return cards;
    }

    public GWTJahiaNewPortletInstance getGwtJahiaNewPortletInstance() {
        return gwtJahiaNewPortletInstance;
    }

    public void setGwtPortletInstanceWizard(GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance) {
        this.gwtJahiaNewPortletInstance = gwtJahiaNewPortletInstance;
    }

    protected void onButtonPressed(Button button) {
        if (button == nextBtn) {
            if (!cards.get(currentStep).isValid()) {
                return;
            }

            // execute next action
            MashupWizardCard currentCard = (MashupWizardCard) cards.get(currentStep);
            currentCard.next();

            if (currentStep + 1 < cards.size()) {
                MashupWizardCard nextCard = (MashupWizardCard) cards.get(currentStep + 1);
                if (!nextCard.isUiCreated()) {
                    nextCard.createUI();
                    nextCard.setUiCreated(true);
                }
            }
        }
        super.onButtonPressed(button);
    }

    public void resetCards(int index) {
        for (int i = index + 1; i < cards.size(); i++) {
            MashupWizardCard card = (MashupWizardCard) cards.get(i);
            if (card != null) {
                card.resetUI();
            }
        }
    }

    public BrowserLinker getLinker() {
        return linker;
    }

    public GWTJahiaNode getParentNode() {
        return parentNode;
    }

    public void onPortletCreated() {

    }
}
