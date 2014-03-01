/*
 * Copyright 2014 Mikhail Lopatkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitbucket.mlopatkin.android.logviewer.filters;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

public class FilterJsonSerializationTest {

    private static Predicate<LogRecord> PREDICATE = Predicates.alwaysTrue();

    public static class Filter1 extends Filter {

        public final int value;

        public Filter1(int value) {
            super(PREDICATE);
            this.value = value;
        }

        @SuppressWarnings("unused")
        public Filter1(JSONObject obj) throws JSONException {
            super(PREDICATE);
            value = obj.getInt("value");
        }

        @Override
        public void toJson(JSONObject obj) throws JSONException {
            obj.put("value", value);
        }
    }

    @Test
    public void testSerialiazation() throws Exception {
        Filter1 f1 = new Filter1(1);

        ImmutableList<Class<?>> classes = ImmutableList.<Class<?>>of(Filter.class);
        FilterJsonSerialization serialization = new FilterJsonSerialization(f1, classes);

        JSONObject json = new JSONObject();
        serialization.toJson(json);

        FilterJsonSerialization deserialization = new FilterJsonSerialization(json);

        Assert.assertEquals(classes, deserialization.getBindedClasses());
        Assert.assertTrue(deserialization.getFilter() instanceof Filter1);
        Assert.assertNotSame(f1, deserialization.getFilter());
        Assert.assertEquals(f1.value, ((Filter1) deserialization.getFilter()).value);
    }
}
