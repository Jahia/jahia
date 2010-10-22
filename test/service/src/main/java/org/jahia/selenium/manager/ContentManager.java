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
public class ContentManager extends SeleneseTestCase {
    private static Logger logger = Logger.getLogger(ContentManager.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "3000"; //speed between selenium commands

    private final int numberOfFolders = 1;

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

        //Wait for page loading
        selenium.waitForPopUp("Content_manager", "3000");

        //Select the content manager window
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


        //Right click on Home
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//span[text()='Home']");
            }
        };
        selenium.clickAt("//span[text()='Home']", "5,5");
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
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");



        if(!selenium.isElementPresent("//span[text()='Page 1']"))
        {
            selenium.doubleClickAt("//span[text()='Home']", "5,5");
        }
        //Cut page 1
        selenium.clickAt("//span[text()='Page 1']", "5,5");
        selenium.mouseOver("//span[text()='Page 1']");
        selenium.contextMenuAt("//span[text()='Page 1']", "5,5");
        selenium.click("Link=Cut");

        //Paste Page 1
        selenium.clickAt("//span[text()='My Profile']", "5,5");
        selenium.mouseOver("//span[text()='My Profile']");
        selenium.contextMenuAt("//span[text()='My Profile']", "5,5");
        selenium.click("Link=Paste");

        //Rename Page1
        selenium.clickAt("//span[text()='Page 1']", "5,5");
        selenium.mouseOver("//span[text()='Page 1']");
        selenium.contextMenuAt("//span[text()='Page 1']", "5,5");
        selenium.answerOnNextPrompt("Renamed Page");
        selenium.click("Link=Rename");
        selenium.getPrompt();

        //Delete Renamed Page
        deleteElement("//span[text()='Page 1']");

        if(!selenium.isElementPresent("//span[text()='Page 2']"))
        {
            selenium.doubleClickAt("//span[text()='Home']", "5,5");
        }

        //Zip Page2
        selenium.clickAt("//span[text()='Page 2']", "5,5");
        selenium.mouseOver("//span[text()='Page 2']");
        selenium.contextMenuAt("//span[text()='Page 2']", "5,5");
        selenium.answerOnNextPrompt("Zip de page2");
        selenium.click("Link=Zip");
        selenium.getPrompt();

        //Delete the zip
        deleteElement("//span[text()='Zip de page2.zip']");

        //Create a new content list
        selenium.clickAt("//span[text()='listA']", "5,5");
        selenium.mouseOver("//span[text()='listA']");
        selenium.contextMenuAt("//span[text()='listA']", "5,5");
        selenium.click("Link=New content list");

        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[2]/div/div/div[2]/div/div/table/tbody/tr/td/div/div/div/table/tbody/tr/td[3]/img");
            }
        };
        selenium.doubleClick("//div[2]/div/div/div[2]/div/div/table/tbody/tr/td/div/div/div/table/tbody/tr/td[3]/img");
        selenium.doubleClick("//div[2]/div/div/div[2]/div/div[2]/table/tbody/tr/td/div/div/div/table/tbody/tr/td[5]/span/div/table/tbody/tr/td/img");

        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//input[@name='jcr:title']");
            }
        };
        selenium.type("//input[@name='jcr:title']","test list");
        //Click on save
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");

        //Create a new content
        selenium.clickAt("//span[text()='test list']", "5,5");
        selenium.mouseOver("//span[text()='test list']");
        selenium.contextMenuAt("//span[text()='test list']", "5,5");
        selenium.click("Link=New content");

          //click on the image for sort"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//td[@class='x-grid3-header x-grid3-hd x-grid3-cell x-grid3-td-label']/div/span");
            }
        };
        selenium.click("//td[@class='x-grid3-header x-grid3-hd x-grid3-cell x-grid3-td-label']/div/span");


        //doubleClick on "Editorial content"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//img[@src='/modules/default/icons/jmix_editorialContent.png']");
            }
        };
        selenium.doubleClick("//img[@src='/modules/default/icons/jmix_editorialContent.png']");

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
        selenium.type("//input[@name='name']", "Test rich text");

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
        selenium.type("//textarea[@class='cke_source cke_enable_context_menu']", "<p>blabla</p>");

        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");

        deleteElement("//span[text()='test list']");

        //Copy page 2
        selenium.clickAt("//span[text()='Page 2']", "5,5");
        selenium.mouseOver("//span[text()='Page 2']");
        selenium.contextMenuAt("//span[text()='Page 2']", "5,5");
        selenium.click("Link=Copy");

        //Paste as reference page 2
        selenium.clickAt("//span[text()='My Profile']", "5,5");
        selenium.mouseOver("//span[text()='My Profile']");
        selenium.contextMenuAt("//span[text()='My Profile']", "5,5");
        selenium.click("Link=Paste reference");
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//button[text()='Save']");
            }
        };

        selenium.type("//input[@name='jcr:title']", "Reference Page 2");

        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");

        //Delete reference to page 2
        deleteElement("//span[text()='Reference Page 2']");

        selenium.clickAt("//span[text()='mySite']", "5,5");
        selenium.mouseOver("//span[text()='mySite']");
        selenium.contextMenuAt("//span[text()='mySite']", "5,5");
        selenium.click("Link=Export");

        if(!selenium.isElementPresent("//a[@href='/cms/export/default/sites/mySite.xml?cleanup=simple']")||!selenium.isElementPresent("//a[@href='/cms/export/default/sites/mySite.zip?cleanup=simple']"))
        {
            fail("Error on the export. Links missing ");
        }

        selenium.click("//button[text()='Cancel']");

        //Delete page 2
        //deleteElement("//span[text()='Page 2']");

        //Create a new folder
        selenium.clickAt("//span[text()='files']", "5,5");
        selenium.mouseOver("//span[text()='files']");
        selenium.contextMenuAt("//span[text()='files']", "5,5");
        selenium.answerOnNextPrompt("My folder");
        selenium.click("Link=New directory");
        selenium.getPrompt();

        //Create new folders and under folders
        for (int i = 0; i < numberOfFolders; i++) {
            selenium.clickAt("//span[text()='My folder']", "5,5");
            selenium.mouseOver("//span[text()='My folder']");
            selenium.contextMenuAt("//span[text()='My folder']", "5,5");
            selenium.answerOnNextPrompt("folder" + i);
            selenium.click("Link=New directory");
            selenium.getPrompt();
            selenium.clickAt("//span[text()='folder" + i + "']", "5,5");
            selenium.mouseOver("//span[text()='folder" + i + "']");
            selenium.contextMenuAt("//span[text()='folder" + i + "']", "5,5");
            selenium.answerOnNextPrompt("under folder" + i);
            selenium.click("Link=New directory");
            selenium.getPrompt();
        }

        deleteElement("//span[text()='My folder']");


    }

    public void deleteElement(String element) {
        selenium.clickAt(element, "5,5");
        selenium.mouseOver(element);
        selenium.contextMenuAt(element, "5,5");
        selenium.click("Link=Remove");
        if (selenium.isElementPresent("//button[text()='Yes']")) {
            selenium.click("//button[text()='Yes']");
        }
    }

}
