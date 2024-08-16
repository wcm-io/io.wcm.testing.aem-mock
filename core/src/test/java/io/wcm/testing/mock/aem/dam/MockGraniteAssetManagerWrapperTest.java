package io.wcm.testing.mock.aem.dam;

import com.adobe.granite.asset.api.Asset;
import com.adobe.granite.asset.api.AssetManager;
import com.day.cq.commons.jcr.JcrConstants;
import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class MockGraniteAssetManagerWrapperTest {

    private static final String TEST_ASSET_PATH = "/content/dam/test-asset";

    @Rule
    public final AemContext context = TestAemContext.newAemContext();

    private ResourceResolver resourceResolver;
    private AssetManager assetManager;

    @Before
    public void setUp() {
        resourceResolver = context.resourceResolver();
        assetManager = context.graniteAssetManager();
    }

    @Test
    public void testBasicAssetFunctionality() {
        assertNull(resourceResolver.getResource(TEST_ASSET_PATH));
        assertFalse(assetManager.assetExists(TEST_ASSET_PATH));

        Asset asset = assetManager.createAsset(TEST_ASSET_PATH);
        assertNotNull(asset);
        assertEquals("dam:Asset", asset.getValueMap().get(JcrConstants.JCR_PRIMARYTYPE));
        assertTrue(assetManager.assetExists(TEST_ASSET_PATH));
        Resource assetResource = resourceResolver.getResource(TEST_ASSET_PATH);
        assertNotNull(assetResource);
        assertNotNull(assetResource.getChild(JcrConstants.JCR_CONTENT));
        assertNotNull(assetResource.getChild(JcrConstants.JCR_CONTENT + "/metadata"));
        assertNotNull(assetResource.getChild(JcrConstants.JCR_CONTENT + "/renditions"));

        assertEquals(asset, assetManager.getAsset(TEST_ASSET_PATH));

        assetManager.removeAsset(TEST_ASSET_PATH);
        assertNull(resourceResolver.getResource(TEST_ASSET_PATH));
        assertNull(assetManager.getAsset(TEST_ASSET_PATH));
        assertFalse(assetManager.assetExists(TEST_ASSET_PATH));
    }
}