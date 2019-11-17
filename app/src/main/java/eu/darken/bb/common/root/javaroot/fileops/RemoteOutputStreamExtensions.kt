package eu.darken.bb.common.root.javaroot.fileops


import android.os.RemoteException
import timber.log.Timber
import java.io.IOException
import java.io.OutputStream


/**
 * Use this on the root side
 */
fun OutputStream.toRemoteOutputStream(): RemoteOutputStream.Stub = object : RemoteOutputStream.Stub() {
    override fun write(b: Int) = try {
        this@toRemoteOutputStream.write(b)
    } catch (e: IOException) {
        Timber.e(e)
    }

    override fun writeBuffer(b: ByteArray, off: Int, len: Int) = try {
        this@toRemoteOutputStream.write(b, off, len)
    } catch (e: IOException) {
        Timber.e(e)
    }

    override fun flush() = try {
        this@toRemoteOutputStream.flush()
    } catch (e: IOException) {
        Timber.e(e)
    }

    override fun close() = try {
        this@toRemoteOutputStream.close()
    } catch (e: IOException) {
        // no action required
    }

}

/**
 * Use this on the non-root side.
 */
fun RemoteOutputStream.toOutputStream(bufferSize: Int = 64 * 1024): OutputStream = object : OutputStream() {

    @Throws(IOException::class)
    override fun write(b: Int) = try {
        this@toOutputStream.write(b)
    } catch (e: RemoteException) {
        throw IOException("Remote Exception during write($b)", e)
    }

    override fun write(b: ByteArray) = writeBuffer(b, 0, b.size)

    override fun write(b: ByteArray, off: Int, len: Int) = try {
        this@toOutputStream.writeBuffer(b, 0, len)
    } catch (e: RemoteException) {
        throw IOException("Remote Exception during write(size=${b.size}, off=$off, len=$len)", e)
    }

    override fun close() = try {
        this@toOutputStream.close()
    } catch (e: RemoteException) {
        throw IOException("Remote Exception during close() ", e)
    }

    override fun flush() = try {
        this@toOutputStream.flush()
    } catch (e: RemoteException) {
        throw IOException("Remote Exception during flush()", e)
    }

}.buffered(bufferSize)