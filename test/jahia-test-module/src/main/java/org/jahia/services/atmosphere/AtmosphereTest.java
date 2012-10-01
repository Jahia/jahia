/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.atmosphere;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import org.atmosphere.cpr.HeaderConfig;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 01/10/12
 */
public class AtmosphereTest {
    private transient static org.slf4j.Logger logger = LoggerFactory.getLogger(AtmosphereTest.class);
    private String urlTarget = "http://localhost:8080/atmosphere/pubsub/alert/testChannel";

    @Test
    public void testHeaderBroadcasterCache() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        logger.info("{}: running test: testHeaderBroadcasterCache", getClass().getSimpleName());

        final CountDownLatch latch = new CountDownLatch(1);
        long t1 = System.currentTimeMillis();
        AsyncHttpClient c = new AsyncHttpClient();
        try {
            // Suspend
            c.preparePost(urlTarget).addParameter("message", "cacheme").execute().get();

            // Broadcast
            c.preparePost(urlTarget).addParameter("message", "cachememe").execute().get();

            //Suspend
            Response r = c.prepareGet(urlTarget).addHeader(HeaderConfig.X_CACHE_DATE, String.valueOf(t1)).execute(new AsyncCompletionHandler<Response>() {

                @Override
                public Response onCompleted(Response r) throws Exception {
                    try {
                        return r;
                    } finally {
                        latch.countDown();
                    }
                }
            }).get();

            try {
                latch.await(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                fail(e.getMessage());
            }

            assertNotNull(r);
            assertEquals(r.getStatusCode(), 200);
            assertEquals(r.getResponseBody(), "cacheme\ncachememe\n");
        } catch (Exception e) {
            logger.error("test failed", e);
            fail(e.getMessage());
        }

        c.close();
    }
}
