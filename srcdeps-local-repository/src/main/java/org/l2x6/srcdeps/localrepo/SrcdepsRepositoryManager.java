/**
 * Copyright 2015-2016 Maven Source Dependencies
 * Plugin contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.l2x6.srcdeps.localrepo;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.repository.LocalArtifactRegistration;
import org.eclipse.aether.repository.LocalArtifactRequest;
import org.eclipse.aether.repository.LocalArtifactResult;
import org.eclipse.aether.repository.LocalMetadataRegistration;
import org.eclipse.aether.repository.LocalMetadataRequest;
import org.eclipse.aether.repository.LocalMetadataResult;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.l2x6.srcdeps.core.BuildException;
import org.l2x6.srcdeps.core.BuildRequest;
import org.l2x6.srcdeps.core.BuildService;
import org.l2x6.srcdeps.core.SrcVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SrcdepsRepositoryManager implements LocalRepositoryManager {
    private static final Logger log = LoggerFactory.getLogger(SrcdepsRepositoryManager.class);

    private final LocalRepositoryManager delegate;

    private final BuildService buildService;

    public SrcdepsRepositoryManager(LocalRepositoryManager delegate, BuildService buildService) {
        super();
        this.delegate = delegate;
        this.buildService = buildService;
    }

    @Override
    public void add(RepositorySystemSession session, LocalArtifactRegistration request) {
        log.info("======= SrcdepsRepositoryManager.add(RepositorySystemSession session, LocalArtifactRegistration request)");
        delegate.add(session, request);
    }

    @Override
    public void add(RepositorySystemSession session, LocalMetadataRegistration request) {
        log.info("======= SrcdepsRepositoryManager.add(RepositorySystemSession session, LocalMetadataRegistration request)");
        delegate.add(session, request);
    }

    @Override
    public LocalArtifactResult find(RepositorySystemSession session, LocalArtifactRequest request) {
        log.info("======= SrcdepsRepositoryManager.find(RepositorySystemSession session, LocalArtifactRequest ["+ request +"])");
        LocalArtifactResult result = delegate.find(session, request);

        String version = request.getArtifact().getVersion();
        if (!result.isAvailable() && SrcVersion.isSrcVersion(version)) {
            BuildRequest buildRequest = BuildRequest.builder() //
//                    .projectRootDirectory(projectRootDirectory) //
//                    .scmUrls(repo.getUrls()) //
//                    .srcVersion(SrcVersion.parse(version)) //
//                    .buildArguments(repo.getBuildArguments()) //
//                    .verbosity(configuration.getVerbosity()) //
//                    .ioRedirects(configuration.getRedirects()) //
                    .build();

            try {
                buildService.build(buildRequest);
            } catch (BuildException e) {

                log.error("Could not build "+ request, e);

            }
        }

        return result;
    }

    @Override
    public LocalMetadataResult find(RepositorySystemSession session, LocalMetadataRequest request) {
        log.info("======= SrcdepsRepositoryManager.find(RepositorySystemSession session, LocalMetadataRequest request)");
        return delegate.find(session, request);
    }

    @Override
    public String getPathForLocalArtifact(Artifact artifact) {
        log.info("======= SrcdepsRepositoryManager.getPathForLocalArtifact(Artifact artifact)");
        return delegate.getPathForLocalArtifact(artifact);
    }

    @Override
    public String getPathForLocalMetadata(Metadata metadata) {
        log.info("======= SrcdepsRepositoryManager.getPathForLocalMetadata(Metadata metadata)");
        return delegate.getPathForLocalMetadata(metadata);
    }

    @Override
    public String getPathForRemoteArtifact(Artifact artifact, RemoteRepository repository, String context) {
        log.info("======= SrcdepsRepositoryManager.getPathForRemoteArtifact(Artifact artifact, RemoteRepository repository, String context)");
        return delegate.getPathForRemoteArtifact(artifact, repository, context);
    }

    @Override
    public String getPathForRemoteMetadata(Metadata metadata, RemoteRepository repository, String context) {
        log.info("======= SrcdepsRepositoryManager.getPathForRemoteMetadata(Metadata metadata, RemoteRepository repository, String context)");
        return delegate.getPathForRemoteMetadata(metadata, repository, context);
    }

    @Override
    public LocalRepository getRepository() {
        log.info("======= SrcdepsRepositoryManager.getRepository()");
        return delegate.getRepository();
    }

}