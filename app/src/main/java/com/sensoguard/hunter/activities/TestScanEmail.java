package com.sensoguard.hunter.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.sensoguard.hunter.R;

import java.util.Calendar;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.ReceivedDateTerm;

//import com.chilkatsoft.*;


public class TestScanEmail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_scan_email2);

        Thread thread = new Thread() {
            @Override
            public void run() {
                readeLastDayUnreadEmails();
            }
        };

        thread.start();


    }


    public void readeLastDayUnreadEmails() {
        Properties props = new Properties();
//IMAPS protocol
        props.setProperty("mail.store.protocol", "imap");
//Set host address
        props.setProperty("mail.imap.host", "imap.gmail.com");
//Set specified port
        props.setProperty("mail.imap.port", "993");
//Using SSL
        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.imap.socketFactory.fallback", "false");
//Setting IMAP session


        Session imapSession = Session.getInstance(props);

        Store store = null;
        try {
            store = imapSession.getStore("imaps");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
//Connect to server by sending username and password.
//Example emailServer = imap.gmail.com, username = abc, password = abc
        try {
            if (store != null) {
                store.connect("imap.gmail.com", 993, "hag.swead", "ringo1234");
                Log.d("testConnectMail", "connect complete");
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
                inbox.open(Folder.READ_ONLY);
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

                for (int i = 0; i < unReadLastDayMsgs.length; i++) {
                    //search[i].
                    Log.d("testSubject", unReadLastDayMsgs[i].getSubject());
                }

//                int result = inbox.getMessageCount();
//                //String f=result.getSubject();
                Log.d("", "");

//                Message result = inbox.getMessage(1);
//                String f=result.getSubject();
//                Log.d("","");
            }

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        try {
            if (store != null) {
                store.close();
            }
        } catch (MessagingException e) {
            e.printStackTrace();
            return;
        }
    }


    public void readEmails2() {
        Properties props = new Properties();
        //IMAPS protocol
        props.setProperty("mail.store.protocol", "imap");
        //Set host address
        props.setProperty("mail.imap.host", "smtp.gmail.com");
        //Set specified port
        props.setProperty("mail.imap.port", "993");
        //Using SSL
        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.imap.socketFactory.fallback", "false");
        props.setProperty("mail.imap.auth.plain.disable", "true");
        //Session imapSession = Session.getInstance(props);
        Session imapSession = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("hag.swead@gmail.com", "ringo1234");
            }
        });

        if (imapSession == null) {
            Log.d("testConnectMail", "session null");
            return;
        }
        //Session session = Session.getDefaultInstance(props, new GMailAuthenticator("xxxxx@gmail.com", "xxxxx"));
        //session.setDebug(true);
        Store store = null;
        try {
            store = imapSession.getStore("imap");
            Log.d("testConnectMail", "connect start");
            store.connect("smtp.gmail.com", "hag.swead@gmail.com", "ringo1234");
            Log.d("testConnectMail", "connect complete");
            Folder inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);
            Message[] msgs = inbox.getMessages();
        } catch (MessagingException e) {
            e.printStackTrace();
            Log.d("testConnectMail", "exception connect " + e.getMessage());
        }

    }


    public void readEmails3() {

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imap.host", "imap.gmail.com");
//Set specified port
        props.put("mail.imap.port", "993");

        Session session = Session.getDefaultInstance(props, null);

        try {
            Store store = session.getStore("imaps");
            Log.d("testConnectMail", "connect start");
            store.connect("hag.swead@gmail.com", "ringo1234");
            Log.d("testConnectMail", "connect complete");
        } catch (MessagingException e) {
            e.printStackTrace();
            Log.d("testConnectMail", "exception " + e.getMessage());
        }

//        Properties props = new Properties();
//        try {
//            props.load(new FileInputStream(new File("C:\\smtp.properties")));
//            Session session = Session.getDefaultInstance(props, null);
//
//            Store store = session.getStore("imaps");
//            Log.d("testConnectMail","connect start");
//            store.connect("smtp.gmail.com", "hag.crow@gmail.com","crow1234");
//            Log.d("testConnectMail","connect complete");
//
//            Folder inbox = store.getFolder("inbox");
//            inbox.open(Folder.READ_ONLY);
//            int messageCount = inbox.getMessageCount();
//
//            System.out.println("Total Messages:- " + messageCount);
//
////            Message[] messages = inbox.getMessages();
////            System.out.println("------------------------------");
////            for (int i = 0; i < 10; i++) {
////                System.out.println("Mail Subject:- " + messages[i].getSubject());
////            }
//            inbox.close(true);
//            store.close();
//
//        } catch (Exception e) {
//            Log.d("testConnectMail","exception "+e.getMessage());
//            e.printStackTrace();
//        }
    }


}
