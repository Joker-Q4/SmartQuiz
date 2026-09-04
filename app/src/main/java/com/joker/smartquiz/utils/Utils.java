package com.joker.smartquiz.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.Objects;

/**
 * 工具类初始化 & Activity 生命周期管理
 *
 * @author Blankj
 * @since 2016/12/08
 */
@SuppressWarnings("unused")
public final class Utils {

    @SuppressLint("StaticFieldLeak")
    private static Application sApplication;

    private static final ActivityLifecycleImpl ACTIVITY_LIFECYCLE = new ActivityLifecycleImpl();

    private Utils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static void init(final Context context) {
        if (context == null) {
            init(getApplicationByReflect());
            return;
        }
        init((Application) context.getApplicationContext());
    }

    /**
     * 初始化工具类
     *
     * @param app Application
     */
    public static void init(final Application app) {
        if (sApplication == null) {
            sApplication = Objects.requireNonNullElseGet(app, Utils::getApplicationByReflect);
            sApplication.registerActivityLifecycleCallbacks(ACTIVITY_LIFECYCLE);
        } else {
            if (app != null && app.getClass() != sApplication.getClass()) {
                sApplication.unregisterActivityLifecycleCallbacks(ACTIVITY_LIFECYCLE);
                ACTIVITY_LIFECYCLE.mActivityList.clear();
                sApplication = app;
                sApplication.registerActivityLifecycleCallbacks(ACTIVITY_LIFECYCLE);
            }
        }
    }

    /**
     * 获取 Application
     *
     * @return Application
     */
    public static Application getApp() {
        if (sApplication != null) {
            return sApplication;
        }
        Application app = getApplicationByReflect();
        init(app);
        return app;
    }

    private static Application getApplicationByReflect() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object thread = activityThread.getMethod("currentActivityThread").invoke(null);
            Object app = activityThread.getMethod("getApplication").invoke(thread);
            if (app == null) {
                throw new NullPointerException("u should init first");
            }
            return (Application) app;
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        throw new NullPointerException("u should init first");
    }

    /**
     * 获取栈顶 Activity
     *
     * @return 栈顶 Activity
     */
    public static Activity getTopActivity() {
        return ACTIVITY_LIFECYCLE.getTopActivity();
    }

    /**
     * 获取 Activity 列表
     *
     * @return Activity 列表
     */
    public static LinkedList<Activity> getActivityList() {
        return ACTIVITY_LIFECYCLE.mActivityList;
    }

    /**
     * 添加应用状态变化监听器
     *
     * @param listener 监听器
     */
    public static void addOnAppStatusChangedListener(final Object object,
                                                      final OnAppStatusChangedListener listener) {
        ACTIVITY_LIFECYCLE.addOnAppStatusChangedListener(object, listener);
    }

    /**
     * 移除应用状态变化监听器
     */
    public static void removeOnAppStatusChangedListener(final Object object) {
        ACTIVITY_LIFECYCLE.removeOnAppStatusChangedListener(object);
    }

    /**
     * 添加 Activity 销毁监听器
     *
     * @param activity Activity
     * @param listener 监听器
     */
    public static void addOnActivityDestroyedListener(final Activity activity,
                                                       final OnActivityDestroyedListener listener) {
        ACTIVITY_LIFECYCLE.addOnActivityDestroyedListener(activity, listener);
    }

    /**
     * 移除 Activity 销毁监听器
     *
     * @param activity Activity
     */
    public static void removeOnActivityDestroyedListener(final Activity activity) {
        ACTIVITY_LIFECYCLE.removeOnActivityDestroyedListener(activity);
    }

    /**
     * 应用状态变化监听器接口
     */
    public interface OnAppStatusChangedListener {
        void onForeground();
        void onBackground();
    }

    /**
     * Activity 销毁监听器接口
     */
    public interface OnActivityDestroyedListener {
        void onActivityDestroyed(Activity activity);
    }

    /**
     * Activity 生命周期实现类
     */
    static class ActivityLifecycleImpl implements Application.ActivityLifecycleCallbacks {

        final LinkedList<Activity> mActivityList = new LinkedList<>();
        private int mForegroundCount = 0;
        private int mConfigCount = 0;
        private boolean mIsBackground = false;

        @Override
        public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
            setTopActivity(activity);
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            if (!mIsBackground) {
                setTopActivity(activity);
            }
            if (mConfigCount < 0) {
                ++mConfigCount;
            } else {
                ++mForegroundCount;
            }
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            setTopActivity(activity);
            if (mIsBackground) {
                mIsBackground = false;
            }
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            // no-op
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (activity.isChangingConfigurations()) {
                --mConfigCount;
            } else {
                --mForegroundCount;
                if (mForegroundCount <= 0) {
                    mIsBackground = true;
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            // no-op
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            mActivityList.remove(activity);
        }

        Activity getTopActivity() {
            if (!mActivityList.isEmpty()) {
                return mActivityList.getLast();
            }
            return null;
        }

        private void setTopActivity(final Activity activity) {
            if (!mActivityList.contains(activity)) {
                mActivityList.addLast(activity);
            } else if (!mActivityList.getLast().equals(activity)) {
                mActivityList.remove(activity);
                mActivityList.addLast(activity);
            }
        }

        void addOnAppStatusChangedListener(final Object object,
                                            final OnAppStatusChangedListener listener) {
            // Simplified: not used in this project
        }

        void removeOnAppStatusChangedListener(final Object object) {
            // Simplified: not used in this project
        }

        void addOnActivityDestroyedListener(final Activity activity,
                                            final OnActivityDestroyedListener listener) {
            // Simplified: not used in this project
        }

        void removeOnActivityDestroyedListener(final Activity activity) {
            // Simplified: not used in this project
        }
    }
}
