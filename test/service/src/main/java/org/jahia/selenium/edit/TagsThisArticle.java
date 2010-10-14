package org.jahia.selenium.edit;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

/**
 * Created by IntelliJ IDEA.
 * User: Dorth
 * Date: 14 oct. 2010
 * Time: 14:36:01
 * To change this template use File | Settings | File Templates.
 */
public class TagsThisArticle extends SeleneseTestCase {
    private static Logger logger = Logger.getLogger(TagsThisArticle.class);
    public int tags = 10;
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "3000";  //speed between selenium commands

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
        deleteContentCreated();
        CreateAnArticle();
        TagsArticle();
        checkAndRemoveTags();
        checkTagsRemoved();
        deleteContentCreated();
    }

    public void CreateAnArticle() {
        //click on "Any content"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[@path='/sites/mySite/home/listA']/div/div[2]/div/div/div/div/table/tbody/tr/td[2]/button");
            }
        };
        selenium.click("//div[@path='/sites/mySite/home/listA']/div/div[2]/div/div/div/div/table/tbody/tr/td[2]/button");

        //doubleClick on "Editorial content"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[text()='Editorial content']");
            }
        };
        selenium.doubleClick("//div[text()='Editorial content']");

        //doubleClick on "Article"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[text()='Article (title and introduction)']");
            }
        };
        selenium.doubleClick("//div[text()='Article (title and introduction)']");

        //fill Title
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Title");
            }
        };
        selenium.type("//input[@name='jcr:title']", "Acl Article Test");

        //click on "source"
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
        selenium.type("//textarea[@class='cke_source cke_enable_context_menu']", "Acl Article body test");

        //click on "Save"
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Save");
            }
        };
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");
    }

    public void TagsArticle() {
        // wait article pop in the page, on double click on it
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Acl Article body test");
            }
        };
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Add tags");
            }
        };
        selenium.mouseOver("j:newTag"); //tips in order to click on a element in edit mode.
        selenium.doubleClick("j:newTag");

        //click on Tags
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("link=Tags");
            }
        };
        selenium.click("link=Tags");

        //add Tags
        for (int j = 0; j < tags; j++) {
            selenium.setSpeed("200");
            String Tag = "Tag" + j;
            selenium.type("tagName", Tag);
            new Wait("wait") {
                public boolean until() {
                    return selenium.isElementPresent("//table[@class='x-toolbar-right-ct']/tbody/tr/td[1]/table/tbody/tr/td[3]/table/tbody");
                }
            };
            selenium.click("//table[@class='x-toolbar-right-ct']/tbody/tr/td[1]/table/tbody/tr/td[3]/table/tbody");
            Tag = "Tag";
        }
        selenium.setSpeed(TEST_SPEED);

        //click on "Save"
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Save");
            }
        };
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");
    }

    public void checkAndRemoveTags(){
        // wait article pop in the page, on double click on it
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Acl Article body test");
            }
        };
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Add tags");
            }
        };
        selenium.mouseOver("j:newTag"); //tips in order to click on a element in edit mode.
        selenium.doubleClick("j:newTag");

        //click on Tags
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("link=Tags");
            }
        };
        selenium.click("link=Tags");

        //check Tags are correctly saved
        for (int j = 0; j < tags; j++) {
            selenium.setSpeed("200");
            String Tag = "Tag" + j;
            if(!selenium.isTextPresent(Tag)){
                fail("Error during check if all tags are correctly saved");
            }
            Tag = "Tag";
        }
        selenium.setSpeed(TEST_SPEED);

        //Remove All Tags
        while(selenium.isElementPresent("//button[text()='Remove']")){
            selenium.click("//button[text()='Remove']");
        }

       //click on "Save"
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Save");
            }
        };
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");
    }

    public void checkTagsRemoved(){
        // wait article pop in the page, on double click on it
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Acl Article body test");
            }
        };
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Add tags");
            }
        };
        selenium.mouseOver("j:newTag"); //tips in order to click on a element in edit mode.
        selenium.doubleClick("j:newTag");

        //click on Tags
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("link=Tags");
            }
        };
        selenium.click("link=Tags");
        
        //check Tags are correctly removed
        if(selenium.isElementPresent("//button[text()='Remove']")){
            fail("error when check if Tags are correctly removed");
        }

       //click on "Save"
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Save");
            }
        };
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");
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
