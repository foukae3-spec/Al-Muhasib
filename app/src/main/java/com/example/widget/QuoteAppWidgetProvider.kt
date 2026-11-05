package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class QuoteAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        WidgetUpdateHelper.updateQuoteWidgets(context)
    }

    override fun onEnabled(context: Context) {
        WidgetUpdateHelper.updateQuoteWidgets(context)
    }
}
