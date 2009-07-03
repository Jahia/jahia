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
package org.jahia.ajax.gwt.client.widget.content.wizard;

import java.io.Serializable;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.widget.wizard.WizardCard;
import org.jahia.ajax.gwt.client.widget.wizard.WizardWindow;

import com.extjs.gxt.ui.client.widget.button.Button;

/**
 * Widget for adding new content objects using a wizard.
 * 
 * @author Sergiy Shyrkov
 */
public class AddContentWizardWindow extends WizardWindow {

    static class AddContentData implements Serializable {

        private GWTJahiaNodeType nodeType;
        
        private String nodeName;

        public GWTJahiaNodeType getNodeType() {
            return nodeType;
        }

        public void setNodeType(GWTJahiaNodeType nodeType) {
            this.nodeType = nodeType;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }
    }

    static abstract class ContentWizardCard extends WizardCard {
        /**
         * Initializes an instance of this class.
         * 
         * @param cardtitle
         *            the title of the wizard card
         * @param text
         *            the text to be displayed on the card
         */
        public ContentWizardCard(String cardtitle, String text) {
            super(cardtitle, text);
        }

        @Override
        public AddContentWizardWindow getWizardWindow() {
            return (AddContentWizardWindow) super.getWizardWindow();
        }

        /**
         * Returns the wizard data entered by the user.
         * 
         * @return the wizard data entered by the user
         */
        public AddContentData getWizardData() {
            return getWizardWindow().getWizardData();
        }

    }

    private AddContentData data;

    private BrowserLinker linker;

    private GWTJahiaNode parentNode;

    /**
     * Initializes an instance of this class.
     */
    public AddContentWizardWindow() {
        this(null, null);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param linker
     *            browser linker instance
     * @param parentNode
     *            the parent node where the wizard was called
     */
    public AddContentWizardWindow(BrowserLinker linker, GWTJahiaNode parentNode) {
        super(null);
        data = new AddContentData();
        this.parentNode = parentNode;
        this.linker = linker;
        createCards();
        setSize(650, 460);
    }

    protected void createCards() {
        addCard(new ContentDefinitionCard(parentNode)).addCard(new ContentNameCard()).addCard(
                new ContentFormCard());
    }

    @Override
    public String getHeaderTitle() {
        return Messages.get("add_content_wizard_title", "Add content");
    }

    public BrowserLinker getLinker() {
        return linker;
    }

    public GWTJahiaNode getParentNode() {
        return parentNode;
    }

    /**
     * Returns the wizard data entered by the user.
     * 
     * @return the wizard data entered by the user
     */
    public AddContentData getWizardData() {
        return data;
    }

    @Override
    protected void onButtonPressed(Button button) {
        if (button == nextBtn) {
            if (!cards.get(currentStep).isValid()) {
                return;
            }

            // execute next action
            cards.get(currentStep).next();

            if (currentStep + 1 < cards.size()) {
                WizardCard nextCard = (WizardCard) cards.get(currentStep + 1);
                if (!nextCard.isUiCreated()) {
                    nextCard.createUI();
                    nextCard.setUiCreated(true);
                }
            }
        }
        if (button == nextBtn && currentStep + 1 == cards.size()) {
            return;
        }

        super.onButtonPressed(button);
    }
    
}
