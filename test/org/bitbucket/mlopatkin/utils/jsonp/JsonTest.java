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

package org.bitbucket.mlopatkin.utils.jsonp;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class JsonTest {

    private static abstract class Base implements JsonWritable {

        @Override
        public void toJson(JSONObject obj) {
        }
    }

    private static class Dummy extends Base {

        final JSONObject object;

        public Dummy(JSONObject object) throws JSONException, NullPointerException, AssertionError {
            this.object = object;
        }
    }

    private static class Invalid extends Base {

    }

    private static class InvalidConstructor extends Base {

        private final JSONObject obj;
        private final String dummy;

        public InvalidConstructor(JSONObject obj, String dummy) {
            this.obj = obj;
            this.dummy = dummy;
        }
    }

    private static class InvalidForExceptions1 extends Base {

        private final JSONObject object;

        public InvalidForExceptions1(JSONObject object) throws JSONException, InterruptedException {
            this.object = object;
        }
    }

    private static class InvalidForExceptions2 extends Base {

        private final JSONObject object;

        public InvalidForExceptions2(JSONObject object) throws Throwable {
            this.object = object;
        }
    }

    private static class InvalidForExceptions3 extends Base {

        private final JSONObject object;

        public InvalidForExceptions3(JSONObject object) throws Exception {
            this.object = object;
        }
    }

    private static class Valid extends Base {

        private final JSONObject object;

        public Valid(JSONObject object) {
            this.object = object;
        }
    }

    private static class ValidWithNpe extends Base {

        private final JSONObject object;

        public ValidWithNpe(JSONObject object) throws NullPointerException {
            this.object = object;
        }
    }

    @Test
    public void testListBinder_withClazz() throws Exception {
        JsonBinder<List<Dummy>> binder = Json.bindList("str", Dummy.class);

        JSONArray dummyArray = new JSONArray(Arrays.asList(new JSONObject(), new JSONObject()));
        JSONObject obj = new JSONObject().put("str", dummyArray);

        Assert.assertNotNull(binder);

        List<Dummy> dummies = binder.get(obj);

        Assert.assertEquals(dummyArray.length(), dummies.size());

        for (int i = 0; i < dummies.size(); ++i) {
            Assert.assertSame(dummyArray.get(i), dummies.get(i).object);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListBinder_withNoConstructors() throws Exception {
        Json.bindList("str", Invalid.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListBinder_withInvalidConstructor() throws Exception {
        Json.bindList("str", InvalidConstructor.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListBinder_withInvalidException() throws Exception {
        Json.bindList("str", InvalidForExceptions1.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListBinder_withThrowable() throws Exception {
        Json.bindList("str", InvalidForExceptions2.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListBinder_withException() throws Exception {
        Json.bindList("str", InvalidForExceptions3.class);
    }

    @Test
    public void testListBinder_withValidThrows() throws Exception {
        Json.bindList("str", Dummy.class);
        Json.bindList("str", Valid.class);
        Json.bindList("str", ValidWithNpe.class);
    }
}
