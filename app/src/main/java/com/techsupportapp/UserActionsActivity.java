package com.techsupportapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.adapters.UnverifiedUserRecyclerAdapter;
import com.techsupportapp.adapters.UserRecyclerAdapter;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;
import com.techsupportapp.utility.ItemClickSupport;

import java.util.ArrayList;

public class UserActionsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static String mUserId;
    private String mNickname;

    private static Context context;

    private static ViewPager viewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private static RecyclerView unverifiedUsersView;
    private static RecyclerView usersView;

    private static DatabaseReference databaseRef;
    private static ArrayList<User> unverifiedUsersList = new ArrayList<User>();
    private static ArrayList<User> usersList = new ArrayList<User>();
    private static UnverifiedUserRecyclerAdapter adapter;
    private static UserRecyclerAdapter adapter1;

    private MenuItem searchMenu;
    private static SearchView searchView;
    private static boolean search;

    private ImageView currUserImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_actions);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        context = UserActionsActivity.this;

        mUserId = getIntent().getExtras().getString("uuid");
        mNickname = getIntent().getExtras().getString("nickname");

        initializeComponents();
        setEvents();
    }

    private void initializeComponents(){
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

        currUserImage.setImageBitmap(Globals.ImageMethods.getclip(Globals.ImageMethods.createUserImage(mNickname, UserActionsActivity.this)));

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        userName.setText(mNickname);
        userType.setText("Администратор");
        navigationView.getMenu().findItem(R.id.charts).setVisible(false);

        search = false;
    }

    private static String getLogInMessage(User unVerifiedUser) throws Exception {
        int role = unVerifiedUser.getRole();
        String resultString = "Вы действительно хотите создать аккаунт " + unVerifiedUser.getLogin()
                + " и дать ему права ";
        if (role == User.SIMPLE_USER)
            return resultString + "ПОЛЬЗОВАТЕЛЯ";
        else if (role == User.DEPARTMENT_MEMBER)
            return resultString + "РАБОТНИКА ОТДЕЛА";
        else if (role == User.ADMINISTRATOR)
            return resultString + "АДМИНИСТРАТОРА";
        else if (role == User.DEPARTMENT_CHIEF)
            return resultString + "НАЧАЛЬНИКА ОТДЕЛА";
        else throw new Exception("Передана нулевая ссылка или неверно указаны права пользователя");
    }

    private static String getDatabaseUserPath(User unVerifiedUser) throws Exception {
        int role = unVerifiedUser.getRole();
        if (role == User.SIMPLE_USER)
            return DatabaseVariables.Users.DATABASE_VERIFIED_SIMPLE_USER_TABLE;
        else if (role == User.DEPARTMENT_MEMBER)
            return DatabaseVariables.Users.DATABASE_VERIFIED_WORKER_TABLE;
        else if (role == User.ADMINISTRATOR)
            return DatabaseVariables.Users.DATABASE_VERIFIED_ADMIN_TABLE;
        else if (role == User.DEPARTMENT_CHIEF)
            return DatabaseVariables.Users.DATABASE_VERIFIED_CHIEF_TABLE;
        else throw new Exception("Передана нулевая ссылка или неверно указаны права пользователя");
    }

    private void setEvents() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!search) {
                    unverifiedUsersList = Globals.Downloads.getUnverifiedUserList(dataSnapshot);
                    adapter = new UnverifiedUserRecyclerAdapter(getApplicationContext(), unverifiedUsersList);
                    unverifiedUsersView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    usersList = Globals.Downloads.getVerifiedUserList(dataSnapshot);
                    adapter1 = new UserRecyclerAdapter(getApplicationContext(), usersList);
                    usersView.setAdapter(adapter1);
                    adapter1.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                MenuItemCompat.collapseActionView(searchMenu);

                adapter = new UnverifiedUserRecyclerAdapter(getApplicationContext(), unverifiedUsersList);
                unverifiedUsersView.setAdapter(adapter);

                adapter1 = new UserRecyclerAdapter(getApplicationContext(), usersList);
                usersView.setAdapter(adapter1);

                adapter.notifyDataSetChanged();
                adapter1.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

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
            intent.putExtra("uuid", mUserId);
            intent.putExtra("nickname", mNickname);
            startActivity(intent);
        } else if (id == R.id.charts) {
            Intent intent = new Intent(UserActionsActivity.this, ChartsActivity.class);
            intent.putExtra("uuid", mUserId);
            intent.putExtra("nickname", mNickname);
            startActivity(intent);
            finish();
        } else if (id == R.id.settings) {

        } else if (id == R.id.about) {
            Globals.showAbout(UserActionsActivity.this);
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
                if (viewPager.getCurrentItem() == 0) {
                    ArrayList<User> newUnverifiedUsersList = new ArrayList<User>();

                    for (User unverifiedUser : unverifiedUsersList) {
                        if (unverifiedUser.getUserName().toLowerCase().contains(searchView.getQuery().toString().toLowerCase()))
                            newUnverifiedUsersList.add(unverifiedUser);
                    }

                    adapter = new UnverifiedUserRecyclerAdapter(getApplicationContext(), newUnverifiedUsersList);

                    unverifiedUsersView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                else
                {
                    ArrayList<User> newUsersList = new ArrayList<User>();

                    for (User user: usersList){
                        if (user.getUserName().toLowerCase().contains(searchView.getQuery().toString().toLowerCase()))
                            newUsersList.add(user);
                    }

                    adapter1 = new UserRecyclerAdapter(getApplicationContext(), newUsersList);

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
        }
        return super.onOptionsItemSelected(item);
    }

    private void exit(){
        this.finishAffinity();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.e("MyTAG", String.valueOf(position));
            if (position == 0)
                return FirstFragment.newInstance();
            else
                return SecondFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Не авторизованные";
                case 1:
                    return "Все";
            }
            return null;
        }
    }

    public static class FirstFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_user_actions, container, false);

            unverifiedUsersView = (RecyclerView) v.findViewById(R.id.recycler);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);;

            adapter = new UnverifiedUserRecyclerAdapter(context, unverifiedUsersList);

            unverifiedUsersView.setLayoutManager(mLayoutManager);
            unverifiedUsersView.setHasFixedSize(false);

            unverifiedUsersView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            ItemClickSupport.addTo(unverifiedUsersView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, final int position, View v) {
                    final User selectedUser = unverifiedUsersList.get(position);
                    AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);

                    builder.setTitle("Подтвердить пользователя " + selectedUser.getBranchId());
                    try {
                        builder.setMessage(getLogInMessage(selectedUser));
                    }
                    catch (Exception e) {
                        Globals.showLongTimeToast(context, "Передана нулевая ссылка или неверно указаны права пользователя. Обратитесь к разработчику");
                    }

                    builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            search = false;
                            try {
                                databaseRef.child(getDatabaseUserPath(selectedUser))
                                        .child(selectedUser.getBranchId()).setValue(selectedUser);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            databaseRef.child(DatabaseVariables.Users.DATABASE_UNVERIFIED_USER_TABLE).child(selectedUser.getBranchId()).removeValue();
                            Toast.makeText(context, "Пользователь добавлен в базу данных", Toast.LENGTH_LONG).show();
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

            ItemClickSupport.addTo(unverifiedUsersView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                    final User selectedUser = unverifiedUsersList.get(position);
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);

                    builder.setTitle("Отклонить заявку пользователя " + selectedUser.getBranchId());
                    builder.setMessage("Вы действительно хотите отклонить заявку пользователя "
                            + selectedUser.getLogin() + " на регистрацию?" );

                    builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            search = false;
                            databaseRef.child(DatabaseVariables.Users.DATABASE_UNVERIFIED_USER_TABLE).child(selectedUser.getBranchId()).removeValue();
                            Globals.showLongTimeToast(context, "Заявка пользователя была успешно отклонена");
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
                    return true;
                }
            });

            return v;
        }

        public static FirstFragment newInstance() {
            FirstFragment f = new FirstFragment();
            return f;
        }
    }

    public static class SecondFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_user_actions, container, false);

            usersView = (RecyclerView) v.findViewById(R.id.recycler);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);;

            adapter1 = new UserRecyclerAdapter(context, usersList);

            usersView.setLayoutManager(mLayoutManager);
            usersView.setHasFixedSize(false);

            usersView.setAdapter(adapter1);
            adapter1.notifyDataSetChanged();

            ItemClickSupport.addTo(usersView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("userId", usersList.get(position).getLogin());
                    intent.putExtra("currUserId", mUserId);
                    startActivity(intent);
                }
            });

            return v;
        }

        public static SecondFragment newInstance() {
            SecondFragment f = new SecondFragment();
            return f;
        }
    }
}
