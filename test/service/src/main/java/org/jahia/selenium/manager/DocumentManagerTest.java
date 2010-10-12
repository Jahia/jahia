package org.jahia.selenium.manager;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

/**
 * Created by IntelliJ IDEA.
 * User: sophiabatata
 * Date: Oct 12, 2010
 * Time: 2:22:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class DocumentManagerTest extends SeleneseTestCase {
    private static Logger logger = Logger.getLogger(CreateCategories.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "1500"; //speed between selenium commands
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
                    selenium.waitForPageToLoad("34000");
                } else if (selenium.isElementPresent("link=Se connecter")) {
                    selenium.click("link=Se connecter");
                    selenium.waitForPageToLoad("34000");
                }
            }
        }
          createNewFolder();

    }

    public void createNewFolder() {

        //click on "Managers"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//button[text()='Managers']");
            }
        };
        selenium.click("//button[text()='Managers']");

        //Click on "Document Manager"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("Link=Document manager");
            }
        };
        selenium.click("Link=Document manager");

        //Select Window "Document manager"
        selenium.waitForPopUp("Document_manager","3000");
        selenium.selectWindow("Document manager");

        //Right click on central panel
         new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[@class='x-grid-empty']");
            }
        };
        selenium.mouseOver("//div[@class='x-grid-empty']");
        selenium.contextMenuAt("//div[@class='x-grid-empty']", "0,0");
        selenium.click("//img[@src='/icons/newFolder.png']");
        selenium.answerOnNextPrompt("Folder1");
        selenium.getPrompt();
        selenium.refresh();


        pause(15000);


    }
}
