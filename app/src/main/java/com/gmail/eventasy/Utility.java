package com.gmail.eventasy;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.gmail.eventasy.data.EventContract;

import static com.gmail.eventasy.ImportantConstants.DETAIL_FORMAT;

/**
 * Created by kakalra on 1/10/2017.
 */

public class Utility {

    public static final String ACTION_DATA_UPDATED = "com.gmail.kavleen.eventasy.ACTION_DATA_UPDATED";

    public static int isEventSetToFavourite(Context context, String eventId){

        Cursor cursor=context.getContentResolver().query(EventContract.EventEntry.CONTENT_URI,
                null,
                EventContract.EventEntry.COLUMN_EVENT_ID+" = ?",
                new String[]{eventId},
                null
        );
        int numOfRows=cursor.getCount();
        cursor.close();
        return numOfRows;
    }

    public static boolean isNetworkAvailable(Context c){
        ConnectivityManager cm=(ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork=cm.getActiveNetworkInfo();
        return activeNetwork!=null && activeNetwork.isConnectedOrConnecting();
    }

    public static String getFormattedDate(String date,int format){

        Date originalDate;
        String formattedDate;
        String extractedDate=date.substring(0,(date.indexOf(" ")));
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");

        try{
            originalDate=simpleDateFormat.parse(extractedDate);
            SimpleDateFormat newDateFormat;
            if(format==DETAIL_FORMAT)
                newDateFormat=new SimpleDateFormat("EEE dd MMM, yyyy ");
            else
                newDateFormat=new SimpleDateFormat("dd MMM, yyyy ");
            formattedDate=newDateFormat.format(originalDate);
            return formattedDate;
        }
        catch (ParseException e){
        }
        return new String("N/A");
    }

    public static String getFormattedTime(String time){

        Date originalTime;
        String formattedTime;
        String extractedTime=time.substring(time.indexOf(" ")+1,(time.length())-1);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss");

        try{
            originalTime=simpleDateFormat.parse(extractedTime);
            SimpleDateFormat newTimeFormat=new SimpleDateFormat("hh:mm a");
            formattedTime=newTimeFormat.format(originalTime);
            return formattedTime;
        }
        catch (ParseException e){
        }
        return new String("N/A");
    }

    public static void updateWidgets(Context context) {
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    public static String getDateRangeString(int noOfDays){
        DateFormat df=new SimpleDateFormat("yyyyMMdd");
        Calendar c=Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE,1);

        //getting the start date.
        String startDate=df.format(c.getTime())+"00";

        c.setTime(new Date());
        c.add(Calendar.DATE,noOfDays);

        //getting the end date.(adding 4 months)
        String endDate=df.format(c.getTime())+"00";
        return startDate+"-"+endDate;
    }

    public static String getStartDate(){
        DateFormat df=new SimpleDateFormat("yyyyMMdd");
        Calendar c=Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE,1);

        String startDate=df.format(c.getTime());
        return startDate;
    }
}
