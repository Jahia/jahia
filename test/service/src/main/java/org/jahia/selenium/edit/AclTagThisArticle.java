package org.jahia.selenium.edit;

import com.thoughtworks.selenium.Wait;
import com.thoughtworks.selenium.SeleneseTestCase;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

/**
 * Created by IntelliJ IDEA.
 * User: Dorth
 * Date: 22 sept. 2010
 * Time: 17:18:23
 * To change this template use File | Settings | File Templates.
 */
public class AclTagThisArticle extends SeleneseTestCase {
    private static Logger logger = Logger.getLogger(AclTagThisArticle.class);
    public String[] ids;
    public int tags = 10;
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "500";  //speed between selenium commands

    public void setUp() throws Exception {
        try {
            final JahiaSite mySite = ServicesRegistry.getInstance().getJahiaSitesService().getSite("localhostTest");
            if (mySite == null) {
                site = TestHelper.createSite(TESTSITE_NAME, "localhostTest", TestHelper.TEST_TEMPLATES);
                assertNotNull(site);
            } else {
                logger.warn("can't create mySite for running tests, because already exist...");
                logger.warn("your test(s) will become in few moment");
            }
        } catch (Exception e) {
            logger.warn("Exception during test setUp", e);
        }
        setUp("http://localhost:8080/jahia", "*firefox");
    }

    public void tearDown() throws Exception {
        try {
            // TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception e) {
            logger.warn("Exception during test tearDown", e);
        }
    }

    public void test() throws InterruptedException {
        selenium.setSpeed(TEST_SPEED);
        try {
            selenium.open("/jahia/cms/edit/default/en/sites/mySite/home.html");
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
    }

    public void CreateAnArticle() {
        //click on "Any content"
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Area : listA");
            }
        };
        ids = selenium.getEval(AnyContentButtons()).split(",");
        final String[] finalButtonIds = ids;
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent(finalButtonIds[1]);
            }
        };
        selenium.click(ids[1]);

        //doubleClick on "Editorial content"
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Editorial content");
            }
        };
        ids = selenium.getEval(TreeNodes()).split(",");
        selenium.doubleClick(ids[5]);

        //doubleClick on "Article"
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Article");
            }
        };
        ids = selenium.getEval(TreeNodes()).split(",");
        selenium.doubleClick(ids[6]);

        //fill Title
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Title");
            }
        };
        ids = selenium.getEval(TitleInput()).split(",");
        selenium.type(ids[0], "Acl Article Test");

        //click on "source"
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Source");
            }
        };
        ids = selenium.getEval(sourceButton()).split(",");
        selenium.waitForCondition("selenium.browserbot.getCurrentWindow().document.getElementById('" + ids[0] + "')", "10000");
        selenium.click(ids[0]);

        //Fill in source content
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Source");
            }
        };
        ids = selenium.getEval(sourceContent()).split(",");
        selenium.type("//td[@id='" + ids[0] + "']/textarea", "Acl Article body test");

        //click on "Save"
        new Wait("wait") {
            public boolean until() {
                return selenium.isTextPresent("Save");
            }
        };
        ids = selenium.getEval(saveButton()).split(",");
        selenium.click("//div[@id='" + ids[0] + "']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");
    }

    public void AclThisArticle() {
        for (int i = 0; i < 2; i++) {
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
            while (!selenium.isTextPresent("Edit acl-article-test")) {
                selenium.mouseOver("j:newTag"); //tips in order to click on a element in edit mode.
                selenium.doubleClick("j:newTag");
            }
            //click on Rights
            new Wait("wait") {
                public boolean until() {
                    return selenium.isElementPresent("link=Rights");
                }
            };
            selenium.click("link=Rights");
            if (i == 0) {
                new Wait("wait") {
                    public boolean until() {
                        return selenium.isElementPresent("gwt-uid-45");
                    }
                };
            } else {
                new Wait("wait") {
                    public boolean until() {
                        return selenium.isTextPresent("web-designer");
                    }
                };
            }
            //déselect or select all acl
            ids = selenium.getEval(getAllCheckBoxAcl()).split(",");
            for (String id : ids) {
                selenium.setSpeed("100");
                if (i == 0) {
                    //deselect
                    if (selenium.isChecked(id)) {
                        selenium.click(id);
                    }
                } else if (i == 1) {
                    //select
                    if (!selenium.isChecked(id)) {
                        selenium.click(id);
                    }
                } else {
                    //restore
                }
            }
            selenium.setSpeed(TEST_SPEED);
            //save
            ids = selenium.getEval(saveButton()).split(",");
            selenium.click("//div[@id='" + ids[0] + "']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");
        }
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
        while (!selenium.isTextPresent("Edit acl-article-test")) {
            selenium.mouseOver("j:newTag"); //tips in order to click on a element in edit mode.
            selenium.doubleClick("j:newTag");
        }

        //click on Tags
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("link=Tags");
            }
        };
        selenium.click("link=Tags");
        addTags(numberOfTags);
        ids = selenium.getEval(saveButton()).split(",");
        selenium.click("//div[@id='" + ids[0] + "']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");
    }

    public void addTags(int i) {
        final String[] addTag = selenium.getEval(getAddTagButton()).split(",");
        for (int j = 0; j < i; j++) {
            selenium.setSpeed("200");
            String Tag = "Tag" + j;
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Add Tag:");
                }
            };
            selenium.type("tagName", Tag);
            new Wait("wait") {
                public boolean until() {
                    return selenium.isElementPresent(addTag[3]);
                }
            };
            selenium.click(addTag[3]);
            Tag = "Tag";
        }
        selenium.setSpeed(TEST_SPEED);
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

    public String getAddTagButton() {
        String script = "var buttonId = new Array();";
        script += "var cnt = 0;";
        script += "var buttons = new Array();";
        script += "buttons = window.document.getElementsByTagName('table');";
        script += "for(var i=0; i<buttons.length; i++) {";
        script += "if(buttons[i].id !=null" +
                "&& buttons[i].getAttribute('class') == ' x-btn x-component x-btn-noicon') {";
        script += "buttonId[cnt]=buttons[i].id;" +
                "cnt ++;" +
                "}" +
                "}";
        script += "buttonId.toString();";
        return script;
    }

    public String AnyContentButtons() {
        String script = "var buttonId = new Array();";
        script += "var cnt = 0;";
        script += "var buttons = new Array();";
        script += "buttons = window.document.getElementsByTagName('button');";
        script += "for(var i=0; i<buttons.length; i++) {";
        script += "if(buttons[i].id !=null" +
                "&& buttons[i].getAttribute('class') == 'button-placeholder x-component') {";
        script += "buttonId[cnt]=buttons[i].id;" +
                "cnt ++;" +
                "}" +
                "}";
        script += "buttonId.toString();";
        return script;
    }

    public String TreeNodes() {
        String script = "var buttonId = new Array();";
        script += "var cnt = 0;";
        script += "var buttons = new Array();";
        script += "buttons = window.document.getElementsByTagName('div');";
        script += "for(var i=0; i<buttons.length; i++) {";
        script += "if(buttons[i].id !=null" +
                "&& buttons[i].getAttribute('class') == 'x-tree3-node') {";
        script += "buttonId[cnt]=buttons[i].id;" +
                "cnt ++;" +
                "}" +
                "}";
        script += "buttonId.toString();";
        return script;
    }

    public String TitleInput() {
        String script = "var buttonId = new Array();";
        script += "var cnt = 0;";
        script += "var buttons = new Array();";
        script += "buttons = window.document.getElementsByTagName('input');";
        script += "for(var i=0; i<buttons.length; i++) {";
        script += "if(buttons[i].id !=null" +
                "&& buttons[i].getAttribute('name') == 'jcr:title') {";
        script += "buttonId[cnt]=buttons[i].id;" +
                "cnt ++;" +
                "}" +
                "}";
        script += "buttonId.toString();";
        return script;
    }

    public String saveButton() {

        String script = "var buttonId = new Array();";
        script += "var cnt = 0;";
        script += "var buttons = new Array();";
        script += "buttons = window.document.getElementsByTagName('div');";
        script += "for(var i=0; i<buttons.length; i++) {";
        script += "if(buttons[i].id !=null" +
                "&& buttons[i].getAttribute('class') == ' x-small-editor x-panel-btns-center x-panel-fbar x-component x-toolbar-layout-ct') {";
        script += "buttonId[cnt]=buttons[i].id;" +
                "cnt ++;" +
                "}" +
                "}";
        script += "buttonId.toString();";
        return script;
    }

    public String sourceButton() {
        String script = "var buttonId = new Array();";
        script += "var cnt = 0;";
        script += "var buttons = new Array();";
        script += "buttons = window.document.getElementsByTagName('span');";
        script += "for(var i=0; i<buttons.length; i++) {";
        script += "if(buttons[i].id !=null" +
                "&& buttons[i].getAttribute('class') == 'cke_label') {";
        script += "buttonId[cnt]=buttons[i].id;" +
                "cnt ++;" +
                "}" +
                "}";
        script += "buttonId.toString();";
        return script;
    }

    public String sourceContent() {
        String script = "var buttonId = new Array();";
        script += "var cnt = 0;";
        script += "var buttons = new Array();";
        script += "buttons = window.document.getElementsByTagName('td');";
        script += "for(var i=0; i<buttons.length; i++) {";
        script += "if(buttons[i].id !=null" +
                "&& buttons[i].getAttribute('class') == 'cke_contents') {";
        script += "buttonId[cnt]=buttons[i].id;" +
                "cnt ++;" +
                "}" +
                "}";
        script += "buttonId.toString();";
        return script;
    }
}
