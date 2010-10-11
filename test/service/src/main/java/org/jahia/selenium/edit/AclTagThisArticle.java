package org.jahia.selenium.edit;

import com.thoughtworks.selenium.Wait;
import com.thoughtworks.selenium.SeleneseTestCase;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

import java.util.ArrayList;

/**
 * this test create an Article in edit, déselect all Acl on this and save, after that, select all Acl and Save, and to finish add add manys tags
 */
public class AclTagThisArticle extends SeleneseTestCase {
    private static Logger logger = Logger.getLogger(AclTagThisArticle.class);
    public int tags = 10;
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
        CreateAnArticle();
        AclThisArticle();
        TagThisArticle(tags);
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
                return selenium.isElementPresent("//img[@src='/modules/default/icons/jmix_editorialContent.png']");
            }
        };
        selenium.doubleClick("//img[@src='/modules/default/icons/jmix_editorialContent.png']");

        //doubleClick on "Article"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//img[@src='/modules/article/icons/jnt_article.png']");
            }
        };
        selenium.doubleClick("//img[@src='/modules/article/icons/jnt_article.png']");

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

    public void AclThisArticle() {
        // déselect all Acl
        String[] ids = getRights();
        selenium.setSpeed("100");
        for (String id : ids) {
            //deselect
            if (selenium.isChecked(id)) {
                defaultAcl.add(id);
                selenium.click(id);
            }
        }
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");

        //check if all acl are deselect
        ids = getRights();
        for (String id : ids) {
            if (selenium.isChecked(id)) {
                fail("Error: when check if all Acl are deselect");
            }
        }
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");


        //restore
        selenium.setSpeed(TEST_SPEED);
        getRights();
        int j = 1;
        while (selenium.isElementPresent("//div[@class=' x-panel-noborder x-panel x-component x-border-panel']/div[2]/div/div/div/div[2]/div[2]/div/div[2]/div[2]/div/div/div/div[2]/div/div[" + j + "]/table/tbody/tr/td[8]/div/div/table/tbody/tr[2]/td[2]/em/button")) {
            selenium.click("//div[@class=' x-panel-noborder x-panel x-component x-border-panel']/div[2]/div/div/div/div[2]/div[2]/div/div[2]/div[2]/div/div/div/div[2]/div/div[" + j + "]/table/tbody/tr/td[8]/div/div/table/tbody/tr[2]/td[2]/em/button");
            j++;
        }
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");

        //check if all acl are restored
        ids = getRights();
        for (String id : ids) {
            if (selenium.isChecked(id)) {
                defaultAclAfterRestore.add(id);
            }
        }
        if (defaultAcl.size() != defaultAclAfterRestore.size()) {
            fail("Error durring check default Acl are restore");
        }
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");

    }

    public void TagThisArticle(int numberOfTags) {
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
        addTags(numberOfTags);
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");
    }

    public void addTags(int i) {
        for (int j = 0; j < i; j++) {
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
    }

    public String[] getRights() {
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("j:newTag");
            }
        };
        selenium.mouseOver("j:newTag"); //tips in order to click on a element in edit mode.
        selenium.doubleClick("j:newTag");
        //click on Rights
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("link=Rights");
            }
        };
        selenium.click("link=Rights");
        String[] ids = selenium.getEval(getAllCheckBoxAcl()).split(",");
        return ids;
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

    public String getAllCheckBoxAcl() {
        String script = "var buttonId = new Array();";
        script += "var cnt = 0;";
        script += "var buttons = new Array();";
        script += "buttons = window.document.getElementsByTagName('input');";
        script += "for(var i=0; i<buttons.length; i++) {";
        script += "if(buttons[i].id !=null" +
                "&& buttons[i].getAttribute('type') == 'checkbox') {";
        script += "buttonId[cnt]=buttons[i].id;" +
                "cnt ++;" +
                "}" +
                "}";
        script += "buttonId.toString();";
        return script;
    }
}
