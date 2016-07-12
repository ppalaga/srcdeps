package org.l2x6.srcdeps.localrepo;

import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Singleton
public class LocalRepositoryManagerFactories {
    private static final Logger log = LoggerFactory.getLogger(LocalRepositoryManagerFactories.class);
    private final Map<String, LocalRepositoryManagerFactory> factories;

    public LocalRepositoryManagerFactories(Map<String, LocalRepositoryManagerFactory> factories) {
        super();
        this.factories = factories;

        log.info("Got {} LocalRepositoryManagerFactory instances", factories.size());

        for (Entry<String, LocalRepositoryManagerFactory> en : factories.entrySet()) {
            log.info("{}: {}", en.getKey(), en.getValue().getClass().getName());
        }

    }

}
