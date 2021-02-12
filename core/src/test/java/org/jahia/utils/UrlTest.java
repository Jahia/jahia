/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2021 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.utils;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.Assert.*;
/**
 * Testing class for Url class
 */
public class UrlTest {

    @Test
    public void testEncodeUrlNullInput() {
        try {
            assertNull(Url.encodeUri(null, StandardCharsets.UTF_8.name()));
        } catch(Exception e) { fail(); }
    }

    @Test
    public void testEncodingNullInput() {
        try {
            assertEquals("/sites/FRP/home/check-eligibility.ServiceAction.do?eulId=50002950&zipCode=03446",
                    Url.encodeUri("/sites/FRP/home/check-eligibility.ServiceAction.do?&eulId=50002950&zipCode=03446",
                            null));
        } catch(Exception e) { fail(); }
    }

    @Test
    public void testEncodeUrlStandardPath() {
        try {
            assertEquals("/sites/FRP/home/check-eligibility.ServiceAction.do?eulId=50002950&zipCode=03446",
                    Url.encodeUri("/sites/FRP/home/check-eligibility.ServiceAction.do?&eulId=50002950&zipCode=03446",
                            StandardCharsets.UTF_8.name()));
        } catch(Exception e) { fail(); }
    }

    @Test
    public void testEncodeUrlWithWhiteSpace() {
        try {
            assertEquals("/sites/FRP/home/residential/offers/check-eligibility.ServiceAction.do?&eulId=50002950&zipCode=03446%20M",
                    Url.encodeUri("/sites/FRP/home/residential/offers/check-eligibility.ServiceAction.do?&eulId=50002950&zipCode=03446 M"
                            , StandardCharsets.UTF_8.name()));
        } catch(Exception e) { fail(); }
    }
}
