
/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

package io.advantageous.boon;


import io.advantageous.boon.core.*;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.boon.core.Str.sputs;
import static io.advantageous.boon.core.Exceptions.requireNonNull;
import static io.advantageous.boon.core.Lists.*;

public class Classpaths {


    public final static String CLASSPATH_SCHEMA = "classpath";


    public static List<URL> classpathResources( ClassLoader loader, String resource ) {
        try {

            Enumeration<URL> resources = loader.getResources( resource );
            List<URL> list = Lists.list(resources);

            if ( isEmpty( list ) && resource.startsWith( "/" ) ) {
                resource = resource.substring( 1 );
                return classpathResources( loader, resource );
            }

            return list;


        } catch ( Exception ex ) {

            return Exceptions.handle(List.class, Str.sputs("Unable to load listFromClassLoader for", resource),
                    ex);
        }


    }

    public static List<URL> classpathResources( Class<?> clazz, String resource ) {


        List<URL> list = classpathResources( Thread.currentThread().getContextClassLoader(), resource );

        if ( isEmpty( list ) ) {
            list = classpathResources( clazz.getClassLoader(), resource );
        }


        if ( isEmpty( list ) && resource.startsWith( "/" ) ) {
            resource = resource.substring( 1 );
            return classpathResources( clazz, resource );
        }

        return list;
    }

    public static List<String> resources( Class<?> clazz, String resource ) {


        List<String> list = listFromClassLoader(Thread.currentThread().getContextClassLoader(), resource);

        if ( isEmpty( list ) ) {
            list = listFromClassLoader(clazz.getClassLoader(), resource);
        }


        if ( isEmpty( list ) && resource.startsWith( "/" ) ) {
            resource = resource.substring( 1 );
            return resources( clazz, resource );
        }

        return list;
    }


    public static List<Path> paths( Class<?> clazz, String resource ) {


        List<Path> list = pathsFromClassLoader(Thread.currentThread().getContextClassLoader(), resource);

        if ( isEmpty( list ) ) {
            list = pathsFromClassLoader(clazz.getClassLoader(), resource);
        }


        if ( isEmpty( list ) && resource.startsWith( "/" ) ) {
            resource = resource.substring( 1 );
            return paths( clazz, resource );
        }

        return list;
    }

    /**
     * Load the listFromClassLoader
     * @param loader loader
     * @param resource resource
     * @return list of strings
     */
    public static List<String> listFromClassLoader(ClassLoader loader, String resource) {
        final List<URL> resourceURLs = Classpaths.classpathResources( loader, resource );
        final List<String> resourcePaths = Lists.list( String.class );
        final Map<URI, FileSystem> pathToZipFileSystems = new HashMap<>(); //So you don't have to keep loading the same jar/zip file.
        for ( URL resourceURL : resourceURLs ) {

            if ( resourceURL.getProtocol().equals( "jar" ) ) {
                resourcesFromJar( resourcePaths, resourceURL, pathToZipFileSystems );

            } else {
                resourcesFromFileSystem( resourcePaths, resourceURL );
            }
        }
        return resourcePaths;
    }


    /**
     * Load the listFromClassLoader
     * @param loader loader
     * @param resource resource
     * @return array of strings
     */
    public static List<Path> pathsFromClassLoader(ClassLoader loader, String resource) {
        final List<URL> resourceURLs = Classpaths.classpathResources( loader, resource );
        final List<Path> resourcePaths = Lists.list( Path.class );
        final Map<URI, FileSystem> pathToZipFileSystems = new HashMap<>(); //So you don't have to keep loading the same jar/zip file.
        for ( URL resourceURL : resourceURLs ) {

            if ( resourceURL.getProtocol().equals( "jar" ) ) {
                pathsFromJar( resourcePaths, resourceURL, pathToZipFileSystems );

            } else {
                pathsFromFileSystem( resourcePaths, resourceURL );
            }
        }
        return resourcePaths;
    }



    private static void resourcesFromFileSystem( List<String> resourcePaths, URL u ) {
        URI fileURI = IO.createURI(u.toString());


        add( resourcePaths, IO.uriToPath( fileURI ).toString() );
    }



    private static void pathsFromFileSystem( List<Path> resourcePaths, URL u ) {
        URI fileURI = IO.createURI( u.toString() );


        add( resourcePaths, IO.uriToPath( fileURI ) );
    }

    private static void resourcesFromJar( List<String> resourcePaths, URL resourceURL, Map<URI, FileSystem> pathToZipFileSystems ) {

        String str = resourceURL.toString();

        final String[] strings = StringScanner.split(str, '!');

        URI fileJarURI = URI.create( strings[ 0 ] );
        String resourcePath = strings[ 1 ];

        if ( !pathToZipFileSystems.containsKey( fileJarURI ) ) {
            pathToZipFileSystems.put( fileJarURI, IO.zipFileSystem(fileJarURI) );
        }

        FileSystem fileSystem = pathToZipFileSystems.get( fileJarURI );

        Path path = fileSystem.getPath(resourcePath);

        if (path != null) {
            add( resourcePaths, str);
        }
    }

    private static void pathsFromJar( List<Path> resourcePaths, URL resourceURL, Map<URI, FileSystem> pathToZipFileSystems ) {

        String str = resourceURL.toString();

        final String[] strings = StringScanner.split( str, '!' );

        URI fileJarURI = URI.create( strings[ 0 ] );
        String resourcePath = strings[ 1 ];

        if ( !pathToZipFileSystems.containsKey( fileJarURI ) ) {
            pathToZipFileSystems.put( fileJarURI, IO.zipFileSystem(fileJarURI) );
        }

        FileSystem fileSystem = pathToZipFileSystems.get( fileJarURI );

        Path path = fileSystem.getPath(resourcePath);

        if (path != null) {
            add( resourcePaths, path);
        }
    }


    private static void resourcePathsFromJar( List<Path> resourcePaths, URL resourceURL, Map<URI, FileSystem> pathToZipFileSystems ) {

        String str = resourceURL.toString();

        final String[] strings = StringScanner.split( str, '!' );

        URI fileJarURI = URI.create( strings[ 0 ] );
        String resourcePath = strings[ 1 ];

        if ( !pathToZipFileSystems.containsKey( fileJarURI ) ) {
            pathToZipFileSystems.put( fileJarURI, IO.zipFileSystem(fileJarURI) );
        }

        FileSystem fileSystem = pathToZipFileSystems.get( fileJarURI );

        Path path = fileSystem.getPath(resourcePath);

        if (path != null) {
            add( resourcePaths, path);
        }
    }

    static List<String> listFromDefaultClassLoader(String s) {
        List<String> result = new ArrayList<>();

        String newPath = s;

        final List<String> resources = resources(
                IO.class, newPath );


        for ( String resourcePath : resources ) {
            Path path = path(resourcePath);
            if ( Files.isDirectory(path) ) {
                result.addAll( IO.list( resourcePath ) );
            } else {
                result.add( resourcePath.toString() );
            }
        }


        return result;
    }

    static Path convertJarFileSystemURIToPath(String resourceURL) {

        String str = resourceURL;

        final String[] strings = StringScanner.split( str, '!' );

        URI fileJarURI = URI.create( strings[ 0 ] );
        String resourcePath = strings[ 1 ];

        String key = Str.slc(strings[0], IO.JAR_FILE_SCHEMA.length() + 1);
        if ( !pathToZipFileSystems.containsKey( fileJarURI ) ) {
            pathToZipFileSystems.put( key, IO.zipFileSystem(fileJarURI) );

            cleanPathToZipFileSystemMap();
        }

        FileSystem fileSystem = pathToZipFileSystems.get( key );

        Path path = fileSystem.getPath(resourcePath);

        return path;
    }


    private static ConcurrentHashMap<String, FileSystem> pathToZipFileSystems = new ConcurrentHashMap<>();


    private static void cleanPathToZipFileSystemMap() {

        Set<String> paths = pathToZipFileSystems.keySet();
        for (String path : paths) {
            if (!Files.exists( path(path) )) {
                pathToZipFileSystems.remove(path);
            }
        }
    }

    public static Path path( String location ) {
        if ( location.startsWith( CLASSPATH_SCHEMA + ":" ) ) {
            String path = StringScanner.split( location, ':' )[ 1 ];

            final List<String> resources = resources(
                    IO.class, path );

            if (resources == null || resources.size() == 0) {
                Exceptions.die("Resource not found", location);
            }

            String result = idx( resources, 0 );
            if ( result == null ) {
                return path( path );
            }
            return path(result);

        } else if (location.startsWith( IO.JAR_FILE_SCHEMA + ":" )) {
            return convertJarFileSystemURIToPath(location);
        } else {
            return Paths.get(location);
        }
    }

    static List<Path> pathsFromDefaultClassLoader(String s) {
        List<Path> result = new ArrayList<>();

        String newPath = s;

        final List<Path> resources = paths(
                IO.class, newPath );


        for ( Path resourcePath : resources ) {
            if ( Files.isDirectory( resourcePath ) ) {
                result.addAll( IO.paths( resourcePath ) );
            } else {
                result.add( resourcePath);
            }
        }


        return result;
    }

    public static String readFromClasspath( Class<?> clazz, String location ) {
        List<String> resources = resources( clazz, location );



        if ( len( resources ) > 0 ) {
            try {
                return IO.read(Files.newBufferedReader(path(resources.get(0)), IO.DEFAULT_CHARSET));
            } catch ( IOException e ) {
                return Exceptions.handle( String.class, "unable to read classpath resource " + location, e );
            }
        } else {
            return null;
        }
    }


    public static List<String> list( final String path ) {

        URI uri = URI.create( path );
        if ( uri.getScheme() == null ) {
            final Path pathFromFileSystem = Classpaths.path(path);
            return IO.list(pathFromFileSystem);
        } else if ( uri.getScheme().equals( CLASSPATH_SCHEMA ) ) {

            return Classpaths.listFromDefaultClassLoader(StringScanner.split(path, ':')[1]);

        } else {
            final Path pathFromFileSystem = Classpaths.path(path);
            return IO.list(pathFromFileSystem);
        }
    }



    public static String readFromClasspath( String location ) {

        Exceptions.requireNonNull(location, "location can't be null");

        if ( !location.startsWith( CLASSPATH_SCHEMA + ":" ) ) {
            Exceptions.die("Location must starts with " + CLASSPATH_SCHEMA);
        }

        Path path = path(location);

        if ( path == null ) {
            return null;
        }
        try {
            return IO.read(Files.newBufferedReader(path, IO.DEFAULT_CHARSET));
        } catch ( IOException e ) {
            return Exceptions.handle( String.class, "unable to read classpath resource " + location, e );

        }
    }

    public static String readResource( final String location ) {
        final URI uri = IO.createURI(location);

        return Exceptions.tryIt( String.class, new Exceptions.TrialWithReturn<String>() {

            @Override
            public String tryIt() throws Exception {

                String path = location;

                path = IO.getWindowsPathIfNeeded(path);

                if ( uri.getScheme() == null ) {

                    Path thePath = FileSystems.getDefault().getPath( path );
                    if (IO.exists(thePath)) {
                        return IO.read(Files.newBufferedReader(thePath, IO.DEFAULT_CHARSET));
                    } else {
                        path = CLASSPATH_SCHEMA + ":/" + location;
                        thePath = Classpaths.path(path);
                        if (IO.exists(thePath)) {
                            return IO.read(Files.newBufferedReader(thePath, IO.DEFAULT_CHARSET));
                        } else {
                            return null;
                        }
                    }

                } else if ( uri.getScheme().equals( IO.FILE_SCHEMA ) ) {

                    return IO.readFromFileSchema(uri);

                } else if ( uri.getScheme().equals( CLASSPATH_SCHEMA )
                        || uri.getScheme().equals( IO.JAR_SCHEMA ) ) {

                    return readFromClasspath( uri.toString() );

                } else {
                    return IO.read(location, uri);
                }


            }
        } );

    }




    public static String read( final String location ) {
        final URI uri = IO.createURI(location);

        return Exceptions.tryIt( String.class, new Exceptions.TrialWithReturn<String>() {

            @Override
            public String tryIt() throws Exception {

                String path = location;

                path = IO.getWindowsPathIfNeeded(path);

                if ( uri.getScheme() == null ) {

                    Path thePath = FileSystems.getDefault().getPath( path );
                    return IO.read(Files.newBufferedReader(thePath, IO.DEFAULT_CHARSET));

                } else if ( uri.getScheme().equals( IO.FILE_SCHEMA ) ) {

                    return IO.readFromFileSchema(uri);

                } else if ( uri.getScheme().equals( CLASSPATH_SCHEMA )
                        || uri.getScheme().equals( IO.JAR_SCHEMA ) ) {

                    return readFromClasspath( uri.toString() );

                } else {
                    return IO.read(location, uri);
                }


            }
        } );

    }



}
