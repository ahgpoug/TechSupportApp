package com.techsupportapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
import com.techsupportapp.adapters.UnverifiedUserAdapter;
import com.techsupportapp.adapters.UserAdapter;
import com.techsupportapp.databaseClasses.UnverifiedUser;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;

public class UserActionsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private ListView unverifiedUsersView;
    private ListView usersView;

    private String mAppId;
    private String mUserId;
    private String mNickname;

    private DatabaseReference databaseRef;
    private ArrayList<UnverifiedUser> unverifiedUsersList = new ArrayList<UnverifiedUser>();
    private ArrayList<User> usersList = new ArrayList<User>();
    private UnverifiedUserAdapter adapter;
    private UserAdapter adapter1;

    private MenuItem searchMenu;
    private SearchView searchView;
    private boolean search;

    private TabHost tabHost;

    private ImageView currUserImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_actions);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mAppId = getIntent().getExtras().getString("appKey");
        mUserId = getIntent().getExtras().getString("uuid");
        mNickname = getIntent().getExtras().getString("nickname");

        initTabHost();
        initializeComponents();
        setEvents();
    }

    private void initializeComponents(){
        unverifiedUsersView = (ListView)findViewById(R.id.listOfUnverifiedUsers);
        usersView = (ListView)findViewById(R.id.listOfUsers);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Пользователи");
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

        currUserImage.setImageBitmap(GlobalsMethods.ImageMethods.getclip(GlobalsMethods.ImageMethods.createUserImage(mNickname, UserActionsActivity.this)));

        userName.setText(mNickname);
        userType.setText("Администратор");
        search = false;
    }

    private void initTabHost(){
        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        initTabSpec("tag1", R.id.tab1, "Авторизация");
        initTabSpec("tag2", R.id.tab2, "Все");

        tabHost.setCurrentTab(0);
    }

    private void initTabSpec(String tag, int viewId, String label) {
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag);
        tabSpec.setContent(viewId);
        tabSpec.setIndicator(label);
        tabHost.addTab(tabSpec);
    }

    private void setEvents() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!search) {
                    unverifiedUsersList.clear();
                    for (DataSnapshot userRecord : dataSnapshot.child(DatabaseVariables.DATABASE_UNVERIFIED_USER_TABLE).getChildren()) {
                        UnverifiedUser user = userRecord.getValue(UnverifiedUser.class);
                        unverifiedUsersList.add(user);
                    }
                    adapter = new UnverifiedUserAdapter(getApplicationContext(), unverifiedUsersList);
                    unverifiedUsersView.setAdapter(adapter);

                    usersList.clear();
                    for (DataSnapshot userRecord : dataSnapshot.child(DatabaseVariables.DATABASE_VERIFIED_USER_TABLE).getChildren()) {
                        User user = userRecord.getValue(User.class);
                        usersList.add(user);
                    }
                    adapter1 = new UserAdapter(getApplicationContext(), usersList);
                    usersView.setAdapter(adapter1);

                    adapter.notifyDataSetChanged();
                    adapter1.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        unverifiedUsersView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(UserActionsActivity.this);

                builder.setTitle("Подтвердить пользователя " + unverifiedUsersList.get(position).getBranchId());
                builder.setMessage("Вы действительно хотите подтвердить пользователя?");

                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        search = false;
                        try {
                            databaseRef.child(DatabaseVariables.DATABASE_VERIFIED_USER_TABLE).child(unverifiedUsersList.get(position).getBranchId()).setValue(unverifiedUsersList.get(position).verifyUser());
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        databaseRef.child(DatabaseVariables.DATABASE_UNVERIFIED_USER_TABLE).child(unverifiedUsersList.get(position).getBranchId()).removeValue();
                        Toast.makeText(getApplicationContext(), "Пользователь добавлен в базу данных", Toast.LENGTH_LONG).show();
                        searchView.setQuery(searchView.getQuery().toString() + "", false);
                    }
                });

                builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.setCancelable(false);
                builder.show();
            }
        });

        usersView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent intent = new Intent(UserActionsActivity.this, UserProfileActivity.class);
                intent.putExtra("userId", usersList.get(position).getLogin());
                intent.putExtra("currUserId", mUserId);
                startActivity(intent);
            }
        });

        currUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActionsActivity.this, UserProfileActivity.class);
                intent.putExtra("userId", mUserId);
                intent.putExtra("currUserId", mUserId);
                startActivity(intent);
            }
        });

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                searchView.setQuery("", false);
                MenuItemCompat.collapseActionView(searchMenu);

                adapter = new UnverifiedUserAdapter(getApplicationContext(), unverifiedUsersList);
                unverifiedUsersView.setAdapter(adapter);

                adapter1 = new UserAdapter(getApplicationContext(), usersList);
                usersView.setAdapter(adapter1);

                adapter.notifyDataSetChanged();
                adapter1.notifyDataSetChanged();
            }
        });
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
        } else if (id == R.id.listOfTickets) {
            Intent intent = new Intent(UserActionsActivity.this, ListOfTicketsActivity.class);
            intent.putExtra("appKey", mAppId);
            intent.putExtra("uuid", mUserId);
            intent.putExtra("nickname", mNickname);
            startActivity(intent);
        } else if (id == R.id.settings) {

        } else if (id == R.id.about) {
            GlobalsMethods.showAbout(UserActionsActivity.this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        searchMenu = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenu.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            public boolean onQueryTextChange(String newText) {
                if (!searchView.getQuery().toString().equals(""))
                    search = true;
                if (tabHost.getCurrentTabTag().equals("tag1")) {
                    ArrayList<UnverifiedUser> newUnverifiedUsersList = new ArrayList<UnverifiedUser>();

                    for (UnverifiedUser unverifiedUser: unverifiedUsersList){
                        if (unverifiedUser.getLogin().contains(searchView.getQuery().toString()))
                            newUnverifiedUsersList.add(unverifiedUser);
                    }

                    adapter = new UnverifiedUserAdapter(getApplicationContext(), newUnverifiedUsersList);
                    unverifiedUsersView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                else
                {
                    ArrayList<User> newUsersList = new ArrayList<User>();

                    for (User user: usersList){
                        if (user.getLogin().contains(searchView.getQuery().toString()))
                            newUsersList.add(user);
                    }

                    adapter1 = new UserAdapter(getApplicationContext(), newUsersList);
                    usersView.setAdapter(adapter1);
                    adapter1.notifyDataSetChanged();
                }
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchMenu,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem menuItem) {
                        return true;
                    }
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                        search = false;
                        return true;
                    }
                });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        } else if (id == R.id.action_sort) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exit(){
        this.finishAffinity();
    }
}
