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

import org.jahia.ajax.gwt.client.widget.wizard.WizardCard;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNewPortletInstance;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * User: ktlili
 * Date: 4 d�c. 2008
 * Time: 11:09:55
 */
public abstract class MashupWizardCard extends WizardCard {
    private MashupWizardCard nextWizardCard;
    private boolean uiCreated;

    public MashupWizardCard(String cardtitle) {
        super(cardtitle);
        setLayout(new FitLayout());
    }

    public PortletWizardWindow getPortletWizardWindow() {
        return (PortletWizardWindow) getWizardWindow();
    }

    public GWTJahiaNewPortletInstance getGwtJahiaNewPortletInstance() {
        return getPortletWizardWindow().getGwtJahiaNewPortletInstance();
    }

    public void setGwtPortletInstanceWizard(GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance) {
        getPortletWizardWindow().setGwtPortletInstanceWizard(gwtJahiaNewPortletInstance);
    }

    public String getJahiNodeType() {
        return getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition().getPortletType();
    }

    public BrowserLinker getLinker() {
        return getPortletWizardWindow().getLinker();
    }

     public GWTJahiaNode getParentNode() {
        return getPortletWizardWindow().getParentNode();
    }

    public MashupWizardCard getNextCard() {
        return nextWizardCard;

    }

    public void setNextWizardCard(MashupWizardCard nextWizardCard) {
        this.nextWizardCard = nextWizardCard;
    }

    public boolean isUiCreated() {
        return uiCreated;
    }

    public void setUiCreated(boolean uiCreated) {
        this.uiCreated = uiCreated;
    }


    public void resetUI() {
        setUiCreated(false);
    }

    public abstract void createUI();

    public abstract void next();
}
