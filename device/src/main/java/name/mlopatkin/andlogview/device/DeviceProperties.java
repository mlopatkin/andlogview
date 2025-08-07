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

import com.android.ddmlib.IDevice;
import com.android.sdklib.AndroidVersion;

import org.jspecify.annotations.Nullable;

/**
 * The set of immutable device properties.
 */
class DeviceProperties {
    public static final String PROP_BUILD_PRODUCT = "ro.build.product";
    public static final String PROP_BUILD_API_LEVEL = IDevice.PROP_BUILD_API_LEVEL;
    public static final String PROP_BUILD_CODENAME = IDevice.PROP_BUILD_CODENAME;
    public static final String PROP_BUILD_FINGERPRINT = "ro.build.fingerprint";

    private final String product;
    private final AndroidVersion androidVersion;
    private final String buildFingerprint;

    public DeviceProperties(String product, String apiLevel, @Nullable String codename, String buildFingerprint) {
        this.product = product;
        // TODO(mlopatkin) Verify this works well on the preview builds.
        this.androidVersion = new AndroidVersion(Integer.parseInt(apiLevel), codename);
        this.buildFingerprint = buildFingerprint;
    }

    public String getProduct() {
        return product;
    }

    public String getBuildFingerprint() {
        return buildFingerprint;
    }

    public int getApiLevel() {
        return androidVersion.getApiLevel();
    }

    public String getApiVersionString() {
        return androidVersion.getApiString();
    }
}
