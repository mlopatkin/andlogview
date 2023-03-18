/*
 * Copyright 2023 the Andlogview authors
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

package name.mlopatkin.andlogview.parsers.logcat;

import name.mlopatkin.andlogview.base.AppResources;
import name.mlopatkin.andlogview.logmodel.Field;
import name.mlopatkin.andlogview.logmodel.LogRecord;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharSource;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A JSON-serializable representation of a logcat log in different formats. The JSON file contains a list of "golden"
 * entries in a parsed form, and a set of filenames pointing to logs in different formats.
 */
public class LogcatTestJsonModel {
    private final List<LogcatEntryJsonModel> entries = Collections.emptyList();
    private final Map<Format, String> formats = Collections.emptyMap();

    /**
     * Returns a text resource for the provided format if it is available.
     *
     * @param basePath the base path to resources
     * @param format the desired format
     * @return the resource or empty optional if there is no resource for this format
     */
    public Optional<CharSource> getResourceForFormat(String basePath, Format format) {
        return Optional.ofNullable(formats.get(format)).map(relPath ->
                AppResources.getResource(basePath + "/" + relPath).asCharSource(StandardCharsets.UTF_8));
    }

    /**
     * Returns the "golden" list of records. The available fields are limited to what is available in the format.
     *
     * @param format the format of the record
     * @return the list of records in this test model
     */
    public List<LogRecord> getRecords(Format format) {
        var requestedFields = format.getAvailableFields();
        Preconditions.checkArgument(requestedFields.contains(Field.MESSAGE), "Message field is mandatory");
        Preconditions.checkArgument(!requestedFields.contains(Field.APP_NAME), "App name is not yet supported");
        Preconditions.checkArgument(!requestedFields.contains(Field.BUFFER), "Buffer is not yet supported");
        return entries.stream()
                .map(entry -> entry.buildRecord(requestedFields))
                .collect(Collectors.toList());
    }

    public Set<Format> getFormats() {
        return ImmutableSet.copyOf(formats.keySet());
    }
}
