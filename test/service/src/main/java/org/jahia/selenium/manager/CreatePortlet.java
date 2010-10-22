package org.jahia.selenium.manager;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

/**
 * Test right click on portlet manager
 * - Create RSS portlet
 * - Create google gadget
 * - Create new portlet
 * - Copy/Cut/Paste Portlets
 * - Rename Portlets
 * 
 */
public class CreatePortlet extends SeleneseTestCase {
    private static Logger logger = Logger.getLogger(CreatePortlet.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "3200"; //speed between selenium commands


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

        addPortlet();
      
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
        selenium.click("//div/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[2]/td[2]/em/button[text()='Save']");

        selenium.doubleClickAt("//span[text()='portlets']", "5,5");
        selenium.mouseOver("//span[text()='portlets']");
        selenium.contextMenuAt("//span[text()='portlets']", "5,5");

        //Click on New Google Gadget
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("Link=New Google Gadget");
            }
        };
        selenium.click("Link=New Google Gadget");


        //Fill name
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//input[@name='name']");
            }
        };
        selenium.type("//input[@name='name']", "Test script google spider brrr");

        //Fill script
        selenium.type("//textarea[@class=' x-form-field x-form-textarea']", "<script src=\"http://www.gmodules.com/ig/ifr?url=http://hosting.gmodules.com/ig/gadgets/file/112581010116074801021/spider.xml&amp;up_spiderName=Spider&amp;up_backgroundImage=http%3A%2F%2F&amp;up_headColor=666666&amp;up_bellyColor=666666&amp;up_legColor=333333&amp;up_backgroundColor=FFFFFF&amp;up_size=1&amp;up_speed=1&amp;up_originalLook=0&amp;up_userColor1=&amp;up_userColor2=&amp;up_userColor3=&amp;up_userColor4=&amp;synd=open&amp;w=320&amp;h=200&amp;title=__UP_spiderName__&amp;border=%23ffffff%7C3px%2C1px+solid+%23999999&amp;output=js\"></script>");
        selenium.click("//div/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[2]/td[2]/em/button[text()='Save']");

        //Create new portlet
        selenium.doubleClickAt("//span[text()='portlets']", "5,5");
        selenium.mouseOver("//span[text()='portlets']");
        selenium.contextMenuAt("//span[text()='portlets']", "5,5");
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("Link=New Google Gadget");
            }
        };
        selenium.click("Link=New portlet");
        //Select HTML portlet
        selenium.doubleClickAt("//div[2]/div/div/div/div/div[2]/div/div/div[2]/div/div/div/div/div[2]/div/div[2]", "5,5");
        selenium.click("//button[text()='Next >']");

        //Fill title  and sources
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//input[@name='jcr:title']");
            }
        };
        selenium.type("//input[@name='jcr:title']", "Test Portlet HTML");
        selenium.type("//textarea[@class=' x-form-field x-form-textarea']", "<portlet-entry name=\"Logo\" hidden=\"false\" type=\"ref\"\n" +
                "    parent=\"HTML\" application=\"false\">\n" +
                "    <meta-info>\n" +
                "        <title>Logo</title>\n" +
                "        <description>Example of HTML portlet</description>\n" +
                "    </meta-info>\n" +
                "    <url>/Logo.html</url>\n" +
                "</portlet-entry>");
        selenium.click("//button[text()='Next >']");
        selenium.click("//button[text()='Next >']");
        selenium.click("//button[text()='Finish']");

        //Copy rss portlet
        selenium.doubleClickAt("//div[@id='Flux RSS lemonde.fr']", "5,5");
        selenium.mouseOver("//div[@id='Flux RSS lemonde.fr']");
        selenium.contextMenuAt("//div[@id='Flux RSS lemonde.fr']", "5,5");
        selenium.click("Link=Copy");

        //Paste rss portlet
        selenium.doubleClickAt("//span[text()='portlets']", "5,5");
        selenium.mouseOver("//span[text()='portlets']");
        selenium.contextMenuAt("//span[text()='portlets']", "5,5");
        selenium.click("Link=Paste");

        //Rename rss portlet
        selenium.doubleClickAt("//div[@id='Flux RSS lemonde-1.fr']", "5,5");
        selenium.mouseOver("//div[@id='Flux RSS lemonde-1.fr']");
        selenium.contextMenuAt("//div[@id='Flux RSS lemonde-1.fr']", "5,5");
        selenium.answerOnNextPrompt("Copy lemonde.fr");
        selenium.click("Link=Rename");
        selenium.getPrompt();

        //Create a new folder
        selenium.doubleClickAt("//span[text()='portlets']", "5,5");
        selenium.mouseOver("//span[text()='portlets']");
        selenium.contextMenuAt("//span[text()='portlets']", "5,5");
        selenium.answerOnNextPrompt("My portlets");
        selenium.click("Link=New directory");
        selenium.getPrompt();

        //Cut rss
        selenium.doubleClickAt("//div[@id='Copy lemonde.fr']", "5,5");
        selenium.mouseOver("//div[@id='Copy lemonde.fr']");
        selenium.contextMenuAt("//div[@id='Copy lemonde.fr']", "5,5");
        selenium.click("Link=Cut");

        //Paste into the new folder
        selenium.clickAt("//div[@id='My portlets']", "5,5");
        selenium.mouseOver("//div[@id='My portlets']");
        selenium.contextMenuAt("//div[@id='My portlets']", "5,5");
        selenium.click("Link=Paste");

        //Click on refresh
        selenium.clickAt("//div[@id='My portlets']", "5,5");
        selenium.mouseOver("//div[@id='My portlets']");
        selenium.contextMenuAt("//div[@id='My portlets']", "5,5");
        selenium.click("Link=Refresh");


        //Remove all portlets
        deleteElement("//div[@id='Flux RSS lemonde.fr']");
        deleteElement("//div[@id='Test script google spider brrr']");
        deleteElement("//span[text()='My portlets']");
        deleteElement("//div[text()='HTML portlet']");


    }

    public void deleteElement(String element)
    {
        selenium.doubleClickAt(element,"5,5");
        selenium.mouseOver(element);
        selenium.contextMenuAt(element,"5,5");
        selenium.click("Link=Remove");
        if (selenium.isElementPresent("//button[text()='Yes']")) {
            selenium.click("//button[text()='Yes']");
        }    }
}

