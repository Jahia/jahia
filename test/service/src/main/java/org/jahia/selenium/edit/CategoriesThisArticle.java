package org.jahia.selenium.edit;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

import java.util.ArrayList;

/**
 *
 * - create an article
 * - reopen the engine
 * - add categories
 * - save
 * - reopen the engine
 * - remove all categories
 * - save
 * - reopen the engine
 * - verify if all categories are removes
 * - delete the article
 *
 */
public class CategoriesThisArticle extends SeleneseTestCase {
    private static Logger logger = Logger.getLogger(CategoriesThisArticle.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "3000";  //speed between selenium commands
    private ArrayList<String> categoriesSaved = new ArrayList<String>();
    private ArrayList<String> categoriesAfterSaved = new ArrayList<String>();

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
        CreateAnArticle();
        addCategories();
        checkAndRemoveCategoriesSaved();
        checkRemovedCategories();
        deleteContentCreated();
    }

    public void CreateAnArticle() {
        //click on "Any content"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[2]/div[2]/div/div/div/div/div/div/div/button");
            }
        };
        selenium.click("//div[2]/div[2]/div/div/div/div/div/div/div/button");

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

    public void addCategories() {
        // wait article pop in the page, on double click on it
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Acl Article body test");
            }
        };
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[@class='intro']");
            }
        };
        selenium.mouseOver("//div[@class='intro']"); //tips in order to click on a element in edit mode.
        selenium.doubleClick("//div[@class='intro']");

        //click on Categories
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("link=Categories");
            }
        };
        selenium.click("link=Categories");

        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//span[text()='categories']");
            }
        };
        selenium.doubleClick("//span[text()='categories']");
        selenium.setSpeed("5000");
        String[] addButtons = selenium.getEval(getAllAddButtons()).split(",");
        selenium.setSpeed(TEST_SPEED);
        for (String s : addButtons) {
            if (selenium.isElementPresent("//div[@id='" + s + "']/table/tbody/tr/td[2]/div/table/tbody/tr[2]/td[2]/em/button")) {
                selenium.click("//div[@id='" + s + "']/table/tbody/tr/td[2]/div/table/tbody/tr[2]/td[2]/em/button");
                categoriesSaved.add(s);
            }
        }

        //click on "Save"
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Save");
            }
        };
        selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");
    }

    public void checkAndRemoveCategoriesSaved() {
        // wait article pop in the page, on double click on it
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Acl Article body test");
            }
        };
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[@class='intro']");
            }
        };
        selenium.mouseOver("//div[@class='intro']"); //tips in order to click on a element in edit mode.
        selenium.doubleClick("//div[@class='intro']");

        //click on Categories
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("link=Categories");
            }
        };
        selenium.click("link=Categories");

        while (selenium.isElementPresent("//button[text()='Remove']")) {
            categoriesAfterSaved.add("present");
            selenium.click("//button[text()='Remove']");
        }
        if (categoriesSaved.size() != categoriesAfterSaved.size()) {
            fail("error, the number of categories saved the first time wrong");
        } else {
            //click on "Save"
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Save");
                }
            };
            selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");
        }
    }

    public void checkRemovedCategories() {
        // wait article pop in the page, on double click on it
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Acl Article body test");
            }
        };
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[@class='intro']");
            }
        };
        selenium.mouseOver("//div[@class='intro']"); //tips in order to click on a element in edit mode.
        selenium.doubleClick("//div[@class='intro']");

        //click on Categories
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("link=Categories");
            }
        };
        selenium.click("link=Categories");

        if (selenium.isElementPresent("//button[text()='Remove']")) {
            fail("error, when save after delete categories, one or more are already set");
        } else {
            //click on "Save"
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Save");
                }
            };
            selenium.click("//div[@class=' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");
        }
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

    public String getAllAddButtons() {
        String script = "var buttonId = new Array();";
        script += "var cnt = 0;";
        script += "var buttons = new Array();";
        script += "buttons = window.document.getElementsByTagName('div');";
        script += "for(var i=0; i<buttons.length; i++) {";
        script += "if(buttons[i].id !=null" +
                "&& buttons[i].getAttribute('class') == 'x-grid3-row  x-unselectable-single') {";
        script += "buttonId[cnt]=buttons[i].id;" +
                "cnt ++;" +
                "}" +
                "}";
        script += "buttonId.toString();";
        return script;
    }
}
