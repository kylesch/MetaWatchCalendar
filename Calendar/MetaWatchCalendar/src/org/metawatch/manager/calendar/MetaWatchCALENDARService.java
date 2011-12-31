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
    private static long elapsedtime;
    private static long UPDATE_INTERVAL = 10*60*1000;  //default
    private static long DELAY_INTERVAL = 1;//5*1000;

	long		lastObservedCount	= 0;
	int		Count	= 0;

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
            	if (elapsedtime != lastObservedCount) {
            		Log.d(Constants.LOG_TAG,"CalendarService.run(): Changed from "	+ lastObservedCount + " to " + elapsedtime + ", refreshing idle screen.");
            		lastObservedCount = elapsedtime;
            		IdleScreenWidgetRenderer.sendIdleScreenWidgetUpdate(ctx);
            		
            		if (lastObservedCount == 1){
        				NotificationBuilder.createCalendar(ctx, "There is 1 minute until the event : " + title + " that is being held at :" + location);
            			
            		}
            	}
            }
        }            


    /*
     * start the processing, the actual work, getting config params, get data from network etc
     */


public static void readCalendar() {

String titletemp="";
String locationtemp="";
long begintemp=0;
	elapsedtime=0;
	
	location="nowhere";
	ContentResolver cr = ctx.getContentResolver();
	Cursor cursor = cr.query(Uri.parse("content://com.android.calendar/calendars"), new String[]{ "_id","calendar_displayName"}, null, null, null);
	cursor.moveToFirst();
	String[] CalNames = new String[cursor.getCount()];
	int[] CalIds = new int[cursor.getCount()];
	for (int i = 0; i < CalNames.length; i++) {
	    CalIds[i] = cursor.getInt(0);
	    //Log.v("CALID : ", "enter");
	    CalNames[i] = cursor.getString(1);
	    cursor.moveToNext();
	}
	cursor.close();
	
	


	Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
	        long now = new Date().getTime();
	        final long CurrentTime = System.currentTimeMillis();
	        ContentUris.appendId(builder, now );
	        ContentUris.appendId(builder, now + DateUtils.DAY_IN_MILLIS);
	        
	        
	        Cursor eventCursor = cr.query(builder.build(),
	                //new String[] { "event_id", "begin", "end", "end"}, "Calendars._id=" + 1, null, "startDay ASC, startMinute ASC"); 
	        		new String[] { "event_id", "begin", "end", "end"}, null, null, "startDay ASC, startMinute ASC");
	        // For a full list of available columns see http://tinyurl.com/yfbg76w
	        while (eventCursor.moveToNext()) {
	        	/*******************/
	        	
	        	if (eventCursor.getLong(1) > CurrentTime){
	        		//Log.v("Title : ", String.valueOf(eventCursor.getLong(1)));
        			//Log.v("Now : ", String.valueOf(now));
		        	String uid2 = eventCursor.getString(0);
		        	
		        	Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/events/" + uid2);
		        	Cursor c = cr.query(CALENDAR_URI,new String[] { "title", "eventLocation", "description",}, null, null, null); 
		        	if (c.moveToFirst())
		        	{
		        			//Log.v("nope : ", uid2);
		        			//Log.v("Title : ", c.getString(c.getColumnIndex("title")));
		        			//Log.v("yup : ", c.getString(c.getColumnIndex("eventLocation")));
		        			//Log.v("yup : ", c.getString(c.getColumnIndex("description")));
	
		        	        while (c.moveToNext())
		        	        {
		        	        	//Log.v("yup : ", uid2);
		        	                titletemp = c.getString(c.getColumnIndex("title"));
		        	                locationtemp = c.getString(c.getColumnIndex("eventLocation"));    
		        	                //Log.v("yup : ", titletemp);
		        	                //Log.v("yup : ", locationtemp);
		        	                
		        	                break;
		        	        }
		        	}
		        	 
		        	c.close();
		        	
		        	
		        	
		        	/******************/
		        
		        		
		            
		            //Log.v("begin : ", uid2);
		           
		            begintemp = eventCursor.getLong(1);
					
					long elapsedtimetemp = (begintemp-CurrentTime)/(60 * 1000);
	
					//Log.v("Current : ",String.valueOf(CurrentTime));
					//Log.v("begin : ",String.valueOf(begintemp));
					//Log.v("Elapsed : ",String.valueOf(elapsedtimetemp));
					
					if (elapsedtime != 0) {
						if (elapsedtime > elapsedtimetemp){
							elapsedtime = elapsedtimetemp;
							title = titletemp;
							begin = begintemp;
							location = locationtemp;
						}
					}
					else
					{
						elapsedtime = elapsedtimetemp;
						title = titletemp;
						begin = begintemp;
						location = locationtemp;
	
					}
					//Log.v("location : ", location);
					break;
	        	}

	        }
	        
	        eventCursor.close();
	        //Log.v("begin : ",String.valueOf(begin));
	
	
	}


    /*
     * shutting down the service
     */

    public static long GetTime()
    {
    	return elapsedtime;
    }

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
