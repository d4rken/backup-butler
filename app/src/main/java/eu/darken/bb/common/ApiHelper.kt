package eu.darken.bb.common

import android.os.Build

object ApiHelper {
    @Suppress("MemberVisibilityCanBePrivate")
    var SDK_INT = Build.VERSION.SDK_INT

    /**
     * @return if ==19
     */
    val isKitKat: Boolean
        get() = SDK_INT == Build.VERSION_CODES.KITKAT

    /**
     * @return if ==26
     */
    val isOreo: Boolean
        get() = Build.VERSION.RELEASE == "O" || SDK_INT == Build.VERSION_CODES.O

    fun setApiLevel(apiLevel: Int) {
        SDK_INT = apiLevel
    }

    /**
     * @return if >=16
     */
    fun hasJellyBean(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
    }

    /**
     * @return if >=17
     */
    fun hasJellyBeanMR1(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
    }

    /**
     * @return if >=18
     */
    fun hasJellyBeanMR2(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    }

    /**
     * @return if >=19
     */
    fun hasKitKat(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.KITKAT
    }

    /**
     * @return if >=21
     */
    fun hasLolliPop(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    /**
     * @return if >=22
     */
    fun hasLolliPopMR1(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
    }

    /**
     * Android 6.0
     *
     * @return if >=23
     */
    fun hasMarshmallow(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.M
    }

    /**
     * @return if >=24, Nougat (7.0)
     */
    fun hasAndroidN(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.N || "N" == Build.VERSION.CODENAME
    }

    /**
     * @return if >=25, Nougat (7.1)
     */
    fun hasAndroidNMR1(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.N_MR1
    }

    /**
     * @return if >=26 Oreo (8.0)
     */
    fun hasOreo(): Boolean {
        return Build.VERSION.RELEASE == "O" || SDK_INT >= Build.VERSION_CODES.O
    }

    /**
     * @return if >=27 Oreo MR1 (8.1)
     */
    fun hasOreoMR1(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.O_MR1
    }

    /**
     * @return if >=28
     */
    fun hasAndroidP(): Boolean {
        return Build.VERSION.RELEASE == "P" || SDK_INT >= Build.VERSION_CODES.P
    }

    /**
     * @return if >=29
     */
    fun hasAndroidQ(): Boolean {
        return Build.VERSION.RELEASE == "Q" || Build.VERSION.RELEASE == "10" || SDK_INT >= 29
    }


    /**
     * @return if >=30
     */
    fun hasAndroid11(): Boolean {
        return "R" == Build.VERSION.RELEASE || "11" == Build.VERSION.RELEASE || SDK_INT >= 30
    }
}

fun ApiHelper.hasAPILevel(level: Int): Boolean = SDK_INT >= level