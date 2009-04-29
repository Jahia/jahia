/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.node.portlet;

import org.jahia.ajax.gwt.client.widget.wizard.WizardWindow;
import org.jahia.ajax.gwt.client.widget.wizard.WizardCard;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNewPortletInstance;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

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
