package org.ops4j.pax.tinybundles.core.builders;import java.io.File;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.IOException;import java.io.InputStream;import java.net.URL;import java.util.Map;import java.util.Set;import java.util.jar.JarEntry;import java.util.jar.JarOutputStream;import java.util.jar.Manifest;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.ops4j.io.StreamUtils;import org.ops4j.pax.tinybundles.core.BuildableBundle;import org.ops4j.pax.tinybundles.core.intern.Info;/** * Alternative, early flushing version. */public class SynchronousRawBuilder extends RawBuilder {    private static Logger LOG = LoggerFactory.getLogger( SynchronousRawBuilder.class );     public InputStream build( final Map<String, URL> resources,                             final Map<String, String> headers )    {        LOG.debug( "make()" );        try {            File f = File.createTempFile( "temp", "bin" );            JarOutputStream jarOut = new JarOutputStream( new FileOutputStream( f ) );            try {                build( resources, headers, jarOut );            } finally {                jarOut.close();            }            return new FileInputStream( f );        } catch( IOException e ) {            throw new RuntimeException( e );        }    }}