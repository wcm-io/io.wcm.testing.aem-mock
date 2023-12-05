package io.wcm.testing.mock.aem;

import java.util.Calendar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.DataType;
import com.adobe.cq.dam.cfm.FragmentData;

class MockContentFragment_MockFragmentData implements FragmentData {

  private Object value;
  private String contentType;
  private final MockContentFragment_MockDataType mockDataType;

  MockContentFragment_MockFragmentData(Object value, boolean isArray) {
    this.value = value;
    this.mockDataType = new MockContentFragment_MockDataType(isArray);
  }

  @Override
  public @NotNull DataType getDataType() {
    return mockDataType;
  }

  @Override
  public <T> @Nullable T getValue(Class<T> type) {
    if (type.isInstance(value)) {
      return (T)value;
    }
    else {
      return null;
    }
  }

  @Override
  public boolean isTypeSupported(Class type) {
    return type.isInstance(value);
  }

  @Override
  public @Nullable Object getValue() {
    return value;
  }

  @Override
  public void setValue(@Nullable Object value) throws ContentFragmentException {
    this.value = value;
  }

  @Override
  public @Nullable String getContentType() {
    return contentType;
  }

  @Override
  public void setContentType(@Nullable String contentType) {
    this.contentType = contentType;
  }

  public @Nullable Calendar getLastModified() {
    return null;
  }

}
