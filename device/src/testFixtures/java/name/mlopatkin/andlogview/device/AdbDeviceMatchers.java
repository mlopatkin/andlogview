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

package name.mlopatkin.andlogview.device;

import name.mlopatkin.andlogview.test.AdaptingMatcher;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public class AdbDeviceMatchers {
    private AdbDeviceMatchers() {}

    public static <T extends ProvisionalDevice> Matcher<T> hasSerial(Matcher<? super String> serial) {
        return new AdaptingMatcher<>("serial", ProvisionalDevice::getSerialNumber, serial);
    }

    public static <T extends ProvisionalDevice> Matcher<T> hasSerial(String serial) {
        return hasSerial(Matchers.is(serial));
    }
}
