package com.longway.theme;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by longway on 16/3/30.
 */
public class ThemeManager extends Observable {
    private static final String TAG = ThemeManager.class.getSimpleName();
    private static final String APK_SUFFIX = ".apk";
    private static volatile ThemeManager sThemeManager;
    private AtomicBoolean mInit;

    private HashMap<String, ApkInfo> mResources;
    private ApkInfo mCurrentResources;
    private Time mTime;


    private ThemeManager() {
        mInit = new AtomicBoolean(false);
        mResources = new HashMap<>();
        mTime = new Time();
    }

    public static ThemeManager getInstance() {
        if (sThemeManager == null) {
            synchronized (ThemeManager.class) {
                if (sThemeManager == null) {
                    sThemeManager = new ThemeManager();
                }
            }
        }
        return sThemeManager;
    }

    public void registerObserver(Observer observer) {
        addObserver(observer);
    }

    public void unregisterObserver(Observer observer) {
        deleteObserver(observer);
    }

    public Drawable getBackground(Context context, String name) throws Resources.NotFoundException {
        ApkInfo apkInfo = mCurrentResources;
        String packageName = context.getPackageName();
        if (apkInfo== null) {
            Resources hostRes = context.getResources();
            return hostRes.getDrawable(hostRes.getIdentifier(name, "drawable", packageName));
        }
        Resources resources = apkInfo.mResources;
        return resources.getDrawable(resources.getIdentifier(name, "drawable",apkInfo.mPackageName));
    }

    public int getColor(Context context, String name) throws Resources.NotFoundException{
        ApkInfo apkInfo = mCurrentResources;
        if (apkInfo == null) {
            Resources hostRes = context.getResources();
            return hostRes.getColor(hostRes.getIdentifier(name, "color", context.getPackageName()));
        }
        Resources resources = apkInfo.mResources;
        int color = resources.getColor(resources.getIdentifier(name, "color", apkInfo.mPackageName));
        return color;
    }


    public boolean addTheme(Context context, String url) {
        mTime.setPerformStartTime(System.currentTimeMillis());
        if (mResources.containsKey(url)) {
            mCurrentResources = mResources.get(url);
            SPUtils.put(Common.THEME_CONFIG,context,Common.THEME_URL_KEY,url);
            return true;
        }
        boolean loadSuccess = loadResources(context, url);
        if (!loadSuccess) {
            // 文件不存在，或者加载失败，可能文件破坏，去服务器拉取最新文件
            // 本地测试 assert
            boolean copySuccess = AssertUtils.copyFileFromAssert(new File(context.getFilesDir(), Common.THEME_DIR).getAbsolutePath(), EncryptionUtils.md5Encrypt(url).concat(APK_SUFFIX), "theme/themedemo1-debug.apk");
            if (copySuccess) {
                loadSuccess = loadResources(context, url);
                if (loadSuccess) {
                    // 加载资源成功，通知ui刷新界面
                    Log.d(TAG, "notify observer");
                    setChanged();
                    notifyObservers(mCurrentResources);
                }
            }
        }
        mTime.print();
        Log.d(TAG, String.valueOf(loadSuccess));
        return loadSuccess;
    }

    public boolean init(Context context) {
        if (mInit.compareAndSet(false, true)) {
            String url = (String) SPUtils.get(Common.THEME_CONFIG, context, Common.THEME_URL_KEY, "");
            Log.d(TAG, url);
            if (!TextUtils.isEmpty(url)) {
                return loadResources(context, url);
            }
            return false;
        }
        return true;
    }

    private boolean loadResources(Context context, String url) {
        String md5Str = EncryptionUtils.md5Encrypt(url);
        if (!TextUtils.isEmpty(md5Str)) {
            File dir = new File(context.getFilesDir(), Common.THEME_DIR);
            if (!dir.exists() && !dir.mkdirs()) {
                return false;
            }
            File res = new File(dir, md5Str.concat(APK_SUFFIX));
            if (!res.exists()) {
                return false;
            }
            Class<?> clz = AssetManager.class;
            try {
                Method method = clz.getDeclaredMethod("addAssetPath", String.class);
                AssetManager object = (AssetManager) clz.newInstance();
                String archivePath = res.getAbsolutePath();
                method.invoke(object, archivePath);
                Resources hostRes = context.getResources();
                Resources resources = new Resources(object, hostRes.getDisplayMetrics(), hostRes.getConfiguration());
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageArchiveInfo(archivePath, Context.MODE_PRIVATE);
                ApkInfo apkInfo = new ApkInfo();
                apkInfo.mResources = resources;
                apkInfo.mPackageName = packageInfo.packageName;
                Log.d(TAG,"["+resources.toString()+","+apkInfo.mPackageName+"]");
                mResources.put(url, apkInfo);
                SPUtils.put(Common.THEME_CONFIG, context, Common.THEME_URL_KEY, url);
                mCurrentResources = apkInfo;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

}
