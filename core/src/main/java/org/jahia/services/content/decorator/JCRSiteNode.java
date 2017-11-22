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

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SitesSettings;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

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
    
    private static final String CANNOT_GET_SITE_PROPERTY = "Cannot get site property "; 
    private static final String CANNOT_SET_SITE_PROPERTY = "Cannot set site property ";

    private static List<String> toUnmodifiableList(String[] values) {
        if (values == null || values.length == 0) {
            return Collections.emptyList();
        }
        List<String> list = new LinkedList<>();
        for (String v : values) {
            list.add(v);
        }

        return Collections.unmodifiableList(list);
    }

    private static Set<String> toUnmodifiableSet(String[] values) {
        if (values == null || values.length == 0) {
            return Collections.emptySet();
        }
        Set<String> set = new HashSet<>();
        for (String v : values) {
            set.add(v);
        }

        return Collections.unmodifiableSet(set);
    }

    private Set<String> activeLiveLanguages;
    
    private List<Locale> activeLiveLanguagesAsLocales;    
    
    private Set<String> inactiveLiveLanguages;

    private Set<String> inactiveLanguages;
    
    private List<Locale> inactiveLanguagesAsLocales;

    private String defaultLanguage;
    
    private JCRNodeWrapper home;
    
    private Set<String> languages;
    
    private List<Locale> languagesAsLocales;
    
    private Set<String> mandatoryLanguages;
    
    private Boolean mixLanguagesActive;

    private Boolean allowsUnlistedLanguages;

    private String templateFolder;
    
    private List<String> allServerNames;

    private List<String> serverAliases;

    private String serverName;

    private JahiaTemplatesPackage templatePackage;
    
    private List<String> installedModules;
    
    private Set<String> installedModulesWithDependencies;

    public JCRSiteNode(JCRNodeWrapper node) {
        super(node);
    }

    /**
     * @return list of inactive live languages
     */
    @Override
    public Set<String> getInactiveLiveLanguages() {
        if (inactiveLiveLanguages == null) {
            inactiveLiveLanguages = getLanguagesInProperty(SitesSettings.INACTIVE_LIVE_LANGUAGES);
        }
        return inactiveLiveLanguages;
    }

    /**
     * @return list of inactive languages
     */
    @Override
    public Set<String> getInactiveLanguages() {
        if (inactiveLanguages == null) {
            inactiveLanguages = getLanguagesInProperty(SitesSettings.INACTIVE_LANGUAGES);
        }
        return inactiveLanguages;
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
        if (activeLiveLanguages == null) {
            Set<String> langs = new HashSet<>(getLanguages());
            langs.removeAll(getInactiveLiveLanguages());
            activeLiveLanguages = langs;
        }
        return activeLiveLanguages;
    }

    /**
     * Returns an List of active site language  ( as Locale ).
     *
     * @return a List of Locale elements
     */
    public List<Locale> getActiveLiveLanguagesAsLocales() {
        if (activeLiveLanguagesAsLocales == null) {
            activeLiveLanguagesAsLocales = getLanguagesAsLocales(getActiveLiveLanguages());
        }
        return activeLiveLanguagesAsLocales;
    }

    /**
     * Returns an List of inactive site language  ( as Locale ).
     *
     * @return a List of Locale elements
     */
    public List<Locale> getInactiveLanguagesAsLocales() {
        if (inactiveLanguagesAsLocales == null) {
            inactiveLanguagesAsLocales = getLanguagesAsLocales(getInactiveLiveLanguages());
        }
        return inactiveLanguagesAsLocales;
    }

    private List<Locale> getLanguagesAsLocales(Set<String> languages) {
        List<Locale> localeList = new ArrayList<>();
        if (languages != null) {
            for (String language : languages) {
                Locale tempLocale = LanguageCodeConverters.languageCodeToLocale(language);
                localeList.add(tempLocale);
            }
        }
        return Collections.unmodifiableList(localeList);
    }

    @Override
    public String getDefaultLanguage() {
        if (defaultLanguage == null) {
            try {
                if (hasProperty(SitesSettings.DEFAULT_LANGUAGE)) {
                    defaultLanguage = getProperty(SitesSettings.DEFAULT_LANGUAGE).getString();
                }
            } catch (RepositoryException e) {
                logger.error(CANNOT_GET_SITE_PROPERTY + SitesSettings.DEFAULT_LANGUAGE, e);
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
                    home = next;
                    return home;
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
            logger.error(CANNOT_GET_SITE_PROPERTY + HTML_MARKUP_FILTERING_TAGS, e);
        }
        return null;
    }

    @Override
    public Set<String> getLanguages() {
        if (languages == null) {
            languages = getLanguagesInProperty(SitesSettings.LANGUAGES);
        }
        return languages;
    }
    /**
     * Returns an List of site language  ( as Locale ).
     *
     * @return an List of Locale elements.
     */
    @Override
    public List<Locale> getLanguagesAsLocales() {
        if (languagesAsLocales == null) {
            languagesAsLocales = getLanguagesAsLocales(getLanguages());
        }
        return languagesAsLocales;
    }

    @Override
    public Set<String> getMandatoryLanguages() {
        if (mandatoryLanguages == null) {
            mandatoryLanguages = getLanguagesInProperty(SitesSettings.MANDATORY_LANGUAGES);
        }
        return mandatoryLanguages;
    }

    private Set<String> getLanguagesInProperty(String property) {
        Set<String> langs = new HashSet<>();
        try {
            if (hasProperty(property)) {
                Value[] values = getProperty(property).getValues();
                for (Value value : values) {
                    langs.add(value.getString());
                }
            }
            langs = Collections.unmodifiableSet(langs);
        } catch (RepositoryException e) {
            logger.error(CANNOT_GET_SITE_PROPERTY + property, e);
        }
        return langs;
    }

    @Override
    public JCRSiteNode getResolveSite() throws RepositoryException {
        return this;
    }

    @Override
    public String getServerName() {
        if (serverName == null) {
            try {
                if (hasProperty(SitesSettings.SERVER_NAME)) {
                    serverName = getProperty(SitesSettings.SERVER_NAME).getString();
                }
            } catch (RepositoryException e) {
                logger.error(CANNOT_GET_SITE_PROPERTY + SitesSettings.SERVER_NAME, e);
                return null;
            }
        }

        return serverName;
    }

    @Override
    public List<String> getServerNameAliases() {
        if (serverAliases == null) {
            try {
                if (hasProperty(SitesSettings.SERVER_NAME_ALIASES)) {
                    List<String> result = new ArrayList<>();
                    Value[] v = getProperty(SitesSettings.SERVER_NAME_ALIASES).getValues();
                    for (Value value : v) {
                        result.add(value.getString());
                    }
                    serverAliases = Collections.unmodifiableList(result);
                } else {
                    serverAliases = Collections.emptyList(); 
                }
            } catch (RepositoryException e) {
                logger.error(CANNOT_GET_SITE_PROPERTY + SitesSettings.SERVER_NAME_ALIASES, e);
                return Collections.emptyList();
            }
        }
        
        return serverAliases;
    }

    @Override
    public List<String> getAllServerNames() {
        if (allServerNames == null) {
            allServerNames = getAllServerNamesInternal();
        }
        return allServerNames;
    }

    private List<String> getAllServerNamesInternal() {
        List<String> result = new ArrayList<>();
        String name = getServerName();
        if (name != null) {
            result.add(name);
        }
        result.addAll(getServerNameAliases());
        return Collections.unmodifiableList(result);
    }

    @Override
    public String getSiteKey() {
        return getName();
    }

    @Override
    public String getTemplateFolder() {
        if (templateFolder == null) {
            String retrievedTemplateFolder = null;
            if (getPath().startsWith("/modules")) {
                retrievedTemplateFolder = getName();
            } else {
                try {
                    retrievedTemplateFolder = getTemplateFolderForSite();
                } catch (RepositoryException e) {
                    logger.error(CANNOT_GET_SITE_PROPERTY, e);
                }
            }
            templateFolder = StringUtils.substringBefore(retrievedTemplateFolder, ":");
        }
        return templateFolder;
    }
    
    private String getTemplateFolderForSite() throws RepositoryException {
        String retrievedTemplateFolder = null;
        if (hasProperty(SitesSettings.TEMPLATES_SET)) {
            retrievedTemplateFolder = getProperty(SitesSettings.TEMPLATES_SET).getString();
        } else if (hasProperty(SitesSettings.INSTALLED_MODULES)) {
            for (Value value : getProperty(SitesSettings.INSTALLED_MODULES).getValues()) {
                JahiaTemplatesPackage modulesTemplatePackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                        .getTemplatePackage(value.getString());
                if (modulesTemplatePackage != null
                        && StringUtils.equals(modulesTemplatePackage.getModuleType(), "templatesSet")) {
                    retrievedTemplateFolder = value.getString();
                    break;
                }
            }
        }
        return retrievedTemplateFolder;
    }

    @Override
    public List<String> getInstalledModules() {
        if (installedModules == null) {
            List<String> modules;
            if (getPath().startsWith("/modules")) {
                modules = new ArrayList<>();
                modules.add(getName());
            } else {
                modules = getInstalledModulesFromProperty();
            }
            installedModules = modules;
        }
        return installedModules;
    }

    private List<String> getInstalledModulesFromProperty() {
        List<String> modules = new ArrayList<>();
        try {
            if (hasProperty(SitesSettings.INSTALLED_MODULES)) {
                Value[] v = getProperty(SitesSettings.INSTALLED_MODULES).getValues();
                for (int i = 0; i < v.length; i++) {
                    Value value = v[i];
                    modules.add(StringUtils.substringBefore(value.getString(), ":"));
                }
            }
        } catch (RepositoryException e) {
            logger.error(CANNOT_GET_SITE_PROPERTY + SitesSettings.INSTALLED_MODULES, e);
        }
        return modules;
    }
    
    /**
     * Returns a set of all installed modules for this site, their direct and transitive dependencies (the whole dependency tree).
     * 
     * @return a set of all installed modules for this site, their direct and transitive dependencies (the whole dependency tree)
     */
    public Set<String> getInstalledModulesWithAllDependencies() {
        if (installedModulesWithDependencies == null) {
            Set<String> modules = new LinkedHashSet<>(getInstalledModules());
            List<String> keys = new ArrayList<>(modules);
            TemplatePackageRegistry reg = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry();
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                JahiaTemplatesPackage aPackage = reg.lookupById(key);
                if (aPackage == null) {
                    logger.debug("Couldn't find module '{}' which is a direct or transitive dependency of the site '{}'", key, getName());
                    continue;
                }
                for (JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                    if (modules.add(depend.getId())) {
                        keys.add(depend.getId());
                    }
                }
            }
            installedModulesWithDependencies = modules;
        }
        return installedModulesWithDependencies;
    }

    /**
     * Get installed modules with their dependencies
     * @return
     */
    public List<String> getAllInstalledModules() {
        List<String> modules = getInstalledModulesFromProperty();
        try {
            if (hasProperty(SitesSettings.TEMPLATES_SET)) {
                final JahiaTemplatesPackage templatesSetPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                        .getTemplatePackageById(getProperty(SitesSettings.TEMPLATES_SET).getString());
                if (templatesSetPackage != null) {
                    for (JahiaTemplatesPackage dependency : templatesSetPackage.getDependencies()) {
                        if (!modules.contains(dependency.getId())) {
                            modules.add(dependency.getId());
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(CANNOT_GET_SITE_PROPERTY + SitesSettings.TEMPLATES_SET, e);
        }
        return modules;
    }

    /**
     * Returns the corresponding template set name of this virtual site.
     *
     * @return the corresponding template set name of this virtual site
     */
    @Override
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

    @Override
    public String getTitle() {
        try {
            return getProperty(Constants.TITLE).getString();
        } catch (RepositoryException e) {
            logger.error(CANNOT_GET_SITE_PROPERTY + Constants.TITLE, e);
            return null;
        }
    }

    public boolean isHtmlMarkupFilteringEnabled() {
        try {
            if (hasProperty(HTML_MARKUP_FILTERING_ENABLED)) {
                return getProperty(HTML_MARKUP_FILTERING_ENABLED).getBoolean();
            }
        } catch (RepositoryException e) {
            logger.error(CANNOT_GET_SITE_PROPERTY + HTML_MARKUP_FILTERING_ENABLED, e);
        }
        return false;
    }

    @Override
    public boolean isMixLanguagesActive() {
        if (mixLanguagesActive == null) {
            mixLanguagesActive = false;
            try {
                if (hasProperty(SitesSettings.MIX_LANGUAGES_ACTIVE)) {
                    mixLanguagesActive = getProperty(SitesSettings.MIX_LANGUAGES_ACTIVE).getBoolean();
                }
            } catch (RepositoryException e) {
                logger.error(CANNOT_GET_SITE_PROPERTY + SitesSettings.MIX_LANGUAGES_ACTIVE, e);
            }
        }
        return mixLanguagesActive;
    }

    @Override
    public boolean isAllowsUnlistedLanguages() {
        if (allowsUnlistedLanguages == null) {
            allowsUnlistedLanguages = false;
            try {
                if (hasProperty(SitesSettings.ALLOWS_UNLISTED_LANGUAGES)) {
                    allowsUnlistedLanguages = getProperty(SitesSettings.ALLOWS_UNLISTED_LANGUAGES).getBoolean();
                }
            } catch (RepositoryException e) {
                logger.error(CANNOT_GET_SITE_PROPERTY + SitesSettings.ALLOWS_UNLISTED_LANGUAGES, e);
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
            logger.error(CANNOT_GET_SITE_PROPERTY + WCAG_COMPLIANCE_CHECKING_ENABLED, e);
        }
        return false;
    }
    
    @Override
    public void setDefaultLanguage(String defaultLanguage) {
        try {
            ensureSiteInDefaultWorkspace();
            setProperty(SitesSettings.DEFAULT_LANGUAGE, defaultLanguage);
        } catch (RepositoryException e) {
            logger.error("Cannot set default language", e);
        }
    }
    
    private void ensureSiteInDefaultWorkspace() throws RepositoryException {
        if ("live".equals(getSession().getWorkspace().getName())) {
            throw new UnsupportedOperationException("Get site in default workspace");
        }
    }

    /**
     * Sets the language settings for this site. This directly interfaces with
     * the persistent storage to store the modifications if there were any.
     *
     * @throws JahiaException when an error occured while storing the modified
     *                        site language settings values.
     */
    @Override
    public void setLanguages(Set<String> languages) {
        try {
            ensureSiteInDefaultWorkspace();
            List<Value> l = new ArrayList<>();
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
            logger.error("Cannot set languages", e);
        }
    }

    @Override
    public void setMandatoryLanguages(Set<String> mandatoryLanguages) {
        try {
            ensureSiteInDefaultWorkspace();
            List<Value> l = new ArrayList<>();
            for (String s : mandatoryLanguages) {
                l.add(getSession().getValueFactory().createValue(s));
            }

            setProperty(SitesSettings.MANDATORY_LANGUAGES, l.toArray(new Value[l.size()]));
        } catch (RepositoryException e) {
            logger.error("Cannot set mandatory languages", e);
        }
    }

    /**
     * Sets the value of the site property that controls
     *
     * @param mixLanguagesActive
     */
    @Override
    public void setMixLanguagesActive(boolean mixLanguagesActive) {
        try {
            ensureSiteInDefaultWorkspace();
            setProperty(SitesSettings.MIX_LANGUAGES_ACTIVE,mixLanguagesActive);
            this.mixLanguagesActive = mixLanguagesActive;
        } catch (RepositoryException e) {
            logger.error("Cannot set " + SitesSettings.MIX_LANGUAGES_ACTIVE, e);
        }
    }

    public void setAllowsUnlistedLanguages(Boolean allowsUnlistedLanguages) {
        try {
            ensureSiteInDefaultWorkspace();
            setProperty(SitesSettings.ALLOWS_UNLISTED_LANGUAGES,allowsUnlistedLanguages);
            this.allowsUnlistedLanguages = allowsUnlistedLanguages;
        } catch (RepositoryException e) {
            logger.error(CANNOT_SET_SITE_PROPERTY + SitesSettings.ALLOWS_UNLISTED_LANGUAGES, e);
        }
    }

    // JahiaSite Implementations


    @Override
    public String getDescription() {
        try {
            if (hasProperty(Constants.DESCRIPTION)) {
                return getProperty(Constants.DESCRIPTION).getString();
            }
        } catch (RepositoryException e) {
            logger.error(CANNOT_GET_SITE_PROPERTY + Constants.DESCRIPTION, e);
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
            return getParent().getProperty(SitesSettings.DEFAULT_SITE).getString().equals(getIdentifier());
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
            ensureSiteInDefaultWorkspace();
            setProperty(Constants.DESCRIPTION,description);
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
            ensureSiteInDefaultWorkspace();
            List<Value> l = new ArrayList<>();
            for (String s : inactiveLanguages) {
                l.add(getSession().getValueFactory().createValue(s));
            }

            setProperty(SitesSettings.INACTIVE_LANGUAGES, l.toArray(new Value[l.size()]));
        } catch (RepositoryException e) {
            logger.error(CANNOT_SET_SITE_PROPERTY + SitesSettings.INACTIVE_LANGUAGES, e);
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
            ensureSiteInDefaultWorkspace();
            List<Value> l = new ArrayList<>();
            for (String s : inactiveLiveLanguages) {
                l.add(getSession().getValueFactory().createValue(s));
            }

            setProperty(SitesSettings.INACTIVE_LIVE_LANGUAGES, l.toArray(new Value[l.size()]));
        } catch (RepositoryException e) {
            logger.error(CANNOT_SET_SITE_PROPERTY + SitesSettings.INACTIVE_LIVE_LANGUAGES, e);
        }
    }

    @Override
    public void setInstalledModules(List<String> installedModules) {
        try {
            ensureSiteInDefaultWorkspace();
            List<Value> l = new ArrayList<>();
            for (String s : installedModules) {
                l.add(getSession().getValueFactory().createValue(s));
            }

            setProperty(SitesSettings.INSTALLED_MODULES, l.toArray(new Value[l.size()]));
        } catch (RepositoryException e) {
            logger.error(CANNOT_SET_SITE_PROPERTY + SitesSettings.INSTALLED_MODULES, e);
        }
    }

    @Override
    public void setAllowsUnlistedLanguages(boolean allowsUnlistedLanguages) {
        try {
            ensureSiteInDefaultWorkspace();
            setProperty(SitesSettings.ALLOWS_UNLISTED_LANGUAGES,allowsUnlistedLanguages);
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
            ensureSiteInDefaultWorkspace();
            setProperty(SitesSettings.SERVER_NAME, name);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void setServerNameAliases(List<String> names) {
        try {
            ensureSiteInDefaultWorkspace();
            if (names == null || names.size() == 0) {
                setProperty(SitesSettings.SERVER_NAME_ALIASES, (String[]) null);
            } else {
                setProperty(SitesSettings.SERVER_NAME_ALIASES, names.toArray(new String[names.size()]));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    @Override
    public void setTitle(String value) {
        try {
            ensureSiteInDefaultWorkspace();
            setProperty(Constants.TITLE, value);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public String getJCRLocalPath() {
        return getPath();
    }

    @Override
    public JCRPropertyWrapper setProperty(String s, String value) throws ValueFormatException, VersionException,
            LockException, ConstraintViolationException, RepositoryException {
        if (SitesSettings.DEFAULT_LANGUAGE.equals(s)) {
            defaultLanguage = value;
        } else if (SitesSettings.TEMPLATES_SET.equals(s)) {
            templateFolder = null;
        } else if (SitesSettings.SERVER_NAME.equals(s)) {
            serverName = value;
            allServerNames = getAllServerNamesInternal();
        }

        return super.setProperty(s, value);
    }

    @Override
    public JCRPropertyWrapper setProperty(String s, boolean value) throws ValueFormatException, VersionException,
            LockException, ConstraintViolationException, RepositoryException {
        if (SitesSettings.MIX_LANGUAGES_ACTIVE.equals(s)) {
            mixLanguagesActive = value;
        } else if (SitesSettings.ALLOWS_UNLISTED_LANGUAGES.equals(s)) {
            allowsUnlistedLanguages = value;
        }
        
        return super.setProperty(s, value);
    }
    
    @Override
    public JCRPropertyWrapper setProperty(String s, String[] values) throws ValueFormatException, VersionException,
            LockException, ConstraintViolationException, RepositoryException {
        if (SitesSettings.INACTIVE_LANGUAGES.equals(s)) {
            inactiveLanguages = toUnmodifiableSet(values);
        } else if (SitesSettings.INACTIVE_LIVE_LANGUAGES.equals(s)) {
            inactiveLiveLanguages = toUnmodifiableSet(values);
            inactiveLanguagesAsLocales = getLanguagesAsLocales(inactiveLiveLanguages);
            activeLiveLanguages = null;
            activeLiveLanguagesAsLocales = null;
        } else if (SitesSettings.LANGUAGES.equals(s)) {
            languages = toUnmodifiableSet(values);
            this.languagesAsLocales = getLanguagesAsLocales(languages);
            activeLiveLanguages = null;
            activeLiveLanguagesAsLocales = null;
        } else if (SitesSettings.MANDATORY_LANGUAGES.equals(s)) {
            mandatoryLanguages = toUnmodifiableSet(values);
        } else if (SitesSettings.INSTALLED_MODULES.equals(s)) {
            installedModules = toUnmodifiableList(values);
            installedModulesWithDependencies = null;
        } else if (SitesSettings.SERVER_NAME_ALIASES.equals(s)) {
            serverAliases = toUnmodifiableList(values);
            allServerNames = getAllServerNamesInternal();
        }

        return super.setProperty(s, values);
    }
}
