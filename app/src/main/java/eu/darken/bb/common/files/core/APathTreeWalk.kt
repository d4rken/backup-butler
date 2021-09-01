package eu.darken.bb.common.files.core

import java.io.IOException
import java.util.*

/**
 * This class is intended to implement different file traversal methods.
 * It allows to iterate through all files inside a given directory.
 *
 * Use [APath.walk], [APath.walkTopDown] or [APath.walkBottomUp] extension functions to instantiate a `FileTreeWalk` instance.

 * If the file path given is just a file, walker iterates only it.
 * If the file path given does not exist, walker iterates nothing, i.e. it's equivalent to an empty sequence.
 */
class APathTreeWalk<PT : APath, GT : APathGateway<PT, out APathLookup<PT>>> private constructor(
    private val gateway: GT,
    private val start: PT,
    private val direction: FileWalkDirection = FileWalkDirection.TOP_DOWN,
    private val onEnter: ((PT) -> Boolean)? = null,
    private val onLeave: ((PT) -> Unit)? = null,
    private val onFail: ((f: PT, e: IOException) -> Unit)? = null,
    private val maxDepth: Int = Int.MAX_VALUE
) : Sequence<PT> {

    constructor(gateway: GT, start: PT, direction: FileWalkDirection = FileWalkDirection.TOP_DOWN)
            : this(gateway, start, direction, null, null, null)

    /** Returns an iterator walking through files. */
    override fun iterator(): Iterator<PT> = FileTreeWalkIterator()

    /** Abstract class that encapsulates file visiting in some order, beginning from a given [root] */
    private abstract class WalkState<T>(val root: T) {
        /** Call of this function proceeds to a next file for visiting and returns it */
        abstract fun step(): T?
    }

    /** Abstract class that encapsulates directory visiting in some order, beginning from a given [rootDir] */
    private abstract class DirectoryState<T>(rootDir: T) : WalkState<T>(rootDir)

    private inner class FileTreeWalkIterator : AbstractIterator<PT>() {

        // Stack of directory states, beginning from the start directory
        private val state = ArrayDeque<WalkState<PT>>()

        init {
            when (gateway.lookup(start).fileType) {
                FileType.DIRECTORY -> state.push(directoryState(start))
                FileType.FILE -> state.push(SingleFileState(start))
                else -> done()
            }
        }

        override fun computeNext() {
            val nextFile = gotoNext()
            if (nextFile != null) setNext(nextFile) else done()
        }


        private fun directoryState(root: PT): DirectoryState<PT> {
            return when (direction) {
                FileWalkDirection.TOP_DOWN -> TopDownDirectoryState(root)
                FileWalkDirection.BOTTOM_UP -> BottomUpDirectoryState(root)
            }
        }

        private tailrec fun gotoNext(): PT? {
            // Take next file from the top of the stack or return if there's nothing left
            val topState = state.peek() ?: return null
            val file = topState.step()
            return if (file == null) {
                // There is nothing more on the top of the stack, go back
                state.pop()
                gotoNext()
            } else {
                val lookup = gateway.lookup(file)
                // Check that file/directory matches the filter
                if (file == topState.root || lookup.fileType != FileType.DIRECTORY || state.size >= maxDepth) {
                    // Proceed to a root directory or a simple file
                    file
                } else {
                    // Proceed to a sub-directory
                    state.push(directoryState(file))
                    gotoNext()
                }
            }
        }

        /** Visiting in bottom-up order */
        private inner class BottomUpDirectoryState(rootDir: PT) : DirectoryState<PT>(rootDir) {

            private var rootVisited = false

            private var fileList: List<PT>? = null

            private var fileIndex = 0

            private var failed = false

            /** First all children, then root directory */
            override fun step(): PT? {
                if (!failed && fileList == null) {
                    if (onEnter?.invoke(root) == false) {
                        return null
                    }

                    fileList = gateway.listFiles(root)
                    if (fileList == null) {
                        onFail?.invoke(root, ReadException(root))
                        failed = true
                    }
                }
                return if (fileList != null && fileIndex < fileList!!.size) {
                    // First visit all files
                    fileList!![fileIndex++]
                } else if (!rootVisited) {
                    // Then visit root
                    rootVisited = true
                    root
                } else {
                    // That's all
                    onLeave?.invoke(root)
                    null
                }
            }
        }

        /** Visiting in top-down order */
        private inner class TopDownDirectoryState(rootDir: PT) : DirectoryState<PT>(rootDir) {

            private var rootVisited = false
            private var fileList: List<PT>? = null
            private var fileIndex = 0

            /** First root directory, then all children */
            override fun step(): PT? {
                if (!rootVisited) {
                    // First visit root
                    if (onEnter?.invoke(root) == false) {
                        return null
                    }

                    rootVisited = true
                    return root
                } else if (fileList == null || fileIndex < fileList!!.size) {
                    if (fileList == null) {
                        // Then read an array of files, if any
                        fileList = gateway.listFiles(root)
                        if (fileList == null) {
                            onFail?.invoke(root, ReadException(root))
                        }
                        if (fileList == null || fileList!!.isEmpty()) {
                            onLeave?.invoke(root)
                            return null
                        }
                    }
                    // Then visit all files
                    return fileList!![fileIndex++]
                } else {
                    // That's all
                    onLeave?.invoke(root)
                    return null
                }
            }
        }

        private inner class SingleFileState(rootFile: PT) : WalkState<PT>(rootFile) {
            private var visited: Boolean = false

            override fun step(): PT? {
                if (visited) return null
                visited = true
                return root
            }
        }

    }

    /**
     * Sets a predicate [function], that is called on any entered directory before its files are visited
     * and before it is visited itself.
     *
     * If the [function] returns `false` the directory is not entered and neither it nor its files are visited.
     */
    fun onEnter(function: (PT) -> Boolean): APathTreeWalk<PT, *> {
        return APathTreeWalk(
            gateway,
            start,
            direction,
            onEnter = function,
            onLeave = onLeave,
            onFail = onFail,
            maxDepth = maxDepth
        )
    }

    /**
     * Sets a callback [function], that is called on any left directory after its files are visited and after it is visited itself.
     */
    fun onLeave(function: (PT) -> Unit): APathTreeWalk<PT, *> {
        return APathTreeWalk(
            gateway,
            start,
            direction,
            onEnter = onEnter,
            onLeave = function,
            onFail = onFail,
            maxDepth = maxDepth
        )
    }

    /**
     * Set a callback [function], that is called on a directory when it's impossible to get its file list.
     *
     * [onEnter] and [onLeave] callback functions are called even in this case.
     */
    fun onFail(function: (PT, IOException) -> Unit): APathTreeWalk<PT, *> {
        return APathTreeWalk(
            gateway,
            start,
            direction,
            onEnter = onEnter,
            onLeave = onLeave,
            onFail = function,
            maxDepth = maxDepth
        )
    }

    /**
     * Sets the maximum [depth] of a directory tree to traverse. By default there is no limit.
     *
     * The value must be positive and [Int.MAX_VALUE] is used to specify an unlimited depth.
     *
     * With a value of 1, walker visits only the origin directory and all its immediate children,
     * with a value of 2 also grandchildren, etc.
     */
    fun maxDepth(depth: Int): APathTreeWalk<PT, *> {
        require(depth > 0) { "depth must be positive, but was $depth." }
        return APathTreeWalk(gateway, start, direction, onEnter, onLeave, onFail, depth)
    }
}