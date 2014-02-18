package org.jahia.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

/**
 * Unit test for the LanguageCodeConverters class.
 */
public class LanguageCodeConvertersTest {

    @Test
    public void testLanguageCodeValidity() {
        String[] validLanguageCodeArray = {
                "en",
                "de_DE",
                "_GB",
                "en_US_WIN",
                "de__POSIX",
                "zh_CN_#Hans",
                "zh_TW_#Hant-x-java",
                "th_TH_TH_#u-nu-thai"
        };
        System.out.println("Using pattern: " + LanguageCodeConverters.JAVA7_LOCALE_TOSTRING);
        for (String languageCode : validLanguageCodeArray) {
            Assert.assertTrue("Language code ["+languageCode +"] should be valid", LanguageCodeConverters.isValidLanguageCode(languageCode));
            Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
            Assert.assertEquals("Original language code and converted one are not equal", languageCode, locale.toString());
        }

        Assert.assertFalse("LanguageCode [en'] should not be valid !", LanguageCodeConverters.isValidLanguageCode("en'"));
        Assert.assertFalse("LanguageCode [en\"] should not be valid !", LanguageCodeConverters.isValidLanguageCode("en\""));
    }

}
