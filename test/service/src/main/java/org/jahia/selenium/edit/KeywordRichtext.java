package org.jahia.selenium.edit;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;
import org.slf4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

import java.util.ArrayList;

/**
 * - Create a richtext
 * - save
 * - open the engine
 * - add a keyword
 * - save
 * - search the keyword
 * - Verify the result
 */
public class KeywordRichtext extends SeleneseTestCase {


    private static Logger logger = org.slf4j.LoggerFactory.getLogger(KeywordRichtext.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "3000";  //speed between selenium commands

    public void setUp() throws Exception {
        try {
            final JahiaSite mySite = ServicesRegistry.getInstance().getJahiaSitesService().getSite("localhost");
            if (mySite == null) {
                site = TestHelper.createSite(TESTSITE_NAME, "localhost", "templates-web");
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
            //  TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception e) {
            logger.warn("Exception during test tearDown", e);
        }
    }

    public void test() throws Exception {

        selenium.setSpeed(TEST_SPEED);  //speed between selenium commands
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
                    selenium.waitForPageToLoad("30000");
                } else if (selenium.isElementPresent("link=Se connecter")) {
                    selenium.click("link=Se connecter");
                    selenium.waitForPageToLoad("30000");
                }
            }
        }

        if (selenium.isElementPresent("//span[text()='Area : listA']")) deleteContentCreated();
        CreateRichtext();
        AddAndSearchKeyword();
        deleteContentCreated();


    }

    public void CreateRichtext() {
        //click on "Any Content"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[2]/div[2]/div/div/div/div/div/div/div/button");
            }
        };
        selenium.click("//div[2]/div[2]/div/div/div/div/div/div/div/button");

        //doubleClick on "Basic content"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[text()='Basic Content']");
            }
        };
        selenium.doubleClick("//div[text()='Basic Content']");

        //doubleClick on "RichText"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//img[@src='/modules/default/icons/jnt_bigText.png']");
            }
        };
        selenium.doubleClick("//img[@src='/modules/default/icons/jnt_bigText.png']");


        //fill name
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Name");
            }
        };
        selenium.type("//input[@name='name']", "Test Richtext");

        //Click on "source"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//span[@class='cke_label']");
            }
        };
        selenium.click("//span[@class='cke_label']");

        //Fill in source content
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//textarea[@class='cke_source cke_enable_context_menu']");
            }
        };
        selenium.type("//textarea[@class='cke_source cke_enable_context_menu']", "<p>first richtext by root with a blabla</p>");

        //click on "Save"
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Save");
            }
        };
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");


    }

    public void AddAndSearchKeyword() {


        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//p[text()='first richtext by root with a blabla']");
            }
        };
        //Open the richtext
        selenium.mouseOver("//p[text()='first richtext by root with a blabla']");
        selenium.doubleClick("//p[text()='first richtext by root with a blabla']");

        //Open Metadata
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("link=Metadata");
            }
        };
        selenium.click("link=Metadata");

        //Select the checkbox
        selenium.click("//input[@type='checkbox']");

        //Add a keyword
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//input[@name='j:keywords']");
            }
        };
        selenium.type("//input[@name='j:keywords']", "tototo");


        //click on "Save"
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Save");
            }
        };
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");


        //Click on Search
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("searchTerm");
            }
        };
        selenium.click("searchTerm");

        //Search tototo
        selenium.type("searchTerm", "tototo");
        selenium.click("//input[@value='']");


        //Return to home
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("link=Home");
            }
        };
        selenium.click("link=Home");

        //Verify is the rich text is present
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//p[text()='first richtext by root with a blabla']");
            }
        };


    }

    public void deleteContentCreated() {
        selenium.mouseOver("//span[text()='Area : listA']");
        selenium.contextMenuAt("//span[text()='Area : listA']", "0,0");
        selenium.click("link=Remove");
        if (selenium.isElementPresent("//button[text()='Yes']")) {
            selenium.click("//button[text()='Yes']");
        }
        selenium.refresh();
    }

}