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

package name.mlopatkin.andlogview.building

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Helper methods to access environment parameters: is build run by CI server? what revision is checked out?
 */
@CompileStatic
class BuildEnvironment {
    private static final Logger logger = LoggerFactory.getLogger('BuildEnvironment')
    private final File projectDir

    BuildEnvironment(File projectDir) {
        this.projectDir = projectDir
    }

    private static boolean isCiBuild() {
        return System.getenv('CI') == 'true'
    }

    static boolean isSnapshotBuild() {
        return !'false'.equalsIgnoreCase(System.getenv('LOGVIEW_SNAPSHOT_BUILD'))
    }

    String getSourceRevision() {
        if (isCiBuild()) {
            return firstNonNull(System.getenv('BITBUCKET_COMMIT'), System.getenv('GITHUB_SHA'))
        }

        return readRevWithGitDescribe()
    }

    private String readRevWithGitDescribe() {
        List<String> command = [
                'git',  // Only try to find Git in PATH
                'describe',
                '--always',  // Always output commit hash
                '--exclude=*',  // Ignore all tags, only hash is sufficient for now
                '--dirty=+'  // Add '+' suffix if the working copy is dirty
        ]
        try {
            Process gitDescribe = new ProcessBuilder(command)
                    .directory(projectDir) // Assume that projectDir is repo root as well
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .start()

            String result = gitDescribe.inputStream.getText('UTF-8').trim()
            waitFor(gitDescribe)
            return result
        } catch (IOException e) {
            // Git probably isn't installed but it isn't critical enough to fail build
            logger.warn('Failed to get version info from Git', e)
            return 'n/a'
        }
    }

    private static void waitFor(Process process) throws IOException, InterruptedException {
        int result = process.waitFor()
        if (result != 0) {
            throw new IOException("Process exited with exit code=$result")
        }
    }

    private static <T> T firstNonNull(T... args) {
        for (T arg : args) {
            if (arg != null) {
                return arg
            }
        }
        throw new NullPointerException("No nonnull argument given")
    }
}
