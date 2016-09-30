package com.techsupportapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sendbird.android.SendBird;
import com.techsupportapp.adapters.TicketAdapter;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;

public class TicketsOverviewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ListView ticketsOverview;

    private String mAppId;
    private String mUserId;
    private String mNickname;
    private boolean isAdmin;
    private String mGcmRegToken;

    private DatabaseReference databaseRef;
    private ArrayList<Ticket> ticketsOverviewList = new ArrayList<Ticket>();
    private ArrayAdapter<Ticket> adapter;

    private ImageView currUserImage;

    private static Context cntxt;

    public static Bundle makeSendBirdArgs(String appKey, String uuid, String nickname, int role) {
        Bundle args = new Bundle();
        args.putString("appKey", appKey);
        args.putString("uuid", uuid);
        args.putString("nickname", nickname);
        args.putInt("isAdmin", role);
        return args;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets_overview);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        cntxt = getBaseContext();

        mAppId = getIntent().getExtras().getString("appKey");
        mUserId = getIntent().getExtras().getString("uuid");
        mNickname = getIntent().getExtras().getString("nickname");
        isAdmin = getIntent().getExtras().getBoolean("isAdmin");
        mGcmRegToken = PreferenceManager.getDefaultSharedPreferences(TicketsOverviewActivity.this).getString("SendBirdGCMToken", "");

        initializeComponents();
        setEvents();
        initSendBird();
    }

    private void initializeComponents(){
        ticketsOverview = (ListView)findViewById(R.id.ticketsOverview);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mUserId);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        currUserImage = (ImageView)navigationView.getHeaderView(0).findViewById(R.id.userImage);
        TextView userName = (TextView)navigationView.getHeaderView(0).findViewById(R.id.userName);
        TextView userType = (TextView)navigationView.getHeaderView(0).findViewById(R.id.userType);

        currUserImage.setImageBitmap(GlobalsMethods.ImageMethods.getclip(GlobalsMethods.ImageMethods.createUserImage(mNickname, TicketsOverviewActivity.this)));

        Menu nav_menu = navigationView.getMenu();
        userName.setText(mNickname);
        if (isAdmin) {
            userType.setText("Администратор");
        }
        else {
            userType.setText("Пользователь");
            nav_menu.findItem(R.id.signUpUser).setVisible(false);
            nav_menu.findItem(R.id.listOfChannels).setTitle("Список ваших заявок");
            nav_menu.findItem(R.id.listOfTickets).setTitle("Создать заявку");
        }
    }

    private void initSendBird() {
        SendBird.init(this, mAppId);
        SendBird.login(SendBird.LoginOption.build(mUserId).setUserName(mNickname).setGCMRegToken(mGcmRegToken));
    }

    private void setEvents() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ticketsOverviewList.clear();
                if (isAdmin) {
                    for (DataSnapshot ticketRecord : dataSnapshot.child(DatabaseVariables.DATABASE_MARKED_TICKET_TABLE).getChildren()) {
                        Ticket ticket = ticketRecord.getValue(Ticket.class);
                        if (ticket.getAdminId().equals(mUserId))
                            ticketsOverviewList.add(ticket);
                    }
                    adapter = new TicketAdapter(getApplicationContext(), ticketsOverviewList);
                    ticketsOverview.setAdapter(adapter);
                } else {
                    for (DataSnapshot markedTicketRecord : dataSnapshot.child(DatabaseVariables.DATABASE_MARKED_TICKET_TABLE).getChildren()) {
                        Ticket markedTicket = markedTicketRecord.getValue(Ticket.class);
                        if (markedTicket.getUserId().equals(mUserId))
                            ticketsOverviewList.add(markedTicket);
                    }
                    for (DataSnapshot unMarkedTicketRecord : dataSnapshot.child(DatabaseVariables.DATABASE_UNMARKED_TICKET_TABLE).getChildren()) {
                        Ticket unMarkedTicket = unMarkedTicketRecord.getValue(Ticket.class);
                        if (unMarkedTicket.getUserId().equals(mUserId))
                            ticketsOverviewList.add(unMarkedTicket);
                    }
                    adapter = new TicketAdapter(getApplicationContext(), ticketsOverviewList);
                    ticketsOverview.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ticketsOverview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                if (isAdmin) {
                    intent = new Intent(TicketsOverviewActivity.this, ChatActivity.class);
                    Bundle args = ChatActivity.makeMessagingStartArgs(mAppId, mUserId, mNickname, ticketsOverviewList.get(position).getUserId());
                    intent.putExtras(args);
                } else {
                    if (ticketsOverviewList.get(position).getAdminId() == null || ticketsOverviewList.get(position).getAdminId().equals("")) {
                        Toast.makeText(getApplicationContext(), "Администратор еще не просматривал ваше сообщение, подождите", Toast.LENGTH_LONG).show();
                        return;
                    }
                    else {
                        intent = new Intent(TicketsOverviewActivity.this, ChatActivity.class);
                        Bundle args = ChatActivity.makeMessagingStartArgs(mAppId, mUserId, mNickname, ticketsOverviewList.get(position).getAdminId());
                        intent.putExtras(args);
                    }
                }
                startActivityForResult(intent, 210);
            }
        });

        ticketsOverview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (isAdmin){
                    //что-то
                } else {
                    //TODO вы точно хотите отозвать тикет
                    if (ticketsOverviewList.get(position).getAdminId() == null || ticketsOverviewList.get(position).getAdminId().equals(""))
                        databaseRef.child(DatabaseVariables.DATABASE_UNMARKED_TICKET_TABLE).child(ticketsOverviewList.get(position).getTicketId()).removeValue();
                    else; //TODO проблема решена
                }
                return true;
            }
        });

        currUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TicketsOverviewActivity.this, UserProfileActivity.class);
                intent.putExtra("userId", mUserId);
                intent.putExtra("currUserId", mUserId);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.listOfTickets) {
            if (isAdmin) {
                Intent intent = new Intent(TicketsOverviewActivity.this, ListOfTicketsActivity.class);
                intent.putExtra("appKey", mAppId);
                intent.putExtra("uuid", mUserId);
                intent.putExtra("nickname", mNickname);
                startActivity(intent);
            }
            else
            {
                Intent intent = new Intent(TicketsOverviewActivity.this, CreateTicketActivity.class);
                intent.putExtra("appKey", mAppId);
                intent.putExtra("uuid", mUserId);
                intent.putExtra("nickname", mNickname);
                startActivity(intent);
            }
        } else if (id == R.id.signUpUser) {
            Intent intent = new Intent(TicketsOverviewActivity.this, UserActionsActivity.class);
            intent.putExtra("appKey", mAppId);
            intent.putExtra("uuid", mUserId);
            intent.putExtra("nickname", mNickname);
            startActivity(intent);
        } else if (id == R.id.settings) {

        } else if (id == R.id.about) {
            GlobalsMethods.showAbout(TicketsOverviewActivity.this);
            return true;
        } else if (id == R.id.exit) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);

            builder.setPositiveButton("Закрыть приложение", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    exit();
                }
            });

            builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.setCancelable(false);
            builder.setMessage("Вы действительно хотите закрыть приложение?");
            builder.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void exit(){
        this.finishAffinity();
    }
}
