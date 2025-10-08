/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.sdkrepo;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import java.net.URI;

public class TestSdkPackage {
    public static final String TEST_LICENSE = "Android SDK License Text";

    public static SdkPackage createPackage(String packageName) {
        return new SdkPackage(
                packageName,
                TEST_LICENSE,
                URI.create("https://example.com"),
                SdkPackage.TargetOs.LINUX,
                Hashing.sha256(),
                HashCode.fromString("0000000000000000000000000000000000000000000000000000000000000000")
        );
    }
}
