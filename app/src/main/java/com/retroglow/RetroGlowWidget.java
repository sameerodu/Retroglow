package com.retroglow;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;
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
            AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            updateWidget(
                    context,
                    appWidgetManager,
                    appWidgetId
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

        int brightness =
                context
                        .getSharedPreferences(
                                "retroglow",
                                Context.MODE_PRIVATE
                        )
                        .getInt(
                                "brightness",
                                60
                        );

        int selectedIndex =
                findClosestLevel(brightness);

        /*
         * Keep the fill visible for now.
         * The next pass will replace this with
         * proper predefined visual states.
         */
        if (selectedIndex == 0) {

            views.setViewVisibility(
                    R.id.retroglow_fill,
                    View.INVISIBLE
            );

        } else {

            views.setViewVisibility(
                    R.id.retroglow_fill,
                    View.VISIBLE
            );
        }

        /*
         * Connect each large touch zone to one
         * of the eight brightness levels.
         */
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

        saveBrightness(
                context,
                brightness
        );

        setBrightness(
                context,
                brightness
        );

        try {
            RetroGlowSound.playDetent();
        } catch (Exception ignored) {
        }

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

    private static void saveBrightness(
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

    private static void setBrightness(
            Context context,
            int percentage) {

        if (!Settings.System.canWrite(context)) {
            return;
        }

        int value =
                Math.round(
                        percentage * 255f / 100f
                );

        Settings.System.putInt(
                context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                value
        );
    }

    private static int findClosestLevel(
            int brightness) {

        int closestIndex = 0;

        int smallestDifference =
                Math.abs(
                        brightness - LEVELS[0]
                );

        for (int i = 1; i < LEVELS.length; i++) {

            int difference =
                    Math.abs(
                            brightness - LEVELS[i]
                    );

            if (difference < smallestDifference) {

                smallestDifference =
                        difference;

                closestIndex =
                        i;
            }
        }

        return closestIndex;
    }
}
