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

import java.io.File;
import java.io.IOException;

/**
 * Exception thrown when attempting to install SDK into a non-empty directory
 * in {@link SdkRepository.InstallMode#FAIL_IF_NOT_EMPTY} mode.
 */
public class TargetDirectoryNotEmptyException extends IOException {
    private final File directory;

    public TargetDirectoryNotEmptyException(File directory) {
        super("Directory is not empty: " + directory.getAbsolutePath());
        this.directory = directory;
    }

    public File getDirectory() {
        return directory;
    }
}
