package io.wcm.testing.mock.aem.dam;

import com.adobe.granite.asset.api.Asset;
import com.adobe.granite.asset.api.AssetManager;
import com.day.cq.dam.api.DamConstants;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Mock implementation of Adobe Granite {@link AssetManager}. This is done by wrapping a {@link MockAssetManager}.
 */
public class MockGraniteAssetManagerWrapper implements AssetManager {

    private final ResourceResolver resourceResolver;
    private final com.day.cq.dam.api.AssetManager cqAssetManager;

    MockGraniteAssetManagerWrapper(@NotNull ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
        this.cqAssetManager = resourceResolver.adaptTo(com.day.cq.dam.api.AssetManager.class);
    }

    @Override
    public Asset createAsset(String s) {
        com.day.cq.dam.api.Asset asset = cqAssetManager.createAsset(s, null, null, false);
        if (asset instanceof MockAsset) {
            return new MockGraniteAssetWrapper((MockAsset) asset);
        }
        throw new UnsupportedOperationException("Not in a mock context");
    }

    @Override
    public Asset getAsset(String s) {
        Resource resource = resourceResolver.getResource(s);
        return resource != null ? resource.adaptTo(Asset.class) : null;
    }

    @Override
    public Asset getAssetByIdentifier(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean assetExists(String s) {
        Resource assetResource = resourceResolver.getResource(s);
        return assetResource != null && Objects.equals(DamConstants.NT_DAM_ASSET, assetResource.getValueMap().get(JcrConstants.JCR_PRIMARYTYPE, String.class));
    }

    @Override
    public void removeAsset(String s) {
        try {
            Resource assetResource = resourceResolver.getResource(s);
            if (assetResource != null) {
                resourceResolver.delete(assetResource);
            }
        } catch (PersistenceException pe) {
            throw new RuntimeException(pe);
        }
    }

    @Override
    public void copyAsset(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveAsset(String s, String s1) {
        throw new UnsupportedOperationException();
    }
}
