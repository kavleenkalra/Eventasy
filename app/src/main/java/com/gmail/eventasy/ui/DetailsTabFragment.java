package com.gmail.eventasy.ui;


import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.eventasy.R;
import com.gmail.eventasy.Utility;
import com.gmail.eventasy.retrofit.model.Event;

import static com.gmail.eventasy.ImportantConstants.DETAIL_FORMAT;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsTabFragment extends Fragment {

    Event event;
    public DetailsTabFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments=getArguments();
        if(arguments!=null){
            event=arguments.getParcelable("EventDetailObject");
        }

        View rootView=inflater.inflate(R.layout.fragment_details_tab, container, false);
        TextView eventStartTimeTextView=(TextView)rootView.findViewById(R.id.event_detail_start_time);
        TextView eventVenueNameTextView=(TextView)rootView.findViewById(R.id.event_detail_venue_name);
        CardView eventDetailCardView=(CardView)rootView.findViewById(R.id.event_detail_card_view);
        TextView eventUrlTextView=(TextView)rootView.findViewById(R.id.event_detail_url);

        String venue=new String("Venue: ");
        if(event.getEventVenueName()!=null)
            venue=venue+event.getEventVenueName()+" ";
        if(event.getEventAddress()!=null)
            venue=venue+event.getEventAddress()+" ";
        if(event.getEventCity()!=null)
            venue=venue+event.getEventCity()+",";
        if(event.getEventCountry()!=null)
            venue=venue+event.getEventCountry();
        if(event.getEventPostalCode()!=null)
            venue=venue+"\nPostal Code: "+event.getEventPostalCode();


        if(event!=null){
            if(event.getEventStartTime()!=null)
                eventStartTimeTextView.setText(Utility.getFormattedDate(event.getEventStartTime(),DETAIL_FORMAT)+" at "+Utility.getFormattedTime(event.getEventStartTime()));
            eventVenueNameTextView.setText(venue);
            if(event.getEventUrl()!=null){
                eventUrlTextView.setText(getString(R.string.event_link));
                eventUrlTextView.setPaintFlags(eventUrlTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
                eventDetailCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(event.getEventUrl()));
                        startActivity(intent);
                    }
                });
            }

        }
        return rootView;
    }
}