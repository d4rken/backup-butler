package eu.darken.bb.common.debug;

import android.annotation.SuppressLint;

import com.bugsnag.android.Bugsnag;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import timber.log.Timber;

public class BugTrack {
    @VisibleForTesting
    private static boolean testing;

    static {
        try {
            Class.forName("testhelper.IsAUnitTest");
            testing = true;
        } catch (ClassNotFoundException e) { testing = false; }
    }

    public static void notify(Throwable throwable) {
        notify(null, throwable, null, (Object[]) null);
    }

    public static void notify(@Nullable String tag, Throwable throwable) {
        notify(tag, throwable, null, (Object[]) null);
    }

    @SuppressLint("TimberExceptionLogging")
    public static void notify(@Nullable String tag, Throwable throwable, @Nullable String message, @Nullable Object... args) {
        if (tag != null) {
            Timber.tag(tag).e(throwable, message, args);
        } else {
            Timber.e(throwable, message, args);
        }

        if (testing) return;
        Bugsnag.notify(throwable);
    }
}
