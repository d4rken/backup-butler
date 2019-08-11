//package eu.darken.bb.common;
//
//import android.app.Activity;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.ActivityInfo;
//import android.content.pm.ResolveInfo;
//import android.net.Uri;
//import android.widget.Toast;
//
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//
//import javax.inject.Inject;
//
//import androidx.annotation.StringRes;
//import androidx.core.content.ContextCompat;
//import androidx.core.content.FileProvider;
//import eu.darken.bb.App;
//import eu.darken.bb.R;
//import eu.darken.bb.common.dagger.AppContext;
//import timber.log.Timber;
//
//public class IntentTool {
//    static final String TAG = App.logTag("IntentTool");
//    private final Context context;
//
//    @Inject
//    public IntentTool(@AppContext Context context) {this.context = context;}
//
//    public SendAction send() {
//        return new SendAction(context);
//    }
//
//    public FileViewAction file(SDMFile sdmFile) {
//        return new FileViewAction(context, sdmFile);
//    }
//
//    public LinkViewAction view(String link) {
//        return new LinkViewAction(context, link);
//    }
//
//    public GooglePlayAction gplay(String pkg) {
//        return new GooglePlayAction(context, pkg);
//    }
//
//    public SystemPanelAction sysPanel(String pkg) {
//        return new SystemPanelAction(context, pkg);
//    }
//
//    public static class SystemPanelAction extends Builder<SystemPanelAction> {
//        private static final String TAG = App.logTag("IntentTool", "SystemPanelAction");
//        private final String packageName;
//        private String intentAction;
//
//        SystemPanelAction(Context context, String packageName) {
//            super(context);
//            this.packageName = packageName;
//        }
//
//        @Override
//        protected SystemPanelAction getThis() {
//            return this;
//        }
//
//        public SystemPanelAction appSettings() {
//            this.intentAction = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
//            return this;
//        }
//
//        @Override
//        public Intent create() {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setAction(intentAction);
//            intent.setData(Uri.parse("package:" + packageName));
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            return intent;
//        }
//    }
//
//    public static class GooglePlayAction extends Builder<GooglePlayAction> {
//        private static final String TAG = App.logTag("IntentTool", "GooglePlayAction");
//        private final String packageName;
//
//        GooglePlayAction(Context context, String packageName) {
//            super(context);
//            this.packageName = packageName;
//        }
//
//        @Override
//        protected GooglePlayAction getThis() {
//            return this;
//        }
//
//        @Override
//        public Intent create() {
//            // https://stackoverflow.com/a/28090925/1251958
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
//
//            final List<ResolveInfo> candidates = getContext().getPackageManager().queryIntentActivities(intent, 0);
//            for (ResolveInfo candidate : candidates) {
//                if (!candidate.activityInfo.applicationInfo.packageName.equals("com.android.vending")) continue;
//
//                ActivityInfo activityInfo = candidate.activityInfo;
//                ComponentName componentName = new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name);
//                // make sure it does NOT open in the stack of your activity
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                // task reparenting if needed
//                intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//                // if the Google Play was already open in a search result this make sure it still go to the app page you requested
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                // this make sure only the Google Play app is allowed to intercept the intent
//                intent.setComponent(componentName);
//                return intent;
//            }
//            return new LinkViewAction(getContext(), "https://play.google.com/store/apps/details?id=" + packageName).create();
//        }
//    }
//
//    public static abstract class Builder<BUILDERTYPE extends Builder> {
//        protected FallbackAction<BUILDERTYPE> fallbackAction;
//        private final Context context;
//
//        protected Builder(Context context) {this.context = context;}
//
//        public Context getContext() {
//            return context;
//        }
//
//        public String getString(@StringRes int stringRes) {
//            return context.getString(stringRes);
//        }
//
//        protected abstract BUILDERTYPE getThis();
//
//        public BUILDERTYPE fallback(FallbackAction<BUILDERTYPE> fallbackAction) {
//            this.fallbackAction = fallbackAction;
//            return getThis();
//        }
//
//        public abstract Intent create();
//
//        public void start() {
//            try {
//                Intent intent = create();
//                if (!(context instanceof Activity)) {
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                }
//                context.startActivity(intent);
//            } catch (Exception e) {
//                Timber.w(e);
//                Toast.makeText(context, context.getString(R.string.no_suitable_app_found), Toast.LENGTH_SHORT).show();
//                if (fallbackAction != null) fallbackAction.onFallback(getThis());
//            }
//        }
//
//        public interface FallbackAction<BUILDERTYPE extends Builder> {
//            void onFallback(BUILDERTYPE viewAction);
//        }
//    }
//
//    public static class FileViewAction extends Builder<FileViewAction> {
//        private static final String TAG = App.logTag("IntentTool", "FileViewAction");
//        private final SDMFile sdmFile;
//        private boolean forceChooser = false;
//        private boolean forceAsText = false;
//
//        FileViewAction(Context context, SDMFile sdmFile) {
//            super(context);
//            this.sdmFile = sdmFile;
//        }
//
//        @Override
//        protected FileViewAction getThis() {
//            return this;
//        }
//
//        public FileViewAction asText() {
//            forceAsText = true;
//            return this;
//        }
//
//        public FileViewAction withChooser() {
//            forceChooser = true;
//            return this;
//        }
//
//        @Override
//        public Intent create() {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//
//            Uri uri = addAccessibleFiles(getContext(), intent, Collections.singleton(sdmFile)).get(0);
//
//            if (forceAsText) intent.setDataAndType(uri, "text/plain");
//            else intent.setDataAndType(uri, MimeHelper.INSTANCE.getMime(sdmFile));
//
//            if (forceChooser) intent = Intent.createChooser(intent, getContext().getString(R.string.button_open));
//
//            Timber.tag(TAG).d("Created intent %s", intent);
//
//            return intent;
//        }
//    }
//
//    public static class LinkViewAction extends Builder<LinkViewAction> {
//        private final Intent intent;
//        private final String link;
//        private Activity activity;
//        private boolean chromeTabs;
//        private boolean trackThis;
//
//        LinkViewAction(Context context, String link) {
//            super(context);
//            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            this.link = link;
//        }
//
//        @Override
//        protected LinkViewAction getThis() {
//            return this;
//        }
//
//        public LinkViewAction track() {
//            trackThis = true;
//            return this;
//        }
//
//        public LinkViewAction chromeTabs(Activity activity) {
//            this.activity = activity;
//            chromeTabs = true;
//            return this;
//        }
//
//        @Override
//        public Intent create() {
//            return intent;
//        }
//
//        @Override
//        public void start() {
//            if (trackThis) {
//                try {
//                    App.getSDMContext().getPiwik().trackOutlink(new URL(link));
//                } catch (MalformedURLException e) { Timber.tag(TAG).e(e); }
//            }
//            if (chromeTabs) {
//                try {
//                    CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
//                            .enableUrlBarHiding()
//                            .setShowTitle(true)
//                            .setToolbarColor(ContextCompat.getColor(getContext(), R.color.primary_default))
//                            .setStartAnimations(activity, R.anim.slide_in_right, R.anim.slide_out_left)
//                            .setExitAnimations(activity, R.anim.slide_in_left, R.anim.slide_out_right)
//                            .build();
//                    customTabsIntent.launchUrl(activity, Uri.parse(link));
//                } catch (Exception e) {
//                    Timber.w(e);
//                    Toast.makeText(getContext(), getString(R.string.no_suitable_app_found), Toast.LENGTH_SHORT).show();
//                    if (fallbackAction != null) fallbackAction.onFallback(getThis());
//                }
//            } else super.start();
//        }
//    }
//
//
//}
