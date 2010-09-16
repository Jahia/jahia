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
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";

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
        for (int i = 0; i < 15; i++) {
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Any content");
                }
            };
            String[] buttonIds = selenium.getEval(GwtButtons()).split(",");
            final String[] finalButtonIds = buttonIds;
            new Wait("wait") {
                public boolean until() {
                    return selenium.isElementPresent(finalButtonIds[1]);
                }
            };
            selenium.click(buttonIds[1]);

            //wait for the text "Editorial content appear" it's usefull to wait the pop of the content's choice
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Editorial content");
                }
            };
            //clic on the sort by img
            buttonIds = selenium.getEval(sortImg()).split(",");
            selenium.click(buttonIds[0]);

            //wait after the sort by
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Editorial content");
                }
            };
            //clic on this button
            buttonIds = selenium.getEval(TreeNodes()).split(",");
            selenium.click("//div[@id='" + buttonIds[10] + "']/div/table/tbody/tr/td[3]/img");

            //new wait for the text "Rich text"
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Rich text");
                }
            };
            //clic on Rich text
            buttonIds = selenium.getEval(LabelComponent()).split(",");
            selenium.doubleClick("//div[@id='" + buttonIds[18] + "']/div/table/tbody/tr/td[3]/img");

            //wait for the properties of rich text pop
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Source");
                }
            };
            //clic on "source"  button
            buttonIds = selenium.getEval(sourceButton()).split(",");
            selenium.waitForCondition("selenium.browserbot.getCurrentWindow().document.getElementById('" + buttonIds[0] + "')", "10000");
            selenium.click(buttonIds[0]);

            //wait for write into source
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Source");
                }
            };
            buttonIds = selenium.getEval(sourceContent()).split(",");
            selenium.type("//td[@id='" + buttonIds[0] + "']/textarea", "<p>\\n\t<br />\\n\tJ&#39;ai essay&eacute; avec une autre fonction sp&eacute;cial pour firefox vu la struture du DOM avec mozilla mais ca ne marche pas.<br />\\n\t<br />\\n\tCe qui m&#39;&eacute;tonne le plus dans l&#39;affichage du alert dans firefox c&#39;est le fait qu&#39;il m&#39;affiche : object HTMLImageElement, object HTMLMapElement, object Text, object HTMLMapElement, ...<br />\\n\t<br />\\n\tA chaque clic il me recupere l&#39;arborescence petit a petit (car dans le div 2 il ya une image, une def de map, un espace, une autre def de map, ...) mais ne m&#39;affiche rien dans le div1 <img alt=\"\" border=\"0\" class=\"inlineimg\" src=\"http://www.developpez.net/forums/images/smilies/icon_sad.gif\" title=\":(\" /></p> ");

            //wait for save button is ready
            new Wait("wait") {
                public boolean until() {
                    return selenium.isTextPresent("Save");
                }
            };
            buttonIds = selenium.getEval(saveButton()).split(",");
            selenium.click("//table[@id='" + buttonIds[31] + "']/tbody/tr[2]/td[2]/em/button");

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

    public String LabelComponent() {
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
        script += "buttons = window.document.getElementsByTagName('table');";
        script += "for(var i=0; i<buttons.length; i++) {";
        script += "if(buttons[i].id !=null" +
                ") {";
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
