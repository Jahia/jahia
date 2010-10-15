package org.jahia.selenium.manager;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

/**
 * This test create 2 pages, cut/paste one and copy/paste as reference the second one.
 *
 * Pages are removed at the end of the test
 *
 *
 */
public class SiteManagerTest extends SeleneseTestCase {
    private static Logger logger = Logger.getLogger(CreateCategories.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "3000"; //speed between selenium commands
    private final int numberOfCategories = 10;

    @Override
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
        siteManager();
    }

    public void siteManager() {

        //click on "Managers"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//button[text()='Managers']");
            }
        };
        selenium.click("//button[text()='Managers']");

        //Click on "Site Manager"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("Link=Site manager");
            }
        };
        selenium.click("Link=Site manager");

        //Select Window "Content manager"
        selenium.waitForPopUp("Site_manager", "3000");
        selenium.selectWindow("Site manager");

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


        //Right click on Home
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//span[text()='Home']");
            }
        };
        selenium.doubleClickAt("//span[text()='Home']", "5,5");

        selenium.mouseOver("//span[text()='Home']");
        selenium.contextMenuAt("//span[text()='Home']", "5,5");

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
        selenium.type("//input[@name='jcr:title']", "My Page");
        selenium.type("//input[@name='j:templateNode']", "base");
        selenium.click("//button[text()='Save And Add New']");

        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//input[@name='jcr:title']");
            }
        };
        selenium.type("//input[@name='jcr:title']", "An other page");
        selenium.type("//input[@name='j:templateNode']", "base");
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");


        //Cut My Page
        selenium.clickAt("//span[text()='My Page']", "5,5");
        selenium.mouseOver("//span[text()='My Page']");
        selenium.contextMenuAt("//span[text()='My Page']", "5,5");
        selenium.click("Link=Cut");

        //Paste My Page
        selenium.clickAt("//span[text()='mySite']", "5,5");
        selenium.mouseOver("//span[text()='mySite']");
        selenium.contextMenuAt("//span[text()='mySite']", "5,5");
        selenium.click("Link=Paste");

        //Delete My Page
        selenium.clickAt("//span[text()='My Page']", "5,5");
        selenium.mouseOver("//span[text()='My Page']");
        selenium.contextMenuAt("//span[text()='My Page']", "5,5");
        selenium.click("Link=Remove");
        if (selenium.isElementPresent("//button[text()='Yes']")) {
            selenium.click("//button[text()='Yes']");
        }

        //Copy An other page
        selenium.clickAt("//span[text()='An other page']", "5,5");
        selenium.mouseOver("//span[text()='An other page']");
        selenium.contextMenuAt("//span[text()='An other page']", "5,5");
        selenium.click("Link=Copy");

        //Paste as reference An other page
        selenium.clickAt("//span[text()='mySite']", "5,5");
        selenium.mouseOver("//span[text()='mySite']");
        selenium.contextMenuAt("//span[text()='mySite']", "5,5");
        selenium.click("Link=Paste reference");
        selenium.type("//input[@name='jcr:title']", "Reference to an other page");
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");

        //Rename page
        selenium.clickAt("//span[text()='An other page']", "5,5");
        selenium.mouseOver("//span[text()='An other page']");
        selenium.contextMenuAt("//span[text()='An other page']", "5,5");
        selenium.answerOnNextPrompt("Page Renamed");
        selenium.click("Link=Rename");
        selenium.getPrompt();



        //Delete An other page, reference to An other page
        selenium.doubleClickAt("//span[text()='An other page']", "5,5");
        selenium.mouseOver("//span[text()='An other page']");
        selenium.contextMenuAt("//span[text()='An other page']", "5,5");
        selenium.click("Link=Remove");
        if (selenium.isElementPresent("//button[text()='Yes']")) {
            selenium.click("//button[text()='Yes']");
        }
        selenium.doubleClickAt("//span[text()='Reference to an other page']", "5,5");
        selenium.mouseOver("//span[text()='Reference to an other page']");
        selenium.contextMenuAt("//span[text()='Reference to an other page']", "5,5");
        selenium.click("Link=Remove");
        if (selenium.isElementPresent("//button[text()='Yes']")) {
            selenium.click("//button[text()='Yes']");
        }

    }

}
