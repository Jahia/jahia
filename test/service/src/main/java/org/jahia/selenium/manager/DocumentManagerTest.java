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
 * Time: 2:22:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class DocumentManagerTest extends SeleneseTestCase {
    private static Logger logger = Logger.getLogger(CreateCategories.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "1500"; //speed between selenium commands

            private final int numberOfFolders = 10;

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
                    selenium.waitForPageToLoad("34000");
                } else if (selenium.isElementPresent("link=Se connecter")) {
                    selenium.click("link=Se connecter");
                    selenium.waitForPageToLoad("34000");
                }
            }
        }
          createNewFolder();

    }


    public void createNewFolder() {


        //click on "Managers"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//button[text()='Managers']");
            }
        };
        selenium.click("//button[text()='Managers']");

        //Click on "Document Manager"
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("Link=Document manager");
            }
        };
        selenium.click("Link=Document manager");

        //Select Window "Document manager"
        selenium.waitForPopUp("Document_manager", "3000");
        selenium.selectWindow("Document manager");


        //Create a new folder
        selenium.clickAt("//span[text()='files']", "5,5");
        selenium.mouseOver("//span[text()='files']");
        selenium.contextMenuAt("//span[text()='files']", "5,5");
        selenium.answerOnNextPrompt("My folder");
        selenium.click("Link=New directory");
        selenium.getPrompt();

        //Create new folders and under folders
        for (int i = 0; i < numberOfFolders; i++) {
            selenium.clickAt("//span[text()='My folder']", "5,5");
            selenium.mouseOver("//span[text()='My folder']");
            selenium.contextMenuAt("//span[text()='My folder']", "5,5");
            selenium.answerOnNextPrompt("folder" + i);
            selenium.click("Link=New directory");
            selenium.getPrompt();
            selenium.clickAt("//span[text()='folder" + i + "']", "5,5");
            selenium.mouseOver("//span[text()='folder" + i + "']");
            selenium.contextMenuAt("//span[text()='folder" + i + "']", "5,5");
            selenium.answerOnNextPrompt("under folder" + i);
            selenium.click("Link=New directory");
            selenium.getPrompt();
        }


        //upload documents
        selenium.clickAt("//span[text()='My folder']", "5,5");
        selenium.mouseOver("//span[text()='My folder']");
        selenium.contextMenuAt("//span[text()='My folder']", "5,5");
        selenium.click("Link=Upload");
        selenium.type("//input[@type='file']","/Users/sophiabatata/Downloads/paysage.jpg");
        selenium.click("//button[text()='OK']");

        //Download picture
        selenium.clickAt("//div[text()='paysage.jpg']","5,5");
        selenium.mouseOver("//div[text()='paysage.jpg']");
        selenium.contextMenuAt("//div[text()='paysage.jpg']", "5,5");
        selenium.click("Link=Download");
        if(!selenium.isElementPresent("Link=paysage.jpg"))
        {
            fail("There is no link to download paysage.jpg");
        }

        selenium.click("//div[@class=' x-nodrag x-tool-close x-tool x-component']");

        //Zip picture
        selenium.clickAt("//div[text()='paysage.jpg']","5,5");
        selenium.mouseOver("//div[text()='paysage.jpg']");
        selenium.contextMenuAt("//div[text()='paysage.jpg']", "5,5");
        selenium.answerOnNextPrompt("zipzipzip");
        selenium.click("Link=Zip");
        selenium.getPrompt();

        //Remove Zip
        deleteElement("//div[text()='zipzipzip.zip']");

        //Test Preview
        selenium.clickAt("//div[text()='paysage.jpg']","5,5");
        selenium.mouseOver("//div[text()='paysage.jpg']");
        selenium.contextMenuAt("//div[text()='paysage.jpg']", "5,5");
        selenium.click("Link=Preview");
        if(!selenium.isElementPresent("//img[@src='/files/default/sites/mySite/files/My folder/paysage.jpg']"))
        {
            fail("Impossible to display preview");
        }
        selenium.click("//div[@class=' x-nodrag x-tool-close x-tool x-component']");

        //Crop picture
        selenium.clickAt("//div[text()='paysage.jpg']","5,5");
        selenium.mouseOver("//div[text()='paysage.jpg']");
        selenium.contextMenuAt("//div[text()='paysage.jpg']", "5,5");
        selenium.click("Link=Crop image");
        selenium.type("//input[@name='newname']","crop picture.jpg");
        selenium.click("//button[text()='OK']");

        //Resize picture
        selenium.clickAt("//div[text()='paysage.jpg']","5,5");
        selenium.mouseOver("//div[text()='paysage.jpg']");
        selenium.contextMenuAt("//div[text()='paysage.jpg']", "5,5");
        selenium.click("Link=Resize");
        selenium.type("//input[@type='width']","400");
        selenium.click("//button[text()='OK']");



         //Rename the crop picture
        selenium.clickAt("//div[text()='crop picture.jpg']","5,5");
        selenium.mouseOver("//div[text()='crop picture.jpg']");
        selenium.contextMenuAt("//div[text()='crop picture.jpg']","5,5");
        selenium.answerOnNextPrompt("renamed");
        selenium.click("Link=Rename");
        selenium.getPrompt();

        //Copy renamed picture
        selenium.clickAt("//div[text()='paysage.jpg']","5,5");
        selenium.mouseOver("//div[text()='paysage.jpg']");
        selenium.contextMenuAt("//div[text()='paysage.jpg']","5,5");
        selenium.click("Link=Copy");

        //Paste on files
        selenium.clickAt("//span[text()='files']", "5,5");
        selenium.mouseOver("//span[text()='files']");
        selenium.contextMenuAt("//span[text()='files']", "5,5");
        selenium.click("Link=Paste");

        selenium.clickAt("//span[text()='My folder']", "5,5");

        //Cut renamed page
        selenium.clickAt("//div[text()='renamed']","5,5");
        selenium.mouseOver("//div[text()='renamed']");
        selenium.contextMenuAt("//div[text()='renamed']","5,5");
        selenium.click("Link=Cut");

        //Paste on files
        selenium.clickAt("//span[text()='files']", "5,5");
        selenium.mouseOver("//span[text()='files']");
        selenium.contextMenuAt("//span[text()='files']", "5,5");
        selenium.click("Link=Paste");


        deleteElement("//span[text()='My folder']");



    }
          public void deleteElement(String element) {
        selenium.clickAt(element, "5,5");
        selenium.mouseOver(element);
        selenium.contextMenuAt(element, "5,5");
        selenium.click("Link=Remove");
        if (selenium.isElementPresent("//button[text()='Yes']")) {
            selenium.click("//button[text()='Yes']");
        }
    }
}
