package com.techsupportapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.innodroid.expandablerecycler.ExpandableRecyclerAdapter;
import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.ChatMessage;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.DatabaseStorage;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;

import java.util.ArrayList;

/**
 * Адаптер для ExpandableRecyclerView
 * @author ahgpoug
 */
public class SpecialistsExpandableRecyclerAdapter extends ExpandableRecyclerAdapter<SpecialistsExpandableRecyclerAdapter.TicketListItem> {
    private static final int TYPE_TICKET = 1001;

    private Context context;
    private Ticket currentTicket;

    /**
     * Передача информации в адаптер
     * @param values значения
     */
    public SpecialistsExpandableRecyclerAdapter(Context context, ArrayList<TicketListItem> values, Ticket currentTicket) {
        super(context);

        this.context = context;
        this.currentTicket = currentTicket;

        setItems(values);
    }

    /**
     * Объект списка заявок
     */
    public static class TicketListItem extends ExpandableRecyclerAdapter.ListItem {
        private User user;
        private Ticket ticket;

        /**
         * Пользователь
         */
        public TicketListItem(User user) {
            super(TYPE_HEADER);

            this.user = user;
        }

        /**
         * Заявка каждого пользователя
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

            final User selectedUser = visibleItems.get(position).user;

            name.setText(selectedUser.getUserName());

            View rootView = name.getRootView();

            rootView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new MaterialDialog.Builder(context)
                            .title("Направить заявку пользователю " + selectedUser.getUserName() + "?")
                            .positiveText("Назначить")
                            .negativeText("Отмена")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    currentTicket.addSpecialist(selectedUser.getLogin(), selectedUser.getUserName());

                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                                    databaseReference.child(DatabaseVariables.FullPath.Tickets.DATABASE_MARKED_TICKET_TABLE).child(currentTicket.getTicketId()).setValue(currentTicket);
                                    databaseReference.child(DatabaseVariables.FullPath.Tickets.DATABASE_UNMARKED_TICKET_TABLE).child(currentTicket.getTicketId()).removeValue();

                                    //Отправка полного описания заявки в виде первого сообщения в чате
                                    ChatMessage firstMessage = new ChatMessage(currentTicket.getTopic() + ":\n\n" + currentTicket.getMessage(), currentTicket.getUserName(), currentTicket.getUserId(), "", false);
                                    FirebaseDatabase.getInstance().getReference("chat").child(currentTicket.getTicketId()).push().setValue(firstMessage);

                                    DatabaseStorage.updateLogFile(context, currentTicket.getTicketId(), DatabaseStorage.ACTION_ACCEPTED, Globals.currentUser, selectedUser.getUserName());
                                    ((Activity) context).finish();
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                    return true;
                }
            });
        }
    }

    /**
     * ViewHolder для заявок
     */
    private class TicketViewHolder extends ViewHolder {
        private TextView authorText;
        private TextView dateText;
        private TextView topicText;
        private TextView descText;
        private ImageView ticketImage;

        private TicketViewHolder(View view) {
            super(view);

            ticketImage = (ImageView) view.findViewById(R.id.ticketImage);
            authorText = (TextView) view.findViewById(R.id.ticketAuthor);
            dateText = (TextView) view.findViewById(R.id.ticketDate);
            topicText = (TextView) view.findViewById(R.id.ticketTopic);
            descText = (TextView) view.findViewById(R.id.ticketDesc);
        }

        private void bind(int position) {
            final Ticket currentTicket = visibleItems.get(position).ticket;

            authorText.setText(currentTicket.getUserName());

            dateText.setText(currentTicket.getCreateDate());
            topicText.setText(currentTicket.getTopic());
            descText.setText(currentTicket.getMessage());

            ticketImage.setImageDrawable(Globals.ImageMethods.getSquareImage(context, currentTicket.getTopic()));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderViewHolder(inflate(R.layout.item_user_header, parent));
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
