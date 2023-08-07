/*
 * Copyright 2009 Toni Menzel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.tinybundles.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Manifest;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Jar;
import org.ops4j.pax.tinybundles.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Toni Menzel (tonit)
 * @since Apr 20, 2009
 */
public class BndBuilder implements Builder {

    private static Logger LOG = LoggerFactory.getLogger(BndBuilder.class);

    final private Builder m_builder;
    final private Set<Object> plugins;

    public BndBuilder(Builder builder) {
        m_builder = builder;
        plugins = new HashSet<Object>();
    }

    public BndBuilder addPlugin(Object plugin) {
        plugins.add(plugin);
        return this;
    }

    public InputStream build(Map<String, URL> resources, Map<String, String> headers) {
        return wrapWithBnd(headers, m_builder.build(resources, headers));
    }

    private InputStream wrapWithBnd(Map<String, String> headers, InputStream in) {
        try {
            Properties p = new Properties();
            p.putAll(headers);
            return createBundle(in, p, "BuildByTinyBundles" + UIDProvider.getUID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * All the BND magic happens here.
     *
     * @param jarInputStream On what to operate.
     * @param instructions   BND instructions from user API
     * @param symbolicName   Mandatory Header. In case user does not set it.
     * @return Bundle Jar Stream
     * @throws Exception Problems go here
     */
    private InputStream createBundle(InputStream jarInputStream, Properties instructions, String symbolicName) throws Exception {
        Objects.requireNonNull(jarInputStream, "Jar URL");
        Objects.requireNonNull(instructions, "Instructions");
        Objects.requireNonNull(symbolicName, "Jar info");

        final Jar jar = new Jar("dot", jarInputStream);

        final Properties properties = new Properties();
        properties.putAll(instructions);

        final aQute.bnd.osgi.Builder analyzer = new aQute.bnd.osgi.Builder();
        analyzer.setJar(jar);
        analyzer.setProperties(properties);

        for (Object plugin : plugins) {
            analyzer.addBasicPlugin(plugin);
        }

        // throw away already existing headers that we overwrite:

        analyzer.mergeManifest(jar.getManifest());

        checkMandatoryProperties(analyzer, jar, symbolicName);
        Manifest manifest = analyzer.calcManifest();
        jar.setManifest(manifest);

        return createInputStream(jar);
    }

    /**
     * Creates a piped input stream for the wrapped jar.
     * This is done in a thread, so we can return quickly.
     *
     * @param jar the wrapped jar
     * @return an input stream for the wrapped jar
     * @throws java.io.IOException re-thrown
     */
    private PipedInputStream createInputStream(final Jar jar) throws IOException {
        final PipedInputStream pin = new PipedInputStream();
        final PipedOutputStream pout = new PipedOutputStream(pin);

        new Thread() {
            public void run() {
                try {
                    jar.write(pout);
                } catch (Exception e) {
                    //    LOG.warn( "Bundle cannot be generated",e );
                } finally {
                    try {
                        pout.close();
                    } catch (IOException e) {
                        LOG.warn("Close ?", e);
                    }
                }
            }
        }.start();

        return pin;
    }

    /**
     * Check if mandatory properties are present, otherwise generate default.
     *
     * @param analyzer     bnd analyzer
     * @param jar          bnd jar
     * @param symbolicName bundle symbolic name
     */
    private void checkMandatoryProperties(final Analyzer analyzer, final Jar jar, final String symbolicName) {
        final String localSymbolicName = analyzer.getProperty(Analyzer.BUNDLE_SYMBOLICNAME, symbolicName);
        analyzer.setProperty(Analyzer.BUNDLE_SYMBOLICNAME, generateSymbolicName(localSymbolicName));
    }

    /**
     * Processes symbolic name and replaces osgi spec invalid characters with "_".
     *
     * @param symbolicName bundle symbolic name
     * @return a valid symbolic name
     */
    private static String generateSymbolicName(final String symbolicName) {
        return symbolicName.replaceAll("[^a-zA-Z_0-9.-]", "_");
    }

}
