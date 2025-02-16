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

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * An HTTP resource that can be downloaded.
 */
class HttpResource {
    private static final Logger log = LoggerFactory.getLogger(HttpResource.class);

    @FunctionalInterface
    private interface IoFunction<T, R> {
        R apply(T argument) throws IOException;
    }

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private final URI uri;

    HttpResource(URI uri) {
        Preconditions.checkArgument(
                "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()),
                "Only support http and https URIs, got %s", uri
        );
        this.uri = uri;
    }

    public ByteSource download(long sizeLimit) throws IOException {
        return withUrlConnection(connection -> {
            var length = connection.getContentLengthLong();
            if (length > sizeLimit) {
                throw new IOException("The resource length " + length + " is greater than the size limit " + sizeLimit);
            }
            try (
                    var unbounded = connection.getInputStream();
                    var input = ByteStreams.limit(unbounded, sizeLimit)
            ) {
                var bytes = ByteStreams.toByteArray(input);
                if (unbounded.read() >= 0) {
                    throw new IOException("The resource length is greater than the size limit " + sizeLimit);
                }
                return ByteSource.wrap(bytes);
            }
        });
    }

    @SuppressWarnings("NullAway")
    public void downloadInto(OutputStream output) throws IOException {
        withUrlConnection(connection -> {
            try (var input = connection.getInputStream()) {
                ByteStreams.copy(input, output);
            }
            return null;
        });
    }

    private <T> T withUrlConnection(IoFunction<? super HttpURLConnection, T> function) throws IOException {
        var connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);

        log.info("Initiating HTTP request to {}", uri);
        try {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Server returned " + connection.getResponseCode());
            }

            return function.apply(connection);
        } finally {
            connection.disconnect();
            log.info("HTTP request to {} completed", uri);
        }
    }
}
