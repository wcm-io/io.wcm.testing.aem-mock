package io.wcm.testing.mock.aem;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.SimpleBindings;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.apache.sling.scripting.api.AbstractScriptEngineFactory;
import org.apache.sling.scripting.api.BindingsValuesProvider;
import org.apache.sling.scripting.api.BindingsValuesProvidersByContext;

public class MockOverrideSlingPathRequestWrapper extends SlingHttpServletRequestWrapper {
    private static final String ATTR_SLING_BINDINGS = SlingBindings.class.getName();
    private final AdapterManager adapterManager;
    private final SlingBindings myBindings;
    private final Resource resource;
    private final Map<Class<?>, Object> adaptersCache;

    public MockOverrideSlingPathRequestWrapper(AdapterManager adapterManager, SlingHttpServletRequest request, String path) {
        super(request);
        this.adapterManager = adapterManager;
        this.myBindings = new SlingBindings();
        this.adaptersCache = new HashMap();
        SlingBindings slingBindings = (SlingBindings)this.getSlingRequest().getAttribute(ATTR_SLING_BINDINGS);
        this.resource = this.getSlingRequest().getResourceResolver().resolve(this.getSlingRequest(), path);
        if (slingBindings != null) {
            this.myBindings.putAll(slingBindings);
        }

        this.myBindings.put("properties", this.resource.getValueMap());
        this.myBindings.put("resource", this.resource);
        this.myBindings.put("request", this);
        this.myBindings.put("resolver", this.resource.getResourceResolver());
        Page currentPage = null;
        PageManager pageManager = this.getSlingRequest().getResourceResolver().adaptTo(PageManager.class);
        if (pageManager != null) {
            currentPage = pageManager.getContainingPage(this.resource);
        }

        this.myBindings.put("currentPage", currentPage);
    }

    public MockOverrideSlingPathRequestWrapper(AdapterManager adapterManager, SlingHttpServletRequest request, String path,
                                               BindingsValuesProvidersByContext bindingsValuesProvidersByContext) {
        this(adapterManager, request, path);
        SimpleBindings additionalBindings = new SimpleBindings();
        additionalBindings.putAll(this.myBindings);
        Collection<BindingsValuesProvider> bindingsValuesProviders = bindingsValuesProvidersByContext.getBindingsValuesProviders(new SlingModelsScriptEngineFactory(), "request");
        Iterator<BindingsValuesProvider> bindingsValuesProviderIterator = bindingsValuesProviders.iterator();

        while(bindingsValuesProviderIterator.hasNext()) {
            BindingsValuesProvider provider = (BindingsValuesProvider)bindingsValuesProviderIterator.next();
            provider.addBindings(additionalBindings);
        }

        this.myBindings.putAll(additionalBindings);
    }

    public Object getAttribute(String name) {
        return ATTR_SLING_BINDINGS.equals(name) ? this.myBindings : super.getAttribute(name);
    }

    public Resource getResource() {
        return this.resource;
    }

    /**
     * Overriding `adaptTo` to avoid using the original request as the adaptable.
     */
    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        AdapterType result = null;
        synchronized(this) {
            result = (AdapterType) this.adaptersCache.get(type);

            if (result == null) {
                result = adapterManager.getAdapter(this, type);

                if (result != null) {
                    this.adaptersCache.put(type, result);
                }
            }

            return result;
        }
    }

    private static class SlingModelsScriptEngineFactory extends AbstractScriptEngineFactory implements ScriptEngineFactory {
        SlingModelsScriptEngineFactory() {
            this.setNames(new String[]{"sling-models-exporter", "sling-models"});
        }

        public String getLanguageName() {
            return null;
        }

        public String getLanguageVersion() {
            return null;
        }

        public ScriptEngine getScriptEngine() {
            return null;
        }
    }
}

