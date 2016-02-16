import java.nio.channels.SeekableByteChannel
import java.nio.file.AccessMode
import java.nio.file.CopyOption
import java.nio.file.DirectoryStream
import java.nio.file.FileStore
import java.nio.file.FileSystem
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.spi.FileSystemProvider

/**
 * Created by grant on 11/15/15.
 */
class JavaFSView extends FileSystemProvider{
    @Override
    String getScheme() {
        return null
    }

    @Override
    FileSystem newFileSystem(URI uri, Map<String, ?> map) throws IOException {
        return null
    }

    @Override
    FileSystem getFileSystem(URI uri) {
        return null
    }

    @Override
    Path getPath(URI uri) {
        return null
    }

    @Override
    SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> set, FileAttribute<?>... fileAttributes) throws IOException {
        return null
    }

    @Override
    DirectoryStream<Path> newDirectoryStream(Path path, DirectoryStream.Filter<? super Path> filter) throws IOException {
        return null
    }

    @Override
    void createDirectory(Path path, FileAttribute<?>... fileAttributes) throws IOException {

    }

    @Override
    void delete(Path path) throws IOException {

    }

    @Override
    void copy(Path path, Path path1, CopyOption... copyOptions) throws IOException {

    }

    @Override
    void move(Path path, Path path1, CopyOption... copyOptions) throws IOException {

    }

    @Override
    boolean isSameFile(Path path, Path path1) throws IOException {
        return false
    }

    @Override
    boolean isHidden(Path path) throws IOException {
        return false
    }

    @Override
    FileStore getFileStore(Path path) throws IOException {
        return null
    }

    @Override
    void checkAccess(Path path, AccessMode... accessModes) throws IOException {

    }

    @Override
    def <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> aClass, LinkOption... linkOptions) {
        return null
    }

    @Override
    def <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> aClass, LinkOption... linkOptions) throws IOException {
        return null
    }

    @Override
    Map<String, Object> readAttributes(Path path, String s, LinkOption... linkOptions) throws IOException {
        return null
    }

    @Override
    void setAttribute(Path path, String s, Object o, LinkOption... linkOptions) throws IOException {

    }
}