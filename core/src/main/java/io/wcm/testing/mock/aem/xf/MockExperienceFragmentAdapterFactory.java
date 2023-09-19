package io.wcm.testing.mock.aem.xf;

import com.adobe.cq.xf.ExperienceFragment;
import com.adobe.cq.xf.ExperienceFragmentVariation;
import com.adobe.cq.xf.ExperienceFragmentsConstants;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.adapter.AdapterFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.service.component.annotations.Component;

/**
 * Mock adapter factory for AEM Experience Fragment-related adaptions.
 */
@Component(service = AdapterFactory.class,
        property = {
                AdapterFactory.ADAPTABLE_CLASSES + "=com.day.cq.wcm.api.Page",
                AdapterFactory.ADAPTER_CLASSES + "=com.adobe.cq.xf.ExperienceFragment",
                AdapterFactory.ADAPTER_CLASSES + "=com.adobe.cq.xf.ExperienceFragmentVariation"
        })
@ProviderType
public class MockExperienceFragmentAdapterFactory implements AdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <AdapterType> AdapterType getAdapter(@NotNull Object object, @NotNull Class<AdapterType> type) {
        if (object instanceof Page) {
            Page page = (Page) object;
            if (page.getContentResource().isResourceType(ExperienceFragmentsConstants.RT_EXPERIENCE_FRAGMENT_MASTER)) {
                if (type == ExperienceFragment.class) {
                    return (AdapterType) new MockExperienceFragment(page);
                }
            }
            if (page.getContentResource().isResourceType(ExperienceFragmentsConstants.RT_EXPERIENCE_FRAGMENT_PAGE)) {
                if (type == ExperienceFragmentVariation.class) {
                    return (AdapterType) new MockExperienceFragmentVariation(page);
                }
            }
        }
        return null;
    }
}
