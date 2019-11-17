package eu.darken.bb.common.root.javaroot.fileops


import android.os.RemoteException
import timber.log.Timber
import java.io.IOException
import java.io.InputStream


/**
 * InputStream is a pretty basic class that is easily wrapped, because it really only requires
 * a handful of base methods.
 *
 *
 * We cannot make an InputStream into a Binder-passable interface directly, because its definitions
 * includes throwing IOExceptions. It also defines multiple methods with the same name and a
 * different parameter list, which is not supported in aidl.
 *
 *
 * You should never throw an exception in your Binder interface. We catch the exceptions and
 * return -2 instead, because conveniently all the methods we override should return values >= -1.
 * More complex classes would require more complex solutions.
 */

/**
 * Use this on the root side
 */
fun InputStream.toRemoteInputStream(): RemoteInputStream.Stub = object : RemoteInputStream.Stub() {

    override fun available(): Int = try {
        this@toRemoteInputStream.available()
    } catch (e: IOException) {
        Timber.e(e)
        -2
    }

    override fun read(): Int = try {
        this@toRemoteInputStream.read()
    } catch (e: IOException) {
        Timber.e(e)
        -2
    }

    override fun readBuffer(b: ByteArray, off: Int, len: Int): Int = try {
        this@toRemoteInputStream.read(b, off, len)
    } catch (e: IOException) {
        Timber.e(e)
        -2
    }

    override fun close() = try {
        this@toRemoteInputStream.close()
    } catch (e: IOException) {
        // no action required
    }

}

/**
 * We throw an IOException if we receive a -2 result, because we know we caught one on the
 * other end in that case. The logcat output will obviously not show the real reason for the
 * exception.
 *
 *
 * We also wrap the InputStream we create inside a BufferedInputStream, to potentially reduce
 * the number of calls made. We increase the buffer size to 64kb in case this is ever used
 * to actually read large files, which is quite a bit faster than the default 8kb.
 *
 *
 * Use this on the non-root side.
 */
fun RemoteInputStream.toInputStream(bufferSize: Int = 64 * 1024): InputStream = object : InputStream() {

    @Throws(IOException::class)
    private fun throwIO(r: Int): Int {
        if (r == -2) throw IOException("Remote Exception")
        return r
    }

    @Throws(IOException::class)
    override fun available(): Int = try {
        throwIO(this@toInputStream.available())
    } catch (e: RemoteException) {
        throw IOException("Remote Exception", e)
    }

    @Throws(IOException::class)
    override fun read(): Int = try {
        throwIO(this@toInputStream.read())
    } catch (e: RemoteException) {
        throw IOException("Remote Exception", e)
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int = read(b, 0, b.size)

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int = try {
        // Overriding this one too will make reading much faster than just having read()
        throwIO(this@toInputStream.readBuffer(b, off, len))
    } catch (e: RemoteException) {
        throw IOException("Remote Exception", e)
    }

    @Throws(IOException::class)
    override fun close() = try {
        // This method too is an optional override, but we wouldn't want to leave our files open, would we?
        this@toInputStream.close()
    } catch (e: RemoteException) {
        throw IOException("Remote Exception", e)
    }

}.buffered(bufferSize)