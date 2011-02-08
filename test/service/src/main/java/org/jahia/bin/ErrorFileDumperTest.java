package org.jahia.bin;

import org.jahia.bin.errors.ErrorFileDumper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.StopWatch;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Test unit for error file dumper sub system.
 */
public class ErrorFileDumperTest {

    private final long LOOP_COUNT = 1000L;

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(ErrorFileDumperTest.class);

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
    }

    @Test
    public void testDumper() throws IOException {
        StopWatch stopWatch = new StopWatch("testErrorFileDumper");
        stopWatch.start(Thread.currentThread().getName() + " generating error dumps");
        for (int i=0; i < LOOP_COUNT; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/cms");
            request.setQueryString("name=value");
            request.addHeader("headerName", "headerValue");
            ErrorFileDumper.dumpToFile(new Throwable("mock error " + i), (HttpServletRequest) request);
        }
        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();
        double averageTime = ((double) totalTime) / ((double) LOOP_COUNT);
        logger.info("Milliseconds per exception = " + averageTime);
        logger.info(stopWatch.prettyPrint());
    }
}
