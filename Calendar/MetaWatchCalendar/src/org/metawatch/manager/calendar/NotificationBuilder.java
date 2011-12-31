



 /*****************************************************************************
  *  Copyright (c) 2011 Meta Watch Ltd.                                       *
  *  www.MetaWatch.org                                                        *
  *                                                                           *
  =============================================================================
  *                                                                           *
  *  Licensed under the Apache License, Version 2.0 (the "License");          *
  *  you may not use this file except in compliance with the License.         *
  *  You may obtain a copy of the License at                                  *
  *                                                                           *
  *    http://www.apache.org/licenses/LICENSE-2.0                             *
  *                                                                           *
  *  Unless required by applicable law or agreed to in writing, software      *
  *  distributed under the License is distributed on an "AS IS" BASIS,        *
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
  *  See the License for the specific language governing permissions and      *
  *  limitations under the License.                                           *
  *                                                                           *
  *****************************************************************************/

 /*****************************************************************************
  * NotificationBuilder.java                                                  *
  * NotificationBuilder                                                       *
  * Templates for different kinds of notification screens                     *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager.calendar;

import org.metawatch.manager.core.lib.intents.DisplayNotification;
import android.content.Context;

public class NotificationBuilder {

	public static final class FontSize {
		public static final int SMALL = 1;
		public static final int MEDIUM = 2;
		public static final int LARGE = 3;
	}

	public static final String DEFAULT_NUMBER_OF_BUZZES = "3";



	public static void createCalendar(Context context, String text) {
		//VibratePattern vibratePattern = createVibratePatternFromPreference(context, "settingsCalendarNumberBuzzes");

					DisplayNotification req = new DisplayNotification();
					req.vibrateOnDuration = 500;
					req.vibrateOffDuration = 500;
					req.vibrateNumberOfCycles = 3;
					req.oledTopText = "CAL";
					req.oledBottomLine1Text = text;
					req.oledBottomLine2Text = text;
					req.lcdText = "Upcoming Event in " + MetaWatchCALENDARService.GetTime() + " minutes : " + text;

					context.sendBroadcast(req.toIntent());

	/*	if (MetaWatchService.watchType == WatchType.DIGITAL) {
			Bitmap bitmap = smartLines(context, "calendar.bmp", new String[] {"Calendar Event", text});
			Notification.addBitmapNotification(context, bitmap, vibratePattern, Notification.getDefaultNotificationTimeout(context));
			Notification.addTextNotification(context, text, Notification.VibratePattern.NO_VIBRATE, Notification.getDefaultNotificationTimeout(context));
		} else {
			byte[] scroll = new byte[800];
			int len = Protocol.createOled2linesLong(context, text, scroll);
			Notification.addOledNotification(context, Protocol.createOled1line(context, "calendar.bmp", "  Calendar"), Protocol.createOled2lines(context, "Event Reminder:", text), scroll, len, vibratePattern);
		}
	*/
	}




	

}
