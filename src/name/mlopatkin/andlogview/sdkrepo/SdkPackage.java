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
import com.google.common.hash.HashFunction;

import java.net.URI;

public interface SdkPackage {
    enum TargetOs {
        LINUX,
        MAC_OS,
        WINDOWS
    }
    String getLicense();

    URI getDownloadUrl();

    long getSize();

    TargetOs getTargetOs();

    HashFunction getChecksumAlgorithm();

    HashCode getPackageChecksum();
}
