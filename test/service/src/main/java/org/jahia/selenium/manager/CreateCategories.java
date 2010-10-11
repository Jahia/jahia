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
public class CreateCategories extends SeleneseTestCase {
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
        //open Category manager
        selenium.click("//button[text()='Managers']");
        selenium.click("link=Category manager");
        selenium.waitForPopUp("Category_manager", "30000");
        selenium.selectPopUp("Category_manager");

        //Create main Category
        selenium.clickAt("//span[text()='categories']", "1,1");
        selenium.contextMenuAt("//span[text()='categories']", "1,1");
        selenium.click("link=New category");
        selenium.type("jcr:title", "myMainCategory");
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[2]/td[2]/em/button[text()='Save']");

        for (int i = 0; i < numberOfCategories; i++) {
            //Create new Category
            selenium.clickAt("//span[text()='myMainCategory']", "1,1");
            selenium.contextMenuAt("//span[text()='myMainCategory']", "1,1");
            selenium.click("link=New category");
            selenium.type("jcr:title", "myCategory" + i);
            selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[2]/td[2]/em/button[text()='Save']");

            //Create new Category under myCategory
            selenium.clickAt("//span[text()='myCategory" + i + "']", "1,1");
            selenium.contextMenuAt("//span[text()='myCategory" + i + "']", "1,1");
            selenium.click("link=New category");
            selenium.type("jcr:title", "myUnderCategory" + i);
            selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[2]/td[2]/em/button[text()='Save']");
        }
        deleteContentCreated();
    }

    public void deleteContentCreated() {
        selenium.clickAt("//span[text()='myMainCategory']", "1,1");
        selenium.contextMenuAt("//span[text()='myMainCategory']", "1,1");
        selenium.click("link=Remove");
        if (selenium.isElementPresent("//button[text()='Yes']")) {
            selenium.click("//button[text()='Yes']");
        }
    }
}
