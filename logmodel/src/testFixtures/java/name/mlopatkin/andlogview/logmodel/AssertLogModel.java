/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.logmodel;

import name.mlopatkin.andlogview.utils.TextUtils;

import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

public class AssertLogModel extends AbstractAssert<AssertLogModel, LogModel> {
    protected AssertLogModel(LogModel logModel) {
        super(logModel, AssertLogModel.class);
    }

    public static AssertLogModel assertThat(LogModel logModel) {
        return new AssertLogModel(logModel);
    }

    public AssertLogModel hasSize(int expectedSize) {
        if (actual.size() != expectedSize) {
            throw failureWithActualExpected(actual.size(), expectedSize,
                    "Expected model to have <%d> %s but got <%d> %s", expectedSize, pluralRecord(expectedSize),
                    actual.size(), pluralRecord(actual.size()));
        }
        return this;
    }

    public AssertLogModel isEmpty() {
        if (actual.size() > 0) {
            throw failure("Expected model to be empty but it has <%d> %s", actual.size(), pluralRecord(actual.size()));
        }
        return this;
    }

    public AssertLogModel hasRecordWithMessageAt(int position, String expectedMessage) {
        if (position < 0 || position >= actual.size()) {
            throw failure("Position <%d> is outside of bounds [<0>,<%d>)", position, actual.size());
        }

        String actualMessage = actual.getAt(position).getMessage();
        if (!Objects.equals(actualMessage, expectedMessage)) {
            throw failureWithActualExpected(actualMessage, expectedMessage,
                    "Expected record with message <%s> at <%d>, found <%s>", expectedMessage, position, actualMessage);
        }
        return this;
    }


    private String pluralRecord(int amount) {
        return TextUtils.plural(amount, "record", "records");
    }
}
