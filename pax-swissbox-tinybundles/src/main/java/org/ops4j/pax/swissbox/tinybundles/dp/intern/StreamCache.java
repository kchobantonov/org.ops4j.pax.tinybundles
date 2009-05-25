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
package org.ops4j.pax.swissbox.tinybundles.dp.intern;

import java.io.InputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Cache implementation for deailing with inputstreams TinyDP has to deal with.
 * It provides access to logical (by DeploymentAdmin Spec.) partitions of data
 * as well as meta data that may be cotnained inside inputstreams
 * (in case of bundles: BSN and Version)
 */
public interface StreamCache
{

    /**
     * Streaming out of this input stream must happen as soon as possible
     * because content may time out.
     *
     * Also, additional metadata which should be retrieved by getHeaders
     * must be made available as soon as possible but not necessarily by method return.
     * (could be done by first invokation of getX(..) )
     *
     * @param name            logical name of resource given by InputStream
     * @param resourceContent content
     *
     * @throws java.io.IOException if something goes wrong reading the resourceContent
     */
    void addBundle( String name, InputStream resourceContent )
        throws IOException;

    /**
     * @param name of artifact (resource identifier) to get the headers of. Just applicable on bundles.
     *
     * @return the meta data of bundle. Read from InputStream given by addBundle(..)
     */
    Map<String, String> getHeaders( String name );

    String[] getLocalizationFiles();

    String[] getMetaInfResources();

    String[] getBundles();

    String[] getOtherResources();

    InputStream getStream( String name );

    /**
     * @param name                 section and resource identifier
     * @param inputStream          content
     * @param resourceProcessorPID optional resource processor pid. Can be null if not needed.
     */
    void addResource( String name, InputStream inputStream, String resourceProcessorPID )
        throws IOException;
}
