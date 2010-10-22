package org.jahia.selenium.manager;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

/**
 *  - open the category manager
 * - create categories
 * - create categories under that first created.
 * - Rename categories
 * - Copy / cut / paste categories
 * - Delete categories
 *
 *
 * */
public class CreateCategories extends SeleneseTestCase {
    private static Logger logger = Logger.getLogger(CreateCategories.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "3000"; //speed between selenium commands
    private int numberOfCategories = 5;
    private String elementPath;

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
        createCategory();
        deleteContentCreated();
    }

    public void createCategory() {
        //open Category manager
        selenium.click("//button[text()='Managers']");
        selenium.click("link=Category manager");
        selenium.waitForPopUp("Category_manager", "34000");
        selenium.selectPopUp("Category_manager");

        //Create main Category
        selenium.clickAt("//span[text()='categories']", "1,1");
        selenium.contextMenuAt("//span[text()='categories']", "1,1");
        selenium.click("link=New category");
        selenium.type("jcr:title", "myFirstMainCategory");


        //Click on save
        elementPath = "//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[2]/td[2]/em/button[text()='Save']";
        if (selenium.isElementPresent(elementPath)) {
            selenium.click(elementPath);
        } else fail("Unable to found save button during the creation of the firstCategory");


        selenium.clickAt("//span[text()='myFirstMainCategory']", "1,1");
        selenium.contextMenuAt("//span[text()='myFirstMainCategory']", "1,1");
        selenium.click("link=Edit");
        selenium.type("jcr:title", "myMainCategory");

        elementPath = "//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[2]/td[2]/em/button[text()='Save']";
        if (selenium.isElementPresent(elementPath)) {
            selenium.click(elementPath);
        } else fail("Unable to found save button during the rename of the firstCategory");

        if (numberOfCategories < 3) numberOfCategories = 3;

        for (int i = 0; i < numberOfCategories; i++) {
            //Create new Category
            selenium.clickAt("//span[text()='myMainCategory']", "1,1");
            selenium.contextMenuAt("//span[text()='myMainCategory']", "1,1");
            selenium.click("link=New category");
            selenium.type("jcr:title", "myCategory" + i);
            elementPath = "//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[2]/td[2]/em/button[text()='Save']";
            if (selenium.isElementPresent(elementPath)) {
                selenium.click(elementPath);
            } else fail("Unable to found save button");

            //Create new Category under myCategory
            selenium.clickAt("//span[text()='myCategory" + i + "']", "1,1");
            selenium.contextMenuAt("//span[text()='myCategory" + i + "']", "1,1");
            selenium.click("link=New category");
            selenium.type("jcr:title", "myUnderCategory" + i);
            elementPath = "//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[2]/td[2]/em/button[text()='Save']";
            if (selenium.isElementPresent(elementPath)) {
                selenium.click(elementPath);
            } else fail("Unable to found save button");
        }

        selenium.clickAt("//span[text()='myCategory0']", "1,1");
        selenium.contextMenuAt("//span[text()='myCategory0']", "1,1");
        selenium.click("link=Copy");
        selenium.clickAt("//span[text()='myCategory1']", "1,1");
        selenium.contextMenuAt("//span[text()='myCategory1']", "1,1");
        selenium.click("link=Paste");
        selenium.clickAt("//span[text()='myCategory0']", "1,1");
        selenium.contextMenuAt("//span[text()='myCategory0']", "1,1");
        selenium.click("link=Cut");
        selenium.clickAt("//span[text()='myCategory2']", "1,1");
        selenium.contextMenuAt("//span[text()='myCategory2']", "1,1");
        selenium.click("link=Paste");



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
