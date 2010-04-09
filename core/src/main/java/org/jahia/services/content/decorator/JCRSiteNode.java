package org.jahia.services.content.decorator;

import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.analytics.GoogleAnalyticsProfile;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 30, 2010
 * Time: 12:37:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRSiteNode extends JCRNodeDecorator {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JCRSiteNode.class);

    public JCRSiteNode(JCRNodeWrapper node) {
        super(node);
    }

    public JCRSiteNode resolveSite() throws RepositoryException {
        return this;
    }
    
    public int getID() {
        try {
            return (int) getProperty("j:siteId").getLong();
        } catch (RepositoryException e) {
            return -1;
        }
    }

    public String getTitle() {
        try {
            return getProperty("j:title").getString();
        } catch (RepositoryException e) {
            return null;
        }
    }

    public String getServerName() {
        try {
            return getProperty("j:serverName").getString();
        } catch (RepositoryException e) {
            return null;
        }
    }

    public String getSiteKey() {
        return getName();
    }

    public boolean isURLIntegrityCheckEnabled() {
        return false;
    }

    public boolean isWAIComplianceCheckEnabled() {
        return false;
    }

    public boolean isHtmlCleanupEnabled() {
        return false;
    }

    public boolean isHtmlMarkupFilteringEnabled() {
        return false;
    }

    public String getHtmlMarkupFilteringTags() {
        return null;
    }

    public String getDescr() {
        try {
            return getProperty("j:description").getString();
        } catch (RepositoryException e) {
            return null;
        }
    }

    public Set<String> getLanguages() {
        Set<String> languages = new HashSet<String>() ;
        try {
            Value[] values = getProperty("j:languages").getValues();
            for (Value value : values) {
                languages.add(value.getString());
            }
        } catch (RepositoryException e) {
            return null;
        }
        return languages;
    }

    public String[] getActiveLanguageCodes() {
        return getLanguages().toArray(new String[getLanguages().size()]);
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
     * the persistant storage to store the modifications if there were any.
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    public boolean isMixLanguagesActive() {
        try {
            return getProperty("j:mixLanguage").getBoolean();
        } catch (RepositoryException e) {
            return false;
        }
    }

    public void setMandatoryLanguages(Set<String> mandatoryLanguages) {
        try {
            List<Value> l = new ArrayList<Value>();
            for (String s : mandatoryLanguages) {
                l.add(getSession().getValueFactory().createValue(s));
            }

            setProperty("j:mandatoryLanguages", l.toArray(new Value[l.size()]));
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getMandatoryLanguages() {
        Set<String> languages = new HashSet<String>() ;
        try {
            Value[] values = getProperty("j:mandatoryLanguages").getValues();
            for (Value value : values) {
                languages.add(value.getString());
            }
        } catch (RepositoryException e) {
            return null;
        }
        return languages;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        try {
            setProperty("j:defaultLanguage", defaultLanguage);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    public String getDefaultLanguage() {
        try {
            return getProperty("j:defaultLanguage").getString();
        } catch (RepositoryException e) {
            return null;
        }
    }

    public String getTemplateFolder() {
        try {
            if (getParent().isNodeType("jnt:templatesSetFolder")) {
                return getParent().getName();
            }
            return getProperty("j:installedModules").getValues()[0].getString();
        } catch (RepositoryException e) {
            return null;
        }
    }

    /**
     * Returns the corresponding template set name of this virtual site.
     *
     * @return the corresponding template set name of this virtual site
     */
    public String getTemplatePackageName() {
        return ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getTemplatePackageByFileName(getTemplateFolder()).getName();
    }

    public GoogleAnalyticsProfile getGoogleAnalyticsProfile(){
        String account = this.getPropertyAsString("j:gaAccount");
        String login = this.getPropertyAsString("j:gaLogin");
        String password = this.getPropertyAsString("j:gaPassword");
        String profile = this.getPropertyAsString("j:gaProfile");
        String typeUrl = this.getPropertyAsString("j:gaTypeUrl");
        return new GoogleAnalyticsProfile(typeUrl, password, login, profile, account);
    }


    public boolean hasGoogleAnalyticsProfile(){
        return getGoogleAnalyticsProfile().isEnabled();
    }


    public boolean hasActivatedGoogleAnalyticsProfile(){
        return getGoogleAnalyticsProfile().isEnabled();
    }

}
