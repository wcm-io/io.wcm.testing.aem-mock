package io.wcm.testing.mock.aem.xf;

import com.adobe.cq.xf.ExperienceFragment;
import com.adobe.cq.xf.ExperienceFragmentVariation;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.ValueMap;

import static com.adobe.cq.xf.ExperienceFragmentsConstants.CUSTOM_XF_VARIANT_TYPE;
import static com.adobe.cq.xf.ExperienceFragmentsConstants.PN_XF_VARIANT_TYPE;

/**
 * Mock implementation of {@link ExperienceFragmentVariation}.
 */
public class MockExperienceFragmentVariation extends MockExperienceFragmentBase implements ExperienceFragmentVariation {
    private ExperienceFragment parent;

    public MockExperienceFragmentVariation(Page page) {
        super(page);
    }

    public ExperienceFragment getParent() {
        if (this.parent == null) {
            this.parent = new MockExperienceFragment(getPage().getParent());
        }
        return this.parent;
    }

    public String getType() {
        ValueMap properties = getPage().getProperties();
        String type = properties.get(PN_XF_VARIANT_TYPE, String.class);
        return (type != null) ? type : CUSTOM_XF_VARIANT_TYPE;
    }

    public <AdapterType> AdapterType adaptTo(Class<AdapterType> aClass) {
        if (aClass == Page.class) {
            return (AdapterType) getPage();
        }
        return super.adaptTo(aClass);
    }

    public InheritanceValueMap getPropertiesTree() {
        return getInheritedProperties();
    }
}
