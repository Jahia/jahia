/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.content.decorator;

import static org.jahia.services.sites.SitesSettings.*;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.*;

/**
 * User: toto
 * Date: Mar 30, 2010
 * Time: 12:37:45 PM
 * 
 */
public class JCRSiteNode extends JCRNodeDecorator {
    private static final Logger logger = LoggerFactory.getLogger(JCRSiteNode.class);

    public JCRSiteNode(JCRNodeWrapper node) {
        super(node);
    }

    public JCRSiteNode getResolveSite() throws RepositoryException {
        return this;
    }
    
    public int getID() {
        try {
            return (int) getProperty("j:siteId").getLong();
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
            return -1;
        }
    }

    public String getTitle() {
        try {
            return getProperty("jcr:title").getString();
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
            return null;
        }
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

    public Set<String> getLanguages() {
        Set<String> languages = new HashSet<String>() ;
        try {
            if (hasProperty("j:languages")) {
                Value[] values = getProperty("j:languages").getValues();
                for (Value value : values) {
                    languages.add(value.getString());
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
            return null;
        }
        return languages;
    }

    public String[] getActiveLanguageCodes() {
        Set<String> languages = getLanguages();
        if (languages != null) {
            return languages.toArray(new String[getLanguages().size()]);
        } else {
            return null;
        }
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

            setProperty("j:languages", l.toArray(new Value[l.size()]));
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
            setProperty("j:mixLanguage",mixLanguagesActive);
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
    }

    public boolean isMixLanguagesActive() {
        try {
            if (hasProperty("j:mixLanguage")) {
                return getProperty("j:mixLanguage").getBoolean();
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
        return false;
    }

    public void setMandatoryLanguages(Set<String> mandatoryLanguages) {
        try {
            List<Value> l = new ArrayList<Value>();
            for (String s : mandatoryLanguages) {
                l.add(getSession().getValueFactory().createValue(s));
            }

            setProperty("j:mandatoryLanguages", l.toArray(new Value[l.size()]));
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
    }

    public Set<String> getMandatoryLanguages() {
        Set<String> languages = new HashSet<String>() ;
        try {
            if (hasProperty("j:mandatoryLanguages")) {
                Value[] values = getProperty("j:mandatoryLanguages").getValues();
                for (Value value : values) {
                    languages.add(value.getString());
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
            return null;
        }
        return languages;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        try {
            setProperty("j:defaultLanguage", defaultLanguage);
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
    }

    public String getDefaultLanguage() {
        try {
            if (hasProperty("j:defaultLanguage")) {
                return getProperty("j:defaultLanguage").getString();
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
        return null;
    }

    public String getTemplateFolder() {
        try {
            if (hasProperty("j:installedModules")) {
                return getProperty("j:installedModules").getValues()[0].getString();
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
        return null;
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

}
