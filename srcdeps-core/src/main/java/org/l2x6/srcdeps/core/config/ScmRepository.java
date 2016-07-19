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
package org.l2x6.srcdeps.core.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.l2x6.srcdeps.core.config.SrcdepsConfig.BuilderIoProperty;
import org.l2x6.srcdeps.core.config.SrcdepsConfig.Property;

public class ScmRepository {

    public interface PropertyVisitor {
        void accept(SrcdepsRepositoryBuilder builder, Object value) throws SrcdepsConfigException;
    }

    public enum ScmRepositoryProperty implements PropertyVisitor {
        buildArguments {
            @Override
            public void accept(SrcdepsRepositoryBuilder builder, Object value) throws SrcdepsConfigException {
                // TODO Auto-generated method stub

            }
        },
        selectors {
            @Override
            public void accept(SrcdepsRepositoryBuilder builder, Object value) throws SrcdepsConfigException {
                // TODO Auto-generated method stub

            }
        },
        urls {
            @Override
            public void accept(SrcdepsRepositoryBuilder builder, Object value) throws SrcdepsConfigException {
                // TODO Auto-generated method stub

            }
        };

        public static List<ScmRepository> acceptAll(Map<String, Object> map) throws SrcdepsConfigException {
            List<ScmRepository> result = new ArrayList<>();
            for (Map.Entry<String, Object> repo : map.entrySet()) {
                String repoId = repo.getKey();
                SrcdepsRepositoryBuilder builder = ScmRepository.builder().id(repoId);
                Map<String, Object> repoFields = SrcdepsConfig.cast(repoId, repo.getValue(), Map.class);
                for (Map.Entry<String, Object> repoField : repoFields.entrySet()) {
                    String key = repoField.getKey();
                    try {
                        ScmRepositoryProperty p = ScmRepositoryProperty.valueOf(key);
                        p.accept(builder, repoField.getValue());
                    } catch (IllegalArgumentException e) {
                        throw new SrcdepsConfigException(String.format("Invalid property name [%s] of repository [%s]; expected: [%s]",
                                key, repoId, Arrays.toString(BuilderIoProperty.values())));
                    }
                }
                result.add(builder.build());
            }
            return result;
        }

    }

    public static class SrcdepsRepositoryBuilder {

        private SrcdepsRepositoryBuilder() {
        }

        private List<String> buildArguments = new ArrayList<>();
        private String id;
        private List<String> selectors = new ArrayList<>();
        private Collection<String> urls = new LinkedHashSet<String>();

        public ScmRepository build() {
            return new ScmRepository(id, Collections.unmodifiableList(selectors),
                    Collections.unmodifiableCollection(urls), Collections.unmodifiableList(buildArguments));
        }

        public SrcdepsRepositoryBuilder buildArgument(String buildArgument) {
            this.buildArguments.add(buildArgument);
            return this;
        }

        public SrcdepsRepositoryBuilder id(String id) {
            this.id = id;
            return this;
        }

        public SrcdepsRepositoryBuilder selectors(String selector) {
            this.selectors.add(selector);
            return this;
        }

        public SrcdepsRepositoryBuilder urls(String url) {
            this.urls.add(url);
            return this;
        }

    }

    public static SrcdepsRepositoryBuilder builder() {
        return new SrcdepsRepositoryBuilder();
    }

    private final List<String> buildArguments;
    private final String id;
    private final List<String> selectors;
    private final Collection<String> urls;

    protected ScmRepository(String id, List<String> selectors, Collection<String> urls, List<String> buildArgs) {
        super();
        this.id = id;
        this.selectors = selectors;
        this.urls = urls;
        this.buildArguments = buildArgs;
    }

    public List<String> getBuildArguments() {
        return buildArguments;
    }

    public String getId() {
        return id;
    }

    public List<String> getSelectors() {
        return selectors;
    }

    public Collection<String> getUrls() {
        return urls;
    }

}
