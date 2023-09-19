package io.wcm.testing.mock.aem.xf;

import com.adobe.cq.xf.ExperienceFragment;
import com.adobe.cq.xf.ExperienceFragmentVariation;
import com.day.cq.commons.Filter;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;

import java.util.*;

import static com.adobe.cq.xf.ExperienceFragmentsConstants.PN_XF_VARIANT_TYPE;
import static com.adobe.cq.xf.ExperienceFragmentsConstants.RT_EXPERIENCE_FRAGMENT_PAGE;

/**
 * Mock implementation of {@link ExperienceFragment}.
 */
public class MockExperienceFragment extends MockExperienceFragmentBase implements ExperienceFragment {
    public MockExperienceFragment(Page page) {
        super(page);
    }

    public List<ExperienceFragmentVariation> getVariations() {
        List<ExperienceFragmentVariation> variations = new ArrayList<>();
        Iterator<Page> it = getPage().listChildren(element -> element.getContentResource().isResourceType(RT_EXPERIENCE_FRAGMENT_PAGE));
        while (it.hasNext()) {
            Page thePage = it.next();
            variations.add(thePage.adaptTo(ExperienceFragmentVariation.class));
        }
        return variations;
    }

    public List<ExperienceFragmentVariation> getVariations(String... type) {
        final Set<String> typeValues = new HashSet<>(List.of(type));
        Filter<Page> typeFilter = element -> {
            ValueMap properties = element.getProperties();
            String variantType = properties.get(PN_XF_VARIANT_TYPE, "");
            if (StringUtils.isEmpty(variantType))
                return false;
            return typeValues.contains(variantType);
        };
        List<ExperienceFragmentVariation> variations = new ArrayList<>();
        for (Iterator<Page> it = getPage().listChildren(typeFilter); it.hasNext(); ) {
            Page page = it.next();
            variations.add(page.adaptTo(ExperienceFragmentVariation.class));
        }
        return variations;
    }
}