package com.techsupportapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.innodroid.expandablerecycler.ExpandableRecyclerAdapter;
import com.techsupportapp.AssignTicketActivity;
import com.techsupportapp.MessagingActivity;
import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.utility.DatabaseStorage;
import com.techsupportapp.utility.Globals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Адаптер для ExpandableRecyclerView с разделением заявок на категории
 * @author ahgpoug
 *
 *
 *
 * ДО ЧЕГО ЖЕ ТЫ ОТЧАЯЛСЯ, РАЗ ЗАШЕЛ СЮДА?
 *
 *
 *
 *
 */
public class TicketExpandableRecyclerAdapter extends ExpandableRecyclerAdapter<TicketExpandableRecyclerAdapter.TicketListItem> {
    private static final int TYPE_TICKET = 1001;

    public static final int TYPE_AVAILABLE = 1;
    public static final int TYPE_ACTIVE = 2;
    public static final int TYPE_CLOSED = 3;

    private Context context;
    private int type;

    /**
     * Передача информации в адаптер
     * @param type тип адаптера. TYPE_AVAILABLE - список доступных заявок. TYPE_ACTIVE - список заявок, закрытых текущим пользователм. TYPE_CLOSED - список всех закрытых заявок.
     * @param values список заявок, определенных типом списка (type)
     */
    public TicketExpandableRecyclerAdapter(int type, Context context, ArrayList<TicketListItem> values) {
        super(context);

        this.context = context;
        this.type = type;

        setItems(values);
    }

    /**
     * Метод запоминания раскрытых категорий
     */
    private void checkExpanded(){
        if (type == TYPE_AVAILABLE) {
            Globals.expandedItemsAvailable.clear();
            for (int i = 0; i < getItemCount(); i++)
                if (getItemViewType(i) == TYPE_HEADER && isExpanded(i))
                    Globals.expandedItemsAvailable.add(i);
        } else if (type == TYPE_ACTIVE){
            Globals.expandedItemsActive.clear();
            for (int i = 0; i < getItemCount(); i++)
                if (getItemViewType(i) == TYPE_HEADER && isExpanded(i))
                    Globals.expandedItemsActive.add(i);
        } else if (type == TYPE_CLOSED){
            Globals.expandedItemsClosed.clear();
            for (int i = 0; i < getItemCount(); i++)
                if (getItemViewType(i) == TYPE_HEADER && isExpanded(i))
                    Globals.expandedItemsClosed.add(i);
        }
    }


    /**
     * Метод, вызывающийся при раскрытии категории
     */
    @Override
    public void expandItems(int position, boolean notify) {
        super.expandItems(position, notify);
        checkExpanded();
    }

    /**
     * Метод, вызывающийся при закрытии категории
     */
    @Override
    public void collapseItems(int position, boolean notify) {
        super.collapseItems(position, notify);
        checkExpanded();
    }

    /**
     * Объект списка заявок
     */
    public static class TicketListItem extends ExpandableRecyclerAdapter.ListItem {
        private String text;
        private Ticket ticket;

        /**
         * Название категории
         */
        public TicketListItem(String group) {
            super(TYPE_HEADER);

            text = group;
        }

        /**
         * Заявка внутри категории
         */
        public TicketListItem(Ticket ticket) {
            super(TYPE_TICKET);

            this.ticket = ticket;
        }
    }

    /**
     * ViewHolder для заголовков
     */
    private class HeaderViewHolder extends ExpandableRecyclerAdapter.HeaderViewHolder {
        private TextView name;

        private HeaderViewHolder(View view) {
            super(view, (ImageView) view.findViewById(R.id.item_arrow));

            name = (TextView) view.findViewById(R.id.item_header_name);
        }

        public void bind(int position) {
            super.bind(position);

            name.setText(visibleItems.get(position).text);
        }
    }

    /**
     * ViewHolder для заявок
     */
    private class TicketViewHolder extends ExpandableRecyclerAdapter.ViewHolder {
        private TextView authorText;
        private TextView dateText;
        private TextView topicText;
        private TextView descText;
        private ImageView ticketImage;
        private TextView descLabel;

        private TicketViewHolder(View view) {
            super(view);

            ticketImage = (ImageView) view.findViewById(R.id.ticketImage);
            authorText = (TextView) view.findViewById(R.id.ticketAuthor);
            dateText = (TextView) view.findViewById(R.id.ticketDate);
            topicText = (TextView) view.findViewById(R.id.ticketTopic);
            descText = (TextView) view.findViewById(R.id.ticketDesc);
            descLabel = (TextView) view.findViewById(R.id.descLabel);
        }

        private void bind(int position) {
            final Ticket currentTicket = visibleItems.get(position).ticket;

            authorText.setText(currentTicket.getUserName());
            dateText.setText(currentTicket.getCreateDate());
            topicText.setText(currentTicket.getTopic());

            if (type == TYPE_ACTIVE || type == TYPE_CLOSED) {
                descLabel.setText("Консультант:");
                descText.setText(currentTicket.getSpecialistName());
            } else {
                descLabel.setVisibility(View.INVISIBLE);
                descText.setVisibility(View.INVISIBLE);
            }

            ticketImage.setImageDrawable(Globals.ImageMethods.getSquareImage(context, currentTicket.getTopic()));

            View rootView = ticketImage.getRootView();

            if (type == TYPE_ACTIVE){
                rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MaterialDialog.Builder(context)
                                .title(currentTicket.getTopic())
                                .content("Отозвать заявку?")
                                .positiveText("Отозвать")
                                .negativeText("Отмена")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        //TODO сделать отзыв заявки обратно в пул
                                        DatabaseStorage.updateLogFile(context, currentTicket.getTicketId(), DatabaseStorage.ACTION_WITHDRAWN, Globals.currentUser, null);
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
                });
            } else if (type == TYPE_AVAILABLE) {
                rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MaterialDialog.Builder(context)
                                .title(currentTicket.getTopic())
                                .content("Полное описание заявки:\n" + currentTicket.getMessage())
                                .positiveText("Распределить")
                                .negativeText("Отмена")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        Intent intent = new Intent(context, AssignTicketActivity.class);
                                        intent.putExtra("currentTicket", (Serializable) currentTicket);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
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
                });
            } else {
                rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        new MaterialDialog.Builder(context)
                                .title("Заявка " + currentTicket.getTicketId())
                                .items(new String[]{"Показать лог", "Показать историю чата"})
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        if (which == 0) {
                                            final MaterialDialog materialDialog = new MaterialDialog.Builder(context)
                                                    .title("Лог")
                                                    .content("Загрузка...")
                                                    .positiveText("Ок")
                                                    .show();

                                            final StorageReference storageReference = FirebaseStorage.getInstance().getReference("logs").child(currentTicket.getTicketId() + ".log");
                                            try {
                                                final File localFile = File.createTempFile(currentTicket.getTicketId(), "log");

                                                storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                        try {
                                                            FileInputStream fis = new FileInputStream(localFile);
                                                            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

                                                            String result = "";
                                                            String line = null;
                                                            while ((line = br.readLine()) != null) {
                                                                result += line;
                                                                result += "\n";
                                                            }
                                                            br.close();

                                                            result = result.substring(0, result.length()-2);
                                                            result = result.replaceAll(": ", ":\n");
                                                            materialDialog.setContent(result);
                                                        } catch (FileNotFoundException e){
                                                            e.printStackTrace();
                                                            materialDialog.setContent("Ошибка загрузки");
                                                        } catch (IOException e){
                                                            e.printStackTrace();
                                                            materialDialog.setContent("Ошибка загрузки");
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        materialDialog.setContent("Ошибка загрузки");
                                                    }
                                                });
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            Intent intent = new Intent(context, MessagingActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.putExtra("currentTicket", (Serializable) currentTicket);
                                            intent.putExtra("isActive", false);
                                            context.startActivity(intent);
                                        }

                                    }
                                })
                                .show();
                    }
                });
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderViewHolder(inflate(R.layout.item_ticket_header, parent));
            case TYPE_TICKET:
            default:
                return new TicketViewHolder(inflate(R.layout.item_ticket, parent));
        }
    }

    @Override
    public void onBindViewHolder(ExpandableRecyclerAdapter.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                ((HeaderViewHolder) holder).bind(position);
                break;
            case TYPE_TICKET:
            default:
                ((TicketViewHolder) holder).bind(position);
                break;
        }
    }
}
