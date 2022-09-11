/*
 * Copyright 2021 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.liblogcat.ddmlib;

import name.mlopatkin.andlogview.device.Device;
import name.mlopatkin.andlogview.liblogcat.SourceMetadata;
import name.mlopatkin.andlogview.liblogcat.SourceMetadataItem;

import java.util.Collection;
import java.util.Collections;

class AdbSourceMetadata implements SourceMetadata {
    private final Device device;

    public AdbSourceMetadata(Device device) {
        this.device = device;
    }

    @Override
    public Collection<SourceMetadataItem> getMetadataItems() {
        return Collections.singleton(new SourceMetadataItem("serial", device.getSerialNumber()));
    }
}
