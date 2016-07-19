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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Provider;

import org.apache.maven.execution.MavenSession;
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
import org.l2x6.srcdeps.config.yaml.YamlConfigurationIo;
import org.l2x6.srcdeps.core.BuildException;
import org.l2x6.srcdeps.core.BuildRequest;
import org.l2x6.srcdeps.core.BuildService;
import org.l2x6.srcdeps.core.SrcVersion;
import org.l2x6.srcdeps.core.config.BuilderIo;
import org.l2x6.srcdeps.core.config.Configuration;
import org.l2x6.srcdeps.core.config.ConfigurationException;
import org.l2x6.srcdeps.core.config.ScmRepository;
import org.l2x6.srcdeps.core.shell.IoRedirects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SrcdepsRepositoryManager implements LocalRepositoryManager {
    private static final Logger log = LoggerFactory.getLogger(SrcdepsRepositoryManager.class);

    private final BuildService buildService;

    private volatile Configuration configuration;
    private final Object configurationLock = new Object();
    private final LocalRepositoryManager delegate;

    private final Provider<MavenSession> sessionProvider;

    public SrcdepsRepositoryManager(LocalRepositoryManager delegate, Provider<MavenSession> sessionProvider, BuildService buildService) {
        super();
        this.delegate = delegate;
        this.buildService = buildService;
        this.sessionProvider = sessionProvider;
    }



    @Override
    public void add(RepositorySystemSession session, LocalArtifactRegistration request) {
        // log.info("======= SrcdepsRepositoryManager.add(RepositorySystemSession session, LocalArtifactRegistration request)");
        delegate.add(session, request);
    }

    @Override
    public void add(RepositorySystemSession session, LocalMetadataRegistration request) {
        // log.info("======= SrcdepsRepositoryManager.add(RepositorySystemSession session, LocalMetadataRegistration request)");
        delegate.add(session, request);
    }

    @Override
    public LocalArtifactResult find(RepositorySystemSession session, LocalArtifactRequest request) {
        LocalArtifactResult result = delegate.find(session, request);

        Artifact artifact = request.getArtifact();
        String version = artifact.getVersion();
        if (!result.isAvailable() && SrcVersion.isSrcVersion(version)) {
            Configuration lazyConfig = getConfiguration();

            ScmRepository scmRepo = findScmRepo(lazyConfig.getRepositories(), artifact);


            Path scrdepsDir = delegate.getRepository().getBasedir().toPath().getParent().resolve("srcdeps");

            log.info("====Got config {}", lazyConfig);

            BuilderIo builderIo = configuration.getBuilderIo();
            IoRedirects ioRedirects = IoRedirects.builder() //
                    .stdin(IoRedirects.parseUri(builderIo.getStdin())) //
                    .stdout(IoRedirects.parseUri(builderIo.getStdout())) //
                    .stderr(IoRedirects.parseUri(builderIo.getStderr())) //
                    .build();
            BuildRequest buildRequest = BuildRequest.builder() //
                    .projectRootDirectory(scrdepsDir) //
                    .scmUrls(scmRepo.getUrls()) //
                    .srcVersion(SrcVersion.parse(version)) //
                    .buildArguments(scmRepo.getBuildArguments()) //
                    .verbosity(configuration.getVerbosity()) //
                    .ioRedirects(ioRedirects) //
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
        // log.info("======= SrcdepsRepositoryManager.find(RepositorySystemSession session, LocalMetadataRequest request)");
        return delegate.find(session, request);
    }


    private ScmRepository findScmRepo(List<ScmRepository> repositories, Artifact artifact) {
        final String groupId = artifact.getGroupId();
        for (ScmRepository scmRepository : repositories) {
            if (scmRepository.getSelectors().contains(groupId )) {
                return scmRepository;
            }
        }
        throw new IllegalStateException(String.format("No srcdeps SCM repository configured in .mvn/srcdeps.yaml for groupId [%s]", groupId));
    }

    private Configuration getConfiguration() {
        synchronized (configurationLock) {
            if (configuration == null) {
                try (Reader r = new InputStreamReader(new FileInputStream(locateSrcdepsYaml()), "utf-8")) {
                    configuration = new YamlConfigurationIo().read(r);
                } catch (IOException | ConfigurationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return configuration;
    }


    @Override
    public String getPathForLocalArtifact(Artifact artifact) {
        // log.info("======= SrcdepsRepositoryManager.getPathForLocalArtifact(Artifact artifact)");
        return delegate.getPathForLocalArtifact(artifact);
    }

    @Override
    public String getPathForLocalMetadata(Metadata metadata) {
        // log.info("======= SrcdepsRepositoryManager.getPathForLocalMetadata(Metadata metadata)");
        return delegate.getPathForLocalMetadata(metadata);
    }

    @Override
    public String getPathForRemoteArtifact(Artifact artifact, RemoteRepository repository, String context) {
        // log.info("======= SrcdepsRepositoryManager.getPathForRemoteArtifact(Artifact artifact, RemoteRepository repository, String context)");
        return delegate.getPathForRemoteArtifact(artifact, repository, context);
    }

    @Override
    public String getPathForRemoteMetadata(Metadata metadata, RemoteRepository repository, String context) {
        // log.info("======= SrcdepsRepositoryManager.getPathForRemoteMetadata(Metadata metadata, RemoteRepository repository, String context)");
        return delegate.getPathForRemoteMetadata(metadata, repository, context);
    }

    @Override
    public LocalRepository getRepository() {
        // log.info("======= SrcdepsRepositoryManager.getRepository()");
        return delegate.getRepository();
    }

    private File locateSrcdepsYaml() {
        MavenSession mavenSession = sessionProvider.get();
        log.info("==== got maven session "+ mavenSession);
        String baseDir = mavenSession.getRequest().getBaseDirectory();
        log.info("==== using basedir "+ baseDir);
        Path mvnSrcdepsYaml = Paths.get(".mvn", "srcdeps.yaml");
        for (Path basePath = Paths.get(baseDir); basePath != null; basePath = basePath.getParent()) {
            Path result = basePath.resolve(mvnSrcdepsYaml);
            if (Files.exists(result)) {
                return result.toFile();
            }
        }
        throw new RuntimeException(String.format("Could not locate [%s] starting at path [%s]", mvnSrcdepsYaml, baseDir));
    }

}
