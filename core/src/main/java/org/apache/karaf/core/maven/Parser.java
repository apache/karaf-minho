/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.core.maven;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for mvn: URL.
 */
public class Parser {

    private static final Pattern VERSION_FILE_PATTERN = Pattern.compile("^(.*)-([0-9]{8}\\.[0-9]{6})-([0-9]+)$");

    /**
     * Default version if none present in the url.
     */
    public static final String VERSION_LATEST = "LATEST";

    /**
     * Syntax for the url; to be shown on exception messages.
     */
    public static final String SYNTAX = "mvn:[repository_url!]groupId/artifactId[/[version]/[type]]";

    /**
     * Separator between repository and artifact definition.
     */
    private static final String REPOSITORY_SEPARATOR = "!";

    /**
     * Artifact definition segments separator.
     */
    private static final String ARTIFACT_SEPARATOR = "/";

    /**
     * Snapshot version.
     */
    private static final String VERSION_SNAPSHOT = "SNAPSHOT";

    /**
     * Default type if not present in the url.
     */
    private static final String TYPE_JAR = "jar";

    /**
     * Final artifact path separator.
     */
    public static final String FILE_SEPARATOR = "/";

    /**
     * Group ID path separator.
     */
    private static final String GROUP_SEPARATOR = "\\.";

    /**
     * Separator used to constructs the artifact file name.
     */
    private static final String VERSION_SEPARATOR = "-";

    /**
     * Artifact extension (type) separator.
     */
    private static final String TYPE_SEPARATOR = ".";

    /**
     * Separator used to separate classifier in artifact name.
     */
    private static final String CLASSIFIER_SEPARATOR = "-";

    /**
     * Maven metadata file.
     */
    private static final String METADATA_FILE = "maven-metadata.xml";

    /**
     * Maven local metadata file.
     */
    private static final String METADATA_FILE_LOCAL = "maven-metadata-local.xml";

    /**
     * Repository URL. Null if not present.
     */
    private String repositoryUrl;

    /**
     * Artifact groupId.
     */
    private String groupId;

    /**
     * ArtifactId.
     */
    private String artifactId;

    /**
     * Artifact version.
     */
    private String version;

    /**
     * Artifact type.
     */
    private String type;

    /**
     * Artifact classifier.
     */
    private String classifier;

    /**
     * Artifact classifier to use to build artifact name.
     */
    private String fullClassifier;

    /**
     * Creates a new URL parser.
     *
     * @param url the URL.
     * @throws java.net.MalformedURLException if provided URL is not valid.
     */
    public Parser(final String url) throws MalformedURLException {
        if (url == null) {
            throw new MalformedURLException("URL can not be null. Syntax " + SYNTAX);
        }
        if (url.startsWith(REPOSITORY_SEPARATOR) || url.endsWith(REPOSITORY_SEPARATOR)) {
            throw new MalformedURLException("URL cannot start or end with " + REPOSITORY_SEPARATOR + ". Syntax " + SYNTAX);
        }
        if (url.contains(REPOSITORY_SEPARATOR)) {
            int pos = url.lastIndexOf(REPOSITORY_SEPARATOR);
            parseArtifactPart(url.substring(pos + 1));
            repositoryUrl = url.substring(0, pos);
        } else {
            parseArtifactPart(url);
        }
    }

    /**
     * Return the artifact path from the given Maven URL.
     *
     * @param uri the Maven URL.
     * @return the artifact path.
     * @throws MalformedURLException in case of "bad" URL.
     */
    public static String pathFromMaven(String uri) throws MalformedURLException {
        return pathFromMaven(uri, null);
    }

    public static String pathFromMaven(String uri, String resolved) throws MalformedURLException {
        if (!uri.startsWith("mvn:")) {
            return uri;
        }
        Parser parser = new Parser(uri.substring("mvn:".length()));
        if (resolved != null) {
            String grp = FILE_SEPARATOR
                    + parser.getGroupId().replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                    + FILE_SEPARATOR
                    + parser.getArtifactId()
                    + FILE_SEPARATOR;
            int idx = resolved.indexOf(grp);
            if (idx >= 0) {
                String version = resolved.substring(idx + grp.length(), resolved.indexOf('/', idx + grp.length()));
                return parser.getArtifactPath(version);
            }
        }
        return parser.getArtifactPath();
    }

    public static String fileNameFromMaven(String uri, boolean exclude) throws MalformedURLException {
        if (!uri.startsWith("mvn:")) {
            return uri;
        }
        Parser parser = new Parser(uri.substring("mvn:".length()));
        return parser.getArtifactFileName(parser.getVersion(), exclude);
    }

    public static String pathToMaven(String location, Map parts) {
        String[] p = location.split("/");
        if (p.length >= 4 && p[p.length - 1].startsWith(p[p.length - 3] + "-" + p[p.length - 2])) {
            String artifactId = p[p.length - 3];
            String version = p[p.length - 2];
            String classifier;
            String type;
            String artifactIdVersion = artifactId + "-" + version;
            StringBuilder sb = new StringBuilder();
            if (p[p.length - 1].charAt(artifactIdVersion.length()) == '-') {
                classifier = p[p.length - 1].substring(artifactIdVersion.length() + 1, p[p.length - 1].lastIndexOf('.'));
            } else {
                classifier = null;
            }
            type = p[p.length - 1].substring(p[p.length - 1].lastIndexOf('.') + 1);
            sb.append("mvn:");
            if (parts != null) {
                parts.put("artifactId", artifactId);
                parts.put("version", version);
                parts.put("classifier", classifier);
                parts.put("type", type);
            }
            for (int j = 0; j < p.length - 3; j++) {
                if (j > 0) {
                    sb.append('.');
                }
                sb.append(p[j]);
            }
            sb.append('/').append(artifactId).append('/').append(version);
            if (!"jar".equals(type) || classifier != null) {
                sb.append('/');
                if (!"jar".equals(type)) {
                    sb.append(type);
                }
                if (classifier != null) {
                    sb.append('/').append(classifier);
                }
            }
            return sb.toString();
        }
        return location;
    }

    public static String pathToMaven(String location) {
        return pathToMaven(location, null);
    }

    /**
     * Parse the artifact part of the url (without the repository).
     *
     * @param part url part without protocol and repository.
     * @throws MalformedURLException if provided path does not comply to syntax.
     */
    private void parseArtifactPart(final String part)
            throws MalformedURLException {
        String[] segments = part.split(ARTIFACT_SEPARATOR);
        if (segments.length < 2) {
            throw new MalformedURLException("Invalid path. Syntax " + SYNTAX);
        }
        // we must have a valid group
        groupId = segments[0];
        if (groupId.trim().length() == 0) {
            throw new MalformedURLException("Invalid groupId. Syntax " + SYNTAX);
        }
        // valid artifact
        artifactId = segments[1];
        if (artifactId.trim().length() == 0) {
            throw new MalformedURLException("Invalid artifactId. Syntax " + SYNTAX);
        }
        // version is optional but we have a default value
        version = VERSION_LATEST;
        if (segments.length >= 3 && segments[2].trim().length() > 0) {
            version = segments[2];
        }
        // type is optional but we have a default value
        type = TYPE_JAR;
        if (segments.length >= 4 && segments[3].trim().length() > 0) {
            type = segments[3];
        }
        // classifier is optional (if not present or empty we will have a null classifier
        fullClassifier = "";
        if (segments.length >= 5 && segments[4].trim().length() > 0) {
            classifier = segments[4];
            fullClassifier = CLASSIFIER_SEPARATOR + classifier;
        }
    }

    /**
     * Return the repository URL if present, null otherwise.
     *
     * @return repository URL.
     */
    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    /**
     * Prints parsed mvn: URI (after possible change of any component)
     *
     * @return
     */
    public String toMvnURI() {
        StringBuilder sb = new StringBuilder();
        sb.append(groupId).append(ARTIFACT_SEPARATOR).append(artifactId).append(ARTIFACT_SEPARATOR).append(version);
        if (!TYPE_JAR.equals(type)) {
            sb.append(ARTIFACT_SEPARATOR).append(type);
        }
        if (classifier != null && !"".equals(classifier)) {
            if (TYPE_JAR.equals(type)) {
                sb.append(ARTIFACT_SEPARATOR).append(type);
            }
            sb.append(ARTIFACT_SEPARATOR).append(classifier);
        }

        return sb.toString();
    }

    /**
     * Return the group id of the artifact.
     *
     * @return group ID.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Return the artifact id.
     *
     * @return artifact id.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Return the artifact version.
     *
     * @return version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Return the artifact type.
     *
     * @return type.
     */
    public String getType() {
        return type;
    }

    /**
     * Return the artifact classifier.
     *
     * @return classifier.
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * Changes parsed group - to allow printing mvn: URI with changed groupId
     *
     * @param groupId
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Changes parsed artifact - to allow printing mvn: URI with changed artifactId
     *
     * @param artifactId
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Changes parsed version - to allow printing mvn: URI with changed version
     *
     * @param version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Changes parsed type - to allow printing mvn: URI with changed type
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Changes parsed classifier - to allow printing mvn: URI with changed classifier
     *
     * @param classifier
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    /**
     * Return the complete path to artifact as stated by Maven 2 repository layout.
     *
     * @return artifact path.
     */
    public String getArtifactPath() {
        return getArtifactPath(version);
    }

    /**
     * Return the complete path to artifact as stated by Maven 2 repository layout.
     *
     * @param version The version of the artifact.
     * @return artifact path.
     */
    public String getArtifactPath(final String version) {

        Matcher m = VERSION_FILE_PATTERN.matcher(version);

        if (m.matches()) {
            this.version = m.group(1) + "-" + "SNAPSHOT";
            String ret = groupId.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                    + FILE_SEPARATOR
                    + artifactId
                    + FILE_SEPARATOR
                    + version
                    + FILE_SEPARATOR
                    + artifactId
                    + VERSION_SEPARATOR
                    + version
                    + fullClassifier
                    + TYPE_SEPARATOR
                    + type;
            if (getRepositoryUrl() != null) {
                ret = getRepositoryUrl() + "/" + ret;
            }
            return ret;
        } else {
            String ret = groupId.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                    + FILE_SEPARATOR
                    + artifactId
                    + FILE_SEPARATOR
                    + version
                    + FILE_SEPARATOR
                    + artifactId
                    + VERSION_SEPARATOR
                    + version
                    + fullClassifier
                    + TYPE_SEPARATOR
                    + type;
            if (getRepositoryUrl() != null) {
                ret = getRepositoryUrl() + "/" + ret;
            }
            return ret;
        }
    }

    /**
     * Return the file name (groupId path excluded) from a mvn URL.
     *
     * @param version The version of the artifact.
     * @return artifact file name.
     */
    public String getArtifactFileName(final String version, boolean exclude) {
        Matcher m = VERSION_FILE_PATTERN.matcher(version);
        if (m.matches()) {
            this.version = m.group(1) + "-" + "SNAPSHOT";
        }
        if (exclude) {
            return artifactId + TYPE_SEPARATOR + type;
        } else {
            return artifactId + VERSION_SEPARATOR + version + fullClassifier + TYPE_SEPARATOR + type;
        }
    }

    /**
     * Return the version for an artifact for a snapshot version.
     *
     * @param version     The version of the snapshot.
     * @param timestamp   The timestamp of the snapshot.
     * @param buildnumber The buildnumber of the snapshot.
     * @return artifact path.
     */
    public String getSnapshotVersion(final String version, final String timestamp, final String buildnumber) {
        return version.replace(VERSION_SNAPSHOT, timestamp) + VERSION_SEPARATOR + buildnumber;
    }

    /**
     * Return the complete path to artifact for a snapshot file.
     *
     * @param version     The version of the snapshot.
     * @param timestamp   The timestamp of the snapshot.
     * @param buildnumber The buildnumber of the snapshot.
     * @return artifact path.
     */
    public String getSnapshotPath(final String version, final String timestamp, final String buildnumber) {
        return groupId.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                + FILE_SEPARATOR
                + artifactId
                + FILE_SEPARATOR
                + version
                + FILE_SEPARATOR
                + artifactId
                + VERSION_SEPARATOR
                + getSnapshotVersion(version, timestamp, buildnumber)
                + fullClassifier
                + TYPE_SEPARATOR
                + type;
    }

    /**
     * Return the path to metadata file corresponding to this artifact version.
     *
     * @param version The version of the the metadata.
     * @return metadata file path.
     */
    public String getVersionMetadataPath(final String version) {
        return groupId.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                + FILE_SEPARATOR
                + artifactId
                + FILE_SEPARATOR
                + version
                + FILE_SEPARATOR
                + METADATA_FILE;
    }

    /**
     * Return the path to local metadata file corresponding to this artifact version.
     *
     * @param version The version of the the metadata.
     * @return metadata file path.
     */
    public String getVersionLocalMetadataPath(final String version) {
        return groupId.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                + FILE_SEPARATOR
                + artifactId
                + FILE_SEPARATOR
                + version
                + FILE_SEPARATOR
                + METADATA_FILE_LOCAL;
    }

    /**
     * Return the complete path to artifact local metadata file.
     *
     * @return artifact path.
     */
    public String getArtifactLocalMetadataPath() {
        return groupId.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                + FILE_SEPARATOR
                + artifactId
                + FILE_SEPARATOR
                + METADATA_FILE_LOCAL;
    }

    /**
     * Return the complete path to artifact metadata file.
     *
     * @return artifact path.
     */
    public String getArtifactMetadataPath() {
        return groupId.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR)
                + FILE_SEPARATOR
                + artifactId
                + FILE_SEPARATOR
                + METADATA_FILE;
    }

}
