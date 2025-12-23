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

import com.google.common.annotations.VisibleForTesting;

import java.net.URI;

import javax.inject.Inject;

/**
 * A factory for the HTTP resources.
 */
class HttpClient {
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private final int connectTimeout;
    private final int readTimeout;

    @Inject
    public HttpClient() {
        this(CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS);
    }

    @VisibleForTesting
    HttpClient(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public HttpResource get(URI uri) {
        return new HttpResource(uri, connectTimeout, readTimeout);
    }
}
