package com.retroglow;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.RemoteViews;

public class RetroGlowWidget extends AppWidgetProvider {

    private static final int[] LEVELS = {
            0, 15, 30, 45, 60, 75, 90, 100
    };

    private static final int[] ZONE_IDS = {
            R.id.retroglow_zone_0,
            R.id.retroglow_zone_15,
            R.id.retroglow_zone_30,
            R.id.retroglow_zone_45,
            R.id.retroglow_zone_60,
            R.id.retroglow_zone_75,
            R.id.retroglow_zone_90,
            R.id.retroglow_zone_100
    };

    private static final int TRACK_START_DP = 39;
    private static final int TRACK_END_DP = 39;

    @Override
    public void onUpdate(
            Context context,
            AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            updateWidget(
                    context,
                    appWidgetManager,
                    appWidgetId,
                    getSavedLevel(context)
            );
        }
    }

    private static void updateWidget(
            Context context,
            AppWidgetManager manager,
            int widgetId,
            int level) {

        RemoteViews views =
                new RemoteViews(
                        context.getPackageName(),
                        R.layout.retroglow_widget
                );

        for (int i = 0; i < ZONE_IDS.length; i++) {

            Intent intent =
                    new Intent(
                            context,
                            RetroGlowWidget.class
                    );

            intent.setAction(
                    "com.retroglow.SET_BRIGHTNESS"
            );

            intent.putExtra(
                    "brightness",
                    LEVELS[i]
            );

            intent.putExtra(
                    "widget_id",
                    widgetId
            );

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(
                            context,
                            widgetId * 100 + i,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                                    | PendingIntent.FLAG_IMMUTABLE
                    );

            views.setOnClickPendingIntent(
                    ZONE_IDS[i],
                    pendingIntent
            );
        }

        updateVisualState(
                views,
                level
        );

        manager.updateAppWidget(
                widgetId,
                views
        );
    }

    @Override
    public void onReceive(
            Context context,
            Intent intent) {

        super.onReceive(
                context,
                intent
        );

        if (!"com.retroglow.SET_BRIGHTNESS"
                .equals(intent.getAction())) {

            return;
        }

        int brightness =
                intent.getIntExtra(
                        "brightness",
                        50
                );

        int widgetId =
                intent.getIntExtra(
                        "widget_id",
                        -1
                );

        setBrightness(
                context,
                brightness
        );

        saveLevel(
                context,
                brightness
        );

        if (widgetId != -1) {

            AppWidgetManager manager =
                    AppWidgetManager.getInstance(
                            context
                    );

            updateWidget(
                    context,
                    manager,
                    widgetId,
                    brightness
            );

        } else {

            updateAllWidgets(
                    context,
                    brightness
            );
        }
    }

    private static void updateAllWidgets(
            Context context,
            int brightness) {

        AppWidgetManager manager =
                AppWidgetManager.getInstance(
                        context
                );

        ComponentName componentName =
                new ComponentName(
                        context,
                        RetroGlowWidget.class
                );

        int[] ids =
                manager.getAppWidgetIds(
                        componentName
                );

        for (int id : ids) {

            updateWidget(
                    context,
                    manager,
                    id,
                    brightness
            );
        }
    }

    private static void setBrightness(
            Context context,
            int percentage) {

        if (!Settings.System.canWrite(context)) {

            Intent intent =
                    new Intent(
                            Settings.ACTION_MANAGE_WRITE_SETTINGS
                    );

            intent.setData(
                    Uri.parse(
                            "package:"
                                    + context.getPackageName()
                    )
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(intent);

            return;
        }

        int value =
                Math.round(
                        percentage
                                * 255f
                                / 100f
                );

        Settings.System.putInt(
                context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                value
        );
    }

    private static void updateVisualState(
            RemoteViews views,
            int brightness) {

        int index = 0;

        for (int i = 0; i < LEVELS.length; i++) {

            if (brightness >= LEVELS[i]) {
                index = i;
            }
        }

        float position =
                index
                        / (float)
                        (LEVELS.length - 1);

        int trackWidthDp = 180;

        int knobWidthDp = 42;

        int usableWidthDp =
                trackWidthDp
                        - knobWidthDp;

        int knobOffsetDp =
                Math.round(
                        usableWidthDp
                                * position
                );

        views.setViewPadding(
                R.id.retroglow_knob,
                0,
                0,
                0,
                0
        );

        views.setInt(
                R.id.retroglow_knob,
                "setTranslationX",
                dpToPx(
                        position,
                        usableWidthDp
                )
        );

        int amberWidth =
                Math.max(
                        8,
                        Math.round(
                                trackWidthDp
                                        * position
                        )
                );

        views.setViewLayoutWidth(
                R.id.retroglow_light,
                dpToPx(
                        amberWidth,
                        amberWidth
                )
        );
    }

    private static int dpToPx(
            float position,
            int widthDp) {

        return Math.round(
                position
                        * widthDp
        );
    }

    private static int getSavedLevel(
            Context context) {

        return context
                .getSharedPreferences(
                        "retroglow",
                        Context.MODE_PRIVATE
                )
                .getInt(
                        "brightness",
                        60
                );
    }

    private static void saveLevel(
            Context context,
            int brightness) {

        context
                .getSharedPreferences(
                        "retroglow",
                        Context.MODE_PRIVATE
                )
                .edit()
                .putInt(
                        "brightness",
                        brightness
                )
                .apply();
    }
}
