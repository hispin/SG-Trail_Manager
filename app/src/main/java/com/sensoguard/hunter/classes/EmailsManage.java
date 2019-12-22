package com.sensoguard.hunter.classes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.sensoguard.hunter.R;
import com.sensoguard.hunter.activities.InitAppActivity;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.ReceivedDateTerm;

import static com.sensoguard.hunter.global.ConstsKt.ADD_ATTACHED_PHOTOS_KEY;
import static com.sensoguard.hunter.global.ConstsKt.ALARM_OTHER;
import static com.sensoguard.hunter.global.ConstsKt.CHANNEL_ID;
import static com.sensoguard.hunter.global.ConstsKt.CHANNEL_NAME;
import static com.sensoguard.hunter.global.ConstsKt.CREATE_ALARM_ID_KEY;
import static com.sensoguard.hunter.global.ConstsKt.CREATE_ALARM_KEY;
import static com.sensoguard.hunter.global.ConstsKt.CREATE_ALARM_NAME_KEY;
import static com.sensoguard.hunter.global.ConstsKt.CREATE_ALARM_TYPE_KEY;
import static com.sensoguard.hunter.global.ConstsKt.ERROR_RESULT_VALIDATION_EMAIL_ACTION;
import static com.sensoguard.hunter.global.ConstsKt.ERROR_VALIDATION_EMAIL_MSG_KEY;

public class EmailsManage {
    private static final EmailsManage ourInstance = new EmailsManage();

    private EmailsManage() {
    }

    public static EmailsManage getInstance() {
        return ourInstance;
    }

    //read camera's unread emails of last day
    public void readeLastDayUnreadEmails(Camera camera, Context context) {

        Properties props = new Properties();
        String protocol = null;

        //IMAPS protocol
        if (camera.getEmailServer() != null) {
            protocol = camera.getEmailServer().substring(0, camera.getEmailServer().indexOf("."));
            props.setProperty("mail.store.protocol", protocol);
        } else {
            return;
        }

        //Set host address
        props.setProperty("mail.imap.host", camera.getEmailServer());

        //Set specified port
        if (camera.getEmailPort() != null) {
            props.setProperty("mail.imap.port", camera.getEmailPort());
        }

        //Using SSL
        if (camera.isUseSSL()) {
            props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        props.setProperty("mail.imap.socketFactory.fallback", "false");


        //Setting IMAP session
        Session imapSession = Session.getInstance(props);

        Store store = null;
        try {
            store = imapSession.getStore(protocol);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }


        //Connect to server by sending username and password.
        try {
            if (store != null) {
                int port = -1;
                try {
                    port = Integer.valueOf(Objects.requireNonNull(camera.getEmailPort()));
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                    return;
                }

                store.connect(camera.getEmailServer(), port, camera.getEmailAddress(), camera.getPassword());
            }
        } catch (MessagingException e) {
            e.printStackTrace();
            Log.d("testConnectMail", "exception connect " + e.getMessage());
            return;
        }

        //Get all mails in Inbox Forlder
        Folder inbox = null;
        try {
            if (store != null) {
                inbox = store.getFolder("Inbox");
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        try {
            if (inbox != null) {
                inbox.open(Folder.READ_WRITE);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        //Return result to array of message
        try {
            if (inbox != null) {


                Calendar cal = Calendar.getInstance();
                cal.roll(Calendar.DATE, false);
                Message[] lastDayMsgs = inbox.search(new ReceivedDateTerm(ComparisonTerm.GT, cal.getTime()));

                Flags seen = new Flags(Flags.Flag.SEEN);
                FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
                Message[] unReadLastDayMsgs = inbox.search(unseenFlagTerm, lastDayMsgs);

                for (Message unReadLastDayMsg : unReadLastDayMsgs) {

                    //int msgNum=unReadLastDayMsg.getMessageNumber();
                    String mySubject = unReadLastDayMsg.getSubject();


                    if (camera.getCameraModel() != null) {
                        //if the mode is without "-"
                        String shortModel = camera.getCameraModel().replace("-", "");

                        if (mySubject.contains(camera.getCameraModel()) || mySubject.contains(shortModel)) {

                            int index = mySubject.indexOf(camera.getCameraModel());
                            if (index == -1) {
                                index = mySubject.indexOf(shortModel);
                            }

                            mySubject = mySubject.substring(index);

                            //change the message to read email
                            inbox.setFlags(new Message[]{unReadLastDayMsg}, new Flags(Flags.Flag.SEEN), true);

                            //Log.d("testSubject", mySubject);

                            String[] arr = mySubject.split("-");


                            Alarm myAlarm = new Alarm();
                            myAlarm.setMsgNumber(unReadLastDayMsg.getMessageNumber());
                            myAlarm.setCameFromEmail(true);
                            myAlarm.setLoadPhoto(true);

                            if (arr.length > 1) {
                                String datetimeStr = arr[2];
                                String[] datetimeArr = datetimeStr.split(" ");

                                if (datetimeArr.length > 1) {
                                    Calendar myCalendar = getCalendarByString(datetimeArr);
                                    if (myCalendar != null) {

                                        myAlarm.addAlarmToHistory(camera, ALARM_OTHER, context, myCalendar);
                                    }
                                }
                            }
                            sendLiveAlarm(camera, context);
                            // Send notification and log the transition details.
                            //createNotificationChannel(context);
                            sendNotif("new alarm detected", context);


                            //get attached picture if exist
                            List<String> attachments = getAttachedFiles(unReadLastDayMsg);

                            Log.d("checkVideo", attachments.get(0));

                            //in version 1 support only one attached picture
                            //do not wait to save photo ,to make the process of showing alarm more faster
                            myAlarm.updateAlarm(attachments.get(0), context);
                            Intent inn = new Intent(ADD_ATTACHED_PHOTOS_KEY);
                            context.sendBroadcast(inn);
                        }
                    }
                }

            }

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        //close connection
        try {
            if (store != null) {
                store.close();
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
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
                    int monthVal = Integer.valueOf(month);
                    int dayVal = Integer.valueOf(day);
                    int hourVal = Integer.valueOf(hour);
                    int minutesVal = Integer.valueOf(minutes);

                    //the month start with 0
                    myCalendar.set(year, monthVal - 1, dayVal, hourVal, minutesVal);
                    return myCalendar;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //send alarm ,catching by map screen
    private void sendLiveAlarm(Camera camera, Context context) {
        //send to create alarm :ic_map_main,sound ect...
        Intent inn = new Intent(CREATE_ALARM_KEY);
        inn.putExtra(CREATE_ALARM_ID_KEY, camera.getId());
        inn.putExtra(CREATE_ALARM_NAME_KEY, camera.getName());
        inn.putExtra(CREATE_ALARM_TYPE_KEY, ALARM_OTHER);
        context.sendBroadcast(inn);
    }


    //save the attached file
    private List<String> getAttachedFiles(Message message) throws MessagingException {
        List<String> attachments = new ArrayList<>();
        Multipart multipart = null;
        try {
            multipart = (Multipart) message.getContent();
        } catch (IOException | MessagingException e) {
            Log.d("testSubject", e.getMessage());
            e.printStackTrace();
        }

        if (multipart != null) {
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                        StringUtils.isBlank(bodyPart.getFileName())) {
                    continue; // dealing with attachments only
                }
                InputStream is = null;
                try {
                    is = bodyPart.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                File myDir = new File(root + "/alarms_hunter");
                if (!myDir.exists()) {
                    //ignore if the action is successfully
                    myDir.mkdirs();
                }

                File file = new File(myDir, bodyPart.getFileName());

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                byte[] buf = new byte[4096];
                int bytesRead = 0;
                while (true) {
                    try {
                        if (is != null && (bytesRead = is.read(buf)) == -1) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (fos != null) {
                            fos.write(buf, 0, bytesRead);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                attachments.add(file.getAbsolutePath());
            }
        }

        return attachments;
    }


    private void sendNotif(String content, Context context) {

        if (context == null)
            return;
        WeakReference<Context> wContext = new WeakReference<Context>(context);

        createNotificationChannel(wContext.get());
//        if(mNotificationId<28){
//            mNotificationId++
//        }else{
//            mNotificationId=23
//        }

        long oneTimeID = SystemClock.uptimeMillis();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New alarm detected")
                .setContentText(content);
        NotificationManager mNotificationManager = (NotificationManager) wContext.get().getSystemService(Context.NOTIFICATION_SERVICE);

        //Enable press on notification to open app
        Intent notificationIntent = new Intent(context, InitAppActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        mBuilder.setContentIntent(contentIntent);

        if (mNotificationManager != null) {
            mNotificationManager.notify((int) oneTimeID, mBuilder.build());
        }
    }


    //validation email by trying to connection
    public boolean emailValidation(Camera camera, Context context) {
        Properties props = new Properties();
        String protocol = null;

        //IMAPS protocol
        if (camera.getEmailServer() != null) {
            protocol = camera.getEmailServer().substring(0, camera.getEmailServer().indexOf("."));
            props.setProperty("mail.store.protocol", protocol);
        } else {
            return false;
        }

        //Set host address
        props.setProperty("mail.imap.host", camera.getEmailServer());

        //Set specified port
        if (camera.getEmailPort() != null) {
            props.setProperty("mail.imap.port", camera.getEmailPort());
        }

        //Using SSL
        if (camera.isUseSSL()) {
            props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        props.setProperty("mail.imap.socketFactory.fallback", "false");


        //Setting IMAP session
        Session imapSession = Session.getInstance(props);

        Store store = null;
        try {
            store = imapSession.getStore(protocol);
        } catch (NoSuchProviderException e) {
            sendErrorMsg(e.getMessage(), context);
            e.printStackTrace();
        }


        //Connect to server by sending username and password.
        try {
            if (store != null) {
                int port = -1;
                try {
                    port = Integer.valueOf(Objects.requireNonNull(camera.getEmailPort()));
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                    sendErrorMsg(ex.getMessage(), context);
                    return false;
                }

                store.connect(camera.getEmailServer(), port, camera.getEmailAddress(), camera.getPassword());

                return store.isConnected();
            }
        } catch (MessagingException e) {
            e.printStackTrace();
            sendErrorMsg(e.getMessage(), context);
            return false;
        }
        return false;
    }

    //send error to extra settings screen
    private void sendErrorMsg(String message, Context context) {
        Intent inn = new Intent(ERROR_RESULT_VALIDATION_EMAIL_ACTION);
        inn.putExtra(ERROR_VALIDATION_EMAIL_MSG_KEY, message);
        context.sendBroadcast(inn);
    }


    //    Notification channels enable us app developers to group our notifications into groups—channels—with
//    the user having the ability to modify notification settings for the entire channel at once. For example,
//    for each channel, users can completely block all notifications, override the importance level, or allow a
//    notification badge to be shown. This new feature helps in greatly improving the user experience of an app
    private void createNotificationChannel(Context context) {
        if (context == null)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = CHANNEL_NAME;
            String descriptionText = "new alarm detected";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(descriptionText);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}