package com.sensoguard.trailmanager.classes;

import android.content.Context;
import android.util.Log;

import java.util.Calendar;

import static com.sensoguard.trailmanager.global.ConstsKt.ALARM_OTHER;

//the parse methods are package-private so they can be unit tested
class EmailParsing {
    private static final String TAG = "EmailParsing";
    private static final EmailParsing ourInstance = new EmailParsing();

    private EmailParsing() {
    }

    static EmailParsing getInstance() {
        return ourInstance;
    }

    void parseByModel(Camera camera, String mySubject, Alarm myAlarm, String myContent, Context context1) {
        if (camera.getCameraModel() == null)
            return;

        Calendar myCalendar;
        float batteryVal = -1;
        float wifiVal = -1;
        switch (camera.getCameraModel()) {
            case ("HIKVISION"):
                myCalendar = parseDateTimeByContent(myContent);
                break;
            case ("MG-984G-30M"):
                //parsing the date time
                myCalendar = parseDateTimeBySubject(mySubject);
                //parsing Wi-Fi by subject
                wifiVal = parseWifiBySubject(mySubject);
                break;
            case ("MG-983G-30M"):
                //parsing the date time
                myCalendar = parseDateTimeBySubject(mySubject);
                //parsing battery by content
                batteryVal = parseBatteryByContent(myContent);
                //parsing Wi-Fi by subject
                wifiVal = parseWifiBySubject(mySubject);
                break;
            case ("BG-668"):
            case ("MG-983G-36M"):
            case ("MG-984G-36M"):
                //parsing the date time
                myCalendar = parseDateTimeBySubject(mySubject);


                //parsing battery
                batteryVal = parseBatteryByContent(myContent);


                //parsing Wi-Fi
                wifiVal = parseWifiByContent(myContent);


                break;
            default:
                throw new IllegalStateException("Unexpected value: " + camera.getCameraModel());
        }
        if (myCalendar != null) {
            myAlarm.addAlarmToHistory(camera, ALARM_OTHER, context1, myCalendar, batteryVal, wifiVal);
        }


    }

    //parsing Wi-Fi by subject
    float parseWifiBySubject(String mySubject) {
        float wifiVal = -1;
        try {
            String[] arr = mySubject.split("-");
            if (arr.length > 4) {
                String wifiStr = arr[4];
                wifiStr = wifiStr.substring(0, 2);
                float wifi = Float.parseFloat(wifiStr);
                if (wifi >= 1 && wifi < 8) {
                    wifiVal = 24;
                } else if (wifi >= 8 && wifi < 15) {
                    wifiVal = 49;
                } else if (wifi >= 15 && wifi < 24) {
                    wifiVal = 74;
                } else if (wifi >= 24 && wifi < 30) {
                    wifiVal = 99;
                }
            }
        } catch (Exception ex) {
            Log.w(TAG, "parsing failed", ex);
        }
        return wifiVal;
    }

    //parsing Wi-Fi
    float parseWifiByContent(String myContent) {
        try {
            int tmpIdx = myContent.indexOf("Signal");
            String tmpStr = myContent.substring(tmpIdx);
            tmpIdx = tmpStr.indexOf("%");
            String wifiValStr = tmpStr.substring(tmpStr.indexOf(":") + 1, tmpIdx);
            return Float.parseFloat(wifiValStr);

        } catch (Exception ex) {
            Log.w(TAG, "parsing failed", ex);
        }
        return -1f;
    }

    //parsing battery
    float parseBatteryByContent(String myContent) {
        try {
            //String myContent = getTextFromMessage(unReadLastDayMsg);
            int tmpIdx = myContent.indexOf("Battery");
            String tmpStr = myContent.substring(tmpIdx);
            tmpIdx = tmpStr.indexOf("%");
            String batteryValStr = tmpStr.substring(tmpStr.indexOf(":") + 1, tmpIdx);
            return Float.parseFloat(batteryValStr);
        } catch (Exception ex) {
            Log.w(TAG, "parsing failed", ex);
            return -1f;
        }
    }

    //parsing the date time
    Calendar parseDateTimeBySubject(String mySubject) {
        String[] arr = mySubject.split("-");
        if (arr.length > 2) {
            String datetimeStr = arr[2];
            String[] datetimeArr = datetimeStr.split(" ");
            if (datetimeArr.length > 1) {
                return getCalendarByString(datetimeArr);
            }
        }
        return null;
    }

    //parsing the date time by content
    Calendar parseDateTimeByContent(String myContent) {
        int tmpIdx = myContent.indexOf("EVENT TIME");
        if (tmpIdx < 0) {
            return null;
        }
        String tmpStr = myContent.substring(tmpIdx);
        tmpIdx = tmpStr.indexOf("\n");
        if (tmpIdx < 0) {
            tmpIdx = tmpStr.length();
        }
        String calendarValStr = tmpStr.substring(tmpStr.indexOf(":") + 1, tmpIdx);
        //remove the space
        calendarValStr = calendarValStr.trim();
        String[] arr = calendarValStr.split(",");
        if (arr.length > 1) {
            return getCalendarByStringHIKVISION(arr);
        }
        return null;
    }

    //parse the String to Calendar
    private Calendar getCalendarByString(String[] datetimeArr) {

        String date = datetimeArr[0];
        String time = datetimeArr[1];

        String[] timeArr = time.split(":");

        try {

            if (timeArr.length > 1) {
                String hour = timeArr[0];
                String minutes = timeArr[1];

                String[] dateArr = date.split("/");
                if (dateArr.length > 1) {
                    String day = dateArr[1];
                    String month = dateArr[0];

                    Calendar myCalendar = Calendar.getInstance();
                    int year = myCalendar.get(Calendar.YEAR);
                    int monthVal = Integer.parseInt(month);
                    int dayVal = Integer.parseInt(day);
                    int hourVal = Integer.parseInt(hour);
                    int minutesVal = Integer.parseInt(minutes);

                    //the month start with 0
                    myCalendar.set(year, monthVal - 1, dayVal, hourVal, minutesVal);
                    return myCalendar;
                }
            }
        } catch (Exception ex) {
            Log.w(TAG, "parsing failed", ex);
        }
        return null;
    }

    //parse the String to Calendar
    private Calendar getCalendarByStringHIKVISION(String[] datetimeArr) {

        String date = datetimeArr[0];
        String time = datetimeArr[1];

        String[] timeArr = time.split(":");

        try {

            if (timeArr.length > 2) {
                String hour = timeArr[0];
                String minutes = timeArr[1];
                String seconds = timeArr[2];

                String[] dateArr = date.split("-");
                if (dateArr.length > 2) {
                    String day = dateArr[2];
                    String month = dateArr[1];
                    String year = dateArr[0];

                    Calendar myCalendar = Calendar.getInstance();
                    int yearVal = Integer.parseInt(year);
                    int monthVal = Integer.parseInt(month);
                    int dayVal = Integer.parseInt(day);
                    int hourVal = Integer.parseInt(hour);
                    int minutesVal = Integer.parseInt(minutes);
                    int secondsVal = Integer.parseInt(seconds);

                    //the month start with 0
                    myCalendar.set(yearVal, monthVal - 1, dayVal, hourVal, minutesVal, secondsVal);
                    return myCalendar;
                }
            }
        } catch (Exception ex) {
            Log.w(TAG, "parsing failed", ex);
        }
        return null;
    }
}
