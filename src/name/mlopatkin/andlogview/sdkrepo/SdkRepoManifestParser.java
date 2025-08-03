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

import name.mlopatkin.andlogview.sdkrepo.SdkRepoSchema.License;
import name.mlopatkin.andlogview.sdkrepo.SdkRepoSchema.RemotePackage;
import name.mlopatkin.andlogview.sdkrepo.SdkRepoSchema.RemotePackage.Archives;
import name.mlopatkin.andlogview.sdkrepo.SdkRepoSchema.RemotePackage.Archives.Archive;
import name.mlopatkin.andlogview.sdkrepo.SdkRepoSchema.RemotePackage.Archives.Archive.Complete;
import name.mlopatkin.andlogview.sdkrepo.SdkRepoSchema.RemotePackage.Archives.Archive.Complete.Checksum;
import name.mlopatkin.andlogview.sdkrepo.SdkRepoSchema.RemotePackage.Archives.Archive.Complete.Url;
import name.mlopatkin.andlogview.sdkrepo.SdkRepoSchema.RemotePackage.Archives.Archive.HostOs;
import name.mlopatkin.andlogview.sdkrepo.SdkRepoSchema.RemotePackage.ChannelRef;
import name.mlopatkin.andlogview.sdkrepo.SdkRepoSchema.RemotePackage.UsesLicense;
import name.mlopatkin.andlogview.utils.Try;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

/**
 * A parser for the Android SDK manifest XML. See {@link SdkRepoSchema} for the schema description.
 * <p>
 * As we don't really control the server side, the parser is designed to be lenient. It still fails if the document is
 * invalid XML or cannot be read, but allows certain degree of schema violations. For example, if the package doesn't
 * specify some important data, it is skipped, but other packages may still be retrieved.
 * <p>
 * The document is still expected to conform to the shape defined by the schema. For example, the parser will abort if
 * the root tag is different.
 */
class SdkRepoManifestParser implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(SdkRepoManifestParser.class);

    private static final String SUPPORTED_LICENSE_TYPE = License.LICENSE_TYPE_TEXT;

    private final URI baseUri;
    private final InputStream manifest;

    private final Map<String, String> licenses = new HashMap<>();
    private final StackedXmlReader reader;

    public SdkRepoManifestParser(URI baseUri, ByteSource manifest) throws ManifestParseException {
        try {
            this.baseUri = baseUri;
            this.manifest = manifest.openStream();
            this.reader = new StackedXmlReader(newXmlFactory().createXMLEventReader(this.manifest));
        } catch (IOException | XMLStreamException e) {
            throw new ManifestParseException("Failed to prepare parser", e);
        }
    }

    public List<SdkPackage> read(String packageName) throws ManifestParseException {
        try {
            var sdkRepo = Objects.requireNonNull(reader.nextTag());
            if (!SdkRepoSchema.isRoot(sdkRepo)) {
                throw new ManifestParseException("Invalid root tag " + sdkRepo.getName());
            }

            var builder = ImmutableList.<SdkPackage>builderWithExpectedSize(getExpectedPackagesCount());

            for (var tag : reader.childrenOf(sdkRepo)) {
                if (License.is(tag)) {
                    readLicense(tag);
                } else if (RemotePackage.is(tag) && packageName.equals(RemotePackage.getPath(tag))) {
                    readPackage(tag, builder::add);
                } else {
                    reader.skipTag(tag);
                }
            }

            return builder.build();
        } catch (XMLStreamException e) {
            throw new ManifestParseException("Failed to parse manifest", e);
        }
    }

    private void readLicense(StartElement licenseTag) throws XMLStreamException {
        var licenseId = Objects.requireNonNull(License.getId(licenseTag));
        var licenseType = License.getType(licenseTag);

        if (!SUPPORTED_LICENSE_TYPE.equals(licenseType)) {
            reader.skipTag(licenseTag);
            return;
        }

        licenses.put(licenseId, reader.getCurrentElementText());
    }

    private void readPackage(StartElement packageTag, Consumer<? super SdkPackage> packageCollector)
            throws ManifestParseException, XMLStreamException {
        var path = Objects.requireNonNull(RemotePackage.getPath(packageTag));

        String licenseId = null;
        var packages = new ArrayList<SdkPackageBuilder>(getExpectedPackagesCount());

        for (var tag : reader.childrenOf(packageTag)) {
            if (UsesLicense.is(tag)) {
                licenseId = UsesLicense.getRef(tag);
            } else if (Archives.is(tag)) {
                readArchives(tag, path, packages::add);
            } else if (ChannelRef.is(tag)) {
                var channel = ChannelRef.getRef(tag);
                if (!ChannelRef.STABLE_CHANNEL.equals(channel)) {
                    // This is a package from pre-stable channel, skip it.
                    reader.skipTag(packageTag);
                    return;
                }
            } else {
                reader.skipTag(tag);
            }
        }

        if (licenseId == null) {
            throw new ManifestParseException("The package does not have a license id");
        }
        var license = licenses.get(licenseId);
        if (license == null) {
            throw new ManifestParseException("The license with id '" + licenseId + "' cannot be found");
        }

        Objects.requireNonNull(packages).stream()
                .map(b -> b.setLicense(license))
                .map(b -> Try.ofCallable(b::build).handleError(th -> log.error("Failed to parse archive", th)))
                .filter(Try::isPresent)
                .map(Try::get)
                .forEach(packageCollector);
    }

    private void readArchives(
            StartElement archivesTag, String path,
            Consumer<? super SdkPackageBuilder> packageCollector
    ) throws XMLStreamException {
        for (var tag : reader.childrenOf(archivesTag)) {
            if (Archive.is(tag)) {
                Try.ofCallable(() -> readArchive(path, tag))
                        .handleError(th -> log.error("Failed to parse archive", th))
                        .toOptional()
                        .ifPresent(packageCollector);
            } else {
                reader.skipTag(tag);
            }
        }
    }

    private SdkPackageBuilder readArchive(String path, StartElement archiveTag)
            throws XMLStreamException, ManifestParseException {
        try {
            var builder = new SdkPackageBuilder(baseUri, path);
            for (var tag : reader.childrenOf(archiveTag)) {
                if (Complete.is(tag)) {
                    readComplete(builder, tag);
                } else if (HostOs.is(tag)) {
                    builder.setHostOs(findTargetOs(reader.getCurrentElementText()));
                } else {
                    reader.skipTag(tag);
                }
            }
            return builder;
        } catch (ManifestParseException e) {
            throw skipAndRethrow(archiveTag, e);
        }
    }

    /**
     * Skips the current tag and rethrows the parse exception. Suppresses the parse exception if the document is
     * malformed. This should be used to recover the document position when parsing the tag's contents fails.
     *
     * @param tag the tag to skip
     * @param parseException the exception caused by reading this tag
     * @return nothing, this method always throws
     * @throws XMLStreamException if the document is malformed. The parse exception is suppressed by this one
     * @throws ManifestParseException the provided parse exception, always if skipping was successful
     */
    private ManifestParseException skipAndRethrow(StartElement tag, ManifestParseException parseException)
            throws XMLStreamException, ManifestParseException {
        try {
            reader.skipTag(tag);
        } catch (XMLStreamException xmlStreamException) {
            xmlStreamException.addSuppressed(parseException);
            throw xmlStreamException;
        }
        throw parseException;
    }

    private void readComplete(SdkPackageBuilder builder, StartElement completeTag)
            throws XMLStreamException, ManifestParseException {
        for (var tag : reader.childrenOf(completeTag)) {
            if (Checksum.is(tag)) {
                builder.setChecksumType(findChecksumAlgorithm(Checksum.getType(tag)))
                        .setChecksum(reader.getCurrentElementText());
            } else if (Url.is(tag)) {
                builder.setUrl(reader.getCurrentElementText());
            } else {
                reader.skipTag(tag);
            }
        }
    }

    @Override
    public void close() {
        Closeables.closeQuietly(manifest);
    }

    private static SdkPackage.TargetOs findTargetOs(String os) throws ManifestParseException {
        return switch (os.trim()) {
            case HostOs.LINUX -> SdkPackage.TargetOs.LINUX;
            case HostOs.MAC_OS -> SdkPackage.TargetOs.MAC_OS;
            case HostOs.WINDOWS -> SdkPackage.TargetOs.WINDOWS;
            default -> throw new ManifestParseException("Unsupported os type " + os);
        };
    }

    @SuppressWarnings("deprecation")
    private static HashFunction findChecksumAlgorithm(String type) throws ManifestParseException {
        return switch (type.trim().toLowerCase(Locale.ROOT)) {
            case "sha-1", "sha1" -> Hashing.sha1();
            case "sha-256", "sha256" -> Hashing.sha256();
            case "sha-384", "sha384" -> Hashing.sha384();
            case "sha-512", "sha512" -> Hashing.sha512();
            case "md5" -> Hashing.md5();
            default -> throw new ManifestParseException("Unsupported checksum type " + type);
        };
    }

    private static int getExpectedPackagesCount() {
        return SdkPackage.TargetOs.values().length;
    }

    private static XMLInputFactory newXmlFactory() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        return factory;
    }
}
