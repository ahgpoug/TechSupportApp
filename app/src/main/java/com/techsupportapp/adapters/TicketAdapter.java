package com.techsupportapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.techsupportapp.R;
import com.techsupportapp.UserProfileActivity;
import com.techsupportapp.databaseClasses.Ticket;
import com.techsupportapp.databaseClasses.User;
import com.techsupportapp.utility.GlobalsMethods;

import java.util.ArrayList;

public class TicketAdapter extends ArrayAdapter<Ticket> {

    private final Context context;
    private final ArrayList<Ticket> values;

    public TicketAdapter(Context context, ArrayList<Ticket> values) {
        super(context, R.layout.item_ticket, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String titleText;
        final String userId;
        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_ticket, parent, false);
        ImageView ticketImage = (ImageView) rowView.findViewById(R.id.ticketImage);
        final TextView authorText = (TextView)rowView.findViewById(R.id.ticketAuthor);
        TextView dateText = (TextView)rowView.findViewById(R.id.ticketDate);
        TextView topicText = (TextView)rowView.findViewById(R.id.ticketTopic);
        TextView descText = (TextView)rowView.findViewById(R.id.ticketDesc);

        userId = values.get(position).getUserId();
        if (values.get(position).getAdminId() == null || values.get(position).getAdminId().equals("")) {
            if (GlobalsMethods.isCurrentAdmin == User.ADMINISTRATOR)
                authorText.setText(values.get(position).getUserId());
            else
                authorText.setText("Не установлено");

            titleText = authorText.getText().toString();
        }
        else {
            if (GlobalsMethods.isCurrentAdmin == User.ADMINISTRATOR) {
                authorText.setText(values.get(position).getUserId() + " ✔");
                titleText = values.get(position).getUserId();
            }
            else {
                authorText.setText(values.get(position).getAdminId() + " ✔");
                titleText = values.get(position).getAdminId();
            }
        }

        dateText.setText(values.get(position).getCreateDate());
        topicText.setText(values.get(position).getTopic());
        descText.setText(values.get(position).getMessage());
        ticketImage.setImageBitmap(GlobalsMethods.ImageMethods.createUserImage(titleText, context));

        ticketImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean fl = true;
                Intent intent = new Intent(context, UserProfileActivity.class);
                if (GlobalsMethods.isCurrentAdmin == User.ADMINISTRATOR)
                    intent.putExtra("userId", userId);
                else
                    if (titleText.equals("Не установлено"))
                        fl = false;
                    else
                        intent.putExtra("userId", titleText);

                intent.putExtra("currUserId", GlobalsMethods.currUserId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (fl)
                    context.startActivity(intent);
            }
        });
        return rowView;
    }
}
