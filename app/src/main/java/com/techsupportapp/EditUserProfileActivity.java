package com.techsupportapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class EditUserProfileActivity extends AppCompatActivity{

    private DatabaseReference databaseRef;

    private ArrayList<User> usersList = new ArrayList<User>();

    private String mUserId;
    private String mCurrUserId;

    private int currUserPosition;
    private int userPosition;

    private boolean changedType;

    private EditText userName;
    private EditText workPlace;
    private Button saveBtn;
    private Button changePasswordBtn;
    private Button changeUserTypeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserId = getIntent().getExtras().getString("userId");
        mCurrUserId = getIntent().getExtras().getString("currUserId");

        setContentView(R.layout.activity_edit_user_profile);

        initializeComponents();
        setEvents();
    }

    private void initializeComponents(){
        databaseRef = FirebaseDatabase.getInstance().getReference();

        userName = (EditText)findViewById(R.id.userName);
        workPlace = (EditText)findViewById(R.id.workPlace);

        changePasswordBtn = (Button)findViewById(R.id.changePasswordBtn);
        changeUserTypeBtn = (Button)findViewById(R.id.changeUserTypeBtn);

        saveBtn = (Button)findViewById(R.id.saveBtn);

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

        changeUserTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(EditUserProfileActivity.this);
                View promptsView = li.inflate(R.layout.prompts_change_user_type, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditUserProfileActivity.this);

                alertDialogBuilder.setView(promptsView);

                final ListView typesOfUsers = (ListView) promptsView.findViewById(R.id.typesOfUsersList);
                final String[] types = { "Пользователь"};
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditUserProfileActivity.this, android.R.layout.simple_list_item_1, types);
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
                            if (usersList.get(userPosition).getRole() != User.SIMPLE_USER) {
                                User chUser;
                                try {
                                    chUser = new User(usersList.get(userPosition).getBranchId(), usersList.get(userPosition).getLogin(), usersList.get(userPosition).getPassword(), User.SIMPLE_USER, usersList.get(userPosition).getBranchId(),
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
                        } else if (usersList.get(userPosition).getRole() != User.ADMINISTRATOR) {
                            User chUser;
                            try {
                                chUser = new User(usersList.get(userPosition).getBranchId(), usersList.get(userPosition).getLogin(), usersList.get(userPosition).getPassword(), User.ADMINISTRATOR, usersList.get(userPosition).getBranchId(),
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

                        if (changedType && mUserId.equals(mCurrUserId)){
                            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }

                    }
                });

                alertDialog.show();
            }
        });

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(EditUserProfileActivity.this);
                View promptsView = li.inflate(R.layout.prompts_change_password, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditUserProfileActivity.this);

                alertDialogBuilder.setView(promptsView);

                final EditText currentPassword = (EditText) promptsView.findViewById(R.id.currentPasswordEt);
                final EditText newPassword = (EditText) promptsView.findViewById(R.id.newPasswordEt);
                final EditText newPasswordRepeat = (EditText) promptsView.findViewById(R.id.newPasswordRepeatEt);

                alertDialogBuilder
                        .setCancelable(true)
                        .setTitle("Смена пароля")
                        .setPositiveButton("Ок",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (currentPassword.getText().toString().equals("")) {
                            Toast.makeText(getApplicationContext(), "Поле текущего пароля пусто", Toast.LENGTH_LONG).show();
                        }
                        else if (newPassword.getText().toString().equals(""))
                            Toast.makeText(getApplicationContext(), "Поле нового пароля пусто", Toast.LENGTH_LONG).show();
                        else if (newPasswordRepeat.getText().toString().equals(""))
                            Toast.makeText(getApplicationContext(), "Нужно повторить пароль", Toast.LENGTH_LONG).show();
                        else if (!currentPassword.getText().toString().equals(usersList.get(userPosition).getPassword()))
                            Toast.makeText(getApplicationContext(), "Введен неправильный пароль", Toast.LENGTH_LONG).show();
                        else if (!newPassword.getText().toString().equals(newPasswordRepeat.getText().toString()))
                            Toast.makeText(getApplicationContext(), "Пароли должны совпадать", Toast.LENGTH_LONG).show();
                        else if (newPassword.getText().toString().length() < 5 || newPasswordRepeat.getText().toString().length() < 5)
                            Toast.makeText(getApplicationContext(), "Пароль должен быть не менее 5 символов", Toast.LENGTH_LONG).show();
                        else if (!GlobalsMethods.isEnglishWord(newPassword.getText().toString()) || !GlobalsMethods.isEnglishWord(newPasswordRepeat.getText().toString()))
                            Toast.makeText(getApplicationContext(), "Пароли должны содержать только английские символы и цифры", Toast.LENGTH_LONG).show();
                        else
                        {
                            try {
                                User chUser = new User(usersList.get(userPosition).getBranchId(), usersList.get(userPosition).getLogin(), newPasswordRepeat.getText().toString(), usersList.get(userPosition).getRole(), usersList.get(userPosition).getBranchId(),
                                        usersList.get(userPosition).getLogin(), "Wayward Pines", false);
                                databaseRef.child(DatabaseVariables.DATABASE_VERIFIED_USER_TABLE).child(chUser.getBranchId()).setValue(chUser);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(getApplicationContext(), "Пароль успешно изменен", Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });



        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

        if (GlobalsMethods.isCurrentAdmin == User.SIMPLE_USER)
            changeUserTypeBtn.setVisibility(View.INVISIBLE);

        userName.setText(usersList.get(userPosition).getLogin());
        //workPlace.setText(usersList.get(index).workPlce); TODO сделать

        setTitle("Профиль " + usersList.get(userPosition).getLogin());
    }
}
