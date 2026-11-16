/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2026 wcm.io
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

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;

import com.day.cq.wcm.api.designer.Design;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockDesignTest {

  private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  @Test
  public void legacyDesign() {
    final Design design = context.create().design("/apps/settings/designs/test");
    assertEquals("/apps/settings/designs/test", design.getId());
    assertEquals("/apps/settings/designs/test", design.getPath());
  }

  @Test
  public void design() {
    final Design design = context.create().design("/etc/designs/test");
    assertEquals("test", design.getId());
    assertEquals("/etc/designs/test", design.getPath());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void getJSON() throws JsonProcessingException, JSONException {
    final Calendar dateProp = getCalendar("Europe/Amsterdam", 1383430039843L);
    final Calendar dateProp2 = getCalendar("UTC", 1253410638984L);
    final Design design = context.create().design("/etc/designs/test", "Test",
        "a", true,
        "b", 10L,
        "c", "test",
        "d", 100,
        "e", dateProp,
        "f", dateProp2);
    context.create().resource(design, "test", "a", false, "b", 440);

    final Map<String, Object> expectedData = new HashMap<>();
    if (context.resourceResolverType() == ResourceResolverType.JCR_OAK) {
      expectedData.put("jcr:created", "<value-ignored>");
      expectedData.put("jcr:createdBy", "admin");
    }
    expectedData.putAll(Map.of(
        "a", true,
        "b", 10,
        "c", "test",
        "d", 100,
        "e", "Sat Nov 02 2013 23:07:19 GMT+0100",
        "f", "Sun Sep 20 2009 01:37:18 GMT+0000",
        "jcr:title", "Test",
        "test", Map.of(
            "a", false,
            "b", 440)));
    JSONAssert.assertEquals(JSON_MAPPER.writeValueAsString(expectedData), design.getJSON(),
        new IgnoringFieldsComparator(JSONCompareMode.STRICT, "jcr:created"));
  }

  private static class IgnoringFieldsComparator extends DefaultComparator {

    @NotNull
    private final Set<String> ignoredFields;

    IgnoringFieldsComparator(@NotNull final JSONCompareMode mode, @NotNull final String... ignoredFields) {
      super(mode);
      this.ignoredFields = Set.of(ignoredFields);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void compareValues(final String prefix, final Object expectedValue, final Object actualValue, final JSONCompareResult result) throws JSONException {
      if (!ignoredFields.contains(prefix)) {
        super.compareValues(prefix, expectedValue, actualValue, result);
      }
    }
  }

  @NotNull
  private static Calendar getCalendar(@NotNull final String zoneId, final long millis) {
    final Calendar c1 = Calendar.getInstance(TimeZone.getTimeZone(zoneId));
    c1.setTimeInMillis(millis);
    return c1;
  }

}
