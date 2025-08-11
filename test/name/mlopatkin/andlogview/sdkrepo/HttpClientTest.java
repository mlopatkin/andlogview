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
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    @Test
    void downloadsFileSuccessfully() throws IOException {
        byte[] testData = testBytes("Hello, WireMock!");

        stubFor(
                get(urlEqualTo("/file.txt")).willReturn(aResponse()
                        .withStatus(200)
                        .withBody(testData)
                )
        );

        var resource = httpClient.get(uri("/file.txt"));

        var downloadedBytes = new ByteArrayOutputStream();
        resource.downloadInto(downloadedBytes);

        assertThat(downloadedBytes.toByteArray()).isEqualTo(testData);
    }

    @ParameterizedTest
    @CsvSource({
            "10, 100",
            "10, 10"
    })
    void canDownloadBoundedResourceWithContentLengthHeader(int dataLength, int maxLength) throws Exception {
        byte[] testData = testBytesOfLength(dataLength);

        stubFor(
                get("/file.bin").willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader(CONTENT_TYPE, OCTET_STREAM)
                        .withHeader(CONTENT_LENGTH, String.valueOf(dataLength))
                        .withBody(testData)
                )
        );

        var resource = httpClient.get(uri("/file.bin"));

        var downloadedBytes = resource.download(maxLength).read();

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

        var resource = httpClient.get(uri("/file.bin"));

        var downloadedBytes = resource.download(maxLength).read();

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

        var resource = httpClient.get(uri("/file.txt"));

        var downloadedBytes = resource.download(testData.length);

        assertThat(downloadedBytes).hasSameContentAs(ByteSource.wrap(testData));
        assertThat(downloadedBytes.read()).isEqualTo(testData);
    }

    @ParameterizedTest
    @ValueSource(
            ints = {
                    HTTP_MOVED_PERM, HTTP_MOVED_TEMP, HTTP_SEE_OTHER, 307
            }
    )
    void downloadingByteSourceCanFollowRedirects(int responseCode) throws Exception {
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

        var resource = httpClient.get(uri("/initial-request"));
        var downloadedBytes = resource.download(testData.length);

        assertThat(downloadedBytes.read()).isEqualTo(testData);
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
}
