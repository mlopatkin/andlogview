/*
 * Copyright 2020 Mikhail Lopatkin
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

package name.mlopatkin.andlogview;

import name.mlopatkin.andlogview.utils.properties.IllegalConfigurationException;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.util.List;

/**
 * Command-line arguments.
 */
public class CommandLine {
    private final boolean shouldShowUsage;
    private final boolean debug;
    private final @Nullable File fileToOpen;

    CommandLine(String[] args) throws IllegalConfigurationException {
        shouldShowUsage = false;
        OptionParser parser = new OptionParser("d");
        try {
            OptionSet result = parser.parse(args);
            debug = result.has("d");

            @SuppressWarnings("unchecked")
            List<String> files = (List<String>) result.nonOptionArguments();
            if (files.size() == 0) {
                // ADB mode
                fileToOpen = null;
            } else if (files.size() == 1) {
                // File mode
                fileToOpen = new File(files.get(0));
            } else {
                throw new IllegalConfigurationException("Expected only one file on the command line");
            }
        } catch (OptionException e) {
            throw new IllegalConfigurationException(e);
        }
    }

    CommandLine() {
        shouldShowUsage = true;
        debug = false;
        fileToOpen = null;
    }

    public @Nullable File getFileArgument() {
        return fileToOpen;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isShouldShowUsage() {
        return shouldShowUsage;
    }
}
