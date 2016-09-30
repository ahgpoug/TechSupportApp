package com.techsupportapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techsupportapp.databaseClasses.UnverifiedUser;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.GlobalsMethods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Класс для аутентификации пользователей
 * @author Monarch
 */
public class SignInActivity extends AppCompatActivity {

    //region Constants

    /**
     *
     */
    private static final String appId = "E78031C1-13EA-4B71-A80A-53120BD37E3F";

    //endregion

    //region Fields

    private ArrayList<User> userList = new ArrayList<User>();
    private ArrayList<String> unverifiedLoginsList = new ArrayList<String>();

    private DatabaseReference databaseReference;

    private boolean isDownloaded = false;

    //endregion

    //region Composite Controls

    private Button closeAppBut;
    private Button signInBut;
    private Button signUpBut;

    private EditText loginET;
    private EditText passwordET;

    private boolean cbState;
    private CheckBox rememberPas;

    private ProgressDialog dialog;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        setTitle("Авторизация");
        initializeComponents();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Загрузка...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        setEvents();
    }

    /**
     * Инициализация переменных и элементов макета
     */
    private void initializeComponents() {
        closeAppBut = (Button)findViewById(R.id.closeAppButton);
        signInBut = (Button)findViewById(R.id.signInButton);
        signUpBut = (Button)findViewById(R.id.signUpButton);

        loginET = (EditText)findViewById(R.id.loginET);
        passwordET = (EditText)findViewById(R.id.passwordET);

        rememberPas = (CheckBox)findViewById((R.id.checkBoxBold));

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Создание методов для событий
     */
    private void setEvents() {
        closeAppBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        signInBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDownloaded)
                {
                    Toast.makeText(getApplicationContext(), "Подождите, грузится база", Toast.LENGTH_LONG);
                    return;
                }
                if (hasConnection()) {
                    int index = Collections.binarySearch(unverifiedLoginsList, loginET.getText().toString(), new Comparator<String>() {
                        @Override
                        public int compare(String lhs, String rhs) {
                            return lhs.compareTo(rhs);
                        }
                    });
                    if (loginET.getText().toString().equals("")) {
                        loginET.requestFocus();
                        Toast.makeText(getApplicationContext(), "Введите логин", Toast.LENGTH_LONG).show();
                    } else if (passwordET.getText().toString().equals("")) {
                        passwordET.requestFocus();
                        Toast.makeText(getApplicationContext(), "Введите пароль", Toast.LENGTH_LONG).show();
                    } else if (userList.size() == 0) {
                        Toast.makeText(getApplicationContext(), "База данных пуста. " +
                                "Зарегистрируйте компанию у нас", Toast.LENGTH_LONG).show(); //Заготовка
                    } else if (index >= 0) {
                        Toast.makeText(getApplicationContext(), "Ваша заявка в списке ожидания. " +
                                "Подождите, пока администратор не примет ее", Toast.LENGTH_LONG).show();
                    } else {
                        int i = 0;
                        while (!loginET.getText().toString().equals(userList.get(i).getLogin()) && ++i < userList.size()); //TODO binarySearch
                        if (i >= userList.size()) {
                            passwordET.setText("");
                            Toast.makeText(getApplicationContext(), "Логин и/или пароль введен неверно. Повторите попытку", Toast.LENGTH_LONG).show();
                        }
                        else if (loginET.getText().toString().equals(userList.get(i).getLogin()) && passwordET.getText().toString().equals(userList.get(i).getPassword()))
                        {
                            Toast.makeText(getApplicationContext(), "Вход выполнен", Toast.LENGTH_LONG).show();

                            Intent intent;
                            String userName;

                            userName = loginET.getText().toString();

                            if (userList.get(i).getRole() == User.ADMINISTRATOR) {
                                intent = new Intent(SignInActivity.this, TicketsOverviewActivity.class);
                            }
                            else {
                                intent = new Intent(SignInActivity.this, TicketsOverviewActivity.class);
                            }

                            Bundle args = TicketsOverviewActivity.makeSendBirdArgs(appId, getId(userName), userName, userList.get(i).getRole());

                            intent.putExtras(args);
                            savePassAndLogin();
                            GlobalsMethods.currUserId = userName;
                            GlobalsMethods.isCurrentAdmin = userList.get(i).getRole();
                            startActivity(intent);
                        }
                        else
                        {
                            passwordET.setText("");
                            Toast.makeText(getApplicationContext(), "Логин и/или пароль введен неверно. Повторите попытку", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Нет подключения к интернету", Toast.LENGTH_LONG).show();
                }
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                unverifiedLoginsList.clear();
                for (DataSnapshot userRecord : dataSnapshot.child(DatabaseVariables.DATABASE_VERIFIED_USER_TABLE).getChildren()) {
                    User user = userRecord.getValue(User.class);
                    userList.add(user);
                }
                for (DataSnapshot userRecord : dataSnapshot.child(DatabaseVariables.DATABASE_UNVERIFIED_USER_TABLE).getChildren()) {
                    UnverifiedUser user = userRecord.getValue(UnverifiedUser.class);
                    unverifiedLoginsList.add(user.getLogin());
                }
                isDownloaded = true;
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Ошибка в работе базы данных. " +
                        "Обратитесь к администратору компании или разработчику", Toast.LENGTH_LONG).show();
            }
        });

        signUpBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(SignInActivity.this, SignUpActivity.class)); TODO вернуть
                startActivity(new Intent(SignInActivity.this, MessagingActivity.class));

            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton("Закрыть приложение", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                savePassAndLogin();
                SignInActivity.super.onBackPressed();
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

    private boolean hasConnection() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    private String getId(String value) //TODO Вернуть
    {
        /*String result = "";
        for (int i = 0; i < value.length(); i++)
            result += (char)(value.charAt(i) + 1);
        return result;*/
        return value;
    }

    private void savePassAndLogin(){
        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        if (rememberPas.isChecked()) {
            String login = loginET.getText().toString();
            String password = passwordET.getText().toString();
            editor.putString("Login", login);
            editor.putString("Password", password);
            editor.putBoolean("cbState", true);
        }
        else
            editor.clear();
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = getPreferences(0);
        loginET.setText(settings.getString("Login",""));
        passwordET.setText(settings.getString("Password",""));
        rememberPas.setChecked(settings.getBoolean("cbState", false));
    }
}
