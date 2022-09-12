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

package name.mlopatkin.andlogview.liblogcat.file;

import name.mlopatkin.andlogview.logmodel.SourceMetadata;
import name.mlopatkin.andlogview.logmodel.SourceMetadataItem;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.Collection;

class FileSourceMetadata implements SourceMetadata {
    private final ImmutableList<SourceMetadataItem> metadataItems;

    public FileSourceMetadata(File path) {
        metadataItems = ImmutableList.of(
                new SourceMetadataItem("name", path.getName()),
                new SourceMetadataItem("path", path.getAbsolutePath()));
    }

    @Override
    public Collection<SourceMetadataItem> getMetadataItems() {
        return metadataItems;
    }

}
