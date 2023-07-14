package io.wcm.testing.mock.aem;

import static org.junit.Assert.assertNotNull;

import com.day.cq.tagging.JcrTagManagerFactory;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;
import javax.jcr.Session;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MockJcrTagManagerFactoryTest {

    @Rule
    public AemContext context = TestAemContext.newAemContext();

    private JcrTagManagerFactory underTest;

    @Before
    public void setUp() {
        context.create().tag("test:test-tag");
        underTest = context.getService(JcrTagManagerFactory.class);
    }

    @Test
    public void testGetTagManagerFromResourceResolver() {
        TagManager tagManager = underTest.getTagManager(context.resourceResolver());
        assertNotNull(tagManager);

        Tag tag = tagManager.resolve("test:test-tag");
        assertNotNull(tag);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTagManagerFromResourceResolverWithNull() {
        underTest.getTagManager((ResourceResolver) null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetTagManagerFromSession() {
        underTest.getTagManager((Session) null);
    }
}
