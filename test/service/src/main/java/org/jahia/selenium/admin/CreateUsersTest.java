package org.jahia.selenium.admin;

import com.thoughtworks.selenium.SeleneseTestCase;
import com.thoughtworks.selenium.Wait;

/**
 * Created by IntelliJ IDEA.
 * User: Dorth
 * Date: 19 août 2010
 * Time: 10:30:39
 * To change this template use File | Settings | File Templates.
 */
public class CreateUsersTest extends SeleneseTestCase {
    private final String userName = "user";
    private final String passWord = "password";
    private final String login_username = "root";
    private final String login_password = "root1234";

    public void setUp() throws Exception {
        setUp("http://localhost:8080/jahia", "*firefox");
        // for the test work you have to import the ACME demo and add a module
        // of user registration in the Home page
        // set the property to page return to "users" of the user registration module
        // you can also change the urls, and variable for run the test .
    }

    public void test() throws InterruptedException {
        selenium.open("/jahia/administration");
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
        new Wait("wait") {
            public boolean until() {
                return selenium.isElementPresent("link=Manage users");
            }
        };
        selenium.click("link=Manage users");
        for (int i = 1; i < 200; i++) {
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
