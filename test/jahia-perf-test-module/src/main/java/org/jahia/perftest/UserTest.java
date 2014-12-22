package org.jahia.perftest;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by toto on 08/12/14.
 */
@RunWith(Parameterized.class)
public class UserTest extends AbstractBenchmark {

    private static String siteKey = "usersTest";
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(UserTest.class);
    private String testName;
    private int nbOfUsers;
    private static ContentGenerator contentGenerator;

    private static UserUtils userUtils;

    public UserTest(String testName, int nbOfUsers) {
        this.testName = testName;
        this.nbOfUsers = nbOfUsers;
    }

    @Parameterized.Parameters(name="{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[] {"100",100}, new Object[] {"500",500} );
    }



    @BeforeClass
    public static void setUpClass() {
        contentGenerator = new ContentGenerator();
        contentGenerator.createSite(siteKey);
        userUtils = new UserUtils();
    }

    @AfterClass
    public static void tearDownClass() {
        try {
            JahiaSitesService.getInstance().removeSite(JahiaSitesService.getInstance().getSiteByKey(siteKey));
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void testSharedUsers() {
        contentGenerator.createUsers(null,nbOfUsers);
    }

    @Test
    public void testRemoveSharedUsers() {
        userUtils.removeUsers(ContentGenerator.USER_GROOVY_,null);
    }

    @Test
    public void testSiteUsers() {
        contentGenerator.createUsers(siteKey,nbOfUsers);
    }

    @Test
    public void testRemoveSiteUsers() {
        userUtils.removeUsers(ContentGenerator.USER_GROOVY_+siteKey,siteKey);
    }
}
