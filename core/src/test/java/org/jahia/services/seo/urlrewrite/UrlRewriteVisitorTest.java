package org.jahia.services.seo.urlrewrite;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link UrlRewriteVisitor} class.
 *
 */
@RunWith(JUnitParamsRunner.class)
public class UrlRewriteVisitorTest {

    @Mock
    private UrlRewriteService urlRewriteService;

    @Mock
    private RenderContext renderContext;

    @Mock
    private Resource resource;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UrlRewriteVisitor visitor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        when(renderContext.getRequest()).thenReturn(request);
        when(renderContext.getResponse()).thenReturn(response);

        visitor = new UrlRewriteVisitor();
        visitor.setUrlRewriteService(urlRewriteService);
    }

    /**
     * Provides test data for srcset attribute rewriting.
     * Format: tagName, attrName, inputValue, expectedOutput, description
     */
    private Object[] srcsetAttributeTestData() {
        return new Object[] { new Object[] { "img", "srcset", "/files/sample.png 100w", "https://cdn.example.com/files/sample.png 100w",
                "Single URL with width descriptor" },

                new Object[] { "img", "srcset", "/files/sample.png 1x, /files/sample@2x.png 2x",
                        "https://cdn.example.com/files/sample.png 1x, https://cdn.example.com/files/sample@2x.png 2x",
                        "Multiple URLs with pixel density descriptors" },

                new Object[] { "img", "srcset", "/files/sample-w200.png?w=100 100w, /files/sample-w200.png 200w",
                        "https://cdn.example.com/files/sample-w200.png?w=100 100w, https://cdn.example.com/files/sample-w200.png 200w",
                        "URLs with one being substring of another (with query string)" },

                new Object[] { "img", "srcset", "/files/sample-w200.png?w=100 100w, /files/sample-w200.png 200w, /files/sample-w200.png 300w",
                        "https://cdn.example.com/files/sample-w200.png?w=100 100w, https://cdn.example.com/files/sample-w200.png 200w, https://cdn.example.com/files/sample-w200.png 300w",
                        "duplicated URLs for different sizes" },

                new Object[] { "img", "srcset", "/files/img1.jpg 320w, /files/img2.jpg 640w, /files/img3.jpg 1024w",
                        "https://cdn.example.com/files/img1.jpg 320w, https://cdn.example.com/files/img2.jpg 640w, https://cdn.example.com/files/img3.jpg 1024w",
                        "Three URLs with different widths" },

                new Object[] { "img", "srcset", "/files/photo.png", "https://cdn.example.com/files/photo.png",
                        "Single URL without descriptor" },

                new Object[] { "img", "srcset", "  /files/a.png 100w , /files/b.png 200w  ",
                        "https://cdn.example.com/files/a.png 100w, https://cdn.example.com/files/b.png 200w",
                        "URLs with extra whitespace" },

                new Object[] { "img", "srcset", "/files/image.png?token=abc123 1x, /files/image.png?token=xyz789 2x",
                        "https://cdn.example.com/files/image.png?token=abc123 1x, https://cdn.example.com/files/image.png?token=xyz789 2x",
                        "URLs with query parameters" },

                new Object[] { "img", "srcset", "/files/path/to/image.jpg 1.5x", "https://cdn.example.com/files/path/to/image.jpg 1.5x",
                        "URL with decimal pixel density" },

                new Object[] { "IMG", "SRCSET", "/files/uppercase.png 100w", "https://cdn.example.com/files/uppercase.png 100w",
                        "Case insensitive tag and attribute names" },

                new Object[] { "img", "data-srcset", "/files/lazy.png 100w", "https://cdn.example.com/files/lazy.png 100w",
                        "Attribute ending with 'srcset' (data-srcset)" } };
    }

    @Test
    @Parameters(method = "srcsetAttributeTestData")
    public void GIVEN_srcsetAttribute_WHEN_visitCalled_THEN_allUrlsRewritten(String tagName, String attrName, String inputValue,
            String expectedOutput, String description) throws ServletException, IOException, InvocationTargetException {
        // Given
        when(urlRewriteService.rewriteOutbound(anyString(), any(), any())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            if (url.startsWith("/files/")) {
                return "https://cdn.example.com" + url;
            }
            return url;
        });

        // When
        String result = visitor.visit(inputValue, renderContext, tagName, attrName, resource);

        // Then
        assertEquals(description, expectedOutput, result);
    }

    /**
     * Provides test data for non-srcset attribute rewriting.
     */
    private Object[] regularSrcAttributeTestData() {
        return new Object[] {
                new Object[] { "a", "href", "/files/document.pdf", "https://cdn.example.com/files/document.pdf", "Link href" },
                new Object[] { "img", "src", "/files/image.jpg", "https://cdn.example.com/files/image.jpg", "Image src" },
                new Object[] { "script", "src", "/files/script.js", "https://cdn.example.com/files/script.js", "Script src" },
                new Object[] { "link", "href", "/files/style.css", "https://cdn.example.com/files/style.css", "Link stylesheet" } };
    }

    @Test
    @Parameters(method = "regularSrcAttributeTestData")
    public void GIVEN_regularAttribute_WHEN_visitCalled_THEN_urlRewritten(String tagName, String attrName, String inputValue,
            String expectedOutput, String description) throws ServletException, IOException, InvocationTargetException {
        // Given
        when(urlRewriteService.rewriteOutbound(anyString(), any(), any())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            if (url.startsWith("/files/")) {
                return "https://cdn.example.com" + url;
            }
            return url;
        });

        // When
        String result = visitor.visit(inputValue, renderContext, tagName, attrName, resource);

        // Then
        assertEquals(description, expectedOutput, result);
    }

    @Test
    public void GIVEN_emptyValue_WHEN_visitCalled_THEN_emptyValueReturned() {
        // Given
        String inputValue = "";

        // When
        String result = visitor.visit(inputValue, renderContext, "img", "src", resource);

        // Then
        assertEquals("Empty value should return empty", "", result);
    }

    @Test
    public void GIVEN_nullValue_WHEN_visitCalled_THEN_nullValueReturned() {
        // Given
        String inputValue = null;

        // When
        String result = visitor.visit(inputValue, renderContext, "img", "src", resource);

        // Then
        assertNull("Null value should return null", result);
    }

    @Test
    public void GIVEN_urlRewriteServiceThrowsException_WHEN_visitCalled_THEN_originalValueReturned()
            throws ServletException, IOException, InvocationTargetException {
        // Given
        String inputValue = "/files/test.jpg";
        when(urlRewriteService.rewriteOutbound(anyString(), any(), any())).thenThrow(new RuntimeException("Test exception"));

        // When
        String result = visitor.visit(inputValue, renderContext, "img", "src", resource);

        // Then
        assertEquals("Should return original value on exception", inputValue, result);
    }

    @Test
    public void GIVEN_srcsetWithSpecialRegexCharacters_WHEN_visitCalled_THEN_urlsRewrittenCorrectly()
            throws ServletException, IOException, InvocationTargetException {
        // Given
        String inputValue = "/files/image.test.png?param=$value 100w";
        String expectedOutput = "https://cdn.example.com/files/image.test.png?param=$value 100w";
        when(urlRewriteService.rewriteOutbound(anyString(), any(), any())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            if (url.startsWith("/files/")) {
                return "https://cdn.example.com" + url;
            }
            return url;
        });

        // When
        String result = visitor.visit(inputValue, renderContext, "img", "srcset", resource);

        // Then
        assertEquals("URLs with special regex characters should be handled correctly", expectedOutput, result);
    }

    @Test
    public void GIVEN_srcsetWithSubstringUrls_WHEN_visitCalled_THEN_bothUrlsRewrittenCorrectly()
            throws ServletException, IOException, InvocationTargetException {
        // Given
        // This is the critical edge case: one URL is a substring of another
        String inputValue = "/files/sample.png?w=100 100w, /files/sample.png 200w";
        String expectedOutput = "https://cdn.example.com/files/sample.png?w=100 100w, https://cdn.example.com/files/sample.png 200w";
        when(urlRewriteService.rewriteOutbound(anyString(), any(), any())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            if (url.startsWith("/files/")) {
                return "https://cdn.example.com" + url;
            }
            return url;
        });

        // When
        String result = visitor.visit(inputValue, renderContext, "img", "srcset", resource);

        // Then
        assertEquals("URLs where one is a substring of another should be handled correctly", expectedOutput, result);
    }
}

