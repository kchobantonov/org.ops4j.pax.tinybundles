package org.ops4j.pax.tinybundles.bnd;import org.ops4j.pax.tinybundles.core.BuildableBundle;import org.ops4j.pax.tinybundles.core.builders.AsyncRawBuilder;import org.ops4j.pax.tinybundles.core.builders.BndBuilder;/** *  */public class BndAsyncTest extends BndTest {    @Override    BuildableBundle getStrategy()    {        return new BndBuilder( new AsyncRawBuilder() );    }}