package io.wcm.testing.mock.aem;

import com.adobe.cq.dam.cfm.DataType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MockDataType implements DataType {

    boolean isArray;

    public MockDataType(boolean isArray) {
        this.isArray = isArray;
    }

    public @Nullable String getSemanticType() {
        return StringUtils.EMPTY;
    }

    @Override
    public boolean isMultiValue() {
        return isArray;
    }

    // --- unsupported operations ---

    @Override
    public @NotNull String getTypeString() {
        throw new UnsupportedOperationException();
    }

    public @NotNull String getValueType() {
        throw new UnsupportedOperationException();
    }
}
