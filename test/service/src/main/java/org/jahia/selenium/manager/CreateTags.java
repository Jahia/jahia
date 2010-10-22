package org.jahia.selenium.manager;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

/**
 this test open tags manager and create many tags
 */
public class CreateTags extends SeleneseTestCase{
    private static Logger logger = Logger.getLogger(CreateTags.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "3000"; //speed between selenium commands
    private final int numberOfTags = 10;

    @Override
    public void setUp() throws Exception {
        try {
            final JahiaSite mySite = ServicesRegistry.getInstance().getJahiaSitesService().getSite("localhost");
            if (mySite == null) {
                site = TestHelper.createSite(TESTSITE_NAME, "localhost",  "templates-web");
                assertNotNull(site);
            } else {
                logger.warn("can't create mySite for running tests, because already exist...");
                logger.warn("your test(s) will become in few moment");
            }
        } catch (Exception e) {
            logger.warn("Exception during test setUp", e);
        }
        setUp("http://localhost:8080", "*firefox");
    }
    @Override
    public void tearDown() throws Exception {
        try {
            selenium.stop();
            // TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception e) {
            logger.warn("Exception during test tearDown", e);
        }
    }

    public void test() throws InterruptedException {
                selenium.setSpeed(TEST_SPEED);
        try {
            selenium.open("/cms/edit/default/en/sites/mySite/home.html");
        } catch (Exception e) {
            new Wait("Couldn't find the login page!") {
                public boolean until() {
                    return selenium.isElementPresent("username");
                }
            };
            if (selenium.isElementPresent("username") && selenium.isElementPresent("password")) {
                selenium.type("username", "root");
                selenium.type("password", "root1234");
                if (selenium.isElementPresent("link=Login")) {
                    selenium.click("link=Login");
                    selenium.waitForPageToLoad("34000");
                } else if (selenium.isElementPresent("link=Se connecter")) {
                    selenium.click("link=Se connecter");
                    selenium.waitForPageToLoad("34000");
                }
            }
        }
       createTag();
        deleteContentCreated();
    }

    public void createTag()
    {
                   //open Category manager
        selenium.click("//button[text()='Managers']");
        selenium.click("link=Tag manager");
        selenium.waitForPopUp("Tag_manager", "34000");
        selenium.selectPopUp("Tag_manager");

        //Add tags
        for(int i = 0; i < numberOfTags; i++){
            selenium.click("//button[text()='File']");
            selenium.click("link=New tag");
            selenium.answerOnNextPrompt("TestTag" + i);
            selenium.getPrompt();
        }
    }

    public void deleteContentCreated() {
        selenium.click("//td[@class='x-grid3-header x-grid3-hd x-grid3-cell x-grid3-td-checker ']/div");
        selenium.contextMenuAt("//img[@src='/modules/default/icons/jnt_tag.png']","");
        selenium.click("link=Remove");
        if (selenium.isElementPresent("//button[text()='Yes']")) {
            selenium.click("//button[text()='Yes']");
        }
    }
}
