package com.techsupportapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.techsupportapp.adapters.ChatRecyclerAdapter;
import com.techsupportapp.databaseClasses.ChatMessage;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseStorage;
import com.techsupportapp.utility.Globals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MessagingActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private ChatRecyclerAdapter chatRecyclerAdapter;

    private RecyclerView recyclerView;

    private String mChatRoom;
    private Ticket currentTicket;
    private String topic;
    private boolean isActive;

    private EditText inputText;
    private ImageButton sendBtn;

    private MaterialDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        currentTicket = (Ticket) getIntent().getExtras().getSerializable("currentTicket");

        isActive = getIntent().getExtras().getBoolean("isActive");

        showLoadingDialog();
        initializeComponents();
        setEvents();
    }

    private void initializeComponents() {
        mChatRoom = currentTicket.getTicketId();
        topic = currentTicket.getTopic();

        databaseReference = FirebaseDatabase.getInstance().getReference("chat").child(mChatRoom);
        inputText = (EditText) findViewById(R.id.messageInput);
        sendBtn = (ImageButton) findViewById(R.id.sendButton);

        if (!isActive) {
            inputText.setEnabled(false);
            sendBtn.setEnabled(false);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle(topic);
    }

    private void setEvents(){
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    sendMessage();
                }
                return true;
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void showLoadingDialog(){
        loadingDialog = new MaterialDialog.Builder(this)
                .content("Загрузка...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerView = (RecyclerView) findViewById(R.id.listChat);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(MessagingActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        chatRecyclerAdapter = new ChatRecyclerAdapter(databaseReference.limitToLast(150), MessagingActivity.this, currentTicket);
        recyclerView.setAdapter(chatRecyclerAdapter);
        chatRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                recyclerView.scrollToPosition(chatRecyclerAdapter.getItemCount() - 1);
            }
        });


        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = !(dataSnapshot.getKey().isEmpty());
                if (connected) {
                    loadingDialog.dismiss();
                    databaseReference.removeEventListener(valueEventListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Ошибка в работе базы данных. Обратитесь к администратору компании или разработчику", Toast.LENGTH_LONG).show();
            }
        };
        chatRecyclerAdapter.notifyDataSetChanged();
        databaseReference.addValueEventListener(valueEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        databaseReference.removeEventListener(valueEventListener);
        chatRecyclerAdapter.cleanup();
    }

    private void sendMessage() {
        EditText inputText = (EditText) findViewById(R.id.messageInput);
        String input = inputText.getText().toString();
        String messageTime;

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, MMM dd", Locale.ENGLISH);
        messageTime = formatter.format(Calendar.getInstance().getTime());
        if (!input.equals("")) {
            ChatMessage chatMessage = new ChatMessage(input, Globals.currentUser.getUserName(), Globals.currentUser.getLogin(), messageTime, true);
            databaseReference.push().setValue(chatMessage);
            inputText.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_messaging, menu);
        if (!isActive)
            menu.findItem(R.id.action_send_request).setVisible(false);
        if (Globals.currentUser.getRole() == User.SIMPLE_USER)
            menu.findItem(R.id.action_send_request).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            onBackPressed();
        else if (id == R.id.action_send_request){
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, MMM dd", Locale.ENGLISH);
            String messageTime = formatter.format(Calendar.getInstance().getTime());

            DatabaseStorage.updateLogFile(MessagingActivity.this, mChatRoom, DatabaseStorage.ACTION_REQUESTED_TO_CLOSE, Globals.currentUser, null);

            ChatMessage chatMessage = new ChatMessage("not answered", "System", Globals.currentUser.getLogin(), messageTime, true);
            databaseReference.push().setValue(chatMessage);
        } else if (id == R.id.action_show_info){
            final MaterialDialog materialDialog = new MaterialDialog.Builder(MessagingActivity.this)
                    .title("Лог")
                    .content("Загрузка...")
                    .positiveText("Ок")
                    .show();

            final StorageReference storageReference = FirebaseStorage.getInstance().getReference("logs").child(mChatRoom + ".log");
            try {
                final File localFile = File.createTempFile(mChatRoom, "log");

                storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        try {
                            FileInputStream fis = new FileInputStream(localFile);
                            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

                            String result = "";
                            String line = null;
                            while ((line = br.readLine()) != null) {
                                result += line;
                                result += "\n";
                            }
                            br.close();

                            result = result.substring(0, result.length()-2);
                            result = result.replaceAll(": ", ":\n");
                            materialDialog.setContent(result);
                        } catch (FileNotFoundException e){
                            e.printStackTrace();
                            materialDialog.setContent("Ошибка загрузки");
                        } catch (IOException e){
                            e.printStackTrace();
                            materialDialog.setContent("Ошибка загрузки");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        materialDialog.setContent("Ошибка загрузки");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
