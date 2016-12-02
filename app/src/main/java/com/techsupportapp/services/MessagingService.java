package com.techsupportapp.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.ChatMessage;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.Globals;

import java.util.ArrayList;
import java.util.Collections;

public class MessagingService extends Service {

    private DatabaseReference databaseRef;
    private ChildEventListener childEventListener;

    private ArrayList<Counter> counterList = new ArrayList<Counter>();
    private ArrayList<String> ticketIDList;

    private NotificationManager notificationManager;

    private class Counter{
        int count;
        String ticketId;

        private Counter(String ticketId, int count){
            this.ticketId = ticketId;
            this.count = count;
        }
    }

    public MessagingService() {
    }

    public static void startMessagingService(Context context){
        Intent intent = new Intent(context, MessagingService.class);
        intent.addCategory("MessagingService");
        context.startService(intent);
    }

    public static void stopMessagingService(Context context){
        Intent intent = new Intent(context, MessagingService.class);
        intent.addCategory("MessagingService");
        context.stopService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate()
    {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        databaseRef = FirebaseDatabase.getInstance().getReference();
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Globals.logInfoAPK(MessagingService.this, "База данных - ЧТЕНИЕ - ОБНОВЛЕНИЕ УЗЛОВ");
                if (!dataSnapshot.getValue(ChatMessage.class).getUserId().equals(Globals.currentUser.getLogin()) && dataSnapshot.getValue(ChatMessage.class).isUnread()) {
                    int index = Collections.binarySearch(ticketIDList, dataSnapshot.getRef().getParent().getKey());
                    counterList.get(index).count++;
                    Globals.logInfoAPK(MessagingService.this, "База данных - УВЕЛИЧЕНИЕ СЧЕТЧИКА");
                }
                //TODO для администратора
                showNotification();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                showNotification();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Globals.logInfoAPK(MessagingService.this, "База данных - ОБНОВЛЕНИЕ ДАННЫХ");
                if (Globals.currentUser.getRole() == User.SIMPLE_USER) {
                    ticketIDList = Globals.Downloads.Strings.getUserMarkedTicketIDs(dataSnapshot, Globals.currentUser.getLogin());
                    for (int i = 0; i < ticketIDList.size(); i++) {
                        counterList.add(new Counter(ticketIDList.get(i), 0));
                        databaseRef.child("chat").child(ticketIDList.get(i)).addChildEventListener(childEventListener);
                    }
                }
                //TODO для администратора
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (Globals.currentUser.getRole() == User.SIMPLE_USER)
            for (int i = 0; i < counterList.size(); i++)
                databaseRef.child("chat").child(counterList.get(i).ticketId).removeEventListener(childEventListener);
    }

    private void showNotification(){
        long messagesCount = 0;
        for (int i = 0; i < counterList.size(); i++)
            messagesCount += counterList.get(i).count;
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setContentTitle("Новые сообщения")
                .setContentText("Количество новых сообщений " + messagesCount)
                .setSmallIcon(R.mipmap.ic_launcher);
        Notification notification = builder.build();
        notificationManager.notify(3, notification);
    }
}
