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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.maven.execution.MavenSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory;
import org.l2x6.srcdeps.core.BuildService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("srcdeps")
public class SrcdepsRepositoryManagerFactory implements LocalRepositoryManagerFactory {
    private static final Logger log = LoggerFactory.getLogger(SrcdepsRepositoryManagerFactory.class);

    @Inject
    private RepositoryManagerFactories factories;

    @Inject
    private BuildService buildService;


    @Inject
    private Provider<MavenSession> sessionProvider;

    public SrcdepsRepositoryManagerFactory() {
        log.info("====== SrcdepsRepositoryManagerFactory");
    }

    @Override
    public LocalRepositoryManager newInstance(RepositorySystemSession session, LocalRepository repository)
            throws NoLocalRepositoryManagerException {
        LocalRepositoryManagerFactory delegate = factories.getDelegate();

        return new SrcdepsRepositoryManager(delegate.newInstance(session, repository), sessionProvider, buildService);
    }

    @Override
    public float getPriority() {
        return 30;
    }

}
