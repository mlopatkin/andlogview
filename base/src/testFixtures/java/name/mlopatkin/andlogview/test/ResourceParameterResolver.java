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

package name.mlopatkin.andlogview.test;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.google.common.io.Resources;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceParameterResolver implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        var parameter = parameterContext.getParameter();
        return parameter.isAnnotationPresent(Resource.class)
               && (parameter.getType() == Path.class || parameter.getType() == File.class);
    }

    @Override
    public @Nullable Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        var resourcePath = parameterContext.getParameter().getAnnotation(Resource.class).value();

        var resourceUrl = isAbsolute(resourcePath)
                ? Resources.getResource(getClass(), resourcePath)
                : Resources.getResource(extensionContext.getRequiredTestMethod().getDeclaringClass(), resourcePath);
        var tempDir = getTempDir(parameterContext, extensionContext);
        var targetFile = tempDir.resolve(Paths.get(resourcePath).getFileName());

        try {
            Resources.asByteSource(resourceUrl).copyTo(MoreFiles.asByteSink(targetFile));
        } catch (IOException e) {
            throw new ParameterResolutionException("Failed to write the contents", e);
        }
        if (parameterContext.getParameter().getType() == Path.class) {
            return targetFile;
        }
        return targetFile.toFile();
    }

    private boolean isAbsolute(String resourcePath) {
        return resourcePath.startsWith("/");
    }

    private Path getTempDir(ParameterContext parameterContext, ExtensionContext extensionContext) {
        var store = extensionContext.getStore(
                ExtensionContext.Namespace.create(getClass(), extensionContext.getUniqueId()));
        return store.getOrComputeIfAbsent(parameterContext.getIndex(), unused -> {
            try {
                return new ScopedDir(Files.createTempDirectory("junit-resource-"));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, ScopedDir.class).dir;
    }


    private static class ScopedDir implements ExtensionContext.Store.CloseableResource {
        final Path dir;

        public ScopedDir(Path dir) {
            this.dir = dir;
        }

        @Override
        public void close() throws IOException {
            MoreFiles.deleteRecursively(dir, RecursiveDeleteOption.ALLOW_INSECURE);
        }
    }
}
