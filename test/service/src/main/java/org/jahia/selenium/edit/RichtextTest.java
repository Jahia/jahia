package org.jahia.selenium.edit;

import org.apache.log4j.Logger;
import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

import javax.jcr.Node;

/**
 * - create several RichText un ListA of mySite
 */
public class RichtextTest extends SeleneseTestCase {
    private static Logger logger = Logger.getLogger(RichtextTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "3700"; //speed between selenium commands
    private final static int numberOfNodes = 10;

    /*  protected DefaultSelenium createSeleniumClient(String url) throws Exception {
    return new DefaultSelenium("localhost", 4444, "*firefox", url);
}    */
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
        TabRichtext(numberOfNodes);
        deleteContentCreated();

    }


    public void TabRichtext(int numberOfNodes) {
        for (int i = 1; i < numberOfNodes; i++) {
            //click on "Any Content"
            if (selenium.isElementPresent("//div[3]/div/div/table/tbody/tr/td[2]/button")) {
                selenium.click("//div[3]/div/div/table/tbody/tr/td[2]/button");
            } else {
                selenium.click("//div[2]/div[2]/div/div/div/div/div/div/div/button");
            }


            //doubleClick on "Basic content"
            new Wait("wait") {
                public boolean until() {
                    return selenium.isElementPresent("//div[text()='Basic Content']");
                }
            };
            selenium.doubleClick("//div[text()='Basic Content']");

            //Double click on "Rich Text"
            new Wait("wait") {
                public boolean until() {
                    return selenium.isElementPresent("//img[@src='/modules/default/icons/jnt_bigText.png']");
                }
            };
            selenium.doubleClick("//img[@src='/modules/default/icons/jnt_bigText.png']");

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
            selenium.type("//textarea[@class='cke_source cke_enable_context_menu']", "<h2>Rich Text Example " + i + "</h2><p>BlaBlaBlaaa ... BlaBlaBlaaa ... BlaBlaBlaaa ... BlaBlaBlaaa ... BlaBlaBlaaa ... BlaBlaBlaaa ... BlaBlaBlaaa ... BlaBlaBlaaa ... </p>");

            //click on "Save"
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Save");
                }
            };
            selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");
        }
        deleteContentCreated();
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
