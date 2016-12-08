package com.techsupportapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.techsupportapp.MessagingActivity;
import com.techsupportapp.R;
import com.techsupportapp.adapters.TicketRecyclerAdapter;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseStorage;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;
import com.techsupportapp.utility.ItemClickSupport;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Класс для фрагментов ViewPager, находящегося в MyTicketsActivity.class.
 * @author ahgpoug
 */
public class MyTicketsFragments {
    /**
     * Адаптер для фрагментов
     */
    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        private FirstFragment firstFragment;
        private SecondFragment secondFragment;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Метод, загружающий новые фрагменты по их номеру
         */
        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return FirstFragment.newInstance();
            else
                return SecondFragment.newInstance();
        }

        //Получение числа фрагментов
        @Override
        public int getCount() {
            return 2;
        }

        /**
         * Установка заголовков в TabLayout для фрагментов
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Активные";
                case 1:
                    return "Закрытые";
            }
            return null;
        }

        /**
         * Метод для запоминания ссылок на фрагменты
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            switch (position) {
                case 0:
                    firstFragment = (FirstFragment) createdFragment;
                    break;
                case 1:
                    secondFragment = (SecondFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }

        /**
         * Метод для обновления информации на первом фрагменте.
         * @param ticketsList список заявок, активных у текущего пользователя.
         * @param usersList список пользователей с подтвержденной регистрацией.
         * @param context контекст Activity, где был создан фрагмент.
         */
        public boolean updateFirstFragment(Context context, ArrayList<Ticket> ticketsList, ArrayList <User> usersList){
            return firstFragment.updateContent(context, ticketsList, usersList);
        }

        /**
         * Метод для обновления информации на первом фрагменте.
         * @param ticketsList список заявок, закрытых текущим пользователем.
         * @param usersList список пользователей с подтвержденной регистрацией.
         * @param context контекст Activity, где был создан фрагмент.
         */
        public void updateSecondFragment(Context context, ArrayList<Ticket> ticketsList, ArrayList <User> usersList){
            secondFragment.updateContent(context, ticketsList, usersList);
        }
    }

    /**
     * Фрагемент списка пользователей с неподтвержденной регистрацией.
     */
    public static class FirstFragment extends Fragment {
        RecyclerView activeTicketsView;
        boolean result;

        /**
         * Метод, вызывающийся при создании фрагмента
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);

            activeTicketsView = (RecyclerView) v.findViewById(R.id.recycler);

            return v;
        }

        /**
         * Метод для обновления информации на первом фрагменте.
         * @param ticketsList список заявок, активных у текущего пользователя.
         * @param usersList список пользователей с подтвержденной регистрацией.
         * @param context контекст Activity, где был создан фрагмент.
         */
        public boolean updateContent(final Context context, final ArrayList<Ticket> ticketsList, ArrayList<User> usersList) {
            final int role = Globals.currentUser.getRole();
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            activeTicketsView.setLayoutManager(mLayoutManager);
            activeTicketsView.setHasFixedSize(false);

            //Создание нового адаптера для activeTicketsView
            TicketRecyclerAdapter adapter = new TicketRecyclerAdapter(context, ticketsList, usersList, getFragmentManager());
            activeTicketsView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            ItemClickSupport.addTo(activeTicketsView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    Intent intent = new Intent(context, MessagingActivity.class);
                    if (role != User.SIMPLE_USER) {
                        intent.putExtra("currentTicket", (Serializable) ticketsList.get(position));
                        intent.putExtra("isActive", true);
                    }
                    else {
                        if (ticketsList.get(position).getSpecialistId() == null || ticketsList.get(position).getSpecialistId().equals("")) {
                            Toast.makeText(context, "Администратор еще не просматривал ваше сообщение, пожалуйста подождите", Toast.LENGTH_LONG).show();
                            return;
                        }
                        else {
                            intent.putExtra("currentTicket", (Serializable) ticketsList.get(position));
                            intent.putExtra("isActive", true);
                        }
                    }
                    startActivity(intent);
                }
            });

            ItemClickSupport.addTo(activeTicketsView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener () {
                @Override
                public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                    if (role == User.SIMPLE_USER){
                        final Ticket selectedTicket = ticketsList.get(position);
                        if (selectedTicket.getSpecialistId() == null || selectedTicket.getSpecialistId().equals("")) {
                            new MaterialDialog.Builder(context)
                                    .title("Отзыв заявки " + selectedTicket.getTicketId() + " от " + selectedTicket.getCreateDate())
                                    .content("Вы действительно хотите отозвать данную заявку?")
                                    .positiveText("Да")
                                    .negativeText("Нет")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            DatabaseReference databaseTicketReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Tickets.DATABASE_ALL_TICKET_TABLE);
                                            databaseTicketReference.child(DatabaseVariables.ExceptFolder.Tickets.DATABASE_UNMARKED_TICKET_TABLE).child(selectedTicket.getTicketId()).removeValue();
                                            DatabaseStorage.updateLogFile(context, selectedTicket.getTicketId(), DatabaseStorage.ACTION_CLOSED, Globals.currentUser, null);
                                        }
                                    })
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.cancel();
                                        }
                                    })
                                    .show();
                        }
                        else {
                            new MaterialDialog.Builder(context)
                                    .title("Подтверждение решения проблемы по заявке " + selectedTicket.getTicketId() + " от " + selectedTicket.getCreateDate())
                                    .content("Ваша проблема действительно была решена?")
                                    .positiveText("Да")
                                    .negativeText("Нет")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            DatabaseReference databaseTicketReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Tickets.DATABASE_ALL_TICKET_TABLE);
                                            databaseTicketReference.child(DatabaseVariables.ExceptFolder.Tickets.DATABASE_SOLVED_TICKET_TABLE).child(selectedTicket.getTicketId()).setValue(selectedTicket);
                                            databaseTicketReference.child(DatabaseVariables.ExceptFolder.Tickets.DATABASE_MARKED_TICKET_TABLE).child(selectedTicket.getTicketId()).removeValue();
                                            DatabaseStorage.updateLogFile(context, selectedTicket.getTicketId(), DatabaseStorage.ACTION_SOLVED, Globals.currentUser, null);
                                        }
                                    })
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.cancel();
                                        }
                                    })
                                    .show();
                        }
                    }
                    return true;
                }
            });

            return result;
        }

        /**
         * Создание нового экземпляра текущего фрагемента
         */
        public static FirstFragment newInstance() {
            FirstFragment f = new FirstFragment();
            return f;
        }
    }

    /**
     * Фрагемент списка всех пользователей
     */
    public static class SecondFragment extends Fragment {
        RecyclerView myClosedTicketsView;

        /**
         * Метод, вызывающийся при создании фрагмента
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_recycler, container, false);

            myClosedTicketsView = (RecyclerView) v.findViewById(R.id.recycler);

            return v;
        }

        /**
         * Метод для обновления информации на первом фрагменте.
         * @param ticketsList список заявок, закрытых текущим пользователем.
         * @param usersList список пользователей с подтвержденной регистрацией.
         * @param context контекст Activity, где был создан фрагмент.
         */
        public void updateContent(final Context context, final ArrayList<Ticket> ticketsList, ArrayList<User> usersList){
            final int role = Globals.currentUser.getRole();
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            myClosedTicketsView.setLayoutManager(mLayoutManager);
            myClosedTicketsView.setHasFixedSize(false);

            TicketRecyclerAdapter adapter = new TicketRecyclerAdapter(context, ticketsList, usersList, getFragmentManager());
            myClosedTicketsView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            ItemClickSupport.addTo(myClosedTicketsView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    Intent intent = new Intent(context, MessagingActivity.class);
                    if (role != User.SIMPLE_USER) {
                        intent.putExtra("currentTicket", (Serializable) ticketsList.get(position));
                        intent.putExtra("isActive", false);
                    } else {
                        intent.putExtra("currentTicket", (Serializable) ticketsList.get(position));
                        intent.putExtra("isActive", false);
                    }
                    startActivity(intent);
                }
            });
        }

        /**
         * Создание нового экземпляра текущего фрагемента
         */
        public static SecondFragment newInstance() {
            SecondFragment f = new SecondFragment();
            return f;
        }
    }
}
