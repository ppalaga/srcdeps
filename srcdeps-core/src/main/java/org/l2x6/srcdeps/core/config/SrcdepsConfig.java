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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.l2x6.srcdeps.core.BuildRequest.Verbosity;
import org.l2x6.srcdeps.core.config.ScmRepository.ScmRepositoryProperty;
import org.l2x6.srcdeps.core.shell.IoRedirects;

public class SrcdepsConfig {
    public static class Builder {

        private Set<String> forwardProperties = new LinkedHashSet<>();
        private IoRedirects redirects;
        private List<ScmRepository> repositories = new ArrayList<>();
        private boolean skip = false;
        private boolean skipTests = true;
        private Path sourcesDirectory;
        private Verbosity verbosity = Verbosity.warn;

        private Builder() {
            super();
        }

        public SrcdepsConfig build() {
            return new SrcdepsConfig(repositories, sourcesDirectory, skipTests, skip, verbosity, redirects, forwardProperties);
        }

        public Builder forwardProperty(String value) {
            forwardProperties.add(value);
            return this;
        }

        public Builder forwardProperties(Collection<String> values) {
            forwardProperties.addAll(values);
            return this;
        }

        public Builder redirects(IoRedirects redirects) {
            this.redirects = redirects;
            return this;
        }

        public Builder skip(boolean value) {
            this.skip = value;
            return this;
        }

        public Builder skipTests(boolean value) {
            this.skipTests = value;
            return this;
        }

        public Builder sourcesDirectory(Path value) {
            this.sourcesDirectory = value;
            return this;
        }

        public Builder verbosity(Verbosity value) {
            this.verbosity = value;
            return this;
        }

        public Builder repositories(List<ScmRepository> value) {
            this.repositories.addAll(value);
            return this;
        }

    }

    public enum BuilderIoProperty {
        stdin, stdout, stderr;

        public static IoRedirects acceptAll(Map<String, String> map) throws SrcdepsConfigException {
            org.l2x6.srcdeps.core.shell.IoRedirects.Builder redirectsBuilder = IoRedirects.builder();

            for (Map.Entry<String, String> en : map.entrySet()) {
                String key = en.getKey();
                try {
                    BuilderIoProperty.valueOf(key);
                } catch (IllegalArgumentException e) {
                    throw new SrcdepsConfigException(String.format("Invalid property name [%s] of [%s]; expected: [%s]",
                            key, Property.builderIo.name(), Arrays.toString(BuilderIoProperty.values())));
                }
            }
            return redirectsBuilder.build();

        }
    }

    public interface PropertyVisitor {
        void accept(Builder builder, Object value) throws SrcdepsConfigException;
    }

    public interface TypedProperty {
        Class<?> getType();
    }

    public enum Property implements PropertyVisitor, TypedProperty {
        configModelVersion(String.class, false) {
            @Override
            public void accept(Builder builder, Object value) throws SrcdepsConfigException {

            }
        }, //
        forwardProperties(List.class, false) {
            @SuppressWarnings("unchecked")
            @Override
            public void accept(Builder builder, Object value) throws SrcdepsConfigException {
                builder.forwardProperties(cast(this.name(), value, List.class));
            }
        }, //
        builderIo(BuilderIoProperty.class, false) {
            @SuppressWarnings("unchecked")
            @Override
            public void accept(Builder builder, Object value) throws SrcdepsConfigException {
                builder.redirects(BuilderIoProperty.acceptAll(cast(this.name(), value, Map.class)));
            }
        }, //
        skip(Boolean.class, true) {
            @Override
            public void accept(Builder builder, Object value) throws SrcdepsConfigException {
                builder.skip(cast(this.name(), value, Boolean.class));
            }
        }, //
        skipTests(Boolean.class, true) {
            @Override
            public void accept(Builder builder, Object value) throws SrcdepsConfigException {
                builder.skipTests(cast(this.name(), value, Boolean.class));
            }
        }, //
        sourcesDirectory(String.class, true) {
            @Override
            public void accept(Builder builder, Object value) throws SrcdepsConfigException {
                String path = cast(this.name(), value, String.class);
                builder.sourcesDirectory(Paths.get(path));
            }
        },
        verbosity(String.class, true) {
            @Override
            public void accept(Builder builder, Object value) throws SrcdepsConfigException {
                Verbosity v = Verbosity.valueOf(cast(this.name(), value, String.class));
                builder.verbosity(v);
            }
        },
        repositories(Object.class, false) {
            @SuppressWarnings("unchecked")
            @Override
            public void accept(Builder builder, Object value) throws SrcdepsConfigException {
                builder.repositories(ScmRepositoryProperty.acceptAll(cast(this.name(), value, Map.class)));
            }
        };

        public static SrcdepsConfig acceptAll(Map<String, Object> map) throws SrcdepsConfigException {
            Builder builder = SrcdepsConfig.builder();
            for (Map.Entry<String, Object> configField : map.entrySet()) {
                String key = configField.getKey();
                try {
                    Property p = Property.valueOf(key);
                    p.accept(builder, configField.getValue());
                } catch (IllegalArgumentException e) {
                    throw new SrcdepsConfigException(String.format("Invalid property name [%s]; expected property names: [%s]",
                            key, Arrays.toString(Property.values())));
                }
            }
            return builder.build();
        }

        private final boolean forwardedByDefault;
        private final Class<?> type;

        private Property(Class<?> type, boolean forwardedByDefault) {
            this.type = type;
            this.forwardedByDefault = forwardedByDefault;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        public String toSrcDepsProperty() {
            return "srcdeps." + toString();
        }

    }
    @SuppressWarnings("unchecked")
    public static <T> T cast(String key, Object value, Class<T> type) throws SrcdepsConfigException {
        if (type.isAssignableFrom(value.getClass())) {
            return (T) value;
        } else {
            throw new SrcdepsConfigException(String.format("Invalid type of [%s]: [%s]; expected: [%s]",
                    key, value.getClass().getName(), type.getName()));
        }
    }

    private final Set<String> forwardProperties;
    private final IoRedirects redirects;
    private final List<ScmRepository> repositories;
    private final boolean skip;
    private final boolean skipTests;
    private final Path sourcesDirectory;
    private final Verbosity verbosity;

    protected SrcdepsConfig(List<ScmRepository> repositories, Path sourcesDirectory, boolean skipTests, boolean skip,
            Verbosity verbosity, IoRedirects redirects, Set<String> forwardProperties) {
        super();
        this.repositories = repositories;
        this.sourcesDirectory = sourcesDirectory;
        this.skip = skip;
        this.skipTests = skipTests;
        this.verbosity = verbosity;
        this.forwardProperties = forwardProperties;
        this.redirects = redirects;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Set<String> getForwardProperties() {
        return forwardProperties;
    }

    public IoRedirects getRedirects() {
        return redirects;
    }

    public List<ScmRepository> getRepositories() {
        return repositories;
    }

    public Path getSourcesDirectory() {
        return sourcesDirectory;
    }

    public Verbosity getVerbosity() {
        return verbosity;
    }

    public boolean isSkip() {
        return skip;
    }

    public boolean isSkipTests() {
        return skipTests;
    }

}
