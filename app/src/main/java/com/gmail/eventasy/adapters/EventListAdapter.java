package com.gmail.eventasy.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.eventasy.R;
import com.gmail.eventasy.Utility;
import com.gmail.eventasy.retrofit.model.Event;
import com.gmail.eventasy.retrofit.model.EventResponse;
import com.gmail.eventasy.ItemClickListener;
import com.squareup.picasso.Picasso;

import static com.gmail.eventasy.ImportantConstants.NORMAL_FORMAT;

/**
 * Created by kakalra on 12/26/2016.
 */

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventListViewHolder>{

    private Context context;
    EventResponse eventResponse;
    private ItemClickListener clickListener;

    public EventListAdapter(Context context,EventResponse eventResponse){
        this.context=context;
        this.eventResponse=eventResponse;
    }

    public void setClickListener(ItemClickListener itemClickListener){
        this.clickListener=itemClickListener;
    }

    public class EventListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        CardView cardView;
        ImageView eventListImageView;
        TextView eventDateView;
        TextView eventTitleTextView;
        TextView eventDateTimeVenueTextView;

        EventListViewHolder(View itemView){
            super(itemView);
            cardView=(CardView)itemView.findViewById(R.id.event_list_card_view);
            eventListImageView=(ImageView)itemView.findViewById(R.id.event_list_image_view);
            eventDateView=(TextView)itemView.findViewById(R.id.event_date_view);
            eventTitleTextView=(TextView)itemView.findViewById(R.id.event_title_text_view);
            eventDateTimeVenueTextView=(TextView)itemView.findViewById(R.id.event_date_time_venue_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener!=null){
                clickListener.onClick(view,getAdapterPosition());
            }
        }
    }

    @Override
    public int getItemCount() {
        return (eventResponse == null? 0:eventResponse.getAllEvents().getEventList().size());
    }

    @Override
    public EventListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item,parent,false);
        EventListViewHolder eventListViewHolder=new EventListViewHolder(v);
        return eventListViewHolder;
    }

    @Override
    public void onBindViewHolder(final EventListAdapter.EventListViewHolder holder, int position) {
        Event event = eventResponse.getAllEvents().getEventList().get(position);
        if (event != null) {
            if (event.getEventImage() != null && event.getEventImage().getBlock200Image() != null)
                Picasso.with(context)
                        .load(event.getEventImage().getBlock200Image().getImageUrl())
                        .placeholder(R.drawable.place_holder)
                        .error(R.drawable.error_image)
                        .into(holder.eventListImageView);
            if(event.getEventStartTime()!=null)
                holder.eventDateView.setText(Utility.getFormattedDate(event.getEventStartTime(),NORMAL_FORMAT));
            if (event.getEventTitle() != null)
                holder.eventTitleTextView.setText(event.getEventTitle());
            if (event.getEventStartTime() != null && event.getEventVenueName() != null)
                holder.eventDateTimeVenueTextView.setText("Venue : " + event.getEventVenueName());
        }
    }
}
