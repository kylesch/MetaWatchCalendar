/*****************************************************************************
 *  Some of the code in this project is derived from the                     *
 *  MetaWatch MWM-for-Android project,                                       *
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
package org.metawatch.manager.calendar;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.util.Log;


import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MetaWatchCALENDARService extends Service {
	private static Context ctx;
    private Timer timer=new Timer();
    private static long currentremaintime = 0;
    private static long UPDATE_INTERVAL = 60*1000;
    private static long DELAY_INTERVAL = 1;

	long	lastremaintime	= 0;


	static String title = "";
	static long begin = 1;
	static String location = "";


    public void onCreate()
    {
        super.onCreate();
        ctx = this; 
        startService();
		IdleScreenWidgetRenderer.sendIdleScreenWidgetUpdate(ctx);
    }


    public void onDestroy()
    {
      super.onDestroy();
    }


    /*
     * starting the service
     */
    private void startService()
    {
    	
        timer.scheduleAtFixedRate(new mainTask(), DELAY_INTERVAL, UPDATE_INTERVAL);
    }
        private class mainTask extends TimerTask
        { 
            public void run() 
            {
               	readCalendar();
            	if (currentremaintime != lastremaintime) {
            		Log.d(Constants.LOG_TAG,"CalendarService.run(): Changed from "	+ lastremaintime + " to " + currentremaintime + ", refreshing idle screen.");
            		lastremaintime = currentremaintime;
            		IdleScreenWidgetRenderer.sendIdleScreenWidgetUpdate(ctx);
            		
            		if (lastremaintime == 1){
        				NotificationBuilder.createCalendar(ctx, "There is 1 minute until the event : " + title + " that is being held at :" + location);
            			
            		}
            	}
            }
        }            


    /*
     * start the processing, the actual work, getting config params, get data from network etc
     */


public static void readCalendar() {
    long now = new Date().getTime();
    final long CurrentTime = System.currentTimeMillis();
	
    String titletemp="";
	String locationtemp="";
	long begintemp=0;
	long elapsedtimetemp=0;
	
	currentremaintime=0;
	location="nowhere";
	
	ContentResolver cr = ctx.getContentResolver();
	Cursor cursor = cr.query(Uri.parse("content://com.android.calendar/calendars"), new String[]{ "_id","calendar_displayName"}, null, null, null);
	cursor.moveToFirst();
	String[] CalNames = new String[cursor.getCount()];
	int[] CalIds = new int[cursor.getCount()];
	for (int i = 0; i < CalNames.length; i++) {
	    CalIds[i] = cursor.getInt(0);
	    CalNames[i] = cursor.getString(1);
	    cursor.moveToNext();
	}
	cursor.close();
	Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();

	ContentUris.appendId(builder, now );
	ContentUris.appendId(builder, now + DateUtils.DAY_IN_MILLIS);	        
	Cursor eventCursor = cr.query(builder.build(),
	new String[] { "event_id", "begin", "end", "end"}, null, null, "startDay ASC, startMinute ASC");
	        // For a full list of available columns see http://tinyurl.com/yfbg76w
	while (eventCursor.moveToNext()) {
		if (eventCursor.getLong(1) > CurrentTime){
		   	String uid2 = eventCursor.getString(0);	
		    Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/events/" + uid2);
		    Cursor c = cr.query(CALENDAR_URI,new String[] { "title", "eventLocation", "description",}, null, null, null); 
		    
		    if (c.moveToFirst())
		    {	
		    	while (c.moveToNext())
		        {
		    		titletemp = c.getString(c.getColumnIndex("title"));
		        	locationtemp = c.getString(c.getColumnIndex("eventLocation"));    
		        	break;
		        }
		    }
		        	 
		    c.close();
		    begintemp = eventCursor.getLong(1);		
			elapsedtimetemp = (begintemp-CurrentTime)/(60 * 1000);
						
			if (currentremaintime != 0) {
				if (currentremaintime > elapsedtimetemp){
					currentremaintime = elapsedtimetemp;
					title = titletemp;
					begin = begintemp;
					location = locationtemp;
				}
			}
			else
			{
				currentremaintime = elapsedtimetemp;
				title = titletemp;
				begin = begintemp;
				location = locationtemp;
	
			}
			
			break;
		}
	}   
	 eventCursor.close();	
}


    /*
     * shutting down the service
     */

    public static long GetTime()
    {
    	return currentremaintime;
    }

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
