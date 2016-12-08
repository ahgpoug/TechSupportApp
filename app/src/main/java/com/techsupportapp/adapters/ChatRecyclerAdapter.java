package com.techsupportapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.techsupportapp.R;
import com.techsupportapp.databaseClasses.ChatMessage;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.utility.DatabaseStorage;
import com.techsupportapp.utility.DatabaseVariables;
import com.techsupportapp.utility.Globals;

import java.util.ArrayList;
import java.util.List;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LEFT = 0;
    private static final int TYPE_RIGHT = 1;
    private static final int TYPE_REQUEST_IN = 2;
    private static final int TYPE_REQUEST_OUT = 3;

    private Query mRef;
    private Ticket selectedTicket;
    private Context context;
    private List<ChatMessage> chatMessages;
    private List<String> keys;
    private ChildEventListener childEventListener;

    public ChatRecyclerAdapter(final Query mRef, final Context context, final Ticket selectedTicket) {
        this.mRef = mRef;
        this.context = context;
        this.selectedTicket = selectedTicket;
        chatMessages = new ArrayList<>();
        keys = new ArrayList<>();

        childEventListener = this.mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                String key = dataSnapshot.getKey();

                if ((chatMessage.isUnread() && !(chatMessage.getUserId().equals(Globals.currentUser.getLogin()))))
                    mRef.getRef().child(key).child("unread").setValue(false);

                if (previousChildName == null) {
                    chatMessages.add(0, chatMessage);
                    keys.add(0, key);
                } else {
                    int previousIndex = keys.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == chatMessages.size()) {
                        chatMessages.add(chatMessage);
                        keys.add(key);
                    } else {
                        chatMessages.add(nextIndex, chatMessage);
                        keys.add(nextIndex, key);
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                ChatMessage newChatMessage = dataSnapshot.getValue(ChatMessage.class);
                int index = keys.indexOf(key);

                chatMessages.set(index, newChatMessage);

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                int index = keys.indexOf(key);

                keys.remove(index);
                chatMessages.remove(index);

                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                String key = dataSnapshot.getKey();
                ChatMessage newChatMessage = dataSnapshot.getValue(ChatMessage.class);
                int index = keys.indexOf(key);
                chatMessages.remove(index);
                keys.remove(index);
                if (previousChildName == null) {
                    chatMessages.add(0, newChatMessage);
                    keys.add(0, key);
                } else {
                    int previousIndex = keys.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == chatMessages.size()) {
                        chatMessages.add(newChatMessage);
                        keys.add(key);
                    } else {
                        chatMessages.add(nextIndex, newChatMessage);
                        keys.add(nextIndex, key);
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ChatRecyclerAdapter", "Listen was cancelled, no more updates will occur");
            }

        });
    }

    private class ViewHolderMessage extends RecyclerView.ViewHolder {
        private TextView authorText;
        private TextView messageText;
        private TextView messageTime;
        private TextView unread;
        private ImageView userImage;

        private ViewHolderMessage(View view) {
            super(view);
            authorText = (TextView) view.findViewById(R.id.messageAuthor);
            messageText = (TextView) view.findViewById(R.id.messageText);
            messageTime = (TextView) view.findViewById(R.id.messageTime);
            unread = (TextView) view.findViewById(R.id.unread);
            userImage = (ImageView) view.findViewById(R.id.userImage);
        }
    }

    private class ViewHolderRequestIn extends RecyclerView.ViewHolder {
        private TextView requestText;
        private Button rejectRequest;
        private Button acceptRequest;

        private ViewHolderRequestIn(View view) {
            super(view);
            requestText = (TextView) view.findViewById(R.id.requestText);
            rejectRequest = (Button) view.findViewById(R.id.rejectRequest);
            acceptRequest = (Button) view.findViewById(R.id.acceptRequest);
        }
    }

    private class ViewHolderRequestOut extends RecyclerView.ViewHolder {
        private TextView requestText;

        private ViewHolderRequestOut(View view) {
            super(view);
            requestText = (TextView) view.findViewById(R.id.requestText);
        }
    }

    public void cleanup() {
        mRef.removeEventListener(childEventListener);
        chatMessages.clear();
        keys.clear();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch(viewType) {
            case TYPE_LEFT:
                return new ViewHolderMessage(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false));
            case TYPE_RIGHT:
                return new ViewHolderMessage(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false));
            case TYPE_REQUEST_IN:
                return new ViewHolderRequestIn(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_request_in, parent, false));
            case TYPE_REQUEST_OUT:
                return new ViewHolderRequestOut(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_request_out, parent, false));
            default:
                return new ViewHolderMessage(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        try {
            if (viewHolder.getItemViewType() == TYPE_LEFT || viewHolder.getItemViewType() == TYPE_RIGHT) {
                ViewHolderMessage holder = (ViewHolderMessage) viewHolder;
                holder.authorText.setText(chatMessage.getAuthor() + ": ");
                holder.messageText.setText(chatMessage.getMessage());
                holder.messageTime.setText(chatMessage.getMessageTime());

                if (chatMessage.isUnread())
                    holder.unread.setText("Непрочит.");
                else
                    holder.unread.setText("");

                holder.userImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO что-то сделать
                    }
                });
                holder.userImage.setImageDrawable(Globals.ImageMethods.getRoundImage(context, chatMessage.getAuthor()));
            } else if (viewHolder.getItemViewType() == TYPE_REQUEST_IN){
                final ViewHolderRequestIn holder = (ViewHolderRequestIn) viewHolder;
                if (chatMessage.getMessage().equals("not answered")) {
                    holder.requestText.setText("Получен запрос на закрытие заявки");
                    holder.rejectRequest.setVisibility(View.VISIBLE);
                    holder.acceptRequest.setVisibility(View.VISIBLE);
                    holder.rejectRequest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mRef.getRef().child(keys.get(position)).child("message").setValue("rejected");
                            holder.rejectRequest.setText("Запрос отклонен");
                            holder.rejectRequest.setVisibility(View.GONE);
                            holder.acceptRequest.setVisibility(View.GONE);
                            DatabaseStorage.updateLogFile(context, mRef.getRef().getKey(), DatabaseStorage.ACTION_REQUEST_REJECTED, Globals.currentUser, null);
                        }
                    });

                    holder.acceptRequest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mRef.getRef().child(keys.get(position)).child("message").setValue("accepted");
                            holder.rejectRequest.setText("Запрос принят");
                            holder.rejectRequest.setVisibility(View.GONE);
                            holder.acceptRequest.setVisibility(View.GONE);

                            DatabaseReference databaseTicketReference = FirebaseDatabase.getInstance().getReference(DatabaseVariables.FullPath.Tickets.DATABASE_ALL_TICKET_TABLE);
                            databaseTicketReference.child(DatabaseVariables.ExceptFolder.Tickets.DATABASE_SOLVED_TICKET_TABLE).child(selectedTicket.getTicketId()).setValue(selectedTicket);
                            databaseTicketReference.child(DatabaseVariables.ExceptFolder.Tickets.DATABASE_MARKED_TICKET_TABLE).child(selectedTicket.getTicketId()).removeValue();
                            DatabaseStorage.updateLogFile(context, selectedTicket.getTicketId(), DatabaseStorage.ACTION_SOLVED, Globals.currentUser, null);

                            DatabaseStorage.updateLogFile(context, mRef.getRef().getKey(), DatabaseStorage.ACTION_REQUEST_ACCEPTED, Globals.currentUser, null);

                            ((Activity) context).finish();
                        }
                    });
                } else {
                    if (chatMessage.getMessage().equals("accepted"))
                        holder.requestText.setText("Запрос принят");
                    else
                        holder.requestText.setText("Запрос отклонен");

                    holder.rejectRequest.setVisibility(View.GONE);
                    holder.acceptRequest.setVisibility(View.GONE);
                }
            } else {
                final ViewHolderRequestOut holder = (ViewHolderRequestOut) viewHolder;
                if (chatMessage.getMessage().equals("not answered"))
                    holder.requestText.setText("Запрос отправлен");
                else if (chatMessage.getMessage().equals("rejected"))
                    holder.requestText.setText("Запрос отклонен");
                else
                    holder.requestText.setText("Запрос принят");
            }
        }
        catch (Exception e) {
            Globals.showLongTimeToast(context, e.getMessage() + "Обратитесь к разработчику");
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position)
    {
        ChatMessage chatMessage = chatMessages.get(position);
        if (!(chatMessage.getUserId().equals(Globals.currentUser.getLogin())) && !chatMessage.getAuthor().equals("System"))
            return TYPE_LEFT;
        else if ((chatMessage.getUserId().equals(Globals.currentUser.getLogin())) && !chatMessage.getAuthor().equals("System"))
            return TYPE_RIGHT;
        else if (!(chatMessage.getUserId().equals(Globals.currentUser.getLogin()) && chatMessage.getAuthor().equals("System")))
            return TYPE_REQUEST_IN;
        else
            return TYPE_REQUEST_OUT;
    }
}
