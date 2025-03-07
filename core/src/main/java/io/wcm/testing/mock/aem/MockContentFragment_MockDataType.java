/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2023 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.testing.mock.aem;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.adobe.cq.dam.cfm.DataType;

class MockContentFragment_MockDataType implements DataType {

  private final boolean isArray;

  MockContentFragment_MockDataType(boolean isArray) {
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
