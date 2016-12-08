package com.techsupportapp.utility;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techsupportapp.databaseClasses.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatabaseStorage {
    public static int ACTION_CREATED = 0;
    public static int ACTION_ACCEPTED = 1;
    public static int ACTION_CLOSED = 2;
    public static int ACTION_SOLVED = 3;
    public static int ACTION_WITHDRAWN = 4;
    public static int ACTION_REQUESTED_TO_CLOSE = 5;
    public static int ACTION_REQUEST_REJECTED = 6;
    public static int ACTION_REQUEST_ACCEPTED = 7;

    public static void updateLogFile(Context context, String ticketId, final int action, final User currentUser, final String user){
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("logs").child(ticketId + ".log");

        try {
            final File localFile = File.createTempFile(ticketId, "log");

            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.e("Error", "Successful download");
                    uploadFile(storageReference, localFile, action, currentUser, user);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("Error", "Error occurred while downloading");
                    uploadFile(storageReference, localFile, action, currentUser, user);
                }
            });
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void uploadFile(StorageReference storageReference, File file, int action, User currentUser, String user){
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, MMM dd yyyy", Locale.ENGLISH);
            String currentTime = formatter.format(Calendar.getInstance().getTime());
            String text = "";

            if (action == ACTION_CREATED)
                text = ": заявка создана пользователем ";
            else if (action == ACTION_ACCEPTED)
                text = ": заявка назанчена пользователю " + user + " пользователем ";
            else if (action == ACTION_CLOSED)
                text = ": заявка закрыта пользователем ";
            else if (action == ACTION_SOLVED)
                text = ": заявка объявлена решенной пользователем ";
            else if (action == ACTION_WITHDRAWN)
                text = ": заявка отозвана пользователем ";
            else if (action == ACTION_REQUESTED_TO_CLOSE)
                text = ": отправлен запрос на закрытие заявки пользователем ";
            else if (action == ACTION_REQUEST_REJECTED)
                text = ": запрос на закрытие заявки отклонен пользователем ";
            else if (action == ACTION_REQUEST_ACCEPTED)
                text = ": запрос на закрытие заявки принят пользователем ";

            String data = (currentTime + text + currentUser.getUserName());

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            out.println(data + "\r\n");
            out.close();

            storageReference.putFile(Uri.fromFile(file)).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("Error", "Error occurred while uploading");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.e("Error", "Successful upload");
                }
            });
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
