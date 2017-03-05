/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.decorator;

import org.apache.commons.collections.list.UnmodifiableList;
import org.apache.commons.collections.set.UnmodifiableSet;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SitesSettings;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.*;

import static org.jahia.services.sites.SitesSettings.*;

/**
 * JCR node representing the Jahia virtual site.
 * 
 * User: toto
 * Date: Mar 30, 2010
 * Time: 12:37:45 PM
 */
public class JCRSiteNode extends JCRNodeDecorator implements JahiaSite {
    private static final Logger logger = LoggerFactory.getLogger(JCRSiteNode.class);

    private Set<String> inactiveLiveLanguages;

    private Set<String> inactiveLanguages;

    private String defaultLanguage;
    
    private JCRNodeWrapper home;
    
    private Set<String> languages;
    
    private List<Locale> languagesAsLocales;
    
    private Set<String> mandatoryLanguages;
    
    private Boolean mixLanguagesActive;

    private Boolean allowsUnlistedLanguages;

    private String templateFolder;
    
    private String serverName;

    private JahiaTemplatesPackage templatePackage;

    public JCRSiteNode(JCRNodeWrapper node) {
        super(node);
    }

    /**
     * @return list of inactive live languages
     */
    public Set<String> getInactiveLiveLanguages() {
        return getInactiveLanguages(inactiveLiveLanguages, SitesSettings.INACTIVE_LIVE_LANGUAGES);
    }

    /**
     * @return list of inactive languages
     */
    public Set<String> getInactiveLanguages() {
        return getInactiveLanguages(inactiveLanguages,SitesSettings.INACTIVE_LANGUAGES);
    }

    @SuppressWarnings("unchecked")
    private Set<String> getInactiveLanguages(Set<String> languages,String propertyName) {
        if (languages == null) {
            Set<String> langs = new HashSet<String>();
            try {
                if (hasProperty(propertyName)) {
                    Value[] values = getProperty(propertyName).getValues();
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
     * @deprecated use either {@link #getActiveLiveLanguagesAsLocales} or {@link #getActiveLiveLanguages()} methods instead
     */
    @Deprecated
    public List<Locale> getActiveLanguagesAsLocales() {
        return getActiveLiveLanguagesAsLocales();
    }

    /**
     * @deprecated use {@link #getActiveLiveLanguages} method instead
     */
    @Deprecated
    public Set<String> getActiveLanguages() {
        return getActiveLiveLanguages();
    }

    /**
     * Returns a set of active site languages
     *
     * @return a set of active site languages
     */
    public Set<String> getActiveLiveLanguages() {
        Set<String> langs = new HashSet<String>(getLanguages()) ;
        langs.removeAll(getInactiveLiveLanguages());
        return langs;
    }

    /**
     * Returns an List of active site language  ( as Locale ).
     *
     * @return a List of Locale elements
     */
    public List<Locale> getActiveLiveLanguagesAsLocales() {
        return getLanguagesAsLocales(getActiveLiveLanguages());
    }

    /**
     * Returns an List of inactive site language  ( as Locale ).
     *
     * @return a List of Locale elements
     */
    public List<Locale> getInactiveLanguagesAsLocales() {
        return getLanguagesAsLocales(getInactiveLiveLanguages());
    }

    @SuppressWarnings("unchecked")
    private List<Locale> getLanguagesAsLocales(Set<String> languages) {
        List<Locale> localeList = new ArrayList<Locale>();
        if (languages != null) {
            for (String language : languages) {
                Locale tempLocale = LanguageCodeConverters.languageCodeToLocale(language);
                localeList.add(tempLocale);
            }

        }
        return UnmodifiableList.decorate(localeList);
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
        return getDescription();
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
        if (languagesAsLocales == null) {
            Set<String> languages = getLanguages();
    
            List<Locale> localeList = new ArrayList<Locale>();
            if (languages != null) {
                for (String language : languages) {
                    Locale tempLocale = LanguageCodeConverters.languageCodeToLocale(language);
                    localeList.add(tempLocale);
                }
    
            }
            languagesAsLocales = localeList;
        }

        return languagesAsLocales;
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
        if (serverName == null) {
            try {
                if (hasProperty("j:serverName")) {
                    serverName = getProperty("j:serverName").getString();
                }
            } catch (RepositoryException e) {
                logger.error("Cannot get site property",e);
                return null;
            }
        }
        
        return serverName;
    }

    public String getSiteKey() {
        return getName();
    }

    public String getTemplateFolder() {
        if (templateFolder == null) {
            if (getPath().startsWith("/modules")) {
                templateFolder = getName();
            } else {
                try {
                    if (hasProperty("j:templatesSet")) {
                        templateFolder = getProperty("j:templatesSet").getValue().getString();
                    } else if (hasProperty("j:installedModules")) {
                        for (Value value : getProperty("j:installedModules").getValues()) {
                            JahiaTemplatesPackage templatePackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(value.getString());
                            if (templatePackage != null) {
                                if (StringUtils.equals(templatePackage.getModuleType(), "templatesSet")) {
                                    templateFolder = value.getString();
                                    break;
                                }
                            }
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error("Cannot get site property",e);
                }
            }
        }
        return StringUtils.substringBefore(templateFolder,":");
    }

    public List<String> getInstalledModules() {
        List<String> modules = new ArrayList<String>();
        try {
            if (getPath().startsWith("/modules")) {
                modules.add(getName());
            } else if (hasProperty("j:installedModules")) {
                Value[] v = getProperty("j:installedModules").getValues();
                for (int i = 0; i < v.length; i++) {
                    Value value = v[i];
                    modules.add(StringUtils.substringBefore(value.getString(),":"));
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get site property", e);
        }
        return modules;
    }

    /**
     * Returns a set of all installed modules for this site, their direct and transitive dependencies (the whole dependency tree).
     * 
     * @return a set of all installed modules for this site, their direct and transitive dependencies (the whole dependency tree)
     */
    public Set<String> getInstalledModulesWithAllDependencies() {
        Set<String> modules = new LinkedHashSet<String>(getInstalledModules());
        List<String> keys = new ArrayList<String>(modules);
        TemplatePackageRegistry reg = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getTemplatePackageRegistry();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            JahiaTemplatesPackage aPackage = reg.lookupById(key);
            if (aPackage != null) {
                for (JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                    if (!modules.contains(depend.getId())) {
                        modules.add(depend.getId());
                        keys.add(depend.getId());
                    }
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug("Couldn't find module '" + key
                        + "' which is a direct or transitive dependency of the site '" + getName() + "'");
            }
        }
        return modules;
    }

    /**
     * Get installed modules with their dependencies
     * @return
     */
    public List<String> getAllInstalledModules() {
        List<String> modules = new ArrayList<String>();
        try {
            if (hasProperty("j:installedModules")) {
                Value[] v = getProperty("j:installedModules").getValues();
                for (int i = 0; i < v.length; i++) {
                    Value value = v[i];
                    modules.add(StringUtils.substringBefore(value.getString(),":"));
                }
            }
            if (hasProperty("j:templatesSet")) {
                final JahiaTemplatesPackage templatePackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(
                        getProperty("j:templatesSet").getString());
                if (templatePackage != null) {
                    for (JahiaTemplatesPackage dependency : templatePackage.getDependencies()) {
                    if(!modules.contains(dependency.getId())) {
                        modules.add(dependency.getId());
                    }
                }
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
        JahiaTemplatesPackage pkg = getTemplatePackage();
        return pkg != null ? pkg.getName() : null;
    }

    /**
     * Returns the corresponding template set of this virtual site.
     *
     * @return the corresponding template set of this virtual site
     */
    public JahiaTemplatesPackage getTemplatePackage() {
        if (templatePackage == null) {
            templatePackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                    .getTemplatePackageById(getTemplateFolder());
        }

        return templatePackage;
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


    public boolean isAllowsUnlistedLanguages() {
        if (allowsUnlistedLanguages == null) {
            allowsUnlistedLanguages = false;
            try {
                if (hasProperty("j:allowsUnlistedLanguages")) {
                    allowsUnlistedLanguages = getProperty("j:allowsUnlistedLanguages").getBoolean();
                }
            } catch (RepositoryException e) {
                logger.error("Cannot get site property",e);
            }
        }
        return allowsUnlistedLanguages;
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
            if (getSession().getWorkspace().getName().equals("live")) {
                throw new UnsupportedOperationException("Get site in default workspace");
            }
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
            if (getSession().getWorkspace().getName().equals("live")) {
                throw new UnsupportedOperationException("Get site in default workspace");
            }
            List<Value> l = new ArrayList<Value>();
            for (String s : languages) {
                if (LanguageCodeConverters.LANGUAGE_PATTERN.matcher(s).matches()) {
                    l.add(getSession().getValueFactory().createValue(s));
                }
            }
            setProperty(SitesSettings.LANGUAGES, l.toArray(new Value[l.size()]));
            this.languages = null;
            this.languagesAsLocales = null;
            this.inactiveLanguages = null;
            this.inactiveLiveLanguages = null;
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
    }

    public void setMandatoryLanguages(Set<String> mandatoryLanguages) {
        try {
            if (getSession().getWorkspace().getName().equals("live")) {
                throw new UnsupportedOperationException("Get site in default workspace");
            }
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
            if (getSession().getWorkspace().getName().equals("live")) {
                throw new UnsupportedOperationException("Get site in default workspace");
            }
            setProperty(SitesSettings.MIX_LANGUAGES_ACTIVE,mixLanguagesActive);
            this.mixLanguagesActive = mixLanguagesActive;
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
    }

    public void setAllowsUnlistedLanguages(Boolean allowsUnlistedLanguages) {
        try {
            if (getSession().getWorkspace().getName().equals("live")) {
                throw new UnsupportedOperationException("Get site in default workspace");
            }
            setProperty("j:allowsUnlistedLanguages",allowsUnlistedLanguages);
            this.allowsUnlistedLanguages = allowsUnlistedLanguages;
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
    }

    // JahiaSite Implementations


    @Override
    public String getDescription() {
        try {
            if (hasProperty("j:description")) {
                return getProperty("j:description").getString();
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
        return null;
    }

    /**
     * Returns <code>true</code> if this site is the default one on the server.
     *
     * @return <code>true</code> if this site is the default one on the server
     */
    @Override
    public boolean isDefault() {
        try {
            return getParent().getProperty("j:defaultSite").getString().equals(getIdentifier());
        } catch (RepositoryException e) {
            logger.debug(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void setDescr(String descr) {
        setDescription(descr);
    }

    @Override
    public void setDescription(String description) {
        try {
            if (getSession().getWorkspace().getName().equals("live")) {
                throw new UnsupportedOperationException("Get site in default workspace");
            }
            setProperty("j:description",description);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Sets languages, which are completely deactivated for browsing and editing.
     *
     * @param inactiveLanguages the set of inactive languages
     */
    @Override
    public void setInactiveLanguages(Set<String> inactiveLanguages) {
        try {
            if (getSession().getWorkspace().getName().equals("live")) {
                throw new UnsupportedOperationException("Get site in default workspace");
            }
            List<Value> l = new ArrayList<Value>();
            for (String s : inactiveLanguages) {
                l.add(getSession().getValueFactory().createValue(s));
            }

            setProperty(SitesSettings.INACTIVE_LANGUAGES, l.toArray(new Value[l.size()]));
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
    }

    /**
     * Sets languages, which are not considered in live mode browsing, i.e. are currently inactive in navigation.
     *
     * @param inactiveLiveLanguages the set of inactive languages
     */
    @Override
    public void setInactiveLiveLanguages(Set<String> inactiveLiveLanguages) {
        try {
            if (getSession().getWorkspace().getName().equals("live")) {
                throw new UnsupportedOperationException("Get site in default workspace");
            }
            List<Value> l = new ArrayList<Value>();
            for (String s : inactiveLiveLanguages) {
                l.add(getSession().getValueFactory().createValue(s));
            }

            setProperty(SitesSettings.INACTIVE_LIVE_LANGUAGES, l.toArray(new Value[l.size()]));
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
    }

    @Override
    public void setInstalledModules(List<String> installedModules) {
        try {
            if (getSession().getWorkspace().getName().equals("live")) {
                throw new UnsupportedOperationException("Get site in default workspace");
            }
            List<Value> l = new ArrayList<Value>();
            for (String s : installedModules) {
                l.add(getSession().getValueFactory().createValue(s));
            }

            setProperty("j:installedModules", l.toArray(new Value[l.size()]));
        } catch (RepositoryException e) {
            logger.error("Cannot get site property",e);
        }
    }

    @Override
    public void setAllowsUnlistedLanguages(boolean allowsUnlistedLanguages) {
        try {
            if (getSession().getWorkspace().getName().equals("live")) {
                throw new UnsupportedOperationException("Get site in default workspace");
            }
            setProperty("j:allowsUnlistedLanguages",allowsUnlistedLanguages);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Set the Full Qualified Domain Name ( www.jahia.org )
     */
    @Override
    public void setServerName(String name) {
        try {
            if (getSession().getWorkspace().getName().equals("live")) {
                throw new UnsupportedOperationException("Get site in default workspace");
            }
            setProperty("j:serverName", name);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void setTitle(String value) {
        try {
            if (getSession().getWorkspace().getName().equals("live")) {
                throw new UnsupportedOperationException("Get site in default workspace");
            }
            setProperty("j:title", value);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public String getJCRLocalPath() {
        return getPath();
    }
}
