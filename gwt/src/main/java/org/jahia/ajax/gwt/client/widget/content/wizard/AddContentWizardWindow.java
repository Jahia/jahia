/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
        addStyleName("add-content-wizard-window");
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
