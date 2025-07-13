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

import com.google.common.base.MoreObjects;

import org.checkerframework.checker.nullness.qual.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

/**
 * A definition of the repo manifest XML schema. It only includes the attributes and tags we care about.
 */
final class SdkRepoSchema {
    private SdkRepoSchema() {}

    // The schemas can be found at:
    // https://cs.android.com/android-studio/platform/tools/base/+/mirror-goog-studio-main:repository/src/main/resources/xsd/repo-common-02.xsd;drc=8adec39cddd9f0f4475c0b5c1231f7ad1ad64fae
    // https://cs.android.com/android-studio/platform/tools/base/+/mirror-goog-studio-main:sdklib/src/main/resources/xsd/sdk-repository-03.xsd;drc=e29705058e2f503ff6757fcaed7b0f9f67f2d3b0
    private static final QName SDK_REPOSITORY_TAG =
            new QName("http://schemas.android.com/sdk/android/repo/repository2/03", "sdk-repository");

    public static boolean isRoot(StartElement tag) {
        return SDK_REPOSITORY_TAG.equals(tag.getName());
    }

    public static class License { // <license id="..." [type="text"]>
        private License() {}

        private static final QName LICENSE_TAG = new QName("license");

        public static boolean is(StartElement tag) {
            return LICENSE_TAG.equals(tag.getName());
        }

        private static final QName LICENSE_ID_ATTR = new QName("id");

        public static @Nullable String getId(StartElement tag) {
            return getAttrValue(tag, LICENSE_ID_ATTR);
        }

        private static final QName LICENSE_TYPE_ATTR = new QName("type");
        public static String LICENSE_TYPE_TEXT = "text";
        private static final String DEFAULT_LICENSE_TYPE = LICENSE_TYPE_TEXT;

        public static String getType(StartElement tag) {
            return getAttrValue(tag, LICENSE_TYPE_ATTR, DEFAULT_LICENSE_TYPE);
        }
    } // </license>

    public static class RemotePackage { // <remotePackage path="...">
        private RemotePackage() {}

        private static final QName REMOTE_PACKAGE_TAG = new QName("remotePackage");

        public static boolean is(StartElement tag) {
            return REMOTE_PACKAGE_TAG.equals(tag.getName());
        }

        private static final QName REMOTE_PACKAGE_PATH_ATTR = new QName("path");

        public static @Nullable String getPath(StartElement tag) {
            return getAttrValue(tag, REMOTE_PACKAGE_PATH_ATTR);
        }

        public static class UsesLicense { // <uses-license ref="..." />
            private UsesLicense() {}

            private static final QName USES_LICENSE_TAG = new QName("uses-license");

            public static boolean is(StartElement tag) {
                return USES_LICENSE_TAG.equals(tag.getName());
            }

            private static final QName USES_LICENSE_REF_ATTR = new QName("ref");

            public static @Nullable String getRef(StartElement tag) {
                return getAttrValue(tag, USES_LICENSE_REF_ATTR);
            }
        }

        public static class ChannelRef { // <channelRef ref="channel-2" />
            public static final String STABLE_CHANNEL = "channel-0";

            private static final QName CHANNEL_REF_TAG = new QName("channelRef");
            private static final QName REF_ATTR = new QName("ref");

            public static boolean is(StartElement channelRef) {
                return CHANNEL_REF_TAG.equals(channelRef.getName());
            }

            public static @Nullable String getRef(StartElement tag) {
                return getAttrValue(tag, REF_ATTR);
            }
        }

        public static class Archives { // <archives>
            private Archives() {}

            private static final QName ARCHIVES_TAG = new QName("archives");

            public static boolean is(StartElement tag) {
                return ARCHIVES_TAG.equals(tag.getName());
            }

            public static class Archive { // <archive>
                private Archive() {}

                private static final QName ARCHIVE_TAG = new QName("archive");

                public static boolean is(StartElement archive) {
                    return ARCHIVE_TAG.equals(archive.getName());
                }

                public static class Complete { // <complete>
                    private Complete() {}

                    private static final QName COMPLETE_TAG = new QName("complete");

                    public static boolean is(StartElement tag) {
                        return COMPLETE_TAG.equals(tag.getName());
                    }

                    public static class Checksum { // <checksum type="...">...</checksum>
                        private Checksum() {}

                        private static final QName CHECKSUM_TAG = new QName("checksum");

                        public static boolean is(StartElement tag) {
                            return CHECKSUM_TAG.equals(tag.getName());
                        }

                        private static final QName CHECKSUM_TYPE_ATTR = new QName("type");
                        private static final String DEFAULT_CHECKSUM_TYPE = "sha1";

                        public static String getType(StartElement tag) {
                            return getAttrValue(tag, CHECKSUM_TYPE_ATTR, DEFAULT_CHECKSUM_TYPE);
                        }
                    }

                    public static class Url { // <url>...<url>
                        private Url() {}

                        private static final QName URL_TAG = new QName("url");

                        public static boolean is(StartElement tag) {
                            return URL_TAG.equals(tag.getName());
                        }
                    }
                } // </complete>

                public static class HostOs { // <host-os>...</host-os>
                    private HostOs() {}

                    private static final QName HOST_OS_TAG = new QName("host-os");

                    public static boolean is(StartElement tag) {
                        return HOST_OS_TAG.equals(tag.getName());
                    }

                    public static final String LINUX = "linux";
                    public static final String MAC_OS = "macosx";
                    public static final String WINDOWS = "windows";
                }
            }  // </archive>
        } // </archives>
    } // </remotePackage>

    /**
     * Returns the value of the given attribute or null if this attribute is not present
     *
     * @param tag the tag to get the attribute from
     * @param attributeName the qualified name of the attribute
     * @return the value of the attribute or null
     */
    private static @Nullable String getAttrValue(StartElement tag, QName attributeName) {
        var attribute = tag.getAttributeByName(attributeName);
        return attribute != null ? attribute.getValue() : null;
    }

    /**
     * Returns the value of the given attribute or default value if this attribute is not present
     *
     * @param tag the tag to get the attribute from
     * @param attributeName the qualified name of the attribute
     * @param defaultValue the default value to use when the attribute is not present
     * @return the value of the attribute or default value
     */
    private static String getAttrValue(StartElement tag, QName attributeName, String defaultValue) {
        return MoreObjects.firstNonNull(getAttrValue(tag, attributeName), defaultValue);
    }
}
