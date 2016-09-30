package com.techsupportapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.adapters.TicketAdapter;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;

public class ListOfTicketsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private ListView viewOfAvailableTickets;
    private ListView viewOfMyClosedTickets;
    private ListView viewOfClosedTickets;

    private DatabaseReference databaseRef;
    private ArrayList<Ticket> listOfAvailableTickets = new ArrayList<Ticket>();
    private ArrayList<Ticket> listOfMyClosedTickets = new ArrayList<Ticket>();
    private ArrayList<Ticket> listOfSolvedTickets = new ArrayList<Ticket>();
    private TicketAdapter adapter;
    private TabHost tabHost;

    private ImageView currUserImage;

    private String mAppId;
    private String mUserId;
    private String mNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_tickets);

        mAppId = getIntent().getExtras().getString("appKey");
        mUserId = getIntent().getExtras().getString("uuid");
        mNickname = getIntent().getExtras().getString("nickname");

        initTabHost();
        initializeComponents();
        setEvents();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.listOfChannels) {
            finish();
        } else if (id == R.id.signUpUser) {
            Intent intent = new Intent(ListOfTicketsActivity.this, UserActionsActivity.class);
            intent.putExtra("appKey", mAppId);
            intent.putExtra("uuid", mUserId);
            intent.putExtra("nickname", mNickname);
            startActivity(intent);
            finish();
        } else if (id == R.id.settings) {

        } else if (id == R.id.listOfTickets) {

        }else if (id == R.id.about) {
            GlobalsMethods.showAbout(ListOfTicketsActivity.this);
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

    private void initTabHost(){
        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        initTabSpec("tag1", R.id.tab1, "Доступные");
        initTabSpec("tag2", R.id.tab2, "Закрытые мною");
        initTabSpec("tag3", R.id.tab3, "Закрытые все");

        tabHost.setCurrentTab(0);
    }

    private void initTabSpec(String tag, int viewId, String label) {
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag);
        tabSpec.setContent(viewId);
        tabSpec.setIndicator(label);
        tabHost.addTab(tabSpec);
    }

    private void initializeComponents() {
        viewOfAvailableTickets = (ListView)findViewById(R.id.listOfAvailableTickets);
        viewOfMyClosedTickets = (ListView)findViewById(R.id.listOfMyClosedTickets);
        viewOfClosedTickets = (ListView)findViewById(R.id.listOfClosedTickets);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Список заявок");
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

        currUserImage.setImageBitmap(GlobalsMethods.ImageMethods.getclip(GlobalsMethods.ImageMethods.createUserImage(mNickname, ListOfTicketsActivity.this)));

        userName.setText(mNickname);
        userType.setText("Администратор");
    }

    private void setEvents() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listOfAvailableTickets.clear();
                listOfMyClosedTickets.clear();
                listOfSolvedTickets.clear();
                for (DataSnapshot ticketRecord : dataSnapshot.child(DatabaseVariables.DATABASE_UNMARKED_TICKET_TABLE).getChildren()) {
                    Ticket ticket = ticketRecord.getValue(Ticket.class);
                    listOfAvailableTickets.add(ticket);
                }
                for (DataSnapshot ticketRecord : dataSnapshot.child(DatabaseVariables.DATABASE_SOLVED_TICKET_TABLE).getChildren()) {
                    Ticket ticket = ticketRecord.getValue(Ticket.class);
                    listOfSolvedTickets.add(ticket);
                }
                for (Ticket ticket : listOfSolvedTickets) {
                    if (ticket.getAdminId().equals(mUserId))
                        listOfMyClosedTickets.add(ticket);
                }
                if (adapter == null) {
                    adapter = new TicketAdapter(getApplicationContext(), listOfAvailableTickets);
                    viewOfAvailableTickets.setAdapter(adapter);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        viewOfAvailableTickets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ListOfTicketsActivity.this);

                builder.setTitle("Принять заявку");
                builder.setMessage("Вы действительно хотите принять заявку?");

                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listOfAvailableTickets.get(position).addAdmin(mUserId);
                                databaseRef.child(DatabaseVariables.DATABASE_MARKED_TICKET_TABLE).child(listOfAvailableTickets.get(position).getTicketId()).setValue(listOfAvailableTickets.get(position));
                                databaseRef.child(DatabaseVariables.DATABASE_UNMARKED_TICKET_TABLE).child(listOfAvailableTickets.get(position).getTicketId()).removeValue();
                            }
                        });

                builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.show();
            }
        });

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                switch (tabId){
                    case "tag1":
                        adapter = new TicketAdapter(getApplicationContext(), listOfAvailableTickets);
                        viewOfAvailableTickets.setAdapter(adapter);
                        return;
                    case "tag2":
                        adapter = new TicketAdapter(getApplicationContext(), listOfMyClosedTickets);
                        viewOfMyClosedTickets.setAdapter(adapter);
                        return;
                    case "tag3":
                        adapter = new TicketAdapter(getApplicationContext(), listOfSolvedTickets);
                        viewOfClosedTickets.setAdapter(adapter);
                        return;
                    default:
                        Toast.makeText(getApplicationContext(), "При смене ТАБа что-то произошло. Сообщите разработчику", Toast.LENGTH_LONG).show();
                        return;
                }
            }
        });

        currUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListOfTicketsActivity.this, UserProfileActivity.class);
                intent.putExtra("userId", mUserId);
                intent.putExtra("currUserId", mUserId);
                startActivity(intent);
            }
        });
    }
}
