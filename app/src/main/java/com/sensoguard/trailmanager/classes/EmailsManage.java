package com.sensoguard.trailmanager.classes;

import static com.sensoguard.trailmanager.global.ConstsKt.ADD_ATTACHED_PHOTOS_KEY;
import static com.sensoguard.trailmanager.global.ConstsKt.ALARM_OTHER;
import static com.sensoguard.trailmanager.global.ConstsKt.CHANNEL_ID;
import static com.sensoguard.trailmanager.global.ConstsKt.CHANNEL_NAME;
import static com.sensoguard.trailmanager.global.ConstsKt.CREATE_ALARM_ID_KEY;
import static com.sensoguard.trailmanager.global.ConstsKt.CREATE_ALARM_KEY;
import static com.sensoguard.trailmanager.global.ConstsKt.CREATE_ALARM_NAME_KEY;
import static com.sensoguard.trailmanager.global.ConstsKt.CREATE_ALARM_TYPE_KEY;
import static com.sensoguard.trailmanager.global.ConstsKt.DETECT_ALARM_KEY;
import static com.sensoguard.trailmanager.global.ConstsKt.ERROR_RESULT_VALIDATION_EMAIL_ACTION;
import static com.sensoguard.trailmanager.global.ConstsKt.ERROR_VALIDATION_EMAIL_MSG_KEY;
import static com.sensoguard.trailmanager.global.ConstsKt.IS_MY_SCREENS_ACTIVITY_FOREGROUND;
import static com.sensoguard.trailmanager.global.ConstsKt.LAST_DATE_ALARM;
import static com.sensoguard.trailmanager.global.SysMethodsSharedPrefKt.getBooleanInPreference;
import static com.sensoguard.trailmanager.global.SysMethodsSharedPrefKt.getLongInPreference;
import static com.sensoguard.trailmanager.global.SysMethodsSharedPrefKt.setLongInPreference;
import static com.sensoguard.trailmanager.global.SysMethodsStorageKt.getAlarmsFromLocally;
import static com.sensoguard.trailmanager.global.SysMethodsStorageKt.getCamerasFromLocally;
import static com.sensoguard.trailmanager.global.SysMethodsStorageKt.writeLog;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.sensoguard.trailmanager.R;
import com.sensoguard.trailmanager.activities.InitAppActivity;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;


public class EmailsManage {
    private static final String TAG = "EmailsManage";
    private static final String NEW_ALARM_NOTIFICATION_TEXT = "new alarm detected";
    private static final EmailsManage ourInstance = new EmailsManage();

    private EmailsManage() {
    }

    public static EmailsManage getInstance() {
        return ourInstance;
    }


    //read camera's unread emails of last day
    public void readeLastDayUnreadEmails(MyEmailAccount myEmailAccount, Context context) throws IOException {

        if (myEmailAccount == null || myEmailAccount.getEmailServer() == null) {
            return;
        }

        //IMAPS protocol
        String protocol = getProtocol(myEmailAccount.getEmailServer());
        Properties props = createImapProperties(protocol, myEmailAccount.getEmailServer(), myEmailAccount.getEmailPort(), myEmailAccount.isUseSSL());

        //Setting IMAP session
        Session imapSession = Session.getInstance(props);

        Store store = null;
        try {
            store = imapSession.getStore(protocol);
        } catch (NoSuchProviderException e) {
            writeLog("readeLastDayUnreadEmails");
            Log.w(TAG, "getStore failed", e);
        }

        //Connect to server by sending username and password.
        store = connectToServer(store, myEmailAccount);

        if (store == null) {
            return;
        }


        //Get all mails in Inbox Folder
        Folder inbox = getInbox(store);


        //Return result to array of message
        try {
            if (inbox != null) {

                Calendar lastEmailDate = Calendar.getInstance();
                long longLastDateAlarm = getLongInPreference(context, LAST_DATE_ALARM, -1);
                if (longLastDateAlarm == -1) {
                    lastEmailDate.roll(Calendar.DATE, false);
                } else {
                    lastEmailDate.setTimeInMillis(longLastDateAlarm);
                }

                Calendar currentDate = Calendar.getInstance();
                currentDate.roll(Calendar.DATE, false);

                Message[] unReadLastDayMsgs = inbox.search(new ReceivedDateTerm(ComparisonTerm.GT, currentDate.getTime()));

                if (unReadLastDayMsgs.length < 1) {
                    return;
                }
                //reverse : the newer message is the first
                ArrayUtils.reverse(unReadLastDayMsgs);
                for (Message unReadLastDayMsg : unReadLastDayMsgs) {


                    //if received date of this email is already scanned
                    Date receivedDate = unReadLastDayMsg.getReceivedDate();
                    if (receivedDate != null
                            && (lastEmailDate.getTime().after(receivedDate) || lastEmailDate.getTime().compareTo(receivedDate) == 0)) {
                        lastEmailDate.setTime(unReadLastDayMsgs[0].getReceivedDate());
                        setLongInPreference(context, LAST_DATE_ALARM, lastEmailDate.getTimeInMillis());

                        //close connection
                        closeConnection(store);
                        return;
                    }


                    //check if the email is already exist
                    if (isEmailAlreadyReceived(unReadLastDayMsg.getMessageNumber(), context)) {
                        //go to next email
                        continue;
                    }

                    String[] arrMySubject = new String[1];
                    //get the camera with the same email from and the same model if exist
                    Camera camera = getAppropriateCamera(unReadLastDayMsg, context, arrMySubject);
                    if (camera == null || arrMySubject[0].equals("-1")) {
                        //go to next email
                        continue;
                    }

                    String mySubject = arrMySubject[0];


                    Alarm myAlarm = new Alarm();
                    myAlarm.setMsgNumber(unReadLastDayMsg.getMessageNumber());
                    myAlarm.setCameFromEmail(true);
                    myAlarm.setLoadPhoto(true);


                    String myContent = getTextFromMessage(unReadLastDayMsg);
                    EmailParsing.getInstance().parseByModel(camera, mySubject, myAlarm, myContent, context);


                    sendLiveAlarm(camera, context);

                    // Send notification when the app is not shown
                    boolean isMyScreenActivityForeground = getBooleanInPreference(context, IS_MY_SCREENS_ACTIVITY_FOREGROUND, false);
                    if (!isMyScreenActivityForeground) {
                        sendNotification(context);
                    }

                    Intent inn = new Intent(DETECT_ALARM_KEY);
                    inn.setPackage(context.getPackageName());
                    context.sendBroadcast(inn);


                    //get attached picture if exist
                    List<String> attachments = getAttachedFiles(unReadLastDayMsg, context);

                    //in version 1 support only one attached picture
                    //do not wait to save the photo, to show the alarm faster
                    if (!attachments.isEmpty()) {
                        myAlarm.updateAlarm(attachments.get(0), context);
                    }
                    inn = new Intent(ADD_ATTACHED_PHOTOS_KEY);
                    inn.setPackage(context.getPackageName());
                    context.sendBroadcast(inn);
                }
                //update the newest email date
                lastEmailDate.setTime(unReadLastDayMsgs[0].getReceivedDate());
                setLongInPreference(context, LAST_DATE_ALARM, lastEmailDate.getTimeInMillis());
            }

        } catch (MessagingException e) {
            writeLog("readeLastDayUnreadEmails");
            Log.w(TAG, "read emails failed", e);
        }

        //close connection
        closeConnection(store);
    }

    private void closeConnection(Store store) {
        try {
            if (store != null) {
                store.close();
            }
        } catch (MessagingException e) {
            writeLog("MessagingException");
            Log.w(TAG, "close connection failed", e);
        }
    }

    //get the camera with the same email from and the same model if exist
    private Camera getAppropriateCamera(Message unReadLastDayMsg, Context context, String[] arrMySubject) {

        arrMySubject[0] = "-1";
        ArrayList<Camera> cameras = getCamerasFromLocally(context);
        if (cameras == null) {
            return null;
        }

        for (Camera cameraItem : cameras) {
            //if the camera is not active do not execute arm
            if (!cameraItem.isArmed()) {
                //go to next camera
                continue;
            }

            //check if the email from is identity to email from that setting in camera
            if (!isEmailFromIsIdentity(cameraItem, unReadLastDayMsg)) {
                //go to next camera
                continue;
            }

            if (isEmailModelIsIdentity(unReadLastDayMsg, cameraItem, arrMySubject)) {
                return cameraItem;
            }

        }
        return null;

    }

    //check if the model is identity to model that setting in camera
    private boolean isEmailModelIsIdentity(Message unReadLastDayMsg, Camera camera, String[] arrMySubject) {
        String mySubject = null;
        try {
            mySubject = unReadLastDayMsg.getSubject();
        } catch (MessagingException e) {
            writeLog("isEmailModelIsIdentity");
            Log.w(TAG, "getSubject failed", e);
        }

        //filter the message with specify models
        String cameraModel = camera.getCameraModel();
        if (mySubject != null && cameraModel != null) {
            //if the mode is without "-"
            String shortModel1 = cameraModel.replace("-", "");
            String shortModel2 = cameraModel.replaceFirst("-", "");


            if (mySubject.contains(cameraModel)
                    || mySubject.contains(shortModel1)
                    || mySubject.contains(shortModel2)
                    || (cameraModel.equals("HIKVISION") && mySubject.contains("Network Video Recorder"))
            ) {

                //in HIKVISION you do not need to specify the subject
                if (!cameraModel.equals("HIKVISION")) {
                    int index = mySubject.indexOf(cameraModel);
                    if (index == -1) {
                        index = mySubject.indexOf(shortModel1);
                    }
                    if (index == -1) {
                        index = mySubject.indexOf(shortModel2);
                    }
                    arrMySubject[0] = mySubject.substring(index);
                    return true;
                }

            }
        }
        arrMySubject[0] = "-1";
        return false;
    }

    //check if the email from is identity to email from that setting in camera
    private boolean isEmailFromIsIdentity(Camera cameraItem, Message unReadLastDayMsg) {
        if (cameraItem.getEmailAddress() != null) {
            Address[] address = new Address[0];
            try {
                address = unReadLastDayMsg.getFrom();
            } catch (MessagingException e) {
                writeLog("isEmailFromIsIdentity");
                Log.w(TAG, "getFrom failed", e);
            }
            if (address != null && address.length > 0) {

                //if the address from is the same as defined
                String addressFrom = ((InternetAddress) address[0]).getAddress();

                return Objects.equals(cameraItem.getEmailAddress(), addressFrom);

            }
        }
        return false;
    }

    //Get all mails in Inbox Folder
    private Folder getInbox(Store store) {
        try {
            Folder inbox = store.getFolder("Inbox");
            if (inbox != null) {
                inbox.open(Folder.READ_WRITE);
            }
            return inbox;
        } catch (MessagingException e) {
            writeLog("getInbox");
            Log.w(TAG, "open inbox failed", e);
            return null;
        }
    }

    //the protocol is the first part of the server address (e.g. "imap" of "imap.gmail.com")
    private String getProtocol(String emailServer) {
        //the names of the providers of JavaMail are in lower case ("IMAP.gmail.com" is "imap")
        String server = emailServer.trim().toLowerCase(Locale.ROOT);
        int dotIndex = server.indexOf(".");
        return dotIndex > 0 ? server.substring(0, dotIndex) : server;
    }

    //IMAP properties of the session
    private Properties createImapProperties(String protocol, String emailServer, String emailPort, boolean isUseSSL) {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", protocol);

        //Set host address
        props.setProperty("mail.imap.host", emailServer);

        //Set specified port
        if (emailPort != null) {
            props.setProperty("mail.imap.port", emailPort);
        }

        //Using SSL
        if (isUseSSL) {
            props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        props.setProperty("mail.imap.socketFactory.fallback", "false");
        return props;
    }

    //Connect to server by sending username and password.
    private Store connectToServer(Store store, MyEmailAccount myEmailAccount) {
        if (store == null) {
            return null;
        }
        try {
            int port = Integer.parseInt(Objects.requireNonNull(myEmailAccount.getEmailPort()));
            store.connect(myEmailAccount.getEmailServer(), port, myEmailAccount.getEmailAddress(), myEmailAccount.getPassword());
        } catch (NumberFormatException | NullPointerException | MessagingException e) {
            writeLog("connectToServer");
            Log.w(TAG, "connect failed", e);
            return null;
        }
        return store;
    }


    //send alarm ,catching by map screen
    private void sendLiveAlarm(Camera camera, Context context) {
        //send to create alarm :ic_map_main,sound ect...
        Intent inn = new Intent(CREATE_ALARM_KEY);
        inn.putExtra(CREATE_ALARM_ID_KEY, camera.getId());
        inn.putExtra(CREATE_ALARM_NAME_KEY, camera.getName());
        inn.putExtra(CREATE_ALARM_TYPE_KEY, ALARM_OTHER);
        inn.setPackage(context.getPackageName());
        context.sendBroadcast(inn);
    }


    //save the attached file
    private List<String> getAttachedFiles(Message message, Context context) throws MessagingException {
        List<String> attachments = new ArrayList<>();
        Multipart multipart = null;
        try {
            multipart = (Multipart) message.getContent();
        } catch (IOException | MessagingException e) {
            writeLog("getAttachedFiles");
            Log.w(TAG, "getContent failed", e);
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
                    writeLog("getAttachedFiles");
                    Log.w(TAG, "getInputStream failed", e);
                }


                //store in external storage
                File file = saveImageExternal(is, bodyPart.getFileName(), context);
                attachments.add(file.getAbsolutePath());
            }
        }

        return attachments;
    }


    //save the picture in the app-specific external storage (no storage permission needed, works with scoped storage)
    private File saveImageExternal(InputStream is, String fileName, Context context) {
        File myDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "alarms_hunter");
        if (!myDir.exists() && !myDir.mkdirs()) {
            writeLog("saveImageExternal: can not create " + myDir);
        }

        //prevent path traversal by a malicious attachment name
        String safeName = fileName == null ? "attachment_" + System.currentTimeMillis() : new File(fileName).getName();
        File file = new File(myDir, safeName);

        if (is == null) {
            return file;
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buf = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buf)) != -1) {
                fos.write(buf, 0, bytesRead);
            }
        } catch (IOException e) {
            writeLog("saveImageExternal");
            Log.w(TAG, "save image failed", e);
        }

        return file;
    }

    //snd notification when accept alarm
    private void sendNotification(Context context) {
        if (context == null) {
            return;
        }

        createNotificationChannel(context);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app_notification)
                .setContentTitle("New alarm detected")
                .setContentText(NEW_ALARM_NOTIFICATION_TEXT)
                //remove after click on notification
                .setAutoCancel(true);

        //Enable press on notification to open app
        Intent notificationIntent = new Intent(context, InitAppActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);

        //since Android 13 the notification is shown only if the user allowed it
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            int notificationId = (int) SystemClock.uptimeMillis();
            NotificationManagerCompat.from(context).notify(notificationId, mBuilder.build());
        }
    }


    //validation camera email by trying to connection
    public boolean emailValidation(Camera camera, Context context) {
        return emailValidation(camera.getEmailServer(), camera.getEmailPort(), camera.isUseSSL(),
                camera.getEmailAddress(), camera.getPassword(), context);
    }

    //validation email by trying to connection
    public boolean emailValidation(MyEmailAccount myEmailAccount, Context context) {
        if (myEmailAccount == null) {
            return false;
        }
        return emailValidation(myEmailAccount.getEmailServer(), myEmailAccount.getEmailPort(), myEmailAccount.isUseSSL(),
                myEmailAccount.getEmailAddress(), myEmailAccount.getPassword(), context);
    }

    //validation email by trying to connection
    private boolean emailValidation(String emailServer, String emailPort, boolean isUseSSL,
                                    String emailAddress, String password, Context context) {
        if (emailServer == null) {
            return false;
        }

        //IMAPS protocol
        String protocol = getProtocol(emailServer);

        //Setting IMAP session
        Session imapSession = Session.getInstance(createImapProperties(protocol, emailServer, emailPort, isUseSSL));

        //Connect to server by sending username and password.
        try {
            Store store = imapSession.getStore(protocol);
            int port = Integer.parseInt(Objects.requireNonNull(emailPort));
            store.connect(emailServer, port, emailAddress, password);
            return store.isConnected();
        } catch (NumberFormatException | NullPointerException | MessagingException e) {
            writeLog("emailValidation");
            Log.w(TAG, "email validation failed", e);
            sendErrorMsg(String.valueOf(e.getMessage()), context);
            return false;
        }
    }


    //send error to extra settings screen
    private void sendErrorMsg(String message, Context context) {
        Intent inn = new Intent(ERROR_RESULT_VALIDATION_EMAIL_ACTION);
        inn.putExtra(ERROR_VALIDATION_EMAIL_MSG_KEY, message);
        inn.setPackage(context.getPackageName());
        context.sendBroadcast(inn);
    }


    //    Notification channels enable us app developers to group our notifications into groups—channels—with
    //    the user having the ability to modify notification settings for the entire channel at once. For example,
    //    for each channel, users can completely block all notifications, override the importance level, or allow a
    //    notification badge to be shown. This new feature helps in greatly improving the user experience of an app
    private void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(NEW_ALARM_NOTIFICATION_TEXT);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }


    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain") || message.isMimeType("text/html")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append("\n").append(bodyPart.getContent());
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result.append("\n").append(org.jsoup.Jsoup.parse(html).text());
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    //check if this message already accepted as alarm
    private boolean isEmailAlreadyReceived(int msgNum, Context context) {
        ArrayList<Alarm> alarms = getAlarmsFromLocally(context);

        if (alarms == null) {
            return false;
        }

        for (Alarm item : alarms) {
            if (item.getMsgNumber() != null && item.getMsgNumber() == msgNum) {
                return true;
            }
        }
        return false;
    }
}
