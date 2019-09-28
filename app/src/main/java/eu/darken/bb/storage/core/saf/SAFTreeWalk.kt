package eu.darken.bb.storage.core.saf

import eu.darken.bb.common.file.AccessDeniedException
import eu.darken.bb.common.file.SAFPath
import java.io.IOException
import java.util.*

/**
 * This class is intended to implement different file traversal methods.
 * It allows to iterate through all files inside a given directory.
 *
 * Use [File.walk], [File.walkTopDown] or [File.walkBottomUp] extension functions to instantiate a `FileTreeWalk` instance.

 * If the file path given is just a file, walker iterates only it.
 * If the file path given does not exist, walker iterates nothing, i.e. it's equivalent to an empty sequence.
 */
class SAFTreeWalk private constructor(
        private val gateway: SAFGateway,
        private val start: SAFPath,
        private val direction: FileWalkDirection = FileWalkDirection.TOP_DOWN,
        private val onEnter: ((SAFPath) -> Boolean)?,
        private val onLeave: ((SAFPath) -> Unit)?,
        private val onFail: ((f: SAFPath, e: IOException) -> Unit)?,
        private val maxDepth: Int = Int.MAX_VALUE
) : Sequence<SAFPath> {

    internal constructor(gateway: SAFGateway, start: SAFPath, direction: FileWalkDirection = FileWalkDirection.TOP_DOWN)
            : this(gateway, start, direction, null, null, null)


    /** Returns an iterator walking through files. */
    override fun iterator(): Iterator<SAFPath> = FileTreeWalkIterator()

    /** Abstract class that encapsulates file visiting in some order, beginning from a given [root] */
    private abstract class WalkState(val root: SAFPath) {
        /** Call of this function proceeds to a next file for visiting and returns it */
        abstract fun step(): SAFPath?
    }

    /** Abstract class that encapsulates directory visiting in some order, beginning from a given [rootDir] */
    private abstract class DirectoryState(rootDir: SAFPath) : WalkState(rootDir)

    private inner class FileTreeWalkIterator : AbstractIterator<SAFPath>() {

        // Stack of directory states, beginning from the start directory
        private val state = ArrayDeque<WalkState>()

        init {
            when {
                start.isDirectory(gateway) -> state.push(directoryState(start))
                start.isFile(gateway) -> state.push(SingleFileState(start))
                else -> done()
            }
        }

        override fun computeNext() {
            val nextFile = gotoNext()
            if (nextFile != null) setNext(nextFile) else done()
        }


        private fun directoryState(root: SAFPath): DirectoryState {
            return when (direction) {
                FileWalkDirection.TOP_DOWN -> TopDownDirectoryState(root)
                FileWalkDirection.BOTTOM_UP -> BottomUpDirectoryState(root)
            }
        }

        private tailrec fun gotoNext(): SAFPath? {
            // Take next file from the top of the stack or return if there's nothing left
            val topState = state.peek() ?: return null
            val file = topState.step()
            return if (file == null) {
                // There is nothing more on the top of the stack, go back
                state.pop()
                gotoNext()
            } else {
                // Check that file/directory matches the filter
                if (file == topState.root || !file.isDirectory(gateway) || state.size >= maxDepth) {
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
        private inner class BottomUpDirectoryState(rootDir: SAFPath) : DirectoryState(rootDir) {

            private var rootVisited = false

            private var fileList: Array<SAFPath>? = null

            private var fileIndex = 0

            private var failed = false

            /** First all children, then root directory */
            override fun step(): SAFPath? {
                if (!failed && fileList == null) {
                    if (onEnter?.invoke(root) == false) {
                        return null
                    }

                    fileList = root.listFiles(gateway)
                    if (fileList == null) {
                        onFail?.invoke(root, AccessDeniedException(file = root, reason = "Cannot list files in a directory"))
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
        private inner class TopDownDirectoryState(rootDir: SAFPath) : DirectoryState(rootDir) {

            private var rootVisited = false
            private var fileList: Array<SAFPath>? = null
            private var fileIndex = 0

            /** First root directory, then all children */
            override fun step(): SAFPath? {
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
                        fileList = root.listFiles(gateway)
                        if (fileList == null) {
                            onFail?.invoke(root, AccessDeniedException(file = root, reason = "Cannot list files in a directory"))
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

        private inner class SingleFileState(rootFile: SAFPath) : WalkState(rootFile) {
            private var visited: Boolean = false

            override fun step(): SAFPath? {
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
    fun onEnter(function: (SAFPath) -> Boolean): SAFTreeWalk {
        return SAFTreeWalk(gateway, start, direction, onEnter = function, onLeave = onLeave, onFail = onFail, maxDepth = maxDepth)
    }

    /**
     * Sets a callback [function], that is called on any left directory after its files are visited and after it is visited itself.
     */
    fun onLeave(function: (SAFPath) -> Unit): SAFTreeWalk {
        return SAFTreeWalk(gateway, start, direction, onEnter = onEnter, onLeave = function, onFail = onFail, maxDepth = maxDepth)
    }

    /**
     * Set a callback [function], that is called on a directory when it's impossible to get its file list.
     *
     * [onEnter] and [onLeave] callback functions are called even in this case.
     */
    fun onFail(function: (SAFPath, IOException) -> Unit): SAFTreeWalk {
        return SAFTreeWalk(gateway, start, direction, onEnter = onEnter, onLeave = onLeave, onFail = function, maxDepth = maxDepth)
    }

    /**
     * Sets the maximum [depth] of a directory tree to traverse. By default there is no limit.
     *
     * The value must be positive and [Int.MAX_VALUE] is used to specify an unlimited depth.
     *
     * With a value of 1, walker visits only the origin directory and all its immediate children,
     * with a value of 2 also grandchildren, etc.
     */
    fun maxDepth(depth: Int): SAFTreeWalk {
        require(depth > 0) { "depth must be positive, but was $depth." }
        return SAFTreeWalk(gateway, start, direction, onEnter, onLeave, onFail, depth)
    }
}