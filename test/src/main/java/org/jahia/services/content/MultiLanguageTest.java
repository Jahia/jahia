package org.jahia.services.content;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.junit.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Regroups tests that test multi-lingual features of Jahia.
 *
 * @author loom
 *         Date: Jan 27, 2010
 *         Time: 2:16:51 PM
 */
public class MultiLanguageTest extends TestCase {

    private static Logger logger = Logger.getLogger(MultiLanguageTest.class);
    private JahiaSite site;
    private ProcessingContext ctx;
    private final static String TESTSITE_NAME = "jcrMultiLanguageTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @Before
    public void setUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME);
        ctx = Jahia.getThreadParamBean();
        Assert.assertNotNull(site);
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
    }

    @org.junit.Test
    public void testFallBackLanguage() throws Exception {

        // the setMixLanguage call is not really used, only session fallback is used, but we do the work here for
        // consistency anyway.
        site.setMixLanguagesActive(true);
        Set currentLanguages = site.getLanguages();
        currentLanguages.add("fr");
        site.setLanguages(currentLanguages);
        ServicesRegistry.getInstance().getJahiaSitesService().updateSite(site);

        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();

        String defaultLanguage = site.getDefaultLanguage();

        Locale englishLocale = LanguageCodeConverters.languageCodeToLocale("en");
        Locale frenchLocale = LanguageCodeConverters.languageCodeToLocale("fr");

        JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRSessionWrapper englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper stageRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper liveRootNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper stageNode = (JCRNodeWrapper) stageRootNode.getNode("home");

        JCRNodeWrapper textNode1 = stageNode.addNode("text1", "jnt:text");
        textNode1.setProperty("text", "English text");

        englishEditSession.save();

        Set<String> languages = new HashSet<String>();
        jcrService.publish(stageNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        JCRSessionWrapper frenchEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, frenchLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper frenchTextNode = frenchEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/text1");
        String frenchTextPropertyValue = frenchTextNode.getPropertyAsString("text");

        assertEquals("English text node should be available in edit workspace when mixed language is activated.", "English text", frenchTextPropertyValue);

        // now let's test without mix language active.
        frenchEditSession.logout();
        site.setMixLanguagesActive(false);
        ServicesRegistry.getInstance().getJahiaSitesService().updateSite(site);

        frenchEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, frenchLocale);
        frenchTextNode = frenchEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/text1");
        frenchTextPropertyValue = frenchTextNode.getPropertyAsString("text");
        assertNull("English text node should not be available in edit workspace when mixed language is de-activated.", frenchTextPropertyValue);

        site.setMixLanguagesActive(true);
        ServicesRegistry.getInstance().getJahiaSitesService().updateSite(site);

        // Now let's do the checks on the live sessions

        JCRSessionWrapper frenchLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, frenchLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper frenchLiveTextNode = frenchLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/text1");
        String frenchLiveTextPropertyValue = frenchLiveTextNode.getPropertyAsString("text");

        assertEquals("English text node should be available in live workspace when mixed language is activated.", "English text", frenchLiveTextPropertyValue);

        // now let's test without mix language active.
        frenchLiveSession.logout();
        site.setMixLanguagesActive(false);
        ServicesRegistry.getInstance().getJahiaSitesService().updateSite(site);

        frenchLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, frenchLocale);
        frenchLiveTextNode = frenchLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/text1");
        frenchLiveTextPropertyValue = frenchLiveTextNode.getPropertyAsString("text");
        assertNull("English text node should not be available in live workspace when mixed language is de-activated.", frenchLiveTextPropertyValue);

    }

    @After
    public void tearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
        session.logout();
    }


}
