package org.metawatch.manager.calendar;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class CalendarAccessibilityService extends AccessibilityService {

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		AccessibilityServiceInfo asi = new AccessibilityServiceInfo();
		asi.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
		asi.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
		asi.flags = AccessibilityServiceInfo.DEFAULT;
		asi.notificationTimeout = 100;
		setServiceInfo(asi);
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {

		/* Acquire details of event. */
		CharSequence packageName = event.getPackageName();
		CharSequence className = event.getClassName();
		Log.d(Constants.LOG_TAG,"CalendarAccessibilityService.onAccessibilityEvent(): Received event, packageName = '"	+ packageName + "' className = '" + className + "'");

		Parcelable p = event.getParcelableData();
		if (p instanceof android.app.Notification == false) {
			Log.d(Constants.LOG_TAG, "CalendarAccessibilityService.onAccessibilityEvent(): Not a real notification, ignoring.");
			return;
		}

		android.app.Notification notification = (android.app.Notification) p;
		Log.d(Constants.LOG_TAG, "CalendarAccessibilityService.onAccessibilityEvent(): notification text = '" + notification.tickerText + "' flags = "	+ notification.flags + " ("	+ Integer.toBinaryString(notification.flags) + ")");

		if ((notification.flags & android.app.Notification.FLAG_ONGOING_EVENT) > 0) {
			/* Ignore updates to ongoing events. */
			Log.d(Constants.LOG_TAG, "CalendarAccessibilityService.onAccessibilityEvent(): Ongoing event, ignoring.");
			return;
		}

		if (notification.tickerText == null || notification.tickerText.toString().trim().length() == 0) {
			Log.d(Constants.LOG_TAG, "CalendarAccessibilityService.onAccessibilityEvent(): Empty text, ignoring.");
			return;
		}

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		/* Forward calendar event */
		if (packageName.equals("com.android.calendar")) {
			if (sharedPreferences.getBoolean("NotifyCalendar", true)) {
				Log.d(Constants.LOG_TAG, "onAccessibilityEvent(): Sending calendar event: '" + notification.tickerText + "'.");

				NotificationBuilder.createCalendar(this, notification.tickerText.toString());
				return;
			}
		}

	}

	@Override
	public void onInterrupt() {
		/* Do nothing */
	}

}
