package org.jahia.perftest;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import org.jahia.registries.ServicesRegistry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by toto on 08/12/14.
 */
@RunWith(Parameterized.class)
public class TaggingTest extends AbstractBenchmark {

    private static String siteKey = "test";

    private TaggingUtils taggingUtils = new TaggingUtils();

    private String testName;
    private int nbOfTags;
    private int nbContentToTag;
    private int tagsPerContent;

    public TaggingTest(String testName, int nbOfTags, int nbContentToTag, int tagsPerContent) {
        this.testName = testName;
        this.nbOfTags = nbOfTags;
        this.nbContentToTag = nbContentToTag;
        this.tagsPerContent = tagsPerContent;
    }

    @Parameterized.Parameters(name="{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[] {"10x10x10",10, 10, 10}, new Object[] {"10x20x10",10, 20, 10} );
    }



    @BeforeClass
    public static void setUpClass() {
        ContentGenerator cg = new ContentGenerator();
        if ((Boolean) cg.createSite(siteKey)) {
            cg.generateContent(siteKey, 1, 1, 1, 1, 1, false, false);
        }
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void testTagContent() {
        taggingUtils.tagContent(siteKey, taggingUtils.tagSubset(0,nbOfTags), nbContentToTag, tagsPerContent);
    }

    @Test
    public void testTagRenaming() {
        taggingUtils.tagRenaming(siteKey, taggingUtils.tagSubset(0,nbOfTags), 1000);
    }

    @Test
    public void testTagDeletion() {
        taggingUtils.tagRenaming(siteKey, taggingUtils.tagSubset(0,nbOfTags), 1000);
    }
}
