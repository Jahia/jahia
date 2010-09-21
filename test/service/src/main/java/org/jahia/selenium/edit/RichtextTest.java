package org.jahia.selenium.edit;

import org.apache.log4j.Logger;
import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

/* YOU NEED A SERVER SELENIUM UP ON YOUR SYSTEM */
public class RichtextTest extends SeleneseTestCase {

    private static Logger logger = Logger.getLogger(RichtextTest.class);
    private String[] ids;
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static int numberOfNodes = 10;

    /*  protected DefaultSelenium createSeleniumClient(String url) throws Exception {
    return new DefaultSelenium("localhost", 4444, "*firefox", url);
}    */

    public void setUp() throws Exception {
        try {
            final JahiaSite mySite = ServicesRegistry.getInstance().getJahiaSitesService().getSite("localhostTest");
            if (mySite == null) {
                site = TestHelper.createSite(TESTSITE_NAME, "localhostTest", TestHelper.INTRANET_TEMPLATES);
                assertNotNull(site);
            } else {
                logger.warn("can't create mySite for running tests, because already exist...");
                logger.warn("your test(s) will become in few moment");
            }
        } catch (Exception e) {
            logger.warn("Exception during test setUp", e);
        }
        setUp("http://localhost:8080/", "*firefox");
    }

    @Override
    public void tearDown() throws Exception {
       try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception e) {
            logger.warn("Exception during test tearDown", e);
        }
    }

    public void test() throws Exception {
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
        for (int i = 1; i < numberOfNodes; i++) {
            //click on "Any content"
            new Wait("wait") {
                    public boolean until() {
                        return selenium.isTextPresent("Any content");
                    }
                };
            while(!selenium.isTextPresent("Editorial content")){
                ids = selenium.getEval(GwtButtons()).split(",");
                selenium.click(ids[1]);
            }

            //click on the image for sort"
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Editorial content");
                }
            };
            ids = selenium.getEval(sortImg()).split(",");
            selenium.click(ids[0]);

            //Double click on "Editorial content"
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Editorial content");
                }
            };
            ids = selenium.getEval(TreeNodes()).split(",");
            selenium.doubleClick(ids[10]);

            //Double click on "Rich Text"
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Rich text");
                }
            };
            ids = selenium.getEval(TreeNodes()).split(",");
            selenium.doubleClick(ids[18]);

            //Click on "source"
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
            selenium.type("//td[@id='" + ids[0] + "']/textarea", "<h2>Rich Text Example " + i + "</h2><p>BlaBlaBlaaa ... BlaBlaBlaaa ... BlaBlaBlaaa ... BlaBlaBlaaa ... BlaBlaBlaaa ... BlaBlaBlaaa ... BlaBlaBlaaa ... BlaBlaBlaaa ... </p>");

            //click on "Save"
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Save");
                }
            };
            ids = selenium.getEval(saveButton()).split(",");
            selenium.click("//div[@id='" + ids[0] + "']/table/tbody/tr/td[1]/table/tbody/tr/td[1]/table");
        }
    }

    public String GwtButtons() {
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

    public String sortImg() {
        String script = "var buttonId = new Array();";
        script += "var cnt = 0;";
        script += "var buttons = new Array();";
        script += "buttons = window.document.getElementsByTagName('div');";
        script += "for(var i=0; i<buttons.length; i++) {";
        script += "if(buttons[i].id !=null" +
                "&& buttons[i].getAttribute('class') == ' x-grid3-hd-inner x-grid3-hd-label x-component sort-asc ') {";
        script += "buttonId[cnt]=buttons[i].id;" +
                "cnt ++;" +
                "}" +
                "}";
        script += "buttonId.toString();";
        return script;
    }
}
