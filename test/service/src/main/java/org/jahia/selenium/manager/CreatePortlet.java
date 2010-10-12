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
 * Time: 6:24:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreatePortlet extends SeleneseTestCase {
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
                selenium.setSpeed(TEST_SPEED);
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
        deleteContentCreated();
        addPortlet();
        deleteContentCreated();
    }

         public void deleteContentCreated(){
        selenium.mouseOver("//span[text()='Area : listA']");
        selenium.contextMenuAt("//span[text()='Area : listA']", "0,0");
        selenium.click("link=Remove");
        if (selenium.isElementPresent("//button[text()='Yes']")){
            selenium.click("//button[text()='Yes']");
        }
        selenium.refresh();
    }

    public void addPortlet() {
        //click on "Managers"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//button[text()='Managers']");
            }
        };
        selenium.click("//button[text()='Managers']");

        //Click on "Porlet Manager"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("Link=Content manager");
            }
        };
        selenium.click("Link=Portlet manager");

        //Select Window "Portlet manager"
        selenium.waitForPopUp("Portlet_manager", "3500");
        selenium.selectWindow("Portlet manager");

        //Right click on portlet
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//span[text()='portlets']");
            }
        };
        selenium.doubleClickAt("//span[text()='portlets']", "5,5");
        selenium.mouseOver("//span[text()='portlets']");
        selenium.contextMenuAt("//span[text()='portlets']", "5,5");

        //Click on new rss
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("Link=New RSS");
            }
        };
        selenium.click("Link=New RSS");



        //Fill name
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//input[@name='name']");
            }
        };
        selenium.type("//input[@name='name']", "Flux RSS lemonde.fr");

        //Fill url
        selenium.type("//input[@name='url']", "http://www.lemonde.fr/rss/une.xml");
        selenium.click("//html/body/div[4]/div[2]/div/div/div/div/div/div[2]/div[2]/div/div/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[2]/td[2]/em/button");

        selenium.selectWindow("Home");
        /*/click on "Any content"
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

        //doubleClick on "Portlet Reference"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//img[@src='/modules/default/icons/jnt_portletReference.png']");
            }
        };
        selenium.doubleClick("//img[@src='/modules/default/icons/jnt_portletReference.png']");        */





    }
}
