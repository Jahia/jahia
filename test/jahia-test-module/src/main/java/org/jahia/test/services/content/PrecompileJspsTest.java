/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.test.services.content;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.logging.log4j.Level;
import org.apache.xerces.impl.dv.util.Base64;
import org.jahia.bin.Jahia;
import org.jahia.test.JahiaTestCase;
import org.jahia.utils.Log4jEventCollectorWrapper;
import org.jahia.utils.Log4jEventCollectorWrapper.LoggingEventWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Precompile all JSPs (of core and all deployed modules) and fail test, if there was an error
 *
 * @author Benjamin Papez
 */

public class PrecompileJspsTest extends JahiaTestCase {

    private static final Logger logger = LoggerFactory.getLogger(PrecompileJspsTest.class);

    private Log4jEventCollectorWrapper logEventCollector;

    @Before
    public void setUp() {
        logEventCollector = new Log4jEventCollectorWrapper(Level.ERROR.name());
    }

    @After
    public void tearDown() {
        logEventCollector.close();
    }

    private String getPrecompileServletURL() {
        return getBaseServerURL()+ Jahia.getContextPath() + "/modules/tools/precompileServlet";
    }

    @Test
    public void testPrecompileJsps() throws IOException {
        CloseableHttpClient client = getHttpClient();
        HttpGet get = new HttpGet(getPrecompileServletURL());
        get.addHeader("Authorization", "Basic " + Base64.encode((JahiaTestCase.getRootUserCredentials().getUserID() + ":" + String.valueOf(JahiaTestCase.getRootUserCredentials().getPassword())).getBytes()));
        try (CloseableHttpResponse response = client.execute(get)) {
            assertThat(response.getCode()).isEqualTo(HttpStatus.SC_OK).withFailMessage("Authenticating to precompile page in tools failed");
            
            Source source = new Source(EntityUtils.toString(response.getEntity()));
            Element aElement = source.getFirstElement(HTMLElementName.A);
            String url = getBaseServerURL() + aElement.getAttributeValue("href");
            logger.info("Starting the precompileServlet with the following url: {}", url);
            get = new HttpGet(url);
        } catch (ParseException e) {
            throw new IOException(e);
        }
        try (CloseableHttpResponse response = client.execute(get)) {
            assertThat(response.getCode()).isEqualTo(HttpStatus.SC_OK).withFailMessage("Precompile servlet failed");
            assertThat(EntityUtils.toString(response.getEntity())).contains("No problems found!");
            assertThat(toText(logEventCollector.getCollectedEvents())).isEmpty();
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private static String toText(List<LoggingEventWrapper> logEvents) {
        DateFormat timestampFormat = DateFormat.getDateTimeInstance();
        StringBuilder errors = new StringBuilder();
        for (LoggingEventWrapper logEvent : logEvents) {
            errors.append(timestampFormat.format(new Date(logEvent.getTimestamp()))).append(" ").append(logEvent.getMessage()).append("\n");
            String[] throwableInfo = logEvent.getThrowableInfo();
            if (throwableInfo != null) {
                for (String throwableInfoItem : throwableInfo) {
                    errors.append(throwableInfoItem).append("\n");
                }
                errors.append("\n");
            }
        }
        return errors.toString();
    }
}
