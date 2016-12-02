package com.techsupportapp.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.firebase.database.DataSnapshot;
import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;

import java.util.ArrayList;

import static com.techsupportapp.utility.Globals.Downloads.Users.getVerifiedUserList;

public class Globals {

    public static User currentUser;

    public static ArrayList<Integer> expandedItemsAvailable = new ArrayList<>();
    public static ArrayList<Integer> expandedItemsActive = new ArrayList<>();
    public static ArrayList<Integer> expandedItemsClosed = new ArrayList<>();

    /**
     * Метод, вызывающий информацию о приложении.
     * @param context Контекст вызывающей Activity.
     * @param t id типа заявки
     */
    public static String getTicketTypeName(Context context, int t){
        String[] groups = context.getResources().getStringArray(R.array.ticket_types_array);
        return groups[t - 10];
    }

    /**
     * Метод, вызывающий клавиатуру на EditText.
     * @param context Контекст вызывающей Activity.
     * @param editText EditText, на который вызывается клавиатура.
     */
    public static void showKeyboardOnEditText(Context context, EditText editText){
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Метод, вызывающий информацию о приложении.
     * @param context Контекст вызывающего класса.
     */
    public static void showAbout(Context context) {
        new MaterialDialog.Builder(context)
                .title("О программе")
                .content(String.format("Tech Support App V%s", context.getString(R.string.app_version)))
                .positiveText("Ок")
                .show();
    }

    /**
     * Метод, инициализирующий и отображающий долговременное, всплываюее сообщение.
     * @param context Контекст приложения.
     * @param message Сообщение.
     */
    public static void showLongTimeToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Метод, проверяющий слово на содержание только английских символов и цифр.
     * @param word проверяемое слово.
     */
    public static boolean isEnglishWord(String word){
        for (int i = 0; i < word.length(); i++)
            if (!isEnglishLetterOrDigit(word.charAt(i)))
                return false;
        return true;
    }

    private static boolean isEnglishLetterOrDigit(char c) {
        return 'A' <= c && c <= 'Z' || 'a' <= c && c <= 'z' || '0' <= c && c <= '9';
    }

    public static class ImageMethods {
        /**
         * Метод, создающий квадратную картинку первой
         * @param context Контекст Activity.
         * @param name Отображаемое имя.
         * @return Возвращает картинку (класс TextDrawable) с первой буквой по центру.
         */
        public static TextDrawable getSquareImage(Context context, String name) {
            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color = generator.getColor(name);

            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    .useFont(Typeface.createFromAsset(context.getAssets(), "OpenSans-Light.ttf"))
                    .bold()
                    .withBorder(2)
                    .endConfig()
                    .buildRect(String.valueOf(name.charAt(0)).toUpperCase(), color);
            return drawable;
        }

        /**
         * Метод, создающий круглую картинку первой
         * @param context Контекст Activity.
         * @param name Отображаемое имя.
         * @return Возвращает картинку (класс TextDrawable) с первой буквой по центру.
         */
        public static TextDrawable getRoundImage(Context context, String name) {
            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color = generator.getColor(name);

            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    .useFont(Typeface.createFromAsset(context.getAssets(), "OpenSans-Light.ttf"))
                    .bold()
                    .withBorder(2)
                    .endConfig()
                    .buildRound(String.valueOf(name.charAt(0)).toUpperCase(), color);
            return drawable;
        }
    }

    public static void logInfoAPK(Context context, String message){
        String activity = context.toString();
        activity = activity.substring(activity.lastIndexOf('.') + 1, activity.indexOf("@"));
        Log.i("APK001/" + activity, message);
    }

    public static class Downloads {

        public static class Users {

            /**
             * Метод, скачивающий из базы данных всех подтвержденных пользователей, независимо от прав.
             * @param dataSnapshot Снимок базы данных.
             * @return Всех подтвержденных пользователей.
             */
            public static ArrayList<User> getVerifiedUserList(DataSnapshot dataSnapshot) {
                ArrayList<User> resultList = new ArrayList<User>();
                resultList.addAll(getSpecificVerifiedUserList(dataSnapshot, DatabaseVariables.ExceptFolder.Users.DATABASE_VERIFIED_CHIEF_TABLE));
                resultList.addAll(getSpecificVerifiedUserList(dataSnapshot, DatabaseVariables.ExceptFolder.Users.DATABASE_VERIFIED_SIMPLE_USER_TABLE));
                resultList.addAll(getSpecificVerifiedUserList(dataSnapshot, DatabaseVariables.ExceptFolder.Users.DATABASE_VERIFIED_SPECIALIST_TABLE));
                resultList.addAll(getSpecificVerifiedUserList(dataSnapshot, DatabaseVariables.ExceptFolder.Users.DATABASE_VERIFIED_MANAGER_TABLE));
                return resultList;
            }

            /**
             * Метод, скачивающий из базы данных подтвержденных пользователей с определенными правами.
             * @param dataSnapshot Снимок базы данных.
             * @param databaseTablePath Путь в базе данных к необходимой категории подтвержденных пользователей.
             * @return Всех подтвержденных пользователей с определенными правами.
             */
            public static ArrayList<User> getSpecificVerifiedUserList(DataSnapshot dataSnapshot, String databaseTablePath) {
                ArrayList<User> resultList = new ArrayList<User>();
                for (DataSnapshot userRecord : dataSnapshot.child(databaseTablePath).getChildren())
                    resultList.add(userRecord.getValue(User.class));
                return resultList;
            }

        }

        public static class Strings {

            /**
             * Метод, скачивающий из базы данных логины неподтвержденных пользователей.
             * @param dataSnapshot Снимок базы данных.
             * @return Логины неподтвержденных пользователей.
             */
            public static ArrayList<String> getUnverifiedLogins(DataSnapshot dataSnapshot) {
                ArrayList<String> resultList = new ArrayList<String>();
                for (DataSnapshot userRecord : dataSnapshot.child(DatabaseVariables.ExceptFolder.Users.DATABASE_UNVERIFIED_USER_TABLE).getChildren())
                    resultList.add(userRecord.getValue(User.class).getLogin());
                return resultList;
            }

            /**
             * Метод, скачивающий из базы данных логины либо подтвержденных, либо неподтвержденных пользователей.
             * @param dataSnapshot Снимок базы данных.
             * @return Логины выбранной категории пользователей.
             */
            public static ArrayList<String> getVerifiedLogins(DataSnapshot dataSnapshot) {
                ArrayList<String> resultList = new ArrayList<String>();
                ArrayList<User> userList = getVerifiedUserList(dataSnapshot);
                for (User user : userList)
                    resultList.add(user.getLogin());
                return resultList;
            }

            /**
             * Метод, скачивающий из базы данных логины всех пользователей.
             * @param dataSnapshot Снимок базы данных.
             * @return Логины всех пользователей.
             */
            public static ArrayList<String> getAllLogins(DataSnapshot dataSnapshot) {
                ArrayList<String> resultList = getUnverifiedLogins(dataSnapshot);
                resultList.addAll(getVerifiedLogins(dataSnapshot));
                return resultList;
            }

            public static ArrayList<String> getUserMarkedTicketIDs(DataSnapshot dataSnapshot, String userId){
                ArrayList<String> resultList = new ArrayList<String>();
                for (DataSnapshot ticketRecord : dataSnapshot.child(DatabaseVariables.FullPath.Tickets.DATABASE_MARKED_TICKET_TABLE).getChildren()){
                    Ticket ticket = ticketRecord.getValue(Ticket.class);
                    if (ticket.getUserId().equals(userId))
                        resultList.add(ticket.getTicketId());
                }
                return resultList;
            }

        }

        public static class Tickets {

            /**
             * Метод, скачивающий из базы данных все заявки, ответственным за которые является определенный работник.
             * @param dataSnapshot Снимок базы данных.
             * @param overseerLogin Логин ответственного, заявки которого нужно получить.
             * @return Заявки определенного ответственного.
             */
            public static ArrayList<Ticket> getOverseerTicketList(DataSnapshot dataSnapshot, String overseerLogin, boolean exceptFolder) {
                ArrayList<Ticket> resultList = new ArrayList<Ticket>();
                if (exceptFolder) {
                    for (DataSnapshot ticketRecord : dataSnapshot.child(DatabaseVariables.ExceptFolder.Tickets.DATABASE_MARKED_TICKET_TABLE).getChildren()) {
                        Ticket ticket = ticketRecord.getValue(Ticket.class);
                        if (ticket.getSpecialistId().equals(overseerLogin))
                            resultList.add(ticket);
                    }
                } else {
                    for (DataSnapshot ticketRecord : dataSnapshot.child(DatabaseVariables.FullPath.Tickets.DATABASE_MARKED_TICKET_TABLE).getChildren()) {
                    Ticket ticket = ticketRecord.getValue(Ticket.class);
                    if (ticket.getSpecialistId().equals(overseerLogin))
                        resultList.add(ticket);
                    }
                }
                return resultList;
            }

            /**
             * Метод, скачивающий из базы данных заявки определнного пользователя, находящиеся в определенном состоянии.
             * @param dataSnapshot Снимок базы данных.
             * @param databaseTablePath Путь в базе данных к необходимой категории заявок.
             * @param userLogin Логин пользователя.
             * @return Заявки определенного пользователя, находящиеся в определенном состоянии.
             */
            public static ArrayList<Ticket> getUserSpecificTickets(DataSnapshot dataSnapshot, String databaseTablePath, String userLogin) {
                ArrayList<Ticket> resultList = new ArrayList<Ticket>();
                for (DataSnapshot ticketRecord : dataSnapshot.child(databaseTablePath).getChildren()) {
                    Ticket markedTicket = ticketRecord.getValue(Ticket.class);
                    if (markedTicket.getUserId().equals(userLogin))
                        resultList.add(markedTicket);
                }
                return resultList;
            }

            /**
             * Метод, скачивающий из базы данных все заявки, находящиеся в определенном состоянии.
             * @param dataSnapshot Снимок базы данных.
             * @param databaseTablePath Путь в базе данных к необходимой категории заявок.
             * @return Заявки, находящиеся в определенном состоянии.
             */
            public static ArrayList<Ticket> getSpecificTickets(DataSnapshot dataSnapshot, String databaseTablePath) {
                ArrayList<Ticket> resultList = new ArrayList<Ticket>();
                for (DataSnapshot ticketRecord : dataSnapshot.child(databaseTablePath).getChildren())
                    resultList.add(ticketRecord.getValue(Ticket.class));
                return resultList;
            }

            public static ArrayList<Ticket> getAllTickets(DataSnapshot dataSnapshot) {
                ArrayList<Ticket> resultList = getSpecificTickets(dataSnapshot, DatabaseVariables.ExceptFolder.Tickets.DATABASE_MARKED_TICKET_TABLE);
                resultList.addAll(getSpecificTickets(dataSnapshot, DatabaseVariables.ExceptFolder.Tickets.DATABASE_UNMARKED_TICKET_TABLE));
                resultList.addAll(getSpecificTickets(dataSnapshot, DatabaseVariables.ExceptFolder.Tickets.DATABASE_SOLVED_TICKET_TABLE));
                return resultList;
            }

        }

    }

}
