package org.jahia.selenium.admin;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;

/**
this integration test, create user in administration, in manage user
 */
public class CreateUsersTest extends SeleneseTestCase {
    private final String userName = "user";
    private final String passWord = "password";
    private final String login_username = "root";
    private final String login_password = "root1234";
    private final int numberOfUsers = 20;
    private final static String TEST_SPEED = "1500";

    public void setUp() throws Exception {
        setUp("http://localhost:8080", "*firefox");
        // for the test work you have to import the ACME demo and add a module
        // of user registration in the Home page
        // set the property to page return to "users" of the user registration module
        // you can also change the urls, and variable for run the test .
    }

    public void test() throws InterruptedException {
        selenium.setSpeed(TEST_SPEED); //speed between selenium commands
        selenium.open("/administration");
        new Wait("Couldn't find the login page!") {
            public boolean until() {
                return selenium.isElementPresent("login_username");
            }
        };
        if (selenium.isElementPresent("login_username") && selenium.isElementPresent("login_password")) {
            selenium.type("login_username", login_username);
            selenium.type("login_password", login_password);
            if (selenium.isElementPresent("link=Login")) {
                selenium.click("link=Login");
                selenium.waitForPageToLoad("30000");
            } else if (selenium.isElementPresent("link=Se connecter")) {
                selenium.click("link=Se connecter");
                selenium.waitForPageToLoad("30000");
            } else if (selenium.isElementPresent("link=S'authentifier")) {
                selenium.click("link=S'authentifier");
                selenium.waitForPageToLoad("30000");
            }
        }
        //switch language to English
        selenium.setSpeed("1500");
        if(selenium.getSelectedLabel("preferredLanguage").equals("français")){
            selenium.select("preferredLanguage","label=English");
            if(selenium.getSelectedLabel("preferredLanguage").equals("français")){
                selenium.click("link=Statut du serveur et du cache");
                selenium.click("flushAllCaches");
                selenium.click("link=Paramètres du serveur");
                selenium.select("preferredLanguage","label=English");
            }
        }
        selenium.setSpeed(TEST_SPEED);
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("link=Manage users");
            }
        };
        selenium.click("link=Manage users");
        for (int i = 1; i < numberOfUsers; i++) {
            final String userName = this.userName + i;
            String passWord = this.passWord + i;
            new Wait("wait") {
                public boolean until() {
                    return selenium.isElementPresent("link=Create new user") && selenium.isTextPresent("Create new user");
                }
            };
            selenium.click("link=Create new user");
            new Wait("wait") {
                public boolean until() {
                    return selenium.isElementPresent("username");
                }
            };
            selenium.type("username", userName);
            selenium.type("passwd", passWord);
            selenium.type("passwdconfirm", passWord);
            selenium.click("link=OK");
            new Wait("wait") {
                public boolean until() {
                    selenium.waitForPageToLoad("10000");
                    return selenium.isElementPresent("link=Sort by provider");
                }
            };

        }
    }
}
