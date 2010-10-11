package org.jahia.selenium.edit;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: sophiabatata
 * Date: Oct 8, 2010
 * Time: 9:40:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class DescriptionRichtextTest extends SeleneseTestCase {


    private static Logger logger = Logger.getLogger(DescriptionRichtextTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "800";  //speed between selenium commands
    private ArrayList<String> defaultAcl = new ArrayList<String>();
    private ArrayList<String> defaultAclAfterRestore = new ArrayList<String>();

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

        deleteContentCreated();
        CreateAnRichtextwithDescription();
        deleteContentCreated();

    }

    public void CreateAnRichtextwithDescription() {


        selenium.waitForPageToLoad("30000");

        //click on "Any content"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[@path='/sites/mySite/home/listA']/div/div[2]/div/div/div/div/table/tbody/tr/td[2]/button");
            }
        };
        selenium.click("//div[@path='/sites/mySite/home/listA']/div/div[2]/div/div/div/div/table/tbody/tr/td[2]/button");


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
        selenium.type("//input[@name='name']", "Test Metadata description");

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
        selenium.type("//textarea[@class='cke_source cke_enable_context_menu']", "<p>first richtext by root with a description</p>");


//Open Metadata
        selenium.click("link=Metadata");

//Add a description
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//textarea[@class='cke_source cke_enable_context_menu']");
            }
        };
        selenium.type("//input[@name='jcr:description']", "ici une explication sur le contenu");


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

//Search explication
        selenium.type("searchTerm", "ici une explication sur le contenu");
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
                return selenium.isElementPresent("//p[text()='first richtext by root with a description']");
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
