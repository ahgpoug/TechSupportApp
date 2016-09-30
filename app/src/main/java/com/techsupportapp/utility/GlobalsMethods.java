package com.techsupportapp.utility;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v7.app.AlertDialog;
import android.view.inputmethod.InputMethodManager;

public class GlobalsMethods {

    /**
     * Метод, вызывающий информацию о приложении.
     * @param context Контекст вызывающего класса.
     */
    public static String currUserId;

    public static int isCurrentAdmin;

    public static void showAbout(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("О программе");
        String str = String.format("Tech Support App V1.0");
        builder.setMessage(str);
        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Метод, скрывающий клавиатуру.
     * @param activity
     */
    public static void hideKeyboard(Activity activity) {
        if (activity == null || activity.getCurrentFocus() == null) {
            return;
        }

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

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
         * Метод, создающий картинку пользователя с первой буквой его имени по центру.
         * @param name Отображаемое имя пользователя.
         * @param context Контекст вызывающего класса.
         * @return Возвращает картинку (класс Bitmap) с первой буквой имени пользователя по центру.
         */
        public static Bitmap createUserImage(String name, Context context) {
            int COVER_IMAGE_SIZE = 100;
            LetterBitmap letterBitmap = new LetterBitmap(context);
            Bitmap letterTile = letterBitmap.getLetterTile(name.substring(0), name, COVER_IMAGE_SIZE, COVER_IMAGE_SIZE);
            return (letterTile);
        }

        /**
         * Метод, задающий округлую форму картинки.
         * @param bitmap Изображение, форму которого необходимо изменить.
         * @return Возвращает закругленную картинку (класс Bitmap).
         */
        public static Bitmap getclip(Bitmap bitmap) {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                    bitmap.getWidth() / 2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            return output;
        }
    }
}
