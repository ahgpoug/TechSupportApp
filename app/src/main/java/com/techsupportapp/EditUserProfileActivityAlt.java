package com.techsupportapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseVariables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class EditUserProfileActivityAlt extends AppCompatActivity{

    private DatabaseReference databaseRef;

    private ArrayList<User> usersList = new ArrayList<User>();

    private String mUserId;
    private String mCurrUserId;

    private int currUserPosition;
    private int userPosition;

    private boolean changedType;

    private Button changePasswordBtn;
    private Button changeUserTypeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserId = getIntent().getExtras().getString("userId");
        mCurrUserId = getIntent().getExtras().getString("currUserId");

        setContentView(R.layout.activity_edit_user_profile_alt);

        initializeComponents();
        setEvents();
    }

    private void initializeComponents(){
        databaseRef = FirebaseDatabase.getInstance().getReference();

        changePasswordBtn = (Button)findViewById(R.id.changePasswordBtn);
        changeUserTypeBtn = (Button)findViewById(R.id.changeUserTypeBtn);

        changedType = false;
    }

    private void setEvents(){
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot userRecord : dataSnapshot.child(DatabaseVariables.DATABASE_VERIFIED_USER_TABLE).getChildren()) {
                    User user = userRecord.getValue(User.class);
                    usersList.add(user);
                }
                setData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        changeUserTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(EditUserProfileActivityAlt.this);
                View promptsView = li.inflate(R.layout.prompts_change_user_type, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditUserProfileActivityAlt.this);

                alertDialogBuilder.setView(promptsView);

                final ListView typesOfUsers = (ListView) promptsView.findViewById(R.id.typesOfUsersList);
                final String[] types = { "Пользователь", "Администратор" };
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditUserProfileActivityAlt.this, android.R.layout.simple_list_item_1, types);
                typesOfUsers.setAdapter(adapter);

                alertDialogBuilder
                        .setCancelable(true)

                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                final AlertDialog alertDialog = alertDialogBuilder.create();

                typesOfUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (types[position].equals("Пользователь")) {
                            if (usersList.get(userPosition).getRole() == User.SIMPLE_USER) {
                                try {
                                    User chUser = new User(usersList.get(userPosition).getBranchId(), usersList.get(userPosition).getLogin(), usersList.get(userPosition).getPassword(), User.SIMPLE_USER, usersList.get(userPosition).getBranchId(),
                                            usersList.get(userPosition).getLogin(), "Wayward Pines", false);
                                    databaseRef.child(DatabaseVariables.DATABASE_VERIFIED_USER_TABLE).child(chUser.getBranchId()).setValue(chUser);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }

                                changedType = true;
                                alertDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Переведен в статус пользователя", Toast.LENGTH_LONG).show();
                            } else
                                Toast.makeText(getApplicationContext(), "Уже является пользователем", Toast.LENGTH_LONG).show();
                        } else if (usersList.get(userPosition).getRole() == User.ADMINISTRATOR) {
                            try {
                                User chUser = new User(usersList.get(userPosition).getBranchId(), usersList.get(userPosition).getLogin(), usersList.get(userPosition).getPassword(), User.ADMINISTRATOR, usersList.get(userPosition).getBranchId(),
                                        usersList.get(userPosition).getLogin(), "Wayward Pines", false);
                                databaseRef.child(DatabaseVariables.DATABASE_VERIFIED_USER_TABLE).child(chUser.getBranchId()).setValue(chUser);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            changedType = true;
                            alertDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Переведен в статус администратора", Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(getApplicationContext(), "Уже является администратором", Toast.LENGTH_LONG).show();
                    }
                    });

                alertDialog.show();
            }
        });
    }

    private void setData(){
        ArrayList<String> idList = new ArrayList<>();
        Collections.sort(usersList, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.getLogin().compareTo(rhs.getLogin());
            }
        });

        for (int i = 0; i < usersList.size(); i++)
            idList.add(usersList.get(i).getLogin());

        userPosition = Collections.binarySearch(idList, mUserId, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        currUserPosition = Collections.binarySearch(idList, mCurrUserId, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        setTitle("Профиль " + usersList.get(userPosition).getLogin());
    }
}
