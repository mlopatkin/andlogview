/*
 * Copyright 2018 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.building;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * A task to generate build metadata java class.
 */
public abstract class GenerateBuildMetadata extends DefaultTask {
    @Input
    public abstract Property<String> getRevision();

    @Input
    public abstract Property<String> getPackageName();

    @Input
    public abstract Property<String> getPropertyFile();

    @Input
    public abstract Property<String> getVersion();

    @OutputDirectory
    public abstract DirectoryProperty getInto();

    @TaskAction
    public void generate() throws IOException {
        var packageName = getPackageName().get();
        var outputDir = getInto().get().getAsFile();
        var targetDir = outputDir.toPath().resolve(packageName.replace('.', '/'));
        Files.createDirectories(targetDir);

        var fileName = getPropertyFile().get();
        var outputFile = targetDir.resolve(fileName);

        try (var out = Files.newBufferedWriter(outputFile, StandardCharsets.ISO_8859_1)) {
            out.write("REVISION=");
            out.write(getRevision().get());
            out.write('\n');
            out.write("VERSION=");
            out.write(getVersion().get());
            out.write('\n');
        }
    }
}
