package io.wcm.testing.mock.aem.xf;

import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.Arrays;
import java.util.List;

public class MockExperienceFragmentBase extends SlingAdaptable {
    private final Page page;

    protected MockExperienceFragmentBase(Page page) {
        this.page = page;
    }

    public String getPath() {
        return page.getPath();
    }

    public ValueMap getProperties() {
        return page.getProperties();
    }

    public List<String> getCloudserviceConfigurationsPaths() {
        return Arrays.asList(getInheritedProperties().getInherited("cq:cloudserviceconfigs", new String[0]));
    }

    protected InheritanceValueMap getInheritedProperties() {
        return new HierarchyNodeInheritanceValueMap(page.getContentResource());
    }

    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if (type == Resource.class) {
            return (AdapterType) page.adaptTo(Resource.class);
        }
        if (type == Page.class) {
            return (AdapterType) page;
        }
        return super.adaptTo(type);
    }

    protected Page getPage() {
        return page;
    }
}