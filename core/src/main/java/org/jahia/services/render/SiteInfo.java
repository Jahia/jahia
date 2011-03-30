package org.jahia.services.render;

import org.jahia.services.content.decorator.JCRSiteNode;

import java.io.Serializable;

/**
* Created by IntelliJ IDEA.
* User: toto
* Date: 3/30/11
* Time: 20:35
* To change this template use File | Settings | File Templates.
*/
public class SiteInfo implements Serializable {

    private boolean htmlMarkupFilterEnabled;
    private boolean mixLanguagesActive;
    private boolean WCAGComplianceCheckEnabled;
    private String defaultLanguage;

    public SiteInfo(JCRSiteNode siteNode) {
        this.htmlMarkupFilterEnabled = siteNode.isHtmlMarkupFilteringEnabled();
        this.mixLanguagesActive = siteNode.isMixLanguagesActive();
        this.WCAGComplianceCheckEnabled = siteNode.isWCAGComplianceCheckEnabled();
        this.defaultLanguage = siteNode.getDefaultLanguage();
    }

    public boolean isHtmlMarkupFilterEnabled() {
        return htmlMarkupFilterEnabled;
    }

    public boolean isMixLanguagesActive() {
        return mixLanguagesActive;
    }

    public boolean isWCAGComplianceCheckEnabled() {
        return WCAGComplianceCheckEnabled;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }
}
