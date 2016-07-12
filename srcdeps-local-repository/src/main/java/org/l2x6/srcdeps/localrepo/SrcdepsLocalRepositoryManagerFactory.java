package org.l2x6.srcdeps.localrepo;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory;

@Named("srcdeps")
public class SrcdepsLocalRepositoryManagerFactory implements LocalRepositoryManagerFactory {

    @Inject
    private LocalRepositoryManagerFactories factories;

    @Override
    public LocalRepositoryManager newInstance(RepositorySystemSession session, LocalRepository repository)
            throws NoLocalRepositoryManagerException {
        return new SrcdepsLocalRepositoryManager();
    }

    @Override
    public float getPriority() {
        return 25;
    }


}
