/*
 * Copyright 2022 Mikhail Lopatkin
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
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

public abstract class GenerateNotices extends DefaultTask {
    private ArtifactCollection packagedClasspath;
    private final Map<String, File> sourceFileNoticesByFileMask = new HashMap<>();

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public FileCollection getPackagedClasspath() {
        // This is a workaround for Gradle quirk: ArtifactCollection cannot be specified as input.
        // See https://github.com/gradle/gradle/issues/19490
        return packagedClasspath.getArtifactFiles();
    }

    public void setPackagedClasspath(Configuration configuration) {
        packagedClasspath = configuration.getIncoming().getArtifacts();
    }

    @InputDirectory
    public abstract DirectoryProperty getLibraryNoticesDirectory();

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getSourceFilesNotices();

    @OutputFile
    public abstract RegularFileProperty getOutputNoticeFile();

    @Inject
    public GenerateNotices(ProjectLayout layout) {
        getOutputNoticeFile().convention(layout.getBuildDirectory().file("generated/license/NOTICE"));
    }

    @TaskAction
    public void generateNotice() throws IOException {
        try (BufferedWriter notice = Files.newBufferedWriter(getOutputNoticeFile().get().getAsFile().toPath())) {
            boolean first = true;
            for (File sourceFileNotice : getSourceFilesNotices()) {
                if (!first) {
                    notice.newLine();
                    notice.newLine();
                }
                appendNoticeFromFile(notice, sourceFileNotice);
                first = false;
            }
            for (ModuleComponentIdentifier dependency : getDependencies()) {
                if (!first) {
                    notice.newLine();
                    notice.newLine();
                }
                appendNoticeFromFile(notice,
                        findNoticeFileForDependency(dependency)
                                .orElseThrow(() -> new GradleException("Failed to find notice for " + dependency)));
                first = false;
            }
        }
    }

    private Optional<File> findNoticeFileForDependency(ModuleComponentIdentifier dependency) throws IOException {
        String noticeFileName = String.format("%s.%s.%s.NOTICE", dependency.getGroup(), dependency.getModule(),
                dependency.getVersion());
        File noticeFile = getLibraryNoticesDirectory().file(noticeFileName).get().getAsFile();
        if (noticeFile.exists() && noticeFile.isFile()) {
            return Optional.of(noticeFile);
        }
        return Optional.empty();
    }

    private Iterable<ModuleComponentIdentifier> getDependencies() {
        return packagedClasspath.getArtifacts().stream()
                .map(artifact -> artifact.getId().getComponentIdentifier())
                .filter(componentId -> componentId instanceof ModuleComponentIdentifier)
                .map(componentId -> (ModuleComponentIdentifier) componentId)
                .collect(Collectors.toList());
    }

    private void appendNoticeFromFile(BufferedWriter notice, File noticeFile) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(noticeFile.toPath())) {
            reader.lines().forEach(line -> {
                try {
                    notice.write(line);
                    notice.newLine();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }
}
