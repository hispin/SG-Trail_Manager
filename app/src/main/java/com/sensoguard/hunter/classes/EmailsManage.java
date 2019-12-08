package com.sensoguard.hunter.classes;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import static com.sensoguard.hunter.global.ConstsKt.CREATE_ALARM_ID_KEY;
import static com.sensoguard.hunter.global.ConstsKt.CREATE_ALARM_KEY;
import static com.sensoguard.hunter.global.ConstsKt.CREATE_ALARM_NAME_KEY;
import static com.sensoguard.hunter.global.ConstsKt.CREATE_ALARM_TYPE_KEY;

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
                            //get attached picture if exist
                            List<String> attachments = getAttachedFiles(unReadLastDayMsg);

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
                    String day = dateArr[0];
                    String month = dateArr[1];

                    Calendar myCalendar = Calendar.getInstance();
                    int year = myCalendar.get(Calendar.YEAR);
                    Log.d("testLogAlarm", "year=" + year);

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


    //read unread emails of last day
//    public void readeLastDayUnreadEmails(Context context){
//        Properties props = new Properties();
//        //IMAPS protocol
//        props.setProperty("mail.store.protocol", "imap");
//        //Set host address
//        props.setProperty("mail.imap.host", "imap.gmail.com");
//        //Set specified port
//        props.setProperty("mail.imap.port", "993");
//        //Using SSL
//        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        props.setProperty("mail.imap.socketFactory.fallback", "false");
//
//        //Setting IMAP session
//        Session imapSession = Session.getInstance(props);
//
//        Store store = null;
//        try {
//            store = imapSession.getStore("imaps");
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
//        }
//        //Connect to server by sending username and password.
//        //Example emailServer = imap.gmail.com, username = abc, password = abc
//        try {
//            if (store != null) {
//                store.connect("imap.gmail.com", 993, "hag.swead", "ringo1234");
//                Log.d("testConnectMail","connect complete");
//            }
//        }catch (MessagingException e) {
//            e.printStackTrace();
//            Log.d("testConnectMail","exception connect "+e.getMessage());
//            return;
//        }
//
//        //Get all mails in Inbox Forlder
//        Folder inbox = null;
//        try {
//            if (store != null) {
//                inbox = store.getFolder("Inbox");
//            }
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//        try {
//            if (inbox != null) {
//                inbox.open(Folder.READ_ONLY);
//            }
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//        //Return result to array of message
//        try {
//            if (inbox != null) {
//
//
//                Calendar cal = Calendar.getInstance();
//                cal.roll(Calendar.DATE, false);
//                Message[] lastDayMsgs = inbox.search(new ReceivedDateTerm(ComparisonTerm.GT, cal.getTime()));
//
//                Flags seen = new Flags(Flags.Flag.SEEN);
//                FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
//                Message[] unReadLastDayMsgs=inbox.search(unseenFlagTerm,lastDayMsgs);
//
//                for (Message unReadLastDayMsg : unReadLastDayMsgs) {
//                    //search[i].
//
//                    if (unReadLastDayMsg.getSubject().contains("MG984G")) {
//                        Log.d("testSubject", unReadLastDayMsg.getSubject());
//
//                        List<File> attachments = getAttachedFiles(unReadLastDayMsg);
//                        Log.d("", "");
//                    }
//                }
//
////                int result = inbox.getMessageCount();
////                //String f=result.getSubject();
//                Log.d("","");
//
////                Message result = inbox.getMessage(1);
////                String f=result.getSubject();
////                Log.d("","");
//            }
//
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            if (store != null) {
//                store.close();
//            }
//        }catch (MessagingException e) {
//            e.printStackTrace();
//        }
//    }
}
