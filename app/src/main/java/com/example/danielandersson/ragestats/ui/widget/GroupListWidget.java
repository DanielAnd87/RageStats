package com.example.danielandersson.ragestats.ui.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.danielandersson.ragestats.Data.Constants;
import com.example.danielandersson.ragestats.R;
import com.example.danielandersson.ragestats.ui.activites.MainActivity;

import static android.content.ContentValues.TAG;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link GroupListWidgetConfigureActivity GroupListWidgetConfigureActivity}
 */
public class GroupListWidget extends AppWidgetProvider {


    public static final String ACTION_DATA_UPDATED = "data_updated";
    public static final String ACTION_SMILEY_UPDATED = "action_smiley_updated";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = GroupListWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.group_list_widget);
//        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        Log.i(TAG, "updateAppWidget: " + appWidgetId);


        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);

//
//        final Intent service = startService(context, appWidgetId);
//        context.startService(service);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        final Intent service = startService(context, appWidgetId);

        context.startService(service);
        appWidgetManager.updateAppWidgetOptions(appWidgetId, newOptions);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.group_list_widget);

            Intent configIntent = new Intent(context, GroupListWidgetConfigureActivity.class);
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            configIntent.setData(Uri.withAppendedPath(Uri.parse("abc" + "://widget/id/"), String.valueOf(appWidgetId)));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);



            // click event handler for the title, launches the app when the user clicks on title
            Intent titleIntent = new Intent(context, MainActivity.class);
            PendingIntent titlePendingIntent = PendingIntent.getActivity(context, 0, titleIntent, 0);
            views.setOnClickPendingIntent(R.id.smiley_button_main, titlePendingIntent);

            Intent intent = new Intent(context, GroupListIntentService.class);
            views.setRemoteAdapter(R.id.widget_list, intent);

            appWidgetManager.updateAppWidget(appWidgetId, views);

            // template to handle the click listener for each item
            Intent clickIntentTemplate = new Intent(context, MainActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);



            Intent intentToStartService = startService(context, appWidgetId);
            views.setRemoteAdapter(R.id.widget_list,
                    intentToStartService);
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

            Log.i(TAG, "onUpdate: " + appWidgetId);
        }
    }

    private Intent updateWidget(Context context) {
        Intent updateWidgetIntent = new Intent(context,
                GroupListWidget.class);
        updateWidgetIntent.setAction(
                GroupListWidget.ACTION_DATA_UPDATED);
        return updateWidgetIntent;
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            GroupListWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }


    @NonNull
    private static Intent startService(Context context, int appWidgetId) {
        final Intent service = new Intent(context, GroupListIntentService.class);
        service.putExtra(Constants.WIDGET_ID_INTENT, appWidgetId);
        return service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(ACTION_DATA_UPDATED)) {
            Log.i(TAG, "onReceive: Updating widget");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        } else if (intent.getAction().equals(ACTION_SMILEY_UPDATED)) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            // TODO: 2017-09-20 need also share whitch widget needs to update.
            editor.putInt(Constants.TEMP_SMILEY_INDEX,  2);
            editor.apply();
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    public static void sendUpdateBroadcast(Context context) {
        Intent updateWidgetIntent = new Intent(context,
                GroupListWidget.class);
        updateWidgetIntent.setAction(
                GroupListWidget.ACTION_DATA_UPDATED);
        context.sendBroadcast(updateWidgetIntent);
    }
    public static void sendSmileyUpdateBroadcast(Context context) {

        Intent updateWidgetIntent = new Intent(context,
                GroupListWidget.class);
        updateWidgetIntent.setAction(
                GroupListWidget.ACTION_SMILEY_UPDATED);
        context.sendBroadcast(updateWidgetIntent);
    }
}

