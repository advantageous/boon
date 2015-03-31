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

package io.advantageous.boon.core;

import io.advantageous.boon.core.reflection.FastStringUtils;
import io.advantageous.boon.primitive.ByteBuf;
import io.advantageous.boon.primitive.CharBuf;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.function.Function;


@SuppressWarnings ( "unchecked" )
public class IO {


    public final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public final static String FILE_SCHEMA = "file";
    public final static String JAR_SCHEMA = "jar";

    public final static String JAR_FILE_SCHEMA = "jar:file";



    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static final int EOF = -1;


    public static FileSystem zipFileSystem( URI fileJarURI ) {



        final Map<String, Object> env = Maps.map( "create", ( Object ) "true" );

        FileSystemProvider provider = loadFileSystemProvider("jar");

        Exceptions.requireNonNull(provider, "Zip file provider not found");

        FileSystem fs = null;

        try {
            fs = provider.getFileSystem( fileJarURI );
        } catch ( Exception ex ) {
            if ( provider != null ) {
                try {
                    fs = provider.newFileSystem( fileJarURI, env );
                } catch ( IOException ex2 ) {
                    Exceptions.handle( FileSystem.class,
                            Str.sputs("unable to load", fileJarURI, "as zip file system"),
                            ex2 );
                }
            }
        }

        Exceptions.requireNonNull(provider, "Zip file system was not found");

        return fs;
    }

    private static FileSystemProvider loadFileSystemProvider(String providerType) {
        FileSystemProvider provider = null;
        for ( FileSystemProvider p : FileSystemProvider.installedProviders() ) {
            if ( providerType.equals(p.getScheme()) ) {
                provider = p;
                break;
            }
        }
        return provider;
    }


    public static ConvertToPathFunction convertToPathFunction = new ConvertToPathFunction();

    /**
     * Adds a newline to the console.
     */
    public static void println() {
        Sys.println("");
    }

    /**
     * Prints an object to the console.
     *
     * @param message object to print.
     */
    public static void println(Object message) {

        print(message);
        println();
    }

    /**
     * Prints to console.
     *
     * @param message message
     */
    public static void print(String message) {
        Sys.print(message);
    }

    /**
     * Print a single object to the console.
     * If null prints out &gt;NULL&lt;
     * If char[] converts to String.
     * If array prints out string version of array
     * by first converting array to a list.
     * If any object, then it uses the toString to print out the object.
     *
     * @param message the object that you wish to print.
     */
    public static void print(Object message) {

        if (message == null) {
            print("<NULL>");
        } else if (message instanceof char[]) {
            print(FastStringUtils.noCopyStringFromChars((char[]) message));
        } else if (message.getClass().isArray()) {
            print(Lists.toListOrSingletonList(message).toString());
        } else {
            print(message.toString());
        }
    }

    /**
     * Like print, but prints out a whole slew of objects on the same line.
     *
     * @param messages objects you want to print on the same line.
     */
    public static void puts(Object... messages) {

        for (Object message : messages) {
            print(message);
        }
        println();

    }

    /**
     * <p>
     * Like puts but prints out each object on its own line.
     * If the object is a list or array,
     * then each item in the list gets printed out on its own line.
     * </p>
     *
     * @param messages the stuff you want to print out.
     */
    public static void putl(Object... messages) {

        for (Object message : messages) {

            if (message instanceof Collection || Typ.isArray(message)) {
                Iterator iterator = Conversions.iterator(message);
                while (iterator.hasNext()) {
                    puts(iterator.next());
                }
                continue;
            }
            print(message);
            println();
        }
        println();

    }


    public static class ConvertToPathFunction implements Function<String, Path> {

        @Override
        public Path apply( String s ) {
            return path(s);
        }
    }

    public static List<String> list( final Path path ) {

        if ( !exists (path) ) {
            return Collections.EMPTY_LIST;
        }

        List<String> result = new ArrayList<>();


        try {
            try ( DirectoryStream<Path> directoryStream = Files.newDirectoryStream( path ) ) {
                for ( Path entry : directoryStream ) {
                    result.add( entry.toAbsolutePath().toString() );
                }
            }
            return result;
        } catch ( IOException ex ) {
            return Exceptions.handle( List.class, ex );
        }

    }


    public static List<Path> listPath( final Path path ) {

        if ( !exists (path) ) {
            return Collections.EMPTY_LIST;
        }

        List<Path> result = new ArrayList<>();


        try {
            try ( DirectoryStream<Path> directoryStream = Files.newDirectoryStream( path ) ) {
                for ( Path entry : directoryStream ) {
                    result.add( entry.toAbsolutePath() );
                }
            }
            return result;
        } catch ( IOException ex ) {
            return Exceptions.handle( List.class, ex );
        }

    }
    public static List<String> listByGlob( final String path, final String glob ) {
        final Path pathFromFileSystem = path(path);
        return listByGlob( pathFromFileSystem, glob );
    }


    public static List<String> listByGlob( Path pathFromFileSystem, String glob ) {

        List<String> result = new ArrayList<>();

        try {
            try ( DirectoryStream<Path> stream = Files.newDirectoryStream( pathFromFileSystem, glob ) ) {
                for ( Path entry : stream ) {
                    result.add( entry.toAbsolutePath().toString() );
                }
            }
            return result;
        } catch ( IOException ex ) {
            return Exceptions.handle( List.class, ex );
        }

    }


    public static List<String> listByFileExtension( final String path, final String ext ) {
        final Path pathFromFileSystem = path(path);
        return listByFileExtension( pathFromFileSystem, ext );
    }

    public static List<String> listByFileExtension( final Path pathFromFileSystem, final String ext ) {
        final String extToLookForGlob = "*." + ext;

        List<String> result = new ArrayList<>();

        try {
            try ( DirectoryStream<Path> stream = Files.newDirectoryStream( pathFromFileSystem, extToLookForGlob ) ) {
                for ( Path entry : stream ) {
                    result.add( entry.toAbsolutePath().toString() );
                }
            }
            return result;
        } catch ( IOException ex ) {
            return Exceptions.handle( List.class, ex );
        }

    }


    public static List<String> listByFileExtensionRecursive( final String path, final String ext ) {
        final Path pathFromFileSystem = path(path);
        return listByFileExtensionRecursive( pathFromFileSystem, ext );
    }


    public static List<String> listByFileExtensionRecursive( final Path pathFromFileSystem, final String ext ) {

        final String extToLookForGlob = "*." + ext;

        List<String> result = new ArrayList<>();

        return doListByFileExtensionRecursive( result, pathFromFileSystem, extToLookForGlob );
    }

    private static List<String> doListByFileExtensionRecursive( final List<String> result,
                                                                final Path pathFromFileSystem,
                                                                final String glob ) {


        try {
            try ( DirectoryStream<Path> stream = Files.newDirectoryStream( pathFromFileSystem, glob ) ) {
                for ( Path entry : stream ) {
                    result.add( entry.toAbsolutePath().toString() );
                }
            }
            try ( DirectoryStream<Path> stream = Files.newDirectoryStream( pathFromFileSystem ) ) {
                for ( Path entry : stream ) {
                    if ( Files.isDirectory( entry ) ) {
                        doListByFileExtensionRecursive( result, entry, glob );
                    }
                }
            }

            return result;
        } catch ( IOException ex ) {
            return Exceptions.handle( List.class, ex );
        }

    }

    public static String readChild( Path parentDir, String childFileName ) {
        try {

            final Path newFilePath = path( parentDir.toString(),
                    childFileName );

            return read( newFilePath );
        } catch ( Exception ex ) {
            return Exceptions.handle( String.class, ex );
        }
    }


    public static char[] readCharBuffer( Path path ) {
        try {

            long bufSize = Files.size( path );
            return readCharBuffer( Files.newBufferedReader( path, DEFAULT_CHARSET ), ( int ) bufSize );

        } catch ( IOException ex ) {
            return Exceptions.handle( char[].class, ex );
        }
    }

    public static String read( InputStream inputStream, Charset charset ) {

        try ( Reader reader = new InputStreamReader( inputStream, charset ) ) {
            return read( reader );
        } catch ( Exception ex ) {
            return Exceptions.handle( String.class, ex );
        }
    }


    public static String read( InputStream inputStream, String charset ) {

        try ( Reader reader = new InputStreamReader( inputStream, charset ) ) {
            return read( reader );
        } catch ( Exception ex ) {
            return Exceptions.handle( String.class, ex );
        }
    }

    public static String readCharBuffer( InputStream inputStream, Charset charset ) {

        try ( Reader reader = new InputStreamReader( inputStream, charset ) ) {
            return read( reader );
        } catch ( Exception ex ) {
            return Exceptions.handle( String.class, ex );
        }
    }

    public static String read( InputStream inputStream ) {

        try ( Reader reader = new InputStreamReader( inputStream, DEFAULT_CHARSET ) ) {
            return read( reader );
        } catch ( Exception ex ) {
            return Exceptions.handle( String.class, ex );
        }

    }


    public static char[] readCharBuffer( InputStream inputStream ) {

        try ( Reader reader = new InputStreamReader( inputStream ) ) {
            return readCharBuffer( reader );
        } catch ( Exception ex ) {
            return Exceptions.handle( char[].class, ex );
        }

    }

    public static CharBuf read( InputStream inputStream, CharBuf charBuf ) {

        try ( Reader reader = new InputStreamReader( inputStream ) ) {
            return read( reader, charBuf );
        } catch ( Exception ex ) {
            return Exceptions.handle( CharBuf.class, ex );
        }

    }


    public static CharBuf read( InputStream inputStream, CharBuf charBuf, Charset charset ) {

        try ( Reader reader = new InputStreamReader( inputStream, charset ) ) {
            return read( reader, charBuf );
        } catch ( Exception ex ) {
            return Exceptions.handle( CharBuf.class, ex );
        }

    }


    public static CharBuf read( InputStream inputStream, CharBuf charBuf, Charset charset, int bufSize, char[] copyBuf ) {

        try ( Reader reader = new InputStreamReader( inputStream, charset ) ) {
            return read( reader, charBuf, bufSize, copyBuf );
        } catch ( Exception ex ) {
            return Exceptions.handle( CharBuf.class, ex );
        }

    }

    public static byte[] input( String fileName ) {
        try {
            return input( Files.newInputStream( path(fileName) ) );
        } catch ( IOException e ) {
            return Exceptions.handle( byte[].class, e );
        }
    }


    public static byte[] input( InputStream inputStream ) {

        try ( InputStream is = inputStream ) {

            ByteBuf buf = ByteBuf.create( DEFAULT_BUFFER_SIZE );
            byte[] bytes = new byte[ DEFAULT_BUFFER_SIZE ];

            int read = -2;


            while ( read != -1 ) {

                read = inputStream.read( bytes );

                if ( read == DEFAULT_BUFFER_SIZE ) {
                    buf.add( bytes );
                } else if ( read > 0 ) {
                    buf.add( bytes, read );
                }
            }
            return buf.toBytes();
        } catch ( Exception ex ) {
            return Exceptions.handle( byte[].class, ex );
        }
    }


    public static long copyLarge( Reader reader, Writer writer ) {
        return copyLarge( reader, writer,  null);
    }

    public static long copyLarge( Reader reader, Writer writer, char[] buffer ) {
        long count = 0;
        int n;

        if (buffer==null) {
            buffer = new char[ DEFAULT_BUFFER_SIZE ];
        }
        try {
            while ( EOF != ( n = reader.read( buffer ) ) ) {
                writer.write( buffer, 0, n );
                count += n;
            }
        } catch ( IOException e ) {
            Exceptions.handle( e );
        }
        return count;
    }


    public static String read( Reader input ) {
        try {

            CharBuf sw = CharBuf.create( DEFAULT_BUFFER_SIZE );
            copy( input, sw );
            return sw.toString();

        } finally {
            try {
                input.close();
            } catch ( IOException e ) {
                Exceptions.handle( e );
            }
        }
    }

    public static CharBuf read( Reader input, CharBuf charBuf, final int bufSize, char[] copyBuffer ) {

        if ( charBuf == null ) {
            charBuf = CharBuf.create( bufSize );
        } else {
            charBuf.readForRecycle();
        }

        try {


            char[] buffer = charBuf.toCharArray();
            int size = input.read( buffer );
            if ( size != -1 ) {
                charBuf._len( size );
            }
            if ( size < buffer.length ) {
                return charBuf;
            }

            copy( input, charBuf, copyBuffer );

        } catch ( IOException e ) {
            Exceptions.handle( e );
        } finally {
            try {
                input.close();
            } catch ( IOException e ) {
                Exceptions.handle( e );
            }
        }

        return charBuf;


    }

    public static CharBuf read( Reader input, CharBuf charBuf ) {
        return read( input, charBuf, 2048, null );
    }

    public static char[] readCharBuffer( Reader input ) {

        try {
            CharBuf sw = CharBuf.create( DEFAULT_BUFFER_SIZE );
            copy( input, sw );
            return sw.toCharArray();

        } finally {
            try {
                input.close();
            } catch ( IOException e ) {
                Exceptions.handle( e );
            }
        }

    }

    public static int copy( Reader input, Writer output ) {
        long count = copyLarge( input, output );
        if ( count > Integer.MAX_VALUE ) {
            return -1;
        }
        return ( int ) count;
    }

    public static int copy( Reader input, Writer output, char[] copyBuf ) {
        long count = copyLarge( input, output, copyBuf );
        if ( count > Integer.MAX_VALUE ) {
            return -1;
        }
        return ( int ) count;
    }

    public static char[] readCharBuffer( Reader reader, int size ) {


        char[] buffer = new char[ size ];

        try ( Reader r = reader ) {

            reader.read( buffer );


        } catch ( Exception ex ) {
            return Exceptions.handle( char[].class, ex );
        }

        return buffer;

    }


    public static String read( File file ) {
        try ( Reader reader = new FileReader( file ) ) {
            return read( reader );
        } catch ( Exception ex ) {
            return Exceptions.handle( String.class, ex );
        }
    }

    public static List<String> readLines( Reader reader ) {

        try ( BufferedReader bufferedReader = new BufferedReader( reader ) ) {

            return readLines( bufferedReader );

        } catch ( Exception ex ) {

            return Exceptions.handle( List.class, ex );
        }
    }

    public static void eachLine( Reader reader, EachLine eachLine ) {

        try ( BufferedReader bufferedReader = new BufferedReader( reader ) ) {

            eachLine( bufferedReader, eachLine );

        } catch ( Exception ex ) {

            Exceptions.handle( List.class, ex );
        }
    }

    public static List<String> readLines( InputStream is ) {

        try ( Reader reader = new InputStreamReader( is, DEFAULT_CHARSET ) ) {

            return readLines( reader );

        } catch ( Exception ex ) {

            return Exceptions.handle( List.class, ex );
        }
    }

    public static void eachLine( InputStream is, EachLine eachLine ) {

        try ( Reader reader = new InputStreamReader( is, DEFAULT_CHARSET ) ) {

            eachLine( reader, eachLine );

        } catch ( Exception ex ) {

            Exceptions.handle( ex );
        }
    }


    public static List<String> readLines( BufferedReader reader ) {
        List<String> lines = new ArrayList<>( 80 );

        try ( BufferedReader bufferedReader = reader ) {


            String line;
            while ( ( line = bufferedReader.readLine() ) != null ) {
                lines.add( line );
            }


        } catch ( Exception ex ) {

            return Exceptions.handle( List.class, ex );
        }
        return lines;
    }

    public static interface EachLine {
        public boolean line( String line, int index );
    }

    public static void eachLine( BufferedReader reader, EachLine eachLine ) {

        try ( BufferedReader bufferedReader = reader ) {


            String line;
            int lineNumber = 0;

            while ( ( line = bufferedReader.readLine() ) != null &&
                    eachLine.line( line, lineNumber++ ) ) { //
                // no op
            }
        } catch ( Exception ex ) {

            Exceptions.handle( ex );
        }

    }

    public static void eachLine( File file, EachLine eachLine ) {
        try ( FileReader reader = new FileReader( file ) ) {
            eachLine( reader, eachLine );
        } catch ( Exception ex ) {
            Exceptions.handle( List.class, ex );
        }
    }


    public static List<String> readLines( File file ) {
        try ( FileReader reader = new FileReader( file ) ) {
            return readLines( reader );
        } catch ( Exception ex ) {
            return Exceptions.handle( List.class, ex );
        }
    }


    public static List<String> readLines( final String location ) {


        final String path = getWindowsPathIfNeeded( location );

        final URI uri = createURI( path );

        return ( List<String> ) Exceptions.tryIt( Typ.list, new Exceptions.TrialWithReturn<List>() {
            @Override
            public List<String> tryIt() throws Exception {
                if ( uri.getScheme() == null ) {

                    Path thePath = FileSystems.getDefault().getPath( path );
                    return Files.readAllLines( thePath, DEFAULT_CHARSET );

                } else if ( uri.getScheme().equals( FILE_SCHEMA ) ) {

                    Path thePath = FileSystems.getDefault().getPath( uri.getPath() );
                    return Files.readAllLines( thePath, DEFAULT_CHARSET );

                } else {
                    return readLines( location, uri );
                }
            }
        } );
    }

    public static URI createURI( final String path ) {
        if ( !Sys.isWindows() ) {
            return URI.create( path );

        } else {

            if ( path.contains( "\\" ) || path.startsWith( "C:" ) || path.startsWith( "D:" ) ) {
                String newPath = new File( path ).toURI().toString();
                if ( newPath.startsWith( "file:/C:" ) ) {
                    newPath = Str.slc(newPath, 8);
                    return URI.create( newPath );
                } else {
                    return URI.create( newPath );
                }

            } else {
                return URI.create( path );
            }
        }
    }


    public static void eachLine( final String location, final EachLine eachLine ) {

        final URI uri = createURI( location );

        Exceptions.tryIt( new Exceptions.Trial() {
            @Override
            public void tryIt() throws Exception {


                if ( uri.getScheme() == null ) {

                    Path thePath = FileSystems.getDefault().getPath( location );
                    BufferedReader buf = Files.newBufferedReader(
                            thePath, DEFAULT_CHARSET );
                    eachLine( buf, eachLine );

                } else if ( uri.getScheme().equals( FILE_SCHEMA ) ) {


                    Path thePath = null;

                    if ( Sys.isWindows() ) {
                        String path = uri.toString();

                        path = path.replace( '/', Sys.windowsPathSeparator() );
                        if ( Str.slc(path, 0, 6).equals( "file:\\" ) ) {
                            path = Str.slc(path, 6);
                        }
                        thePath = FileSystems.getDefault().getPath( path );
                    } else {
                        thePath = FileSystems.getDefault().getPath( uri.getPath() );

                    }
                    BufferedReader buf = Files.newBufferedReader(
                            thePath, DEFAULT_CHARSET );
                    eachLine( buf, eachLine );


                } else {
                    eachLine( location, uri, eachLine );
                }

            }

        } );
    }

    public static String getWindowsPathIfNeeded( String path ) {
        if ( Sys.isWindows() ) {

            if ( path.startsWith( ".\\" ) ) {
                path = Str.slc(path, 2);
            }
        }
        return path;
    }



    public static String read( final Path path ) {
        return readPath(path);
    }

    public static String readPath( final Path path ) {

        return Exceptions.tryIt( String.class, new Exceptions.TrialWithReturn<String>() {

            @Override
            public String tryIt() throws Exception {

                return read( Files.newBufferedReader( path, DEFAULT_CHARSET ) );

            }
        } );

    }




    public static String read( final String location ) {
        final URI uri = createURI( location );

        return Exceptions.tryIt( String.class, new Exceptions.TrialWithReturn<String>() {

            @Override
            public String tryIt() throws Exception {

                String path = location;

                path = getWindowsPathIfNeeded( path );

                if ( uri.getScheme() == null ) {

                    Path thePath = FileSystems.getDefault().getPath( path );
                    return read( Files.newBufferedReader( thePath, DEFAULT_CHARSET ) );

                } else if ( uri.getScheme().equals( FILE_SCHEMA ) ) {

                    return readFromFileSchema( uri );

                } else {
                    return read( location, uri );
                }


            }
        } );

    }





    public static String readFromFileSchema( URI uri ) {
        Path thePath = uriToPath( uri );

        try {
            return read( Files.newBufferedReader( thePath, DEFAULT_CHARSET ) );
        } catch ( IOException e ) {

            return Exceptions.handle( Typ.string, e ); //
        }
    }

    public static Path uriToPath( URI uri ) {
        Path thePath = null;
        if ( Sys.isWindows() ) {
            String newPath = uri.getPath();
            if ( newPath.startsWith( "/C:" ) ) {
                newPath = Str.slc(newPath, 3);
            }
            thePath = FileSystems.getDefault().getPath( newPath );
        } else {
            thePath = FileSystems.getDefault().getPath( uri.getPath() );
        }
        return thePath;
    }

    private static List<String> readLines( String location, URI uri ) throws Exception {
        try {
            String path = location;
            path = getWindowsPathIfNeeded( path );

            FileSystem fileSystem = FileSystems.getFileSystem( uri );
            Path fsPath = fileSystem.getPath( path );

            //Paths.get()
            return Files.readAllLines( fsPath, DEFAULT_CHARSET );
        } catch ( ProviderNotFoundException ex ) {
            return readLines( uri.toURL().openStream() );
        }
    }


    private static void eachLine( String location, URI uri, EachLine eachLine ) throws Exception {
        try {
            FileSystem fileSystem = FileSystems.getFileSystem( uri );
            Path fsPath = fileSystem.getPath( location );
            BufferedReader buf = Files.newBufferedReader( fsPath, DEFAULT_CHARSET );
            eachLine( buf, eachLine );


        } catch ( ProviderNotFoundException ex ) {
            eachLine( uri.toURL().openStream(), eachLine );
        }
    }

    public static String read( String location, URI uri ) throws Exception {
        try {
            FileSystem fileSystem = FileSystems.getFileSystem( uri );
            Path fsPath = fileSystem.getPath( location );
            return read( Files.newBufferedReader( fsPath, DEFAULT_CHARSET ) );
        } catch ( ProviderNotFoundException ex ) {
            return read( uri.toURL().openStream() );
        }
    }


    public static void write( OutputStream out, String content, Charset charset ) {

        try ( OutputStream o = out ) {
            o.write( content.getBytes( charset ) );
        } catch ( Exception ex ) {
            Exceptions.handle( ex );
        }

    }

    public static void writeChild( Path parentDir, String childFileName, String childContents ) {

        try {

            final Path newFilePath = path( parentDir.toString(),
                    childFileName );

            write( newFilePath, childContents );
        } catch ( Exception ex ) {
            Exceptions.handle( ex );
        }
    }

    public static Path createChildDirectory( Path parentDir, String childDir ) {

        try {


            final Path newDir = path( parentDir.toString(),
                    childDir );


            if ( !Files.exists( newDir ) ) {
                Files.createDirectory( newDir );
            }

            return newDir;

        } catch ( Exception ex ) {
            return Exceptions.handle( Path.class, ex );
        }
    }

    public static Path createDirectory( Path dir ) {

        try {


            if ( !Files.exists( dir ) ) {
                return Files.createDirectory( dir );
            } else {
                return null;
            }

        } catch ( Exception ex ) {
            return Exceptions.handle( Path.class, ex );
        }
    }

    public static Path createDirectory( String dir ) {

        try {

            final Path newDir = path(dir);
            createDirectory( newDir );

            return newDir;

        } catch ( Exception ex ) {
            return Exceptions.handle( Path.class, ex );
        }
    }

    public static FileSystem fileSystem() {
        return FileSystems.getDefault();
    }


    public static Path path( String path, String... more ) {
        return Paths.get( path, more );
    }

    public static Path path( Path path, String... more ) {
        return Paths.get( path.toString(), more );
    }

    public static void write( Path file, String contents ) {
        write( file, contents.getBytes( DEFAULT_CHARSET ) );
    }

    public static void write( String file, String contents ) {
        write( path(file), contents.getBytes( DEFAULT_CHARSET ) );
    }


    public static void output( String file, byte[] bytes ) {
        IO.write( path(file), bytes );
    }

    public static void output( Path file, byte[] bytes ) {
        IO.write( file, bytes );
    }


    public static void write( String file, byte[] contents ) {
        write (path(file), contents);
    }

    public static void write( Path file, byte[] contents ) {
        try {
            Files.write( file, contents );

        } catch ( Exception ex ) {
            Exceptions.handle( ex );
        }
    }

    public static void write( OutputStream out, String content ) {

        try ( OutputStream o = out ) {
            o.write( content.getBytes( DEFAULT_CHARSET ) );
        } catch ( Exception ex ) {
            Exceptions.handle( ex );
        }

    }

    public static void writeNoClose( OutputStream out, String content ) {

        try {
            out.write( content.getBytes( DEFAULT_CHARSET ) );
        } catch ( Exception ex ) {
            Exceptions.handle( ex );
        }

    }



    public static InputStream inputStream( String resource ) {
        Path path = path(resource);
        try {
            return Files.newInputStream( path );
        } catch ( IOException e ) {
            return Exceptions.handle( InputStream.class, "unable to open " + resource, e );
        }
    }

    //

    public static List<String> list( final String path ) {
            final Path pathFromFileSystem = path(path);
            return list( pathFromFileSystem );
    }



    public static List<Path> paths( final String path ) {

            final Path pathFromFileSystem = path(path);
            return listPath(pathFromFileSystem);
    }

    public static List<Path> pathsByExt( final String path, String ext ) {

        List<Path> list = paths(path);


        final List<Path> newList = new ArrayList<>();

        for ( Path file : list ) {
            if ( file.toString().endsWith( ext ) ) {
                newList.add( file );
            }
        }

        return newList;
    }



    public static List<Path> paths( final Path path ) {
            return listPath(path);
    }




    public static List<String> listByExt( final String path, String ext ) {

        final List<String> list = list( path );
        final List<String> newList = new ArrayList<>();

        for ( String file : list ) {
            if ( file.endsWith( ext ) ) {
                newList.add( file );
            }
        }

        return newList;

    }



    public static void delete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            Exceptions.handle(e);
        }
    }

    public static void createDirectories(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            Exceptions.handle(e);
        }
    }


    public static void delete(String path) {
        try {
            Files.delete(path(path));
        } catch (IOException e) {
            Exceptions.handle(e);
        }
    }

    public static void createDirectories(String path) {
        try {
            Files.createDirectories(path(path));
        } catch (IOException e) {
            Exceptions.handle(e);
        }
    }



    public static Path path( String location ) {
            return Paths.get(location);
    }







    public static boolean exists (Path path ) {
        return Files.exists(path);
    }


    public static boolean exists (String path ) {
        return Files.exists(path(path));
    }

    public static void move(Path source, Path target) {
        try {
            Files.move(source, target);
        } catch (IOException e) {
            Exceptions.handle(e);
        }
    }


    /**
     * Press enter to continue. Used for console apps.
     *
     * @param pressEnterKeyMessage message
     */
    public static void pressEnterKey(String pressEnterKeyMessage) {
        puts(pressEnterKeyMessage);
        gets();
    }


    /**
     * Used by console apps.
     */
    public static void pressEnterKey() {
        puts("Press enter key to continue");
        gets();
    }


    /**
     * Gets input from console.
     *
     * @return String from console.
     */
    public static String gets() {
        Scanner console = new Scanner(System.in);
        String input = console.nextLine();
        return input.trim();
    }

}
