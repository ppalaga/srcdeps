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

import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class RepositoryManagerFactories  {
    private static final Logger log = LoggerFactory.getLogger(RepositoryManagerFactories.class);

    @Inject
    private Map<String, LocalRepositoryManagerFactory> factories;

    public LocalRepositoryManagerFactory getDelegate() {
        log.info("======== Got {} LocalRepositoryManagerFactory instances", factories.size());

        LocalRepositoryManagerFactory winner = null;
        for (Entry<String, LocalRepositoryManagerFactory> en : factories.entrySet()) {
            LocalRepositoryManagerFactory factory = en.getValue();
            log.info("{}: {}", en.getKey(), factory.getClass().getName());
            if (factory instanceof SrcdepsRepositoryManagerFactory) {
                /* ignore */
            } else if (winner == null || winner.getPriority() < factory.getPriority()) {
                winner = factory;
            }
        }
        log.info("======== the winner is {}: {}", winner.getPriority(), winner.getClass().getName());
        return winner;
    }

}
