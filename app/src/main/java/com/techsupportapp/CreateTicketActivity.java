package com.techsupportapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.utility.DatabaseStorage;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateTicketActivity extends AppCompatActivity{

    //region Fields

    private DatabaseReference ticketReference;
    private DatabaseReference firstDateReference;
    private DatabaseReference lastDateReference;
    private DatabaseReference ticketIndexReference;

    private int ticketCount;
    private String rightDate;

    //endregion

    //region Composite Controls

    private EditText topicET;
    private EditText messageET;

    private FloatingActionButton fab;

    private String ticketTopic;
    private int ticketType;

    //endregion

    //region Listeners

    ValueEventListener ticketIndexListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            ticketCount = dataSnapshot.getValue(int.class);
            Globals.logInfoAPK(CreateTicketActivity.this, "Обновление количества тикетов - ВЫПОЛНЕНО");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ValueEventListener lastDateListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            rightDate = dataSnapshot.getValue(String.class);
            Globals.logInfoAPK(CreateTicketActivity.this, "Обновление крайней даты - ВЫПОЛНЕНО");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ticket);

        ticketTopic = getIntent().getExtras().getString("ticketTopic");
        ticketType = getIntent().getExtras().getInt("ticketType");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initializeComponents();
        setEvents();
    }

    private void initializeComponents() {
        topicET = (EditText)findViewById(R.id.message_topic_text);
        messageET = (EditText)findViewById(R.id.message_text);

        ticketReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Tickets.DATABASE_UNMARKED_TICKET_TABLE);
        firstDateReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Indexes.DATABASE_FIRST_DATE_INDEX);
        lastDateReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Indexes.DATABASE_LAST_DATE_INDEX);
        ticketIndexReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Indexes.DATABASE_TICKET_INDEX_COUNTER);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        if (!ticketTopic.equals("Другие проблемы")) {
            topicET.setText(ticketTopic);
            topicET.setEnabled(false);
        }

        setTitle("Создать заявку");
    }

    private void setEvents() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (topicET.getText().toString().equals("") || messageET.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Заполните поля", Toast.LENGTH_LONG).show();
                } else {
                    String newRightDate = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime());
                    try {
                        if (rightDate == null) {
                            firstDateReference.setValue(newRightDate);
                            lastDateReference.setValue(newRightDate);
                        }
                        else if (!newRightDate.equals(rightDate))
                            lastDateReference.setValue(newRightDate);
                        Ticket newTicket = new Ticket("ticket" + ticketCount, ticketType, Globals.currentUser.getLogin(), Globals.currentUser.getUserName(), topicET.getText().toString(), messageET.getText().toString());
                        ticketReference.child("ticket" + ticketCount++).setValue(newTicket);
                        ticketIndexReference.setValue(ticketCount);
                        Toast.makeText(getApplicationContext(), "Заявка добалена", Toast.LENGTH_LONG).show();

                        DatabaseStorage.updateLogFile(CreateTicketActivity.this, newTicket.getTicketId(), DatabaseStorage.ACTION_CREATED, Globals.currentUser, null);

                        finish();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        ticketIndexReference.removeEventListener(ticketIndexListener);
        lastDateReference.removeEventListener(lastDateListener);
        Globals.logInfoAPK(CreateTicketActivity.this, "onPause - ВЫПОЛНЕН");
        super.onPause();
    }

    @Override
    protected void onResume() {
        ticketIndexReference.addValueEventListener(ticketIndexListener);
        lastDateReference.addValueEventListener(lastDateListener);
        Globals.logInfoAPK(CreateTicketActivity.this, "onResume - ВЫПОЛНЕН");
        super.onResume();
    }

    @Override
    protected void onStop() {
        ticketIndexReference.removeEventListener(ticketIndexListener);
        lastDateReference.removeEventListener(lastDateListener);
        Globals.logInfoAPK(CreateTicketActivity.this, "onStop - ВЫПОЛНЕН");
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
