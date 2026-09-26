package com.retroglow;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.RemoteViews;

public class RetroGlowWidget extends AppWidgetProvider {

    public static final String ACTION_SET_BRIGHTNESS =
            "com.retroglow.SET_BRIGHTNESS";

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

    @Override
    public void onUpdate(
            Context context,
            AppWidgetManager manager,
            int[] widgetIds) {

        for (int widgetId : widgetIds) {

            updateWidget(
                    context,
                    manager,
                    widgetId
            );
        }
    }

    private static void updateWidget(
            Context context,
            AppWidgetManager manager,
            int widgetId) {

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
                    ACTION_SET_BRIGHTNESS
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

        if (!ACTION_SET_BRIGHTNESS.equals(
                intent.getAction())) {

            return;
        }

        int brightness =
                intent.getIntExtra(
                        "brightness",
                        60
                );

        setBrightness(
                context,
                brightness
        );

        saveLevel(
                context,
                brightness
        );

        RetroGlowSound.playDetent();

        updateAllWidgets(
                context
        );
    }

    private static void updateAllWidgets(
            Context context) {

        AppWidgetManager manager =
                AppWidgetManager.getInstance(
                        context
                );

        ComponentName componentName =
                new ComponentName(
                        context,
                        RetroGlowWidget.class
                );

        int[] widgetIds =
                manager.getAppWidgetIds(
                        componentName
                );

        for (int widgetId : widgetIds) {

            updateWidget(
                    context,
                    manager,
                    widgetId
            );
        }
    }

    private static void setBrightness(
            Context context,
            int percentage) {

        if (!Settings.System.canWrite(context)) {

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
