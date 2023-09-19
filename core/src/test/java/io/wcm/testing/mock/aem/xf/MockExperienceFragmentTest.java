package io.wcm.testing.mock.aem.xf;

import com.adobe.cq.xf.ExperienceFragment;
import com.adobe.cq.xf.ExperienceFragmentVariation;
import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.loader.ContentLoader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.adobe.cq.xf.ExperienceFragmentsConstants.TYPE_XF_VARIANT_FACEBOOK;
import static com.adobe.cq.xf.ExperienceFragmentsConstants.TYPE_XF_VARIANT_WEB;
import static org.junit.Assert.*;

public class MockExperienceFragmentTest {

    @Rule
    public AemContext context = TestAemContext.newAemContext();

    @Before
    public void setUp() {
        ContentLoader contentLoader = this.context.load();
        contentLoader.json("/json-import-samples/xf.json", "/content/experience-fragments/sample");
    }

    @Test
    @SuppressWarnings("null")
    public void testExperienceFragment() {
        Resource xfResource = this.context.resourceResolver().getResource("/content/experience-fragments/sample");
        Page xfPage = xfResource.adaptTo(Page.class);
        assertNull(xfPage.adaptTo(ExperienceFragmentVariation.class));
        ExperienceFragment experienceFragment = xfPage.adaptTo(ExperienceFragment.class);
        assertNotNull(experienceFragment);

        assertEquals("/content/experience-fragments/sample", experienceFragment.getPath());
        assertEquals("/conf/something", experienceFragment.getCloudserviceConfigurationsPaths().get(0));
        assertEquals(2, experienceFragment.getVariations().size());
        assertEquals(1, experienceFragment.getVariations(TYPE_XF_VARIANT_FACEBOOK).size());
        assertEquals("/content/experience-fragments/sample/master", experienceFragment.getVariations().get(0).getPath());
        assertEquals("Header", experienceFragment.getProperties().get("jcr:title"));
    }

    @Test
    @SuppressWarnings("null")
    public void testExperienceFragmentVariation() {
        Resource masterResource = this.context.resourceResolver().getResource("/content/experience-fragments/sample/master");
        Page masterPage = masterResource.adaptTo(Page.class);
        assertNull(masterPage.adaptTo(ExperienceFragment.class));
        ExperienceFragmentVariation variation = masterPage.adaptTo(ExperienceFragmentVariation.class);
        assertNotNull(variation);

        assertEquals("/content/experience-fragments/sample/master", variation.getPath());
        assertEquals("/conf/something", variation.getCloudserviceConfigurationsPaths().get(0));
        assertEquals("/conf/something", variation.getPropertiesTree().getInherited("cq:cloudserviceconfigs", ""));
        assertEquals(TYPE_XF_VARIANT_WEB, variation.getType());
        assertEquals("/content/experience-fragments/sample", variation.getParent().getPath());
        assertEquals("Header", variation.getProperties().get("jcr:title"));
    }

}
