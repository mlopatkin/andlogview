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
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;

import java.net.URI;
import java.util.Comparator;

public class SdkPackage implements Comparable<SdkPackage> {
    /**
     * A package with ADB and fastboot.
     */
    public static final String PLATFORM_TOOLS = "platform-tools";

    private static final Comparator<SdkPackage> COMPARATOR =
            Comparator.comparing(SdkPackage::getName).thenComparing(SdkPackage::getTargetOs);

    public enum TargetOs {
        LINUX,
        MAC_OS,
        WINDOWS
    }

    private final String name;
    private final String license;
    private final URI downloadUrl;
    private final TargetOs targetOs;
    private final HashFunction checksumAlgorithm;
    private final HashCode checksum;

    public SdkPackage(String name, String license, URI downloadUrl, TargetOs targetOs, HashFunction checksumAlgorithm,
            HashCode checksum) {
        Preconditions.checkArgument(checksumAlgorithm.bits() == checksum.bits(),
                "Hash function %s doesn't produce the same number of bits as in %s", checksumAlgorithm, checksum);
        this.name = name;
        this.license = license;
        this.downloadUrl = downloadUrl;
        this.targetOs = targetOs;
        this.checksumAlgorithm = checksumAlgorithm;
        this.checksum = checksum;
    }

    public String getName() {
        return name;
    }

    public String getLicense() {
        return license;
    }

    public URI getDownloadUrl() {
        return downloadUrl;
    }

    public TargetOs getTargetOs() {
        return targetOs;
    }

    public HashFunction getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    public HashCode getPackageChecksum() {
        return checksum;
    }

    @Override
    public String toString() {
        return String.format("SdkPackage{%s; hostOs=%s, url=%s}", name, targetOs, downloadUrl);
    }

    @Override
    public int compareTo(SdkPackage o) {
        return COMPARATOR.compare(this, o);
    }
}
