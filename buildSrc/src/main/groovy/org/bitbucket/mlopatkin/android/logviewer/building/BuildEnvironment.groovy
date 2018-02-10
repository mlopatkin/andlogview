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

package org.bitbucket.mlopatkin.android.logviewer.building

/**
 * Helper methods to access environemnt parameters: is build run by CI server? what revision is checked out?
 */
class BuildEnvironment {
    private final File projectDir
    private final String mercurialExecutable

    BuildEnvironment(File projectDir, String mercurialExecutable) {
        this.projectDir = projectDir
        this.mercurialExecutable = mercurialExecutable
    }

    boolean isCiBuild() {
        return System.getenv('CI') == 'true'
    }

    String getSourceRevision() {
        if (isCiBuild()) {
            return System.getenv('BITBUCKET_COMMIT')
        }

        return runMercurial(
                'id', '-i',
                '--color=none',
                '--encoding=utf-8')
    }

    String getBuildNumber() {
        if (isCiBuild()) {
            return System.getenv('BITBUCKET_BUILD_NUMBER')
        }
        return 'Dev'
    }

    private String runMercurial(String... args) {
        List<String> command = new ArrayList<String>(args.length + 1)
        command.add(mercurialExecutable)
        command.addAll(args)

        Process process = new ProcessBuilder(command)
                .directory(projectDir)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start()

        return process.getInputStream().getText('UTF-8').trim()
    }

}
