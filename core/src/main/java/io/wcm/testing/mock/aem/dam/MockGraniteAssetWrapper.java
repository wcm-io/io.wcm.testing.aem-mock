package io.wcm.testing.mock.aem.dam;

import com.adobe.granite.asset.api.Asset;
import com.adobe.granite.asset.api.AssetMetadata;
import com.adobe.granite.asset.api.AssetRelation;
import com.adobe.granite.asset.api.Rendition;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceWrapper;

import javax.jcr.Binary;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of Adobe Granite {@link Asset}.  This is done by wrapping a {@link MockAsset}
 */
@SuppressWarnings("null")
public class MockGraniteAssetWrapper extends ResourceWrapper implements Asset {

    private final MockAsset asset;

    MockGraniteAssetWrapper(MockAsset asset) {
        super(asset.getResource());
        this.asset = asset;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        //to be able to adapt to CQ asset
        if (type == com.day.cq.dam.api.Asset.class) {
            return (AdapterType) asset;
        }
        return asset.adaptTo(type);
    }

    @Override
    public com.adobe.granite.asset.api.Rendition getRendition(String s) {
        return (com.adobe.granite.asset.api.Rendition) asset.getRendition(s);
    }

    @Override
    public Iterator<? extends Rendition> listRenditions() {
        List<Rendition> graniteRenditions = new LinkedList<>();
        Iterator<? extends com.day.cq.dam.api.Rendition> renditions = asset.listRenditions();
        while (renditions.hasNext()) {
            graniteRenditions.add((com.adobe.granite.asset.api.Rendition) renditions.next());
        }
        return graniteRenditions.iterator();
    }

    @Override
    public String getIdentifier() {
        return asset.getID();
    }

    @Override
    public com.adobe.granite.asset.api.Rendition setRendition(String s, InputStream inputStream, Map<String, Object> map) {
        if (map.size() == 1) {
            Object val = map.values().iterator().next();
            if (val != null) {
                return (com.adobe.granite.asset.api.Rendition) asset.addRendition(s, inputStream, val.toString());
            }
        }
        return (com.adobe.granite.asset.api.Rendition) asset.addRendition(s, inputStream, map);
    }

    @Override
    public com.adobe.granite.asset.api.Rendition setRendition(String s, Binary binary, Map<String, Object> map) {
        return (com.adobe.granite.asset.api.Rendition) asset.addRendition(s, binary, map);
    }

    @Override
    public void removeRendition(String s) {
        asset.removeRendition(s);
    }

    @Override
    public Iterator<? extends com.adobe.granite.asset.api.Asset> listRelated(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<? extends AssetRelation> listRelations(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AssetMetadata getAssetMetadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AssetRelation addRelation(String s, String s1, Map<String, Object> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRelation(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AssetRelation addRelation(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void orderRelationBefore(String s, String s1, String s2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeRelation(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeRelation(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MockGraniteAssetWrapper)) {
            return false;
        }
        return StringUtils.equals(getPath(), ((MockGraniteAssetWrapper)obj).getPath());
    }

}
