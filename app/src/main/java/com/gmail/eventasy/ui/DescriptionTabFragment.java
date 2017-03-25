package com.gmail.eventasy.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.eventasy.R;
import com.gmail.eventasy.retrofit.model.Event;

/**
 * A simple {@link Fragment} subclass.
 */
public class DescriptionTabFragment extends Fragment {

    Event event;

    public DescriptionTabFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments=getArguments();
        if(arguments!=null){
            event=arguments.getParcelable("EventDetailObject");
        }

        View rootView=inflater.inflate(R.layout.fragment_description_tab, container, false);
        TextView eventDescriptionTextView=(TextView)rootView.findViewById(R.id.event_description_text_view);

        if(event!=null){
            if(event.getEventDescription()!=null)
                eventDescriptionTextView.setText(Html.fromHtml(event.getEventDescription()));
            else
                eventDescriptionTextView.setText(getString(R.string.description_not_available));
        }
        return rootView;
    }
}
