package org.jahia.selenium.manager;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

/**
 * this test open the category manager, and create categories, and creates categories under that first created.
 */
public class ContentManagerTest extends SeleneseTestCase {
    private static Logger logger = Logger.getLogger(CreateCategories.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "1000"; //speed between selenium commands
    private final int numberOfCategories = 10;

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
         contentManager();
    }
        public void contentManager() {

        //click on "Managers"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//button[text()='Managers']");
            }
        };
        selenium.click("//button[text()='Managers']");

        //Click on "Content Manager"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("Link=Content manager");
            }
        };
        selenium.click("Link=Content manager");

        //Select Window "Content manager"
        selenium.waitForPopUp("Content_manager", "3000");
        selenium.selectWindow("Content manager");

        //Open "root" tree
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//span[text()='root']");
            }
        };
        selenium.doubleClick("//span[text()='root']");

        //Click on "site"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//span[text()='sites']");
            }
        };
        selenium.doubleClick("//span[text()='sites']");

        //Click on "mysite"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//span[text()='mySite']");
            }
        };
        selenium.doubleClick("//span[text()='mySite']");


        //Right click on My profile
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//span[text()='My Profile']");
            }
        };
        selenium.doubleClickAt("//span[text()='My Profile']", "5,5");

        selenium.mouseOver("//span[text()='My Profile']");
        selenium.contextMenuAt("//span[text()='My Profile']", "5,5");

        //Click on "New Page"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("Link=New page");
            }
        };
        selenium.click("Link=New page");

        //Create 2 new pages
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//input[@name='jcr:title']");
            }
        };
        selenium.type("//input[@name='jcr:title']", "Page 1");
        selenium.type("//input[@name='j:templateNode']", "base");
        selenium.click("//button[text()='Save And Add New']");

        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//input[@name='jcr:title']");
            }
        };
        selenium.type("//input[@name='jcr:title']", "Page 2");
        selenium.type("//input[@name='j:templateNode']", "base");
        selenium.click("//button[text()='Save And Add New']");

        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//input[@name='jcr:title']");
            }
        };
        selenium.click("//button[text()='Cancel']");

        //Verify if Page 1 and page 2 exist
         selenium.mouseOver("//div[text()='Page 1']");
         selenium.mouseOver("//div[text()='Page 2']");



        //Copy page 1
        selenium.mouseOver("//span[text()='Page 1']");
        selenium.contextMenuAt("//span[text()='Page 1']", "5,5");
        selenium.click("Link=Copy");

        //Paste Page 1
        selenium.mouseOver("//span[text()='My Profile']");
        selenium.contextMenuAt("//span[text()='My Profile']", "5,5");
        selenium.click("Link=Paste");
        //Delete the two Page 1
        selenium.mouseOver("//div[text()='Page 1']");
        selenium.contextMenuAt("//div[text()='Page 1']", "0,0");
        selenium.click("Link=Remove");
        selenium.mouseOver("//div[text()='Page 1']");
        selenium.contextMenuAt("//div[text()='Page 1']", "5,5");
        selenium.click("Link=Remove");




    }

}
