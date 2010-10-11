package org.jahia.selenium.contribute;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

/**
this integration test, create a home-blog page in edit, and create a blog in contribute mode
 */
public class CreateBlog extends SeleneseTestCase{
    private static Logger logger = Logger.getLogger(CreateBlog.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "1500";  //speed between selenium commands

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
                    selenium.waitForPageToLoad("30000");
                } else if (selenium.isElementPresent("link=Se connecter")) {
                    selenium.click("link=Se connecter");
                    selenium.waitForPageToLoad("30000");
                }
            }
        }
        //Create home blog page
        selenium.contextMenu("//span[@class='x-tree3-node-text']");
        selenium.click("link=New page");
        selenium.click("//html/body/div/div/div/div[2]/div[2]/div[2]/div[2]/div/div/div/div[2]/div/div/div[2]/div/form/fieldset/div/div/div[2]/div/div/div/div/img");
        selenium.typeKeys("j:templateNode", "blog-");
        try {
            selenium.clickAt("//div[@class='x-combo-list-item  x-view-highlightrow x-combo-selected']", "");
        } catch (Exception e) {

        }
        selenium.type("jcr:title", "my blog home");
        selenium.click("//html/body/div/div/div/div[2]/div[2]/div[2]/div[2]/div[2]/div/div/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[2]/td[2]/em/button");

        //Create a blog page in contribute mode
        selenium.open("http://localhost:8080/cms/contribute/default/en/sites/mySite/home/my-blog-home.html");
        selenium.type("jcr:title", "myFirstBlog");
        selenium.type("jcr:description", "my first Blog");
        selenium.click("//input[@value='create']");

        //Add post to this blog
        selenium.open("http://localhost:8080/cms/render/default/en/sites/mySite/home/my-blog-home/myfirstblog.blogNew.html");
        selenium.type("jcr:title", "my first post");
        selenium.click("//html/body/div/div[3]/div/div/div/div/div/div/div/form/div/p/span/span[2]/span/table/tbody/tr/td/div/span[4]/span[2]/span/a/span");
        selenium.type("//input[@class='cke_dialog_ui_input_text']", "www.jahia.org/cms");
        selenium.click("//a[@title='OK']");
        selenium.click("//input[@value='save']");

        //add comment to this post
        selenium.type("jcr:title", "my first comment");
        selenium.type("//textarea[@name='content']", "content of my first Comment");
        selenium.click("//input[@value='Submit']");
        deleteContentCreated();
    }

    public void deleteContentCreated(){
        selenium.open("/cms/edit/default/en/sites/mySite/home.html");
        selenium.waitForPageToLoad("10000");
        selenium.clickAt("//span[text()='my blog home']","0,0");
        selenium.contextMenuAt("//span[text()='my blog home']","0,0");
        selenium.click("link=Remove");
        if (selenium.isElementPresent("//button[text()='Yes']")){
            selenium.click("//button[text()='Yes']");
        }
        selenium.refresh();
    }
}
