/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.content.wizard;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.wizard.WizardCard;
import org.jahia.ajax.gwt.client.widget.wizard.WizardWindow;

import java.io.Serializable;

/**
 * Widget for adding new content objects using a wizard.
 *
 * @author Sergiy Shyrkov
 */
public class AddContentWizardWindow extends WizardWindow {

    static class AddContentData implements Serializable {

        private String nodeName;

        private GWTJahiaNodeType nodeType;


        public AddContentData() {
            this(null);
        }

        public AddContentData(GWTJahiaNodeType nodeType) {
            this.nodeType = nodeType;
        }


        public String getNodeName() {
            return nodeName;
        }

        public GWTJahiaNodeType getNodeType() {
            return nodeType;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public void setNodeType(GWTJahiaNodeType nodeType) {
            this.nodeType = nodeType;
        }
    }

    static abstract class ContentWizardCard extends WizardCard {
        /**
         * Initializes an instance of this class.
         *
         * @param cardtitle the title of the wizard card
         * @param text      the text to be displayed on the card
         */
        public ContentWizardCard(String cardtitle, String text) {
            super(cardtitle, text);
        }

        /**
         * Returns the wizard data entered by the user.
         *
         * @return the wizard data entered by the user
         */
        public AddContentData getWizardData() {
            return getWizardWindow().getWizardData();
        }

        @Override
        public AddContentWizardWindow getWizardWindow() {
            return (AddContentWizardWindow) super.getWizardWindow();
        }

    }

    private AddContentData data;

    private Linker linker;

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
     * @param linker     browser linker instance
     * @param parentNode the parent node where the wizard was called
     */
    public AddContentWizardWindow(Linker linker, GWTJahiaNode parentNode) {
        this(linker, parentNode, null);

    }

    /**
     * Initializes an instance of this class.
     *
     * @param linker     browser linker instance
     * @param parentNode the parent node where the wizard was called
     */
    public AddContentWizardWindow(Linker linker, GWTJahiaNode parentNode, GWTJahiaNodeType nodeType) {
        super(null);
        data = new AddContentData(nodeType);
        this.parentNode = parentNode;
        this.linker = linker;
        createCards();
        setSize(650, 460);
    }

    protected void createCards() {
        if (data.getNodeType() == null) {
            // in this case, content definitin is not provided. So we add a Card to select it
            addCard(new ContentDefinitionCard(parentNode));
            addCard(new ContentNameCard());
            addCard(new ContentFormCard());
        } else {
            ContentNameCard contentNameCard = new ContentNameCard();
            addCard(contentNameCard);
            addCard(new ContentFormCard());

            contentNameCard.createUI();
        }
    }

    @Override
    public String getHeaderTitle() {
        return Messages.get("label.addContent", "Add content");
    }

    public Linker getLinker() {
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

}
