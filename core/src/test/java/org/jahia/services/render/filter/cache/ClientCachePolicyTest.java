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
 *     Copyright (C) 2002-2025 Jahia Solutions Group. All rights reserved.
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
package org.jahia.services.render.filter.cache;

import org.jahia.test.framework.AbstractJUnitTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Jerome Blanchard
 */
public class ClientCachePolicyTest {

    @Test
    public void testClientCachePolicy() {
        ClientCachePolicy publicPolicy = new ClientCachePolicy(ClientCachePolicy.Level.PUBLIC, 3600);
        assertEquals(ClientCachePolicy.Level.PUBLIC, publicPolicy.getLevel());
        assertEquals(3600, publicPolicy.getTtl());

        ClientCachePolicy privatePolicy = new ClientCachePolicy(ClientCachePolicy.Level.PRIVATE);
        assertEquals(ClientCachePolicy.Level.PRIVATE, privatePolicy.getLevel());
        assertEquals(0, privatePolicy.getTtl());

        ClientCachePolicy customPolicy1 = new ClientCachePolicy(ClientCachePolicy.Level.CUSTOM, 300);
        assertEquals(ClientCachePolicy.Level.CUSTOM, customPolicy1.getLevel());
        assertEquals(300, customPolicy1.getTtl());

        ClientCachePolicy customPolicy2 = new ClientCachePolicy(ClientCachePolicy.Level.CUSTOM, 1800);
        assertEquals(ClientCachePolicy.Level.CUSTOM, customPolicy2.getLevel());
        assertEquals(1800, customPolicy2.getTtl());

        ClientCachePolicy immutablePolicy = new ClientCachePolicy(ClientCachePolicy.Level.IMMUTABLE);
        assertEquals(ClientCachePolicy.Level.IMMUTABLE, immutablePolicy.getLevel());
        assertEquals(0, immutablePolicy.getTtl());

        assertTrue(privatePolicy.isStronger(customPolicy1));
        assertTrue(privatePolicy.isStronger(customPolicy2));
        assertTrue(privatePolicy.isStronger(publicPolicy));
        assertTrue(privatePolicy.isStronger(immutablePolicy));

        assertFalse(customPolicy1.isStronger(privatePolicy));
        assertTrue(customPolicy1.isStronger(customPolicy2));
        assertTrue(customPolicy1.isStronger(publicPolicy));
        assertTrue(customPolicy1.isStronger(immutablePolicy));

        assertFalse(customPolicy2.isStronger(privatePolicy));
        assertFalse(customPolicy2.isStronger(customPolicy1));
        assertTrue(customPolicy2.isStronger(publicPolicy));
        assertTrue(customPolicy2.isStronger(immutablePolicy));

        assertFalse(publicPolicy.isStronger(privatePolicy));
        assertFalse(publicPolicy.isStronger(customPolicy1));
        assertFalse(publicPolicy.isStronger(customPolicy2));
        assertTrue(publicPolicy.isStronger(immutablePolicy));

        assertFalse(immutablePolicy.isStronger(privatePolicy));
        assertFalse(immutablePolicy.isStronger(customPolicy1));
        assertFalse(immutablePolicy.isStronger(customPolicy2));
        assertFalse(immutablePolicy.isStronger(publicPolicy));

    }

    @Test
    public void testClientCachePolicyStrongest() {
        ClientCachePolicy publicPolicy = new ClientCachePolicy(ClientCachePolicy.Level.PUBLIC, 3600);
        ClientCachePolicy privatePolicy = new ClientCachePolicy(ClientCachePolicy.Level.PRIVATE);
        ClientCachePolicy immutablePolicy = new ClientCachePolicy(ClientCachePolicy.Level.IMMUTABLE);

        assertEquals(privatePolicy, ClientCachePolicy.strongest(publicPolicy, privatePolicy));
        assertEquals(publicPolicy, ClientCachePolicy.strongest(publicPolicy, immutablePolicy));
        assertEquals(privatePolicy, ClientCachePolicy.strongest(immutablePolicy, privatePolicy));

        assertEquals(privatePolicy, ClientCachePolicy.strongest(List.of(publicPolicy, privatePolicy, immutablePolicy)));
    }

    @Test
    public void testClientCachePolicyReduce() {
        PolicyHolder publicHolder = new PolicyHolder(new ClientCachePolicy(ClientCachePolicy.Level.PUBLIC, 3600));
        PolicyHolder privateHolder = new PolicyHolder(new ClientCachePolicy(ClientCachePolicy.Level.PRIVATE));
        PolicyHolder custom1Holder = new PolicyHolder(new ClientCachePolicy(ClientCachePolicy.Level.CUSTOM, 300));
        PolicyHolder custom2Holder = new PolicyHolder(new ClientCachePolicy(ClientCachePolicy.Level.CUSTOM, 1800));
        PolicyHolder immutableHolder = new PolicyHolder(new ClientCachePolicy(ClientCachePolicy.Level.IMMUTABLE));

        List<PolicyHolder> list1 = List.of(publicHolder, privateHolder, custom1Holder, custom2Holder, immutableHolder);
        List<PolicyHolder> list2 = List.of(publicHolder, immutableHolder);
        List<PolicyHolder> list3 = List.of(publicHolder, publicHolder, publicHolder, immutableHolder);

        ClientCachePolicy computed1 = list1.stream().map(PolicyHolder::getPolicy).reduce(ClientCachePolicy.DEFAULT, ClientCachePolicy::strongest);
        assertEquals(ClientCachePolicy.PRIVATE.getLevel(), computed1.getLevel());

        ClientCachePolicy computed2 = list2.stream().map(PolicyHolder::getPolicy).reduce(ClientCachePolicy.DEFAULT, ClientCachePolicy::strongest);
        assertEquals(ClientCachePolicy.PUBLIC.getLevel(), computed2.getLevel());

        ClientCachePolicy computed3 = list3.stream().map(PolicyHolder::getPolicy).reduce(ClientCachePolicy.DEFAULT, ClientCachePolicy::strongest);
        assertEquals(ClientCachePolicy.PUBLIC.getLevel(), computed3.getLevel());

    }

    @Test
    public void testClientCachePolicyEquals() {
        ClientCachePolicy publicPolicy = new ClientCachePolicy(ClientCachePolicy.Level.PUBLIC, 3600);
        ClientCachePolicy privatePolicy = new ClientCachePolicy(ClientCachePolicy.Level.PRIVATE);
        ClientCachePolicy custom1Policy = new ClientCachePolicy(ClientCachePolicy.Level.CUSTOM, 300);
        ClientCachePolicy custom2Policy = new ClientCachePolicy(ClientCachePolicy.Level.CUSTOM, 1800);
        ClientCachePolicy immutablePolicy = new ClientCachePolicy(ClientCachePolicy.Level.IMMUTABLE);

        ClientCachePolicy publicPolicy2 = new ClientCachePolicy(ClientCachePolicy.Level.PUBLIC, 3600);

        assertEquals(publicPolicy, publicPolicy2);
        assertNotEquals(publicPolicy, privatePolicy);
        assertNotEquals(publicPolicy, custom1Policy);
        assertNotEquals(publicPolicy, custom2Policy);
        assertNotEquals(publicPolicy, immutablePolicy);
        assertNotEquals(custom1Policy, custom2Policy);
    }

    private static class PolicyHolder  {

        private ClientCachePolicy policy;

        public PolicyHolder(ClientCachePolicy policy) {
            this.policy = policy;
        }

        public ClientCachePolicy getPolicy() {
            return policy;
        }

    }
}
