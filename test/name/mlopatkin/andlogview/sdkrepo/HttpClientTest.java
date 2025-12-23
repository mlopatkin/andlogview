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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.google.common.net.HttpHeaders.CONTENT_LENGTH;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.HttpHeaders.LOCATION;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.guava.api.Assertions.assertThat;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.io.ByteSource;
import com.google.common.net.MediaType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junitpioneer.jupiter.cartesian.CartesianTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

class HttpClientTest {
    private static final String TEXT_PLAIN = MediaType.PLAIN_TEXT_UTF_8.toString();
    private static final String OCTET_STREAM = MediaType.OCTET_STREAM.toString();

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private final HttpClient httpClient = new HttpClient();

    @ParameterizedTest
    @EnumSource
    void canDownloadResourceSuccessfully(DownloadMode mode) throws IOException {
        byte[] testData = testBytes("Hello, WireMock!");

        stubFor(
                get(urlEqualTo("/file.txt")).willReturn(aResponse()
                        .withStatus(200)
                        .withBody(testData)
                )
        );

        var downloadedBytes =
                mode.download(httpClient.get(uri("/file.txt")), testData.length, new ByteArrayOutputStream());

        assertThat(downloadedBytes.toByteArray()).isEqualTo(testData);
    }

    @ParameterizedTest
    @CsvSource({
            "10, 100",
            "10, 10"
    })
    void canFetchDataWithSizeLimit(int dataLength, int maxLength) throws Exception {
        byte[] testData = testBytesOfLength(dataLength);

        stubFor(
                get("/file.bin").willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader(CONTENT_TYPE, OCTET_STREAM)
                        .withHeader(CONTENT_LENGTH, String.valueOf(dataLength))
                        .withBody(testData)
                )
        );

        var downloadedBytes = httpClient.get(uri("/file.bin")).download(maxLength).read();

        assertThat(downloadedBytes).isEqualTo(testData);
    }

    @ParameterizedTest
    @CsvSource({
            "10, 100",
            "10, 10"
    })
    void canDownloadBoundedResourceWithoutContentLengthHeader(int dataLength, int maxLength) throws Exception {
        byte[] testData = testBytesOfLength(dataLength);

        stubFor(
                get("/file.bin").willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader(CONTENT_TYPE, OCTET_STREAM)
                        .withBody(testData)
                )
        );

        var downloadedBytes = httpClient.get(uri("/file.bin")).download(maxLength).read();

        assertThat(downloadedBytes).isEqualTo(testData);
    }

    @Test
    void downloadByteSourceCanBeOpenedMultipleTimes() throws Exception {
        byte[] testData = testBytes("Some data");

        stubFor(
                get("/file.txt").willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader(CONTENT_TYPE, TEXT_PLAIN)
                        .withHeader(CONTENT_LENGTH, String.valueOf(testData.length))
                        .withBody(testData)
                )
        );

        var downloadedBytes = httpClient.get(uri("/file.txt")).download(testData.length);

        assertThat(downloadedBytes).hasSameContentAs(ByteSource.wrap(testData));
        assertThat(downloadedBytes.read()).isEqualTo(testData);
    }

    @CartesianTest
    void downloadingByteSourceCanFollowRedirects(
            @CartesianTest.Values(
                    ints = {HTTP_MOVED_PERM, HTTP_MOVED_TEMP, HTTP_SEE_OTHER, 307}
            ) int responseCode,
            @CartesianTest.Enum DownloadMode mode
    ) throws Exception {
        byte[] testData = testBytes("Some data");

        stubFor(
                get("/initial-request").willReturn(aResponse()
                        .withStatus(responseCode)
                        .withHeader(LOCATION, uri("/redirected").toASCIIString())
                )
        );

        stubFor(
                get("/redirected").willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader(CONTENT_TYPE, TEXT_PLAIN)
                        .withHeader(CONTENT_LENGTH, String.valueOf(testData.length))
                        .withBody(testData)
                )
        );

        var downloadedBytes =
                mode.download(httpClient.get(uri("/initial-request")), testData.length, new ByteArrayOutputStream());

        assertThat(downloadedBytes.toByteArray()).isEqualTo(testData);
    }

    @ParameterizedTest
    @Timeout(3)
    @EnumSource(DownloadMode.class)
    void canHandleConnectionTimeouts(DownloadMode mode) throws Exception {
        // Use small connect timeout to run test faster. Read timeout is longer than test timeout to fail the test
        // if something goes wrong.
        var smallConnectTimeoutClient = new HttpClient(100, 5000);
        var timeoutUri = URI.create("http://192.168.255.255:12345/file.txt");

        assertThatThrownBy(
                () -> mode.download(smallConnectTimeoutClient.get(timeoutUri))
        ).isInstanceOf(IOException.class);
    }

    @ParameterizedTest
    @Timeout(3)
    @EnumSource(DownloadMode.class)
    void canHandleReadTimeouts(DownloadMode mode) {
        // Use small read timeout to run test faster. Connect timeout is longer than test timeout to fail the test
        // if something goes wrong.
        var smallReadTimeoutClient = new HttpClient(5000, 100);

        stubFor(
                get("/slow-response").willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader(CONTENT_TYPE, TEXT_PLAIN)
                        .withFixedDelay(500) // Delay longer than the read timeout
                        .withBody("This response will timeout")
                )
        );

        assertThatThrownBy(
                () -> mode.download(smallReadTimeoutClient.get(uri("/slow-response")))
        ).isInstanceOf(IOException.class);
    }

    private static URI uri(String path) {
        return URI.create(wm.url(path));
    }

    private static byte[] testBytes(String message) {
        return message.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] testBytesOfLength(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; ++i) {
            bytes[i] = (byte) (i & 0xFF);
        }
        return bytes;
    }

    private static StubMapping stubFor(MappingBuilder mapping) {
        return wm.stubFor(mapping);
    }

    @SuppressWarnings("unused") // Constants are used in the parameterized tests.
    enum DownloadMode {
        DATA {
            @Override
            <T extends OutputStream> T download(HttpResource resource, int sizeLimit, T destination)
                    throws IOException {
                resource.download(sizeLimit).copyTo(destination);
                return destination;
            }
        },
        STREAMING {
            @Override
            <T extends OutputStream> T download(HttpResource resource, int sizeLimit, T destination)
                    throws IOException {
                resource.downloadInto(destination);
                return destination;
            }
        };

        abstract <T extends OutputStream> T download(HttpResource resource, int sizeLimit, T destination)
                throws IOException;

        void download(HttpResource resource) throws IOException {
            download(resource, Integer.MAX_VALUE, new ByteArrayOutputStream());
        }
    }
}
