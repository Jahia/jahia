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

package org.jahia.services.content.decorator;

import static org.jahia.services.sites.SitesSettings.*;

import org.apache.commons.collections.set.UnmodifiableSet;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.sites.SitesSettings;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.*;

/**
 * JCR node representing the Jahia virtual site.
 * 
 * User: toto
 * Date: Mar 30, 2010
 * Time: 12:37:45 PM
 */
public class JCRSiteNode extends JCRNodeDecorator {
    private static final Logger logger = LoggerFactory.getLogger(JCRSiteNode.class);

    private Set<String> activeLanguages;
    
    private String defaultLanguage;
    
    private JCRNodeWrapper home;

    private Set<String> languages;
    
    private Set<String> mandatoryLanguages;
    
    private Boolean mixLanguagesActive;

    private String templateFolder;

    public JCRSiteNode(JCRNodeWrapper node) {
        super(node);
    }

    /**
     * @deprecated use either {@link #getActiveLanguages()} or {@link #getLanguage()} methods instead
     */
    @Deprecated
    public String[] getActiveLanguageCodes() {
        Set<String> languages = getLanguages();
        if (languages != null) {
            return languages.toArray(new String[getLanguages().size()]);
        } else {
            return null;
        }
    }
    
    /**
     * Returns a set of active site languages
     * 
     * @return a set of active site languages
     */
    @SuppressWarnings("unchecked")
    public Set<String> getActiveLanguages() {
        if (activeLanguages == null) {
            Set<String> langs = new HashSet<String>(getLanguages()) ;
            try {
                if (hasProperty(SitesSettings.INACTIVE_LIVE_LANGUAGES)) {
                    Value[] values = getProperty(SitesSettings.INACTIVE_LIVE_LANGUAGES).getValues();
                    for (Value value : values) {
                        langs.remove(value.getString());
                    }
                }
                activeLanguages = UnmodifiableSet.decorate(langs);
            } catch (RepositoryException e) {
                logger.error("Cannot get site property",e);
                return null;
            }
        }
        
        return activeLanguages;
    }

    /**
     * Returns an List of active site language  ( as Locale ).
     *
     * @return a List of Locale elements
     */
    public List<Locale> getActiveLanguagesAsLocales() {
        Set<String> languages = getActiveLanguages();

        List<Locale> localeList = new ArrayList<Locale>();
        if (languages != null) {
            for (String language : languages) {
                Locale tempLocale = LanguageCodeConverters.languageCodeToLocale(language);
                localeList.add(tempLocale);
            }

        }
        return localeList;
    }

    public String getDefaultLanguage() {
        if (defaultLanguage == null) {
            try {
                if (hasProperty(SitesSettings.DEFAULT_LANGUAGE)) {
                    defaultLanguage = getProperty(SitesSettings.DEFAULT_LANGUAGE).getString();
                }
            } catch (RepositoryException e) {
                logger.error("Cannot get site property",e);
            }
        }
        return defaultLanguage;
    }

    public String getDescr() {
        try {
            if (hasProperty("j:description")) {
                return getProperty("j:description").getString();
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
        return null;
    }

    public JCRNodeWrapper getHome() throws RepositoryException {
        if (home == null) {
            NodeIterator ni = getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                if (next.hasProperty("j:isHomePage") && next.getProperty("j:isHomePage").getBoolean()) {
                    return (home = next);
                }
            }
            if (hasNode("home")) {
                home = getNode("home");
            }
        }
        return home;
    }

    public String getHtmlMarkupFilteringTags() {
        try {
            if (hasProperty(HTML_MARKUP_FILTERING_TAGS)) {
                return getProperty(HTML_MARKUP_FILTERING_TAGS).getString();
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get site property " + HTML_MARKUP_FILTERING_TAGS, e);
        }
        return null;
    }

    public int getID() {
        try {
            return (int) getProperty("j:siteId").getLong();
        } catch (PathNotFoundException e) {
            // ignore it as for template sets the ID is not present
            return 0;
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
            return -1;
        }
    }

    @SuppressWarnings("unchecked")
    public Set<String> getLanguages() {
        if (languages == null) {
            Set<String> langs = new HashSet<String>() ;
            try {
                if (hasProperty(SitesSettings.LANGUAGES)) {
                    Value[] values = getProperty(SitesSettings.LANGUAGES).getValues();
                    for (Value value : values) {
                        langs.add(value.getString());
                    }
                }
                languages = UnmodifiableSet.decorate(langs);
            } catch (RepositoryException e) {
                logger.error("Cannot get site property",e);
                return null;
            }
        }
        return languages;
    }
    /**
     * Returns an List of site language  ( as Locale ).
     *
     * @return an List of Locale elements.
     */
    public List<Locale> getLanguagesAsLocales() {
        Set<String> languages = getLanguages();

        List<Locale> localeList = new ArrayList<Locale>();
        if (languages != null) {
            for (String language : languages) {
                Locale tempLocale = LanguageCodeConverters.languageCodeToLocale(language);
                localeList.add(tempLocale);
            }

        }
        return localeList;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getMandatoryLanguages() {
        if (mandatoryLanguages == null) {
            Set<String> langs = new HashSet<String>() ;
            try {
                if (hasProperty(SitesSettings.MANDATORY_LANGUAGES)) {
                    Value[] values = getProperty(SitesSettings.MANDATORY_LANGUAGES).getValues();
                    for (Value value : values) {
                        langs.add(value.getString());
                    }
                }
                mandatoryLanguages = UnmodifiableSet.decorate(langs);
            } catch (RepositoryException e) {
                logger.error("Cannot get site property",e);
                return null;
            }
        }
        return mandatoryLanguages;
    }

    public JCRSiteNode getResolveSite() throws RepositoryException {
        return this;
    }

    public String getServerName() {
        try {
            if (hasProperty("j:serverName")) {
                return getProperty("j:serverName").getString();
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
        return null;
    }

    public String getSiteKey() {
        return getName();
    }

    public String getTemplateFolder() {
        if (templateFolder == null) {
            try {
                if (hasProperty("j:installedModules")) {
                    templateFolder = getProperty("j:installedModules").getValues()[0].getString();
                }
            } catch (RepositoryException e) {
                logger.error("Cannot get site property",e);
            }
        }
        return templateFolder;
    }

    public List<String> getInstalledModules() {
        List<String> modules = new ArrayList<String>();
        try {
            if (hasProperty("j:installedModules")) {
                Value[] v = getProperty("j:installedModules").getValues();
                for (int i = 0; i < v.length; i++) {
                    Value value = v[i];
                    modules.add(value.getString());
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get site property", e);
        }
        return modules;
    }

    /**
     * Returns the corresponding template set name of this virtual site.
     *
     * @return the corresponding template set name of this virtual site
     */
    public String getTemplatePackageName() {
        JahiaTemplatesPackage templatePackageByFileName = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getTemplatePackageByFileName(getTemplateFolder());
        if (templatePackageByFileName == null ) {
            return null;
        }
        return templatePackageByFileName.getName();
    }

    public String getTitle() {
        try {
            return getProperty("j:title").getString();
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
            return null;
        }
    }

    public boolean isHtmlMarkupFilteringEnabled() {
        try {
            if (hasProperty(HTML_MARKUP_FILTERING_ENABLED)) {
                return getProperty(HTML_MARKUP_FILTERING_ENABLED).getBoolean();
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get site property " + HTML_MARKUP_FILTERING_ENABLED, e);
        }
        return false;
    }

    public boolean isMixLanguagesActive() {
        if (mixLanguagesActive == null) {
            mixLanguagesActive = false;
            try {
                if (hasProperty(SitesSettings.MIX_LANGUAGES_ACTIVE)) {
                    mixLanguagesActive = getProperty(SitesSettings.MIX_LANGUAGES_ACTIVE).getBoolean();
                }
            } catch (RepositoryException e) {
                logger.error("Cannot get site property",e);
            }
        }
        return mixLanguagesActive;
    }

    public boolean isWCAGComplianceCheckEnabled() {
        try {
            if (hasProperty(WCAG_COMPLIANCE_CHECKING_ENABLED)) {
                return getProperty(WCAG_COMPLIANCE_CHECKING_ENABLED).getBoolean();
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get site property " + WCAG_COMPLIANCE_CHECKING_ENABLED, e);
        }
        return false;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        try {
            setProperty(SitesSettings.DEFAULT_LANGUAGE, defaultLanguage);
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
    }

    /**
     * Sets the language settings for this site. This directly interfaces with
     * the persistent storage to store the modifications if there were any.
     *
     * @throws JahiaException when an error occured while storing the modified
     *                        site language settings values.
     */
    public void setLanguages(Set<String> languages) {
        try {
            List<Value> l = new ArrayList<Value>();
            for (String s : languages) {
                l.add(getSession().getValueFactory().createValue(s));
            }

            setProperty(SitesSettings.LANGUAGES, l.toArray(new Value[l.size()]));
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
    }

    public void setMandatoryLanguages(Set<String> mandatoryLanguages) {
        try {
            List<Value> l = new ArrayList<Value>();
            for (String s : mandatoryLanguages) {
                l.add(getSession().getValueFactory().createValue(s));
            }

            setProperty(SitesSettings.MANDATORY_LANGUAGES, l.toArray(new Value[l.size()]));
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
    }

    /**
     * Sets the value of the site property that controls
     *
     * @param mixLanguagesActive
     */
    public void setMixLanguagesActive(boolean mixLanguagesActive) {
        try {
            setProperty(SitesSettings.MIX_LANGUAGES_ACTIVE,mixLanguagesActive);
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
    }

}
