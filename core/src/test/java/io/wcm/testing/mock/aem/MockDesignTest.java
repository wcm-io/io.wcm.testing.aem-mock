package io.wcm.testing.mock.aem;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

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

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockDesignTest {

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
    public void getJSON() throws JSONException {
        final Calendar dateProp = getCalendar("Europe/Amsterdam", 1383430039843L);
        final Calendar dateProp2 = getCalendar("UTC", 1253410638984L);
        final Design design = context.create().design("/etc/designs/test", "Test",
                "a", true,
                "b", 10L,
                "c", "test",
                "d", 100,
                "e", dateProp,
                "f", dateProp2);
        final JsonObjectBuilder expectedJson = Json
                .createObjectBuilder()
                .add("a", true)
                .add("b", 10)
                .add("c", "test")
                .add("d", 100)
                .add("e", "Sat Nov 02 2013 23:07:19 GMT+0100")
                .add("f", "Sun Sep 20 2009 01:37:18 GMT+0000")
                .add("jcr:title", "Test");
        if (context.resourceResolverType() == ResourceResolverType.JCR_OAK) {
            expectedJson
                    .add("jcr:created", "<value-ignored>")
                    .add("jcr:createdBy", "admin");
        }
        JSONAssert.assertEquals(expectedJson.build().toString(), design.getJSON(),
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