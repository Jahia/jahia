package org.jahia.selenium.manager;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;
import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

/**
 * Test all right clic on the document manager
 */
public class DocumentManager extends SeleneseTestCase {
    private static Logger logger = Logger.getLogger(DocumentManager.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "mySite";
    private final static String TEST_SPEED = "3000"; //speed between selenium commands
    private final static String path = "/home/bamboo/doc_selenium_test/";

    private final int numberOfFolders = 10;

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
        documentManager();

    }

    public void  documentManager() {


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
        if (selenium.isElementPresent("//div[text()='My folder']")) {
            deleteElement("//div[text()='My folder']");
        }

        if (selenium.isElementPresent("//div[text()='My second folder']")) {
            deleteElement("//div[text()='My second folder']");
        }


        selenium.mouseOver("//span[text()='files']");
        selenium.contextMenuAt("//span[text()='files']", "5,5");
        selenium.answerOnNextPrompt("My folder");
        selenium.click("Link=New directory");
        selenium.getPrompt();

        if (!selenium.isElementPresent("//span[text()='My folder']")) {
            fail("Creation of folder by right click on the left panel fails");
        }

        selenium.mouseOver("//html/body/div/div/div/div[2]/div[2]/div/div[2]/div[2]/div/div/div/div[2]");
        selenium.contextMenuAt("//html/body/div/div/div/div[2]/div[2]/div/div[2]/div[2]/div/div/div/div[2]", "5,5");
        selenium.answerOnNextPrompt("My second folder");
        selenium.click("Link=New directory");
        selenium.getPrompt();

        if (!selenium.isElementPresent("//div[text()='My second folder']")) {
            fail("Creation of folder by right click on the central panel fails");
        }

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


        //upload picture
        selenium.clickAt("//span[text()='My folder']", "5,5");
        selenium.mouseOver("//span[text()='My folder']");
        selenium.contextMenuAt("//span[text()='My folder']", "5,5");
        selenium.click("Link=Upload");
        selenium.type("//input[@type='file']", path + "paysage.jpg");
        selenium.click("//button[text()='OK']");

        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[text()='paysage.jpg']");
            }
        };
        deleteElement("//div[text()='paysage.jpg']");

        //upload picture
        selenium.clickAt("//html/body/div/div/div/div[2]/div[2]/div/div[2]/div[2]/div/div/div/div[2]", "5,5");
        selenium.mouseOver("//html/body/div/div/div/div[2]/div[2]/div/div[2]/div[2]/div/div/div/div[2]");
        selenium.contextMenuAt("//html/body/div/div/div/div[2]/div[2]/div/div[2]/div[2]/div/div/div/div[2]", "5,5");
        selenium.click("Link=Upload");
        selenium.type("//input[@type='file']", path + "paysage.jpg");
        selenium.click("//button[text()='OK']");

        if (!selenium.isElementPresent("//div[text()='paysage.jpg']")) {
            fail("Upload of image fails (by right click on left panel)");
        }


        //upload docx
        selenium.clickAt("//span[text()='My folder']", "5,5");
        selenium.mouseOver("//span[text()='My folder']");
        selenium.contextMenuAt("//span[text()='My folder']", "5,5");
        selenium.click("Link=Upload");
        selenium.type("//input[@type='file']", path + "doc.docx");
        selenium.click("//button[text()='OK']");

        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[text()='doc.docx']");
            }
        };


        //upload doc
        selenium.clickAt("//span[text()='My folder']", "5,5");
        selenium.mouseOver("//span[text()='My folder']");
        selenium.contextMenuAt("//span[text()='My folder']", "5,5");
        selenium.click("Link=Upload");
        selenium.type("//input[@type='file']", path + "doc.doc");
        selenium.click("//button[text()='OK']");


        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[text()='doc.doc']");
            }
        };

        //Upload PDF
        selenium.clickAt("//span[text()='My folder']", "5,5");
        selenium.mouseOver("//span[text()='My folder']");
        selenium.contextMenuAt("//span[text()='My folder']", "5,5");
        selenium.click("Link=Upload");
        selenium.type("//input[@type='file']", path + "pdf.pdf");
        selenium.click("//button[text()='OK']");

        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[text()='pdf.pdf']");
            }
        };


        //Download picture
        selenium.clickAt("//div[text()='paysage.jpg']", "5,5");
        selenium.mouseOver("//div[text()='paysage.jpg']");
        selenium.contextMenuAt("//div[text()='paysage.jpg']", "5,5");
        selenium.click("Link=Download");
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("Link=paysage.jpg");
            }
        };

        selenium.click("//div[@class=' x-nodrag x-tool-close x-tool x-component']");

        //Zip picture
        selenium.clickAt("//div[text()='paysage.jpg']", "5,5");
        selenium.mouseOver("//div[text()='paysage.jpg']");
        selenium.contextMenuAt("//div[text()='paysage.jpg']", "5,5");
        selenium.answerOnNextPrompt("zipzipzip");
        selenium.click("Link=Zip");
        selenium.getPrompt();

        //Remove Zip
        deleteElement("//div[text()='zipzipzip.zip']");

        //Test Preview
        selenium.clickAt("//div[text()='paysage.jpg']", "5,5");
        selenium.mouseOver("//div[text()='paysage.jpg']");
        selenium.contextMenuAt("//div[text()='paysage.jpg']", "5,5");
        selenium.click("Link=Preview");

        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//img[@src='/files/default/sites/mySite/files/My folder/paysage.jpg']");
            }
        };


        selenium.click("//div[@class=' x-nodrag x-tool-close x-tool x-component']");

        //Crop picture
        selenium.clickAt("//div[text()='paysage.jpg']", "5,5");
        selenium.mouseOver("//div[text()='paysage.jpg']");
        selenium.contextMenuAt("//div[text()='paysage.jpg']", "5,5");
        selenium.click("Link=Crop image");
        selenium.mouseDownAt("//div[@class='jcrop-holder']", "5,5");
        selenium.mouseUpAt("//div[@class='jcrop-holder']", "30,30");
        selenium.type("//input[@name='newname']", "crop picture.jpg");
        selenium.click("//button[text()='Cancel']");

        //Resize picture
        selenium.clickAt("//div[text()='paysage.jpg']", "5,5");
        selenium.mouseOver("//div[text()='paysage.jpg']");
        selenium.contextMenuAt("//div[text()='paysage.jpg']", "5,5");
        selenium.click("Link=Resize");
        selenium.type("//input[@name='width']", "400");
        selenium.click("//button[text()='OK']");

        //Rotate picture
        selenium.clickAt("//div[text()='paysage.jpg']", "5,5");
        selenium.mouseOver("//div[text()='paysage.jpg']");
        selenium.contextMenuAt("//div[text()='paysage.jpg']", "5,5");
        selenium.click("Link=Rotate");
        selenium.type("//input[@name='newname']", "paysage_rotate.jpg");
        selenium.click("//button[text()='Rotate left']");

        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("//div[text()='paysage_rotate.jpg']");
            }
        };


        //Rename the crop picture
        selenium.clickAt("//div[text()='pdf.pdf']", "5,5");
        selenium.mouseOver("//div[text()='pdf.pdf']");
        selenium.contextMenuAt("//div[text()='pdf.pdf']", "5,5");
        selenium.answerOnNextPrompt("renamed");
        selenium.click("Link=Rename");
        selenium.getPrompt();

        //Copy paysage picture
        selenium.clickAt("//div[text()='paysage.jpg']", "5,5");
        selenium.mouseOver("//div[text()='paysage.jpg']");
        selenium.contextMenuAt("//div[text()='paysage.jpg']", "5,5");
        selenium.click("Link=Copy");

        //Paste on My second folder
        selenium.clickAt("//span[text()='My second folder']", "5,5");
        selenium.mouseOver("//span[text()='My second folder']");
        selenium.contextMenuAt("//span[text()='My second folder']", "5,5");
        selenium.click("Link=Paste");

        selenium.clickAt("//span[text()='My folder']", "5,5");

        //Cut renamed page
        selenium.clickAt("//div[text()='renamed']", "5,5");
        selenium.mouseOver("//div[text()='renamed']");
        selenium.contextMenuAt("//div[text()='renamed']", "5,5");
        selenium.click("Link=Cut");

        //Paste on My second folder
        selenium.clickAt("//span[text()='My second folder']", "5,5");
        selenium.mouseOver("//span[text()='My second folder']");
        selenium.contextMenuAt("//span[text()='My second folder']", "5,5");
        selenium.click("Link=Paste");


        deleteElement("//span[text()='My folder']");
        deleteElement("//span[text()='My second folder']");


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
