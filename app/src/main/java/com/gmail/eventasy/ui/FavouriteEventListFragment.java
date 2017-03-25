package com.gmail.eventasy.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.gmail.eventasy.ImportantConstants;
import com.gmail.eventasy.R;
import com.gmail.eventasy.adapters.FavouriteEventCursorAdapter;
import com.gmail.eventasy.data.EventContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavouriteEventListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    ListView favouriteEventListView;
    TextView emptyView;
    FavouriteEventCursorAdapter adapter;

    public FavouriteEventListFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_favourite_event_list, container, false);
        favouriteEventListView=(ListView)rootView.findViewById(R.id.favourite_list_view);
        emptyView=(TextView)rootView.findViewById(R.id.empty_favourite_view);

        Cursor cursor=getActivity().getContentResolver().query(EventContract.EventEntry.CONTENT_URI,null,null,null,null);
        adapter=new FavouriteEventCursorAdapter(getContext(),cursor);
        favouriteEventListView.setAdapter(adapter);

        favouriteEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor c=adapter.getCursor();
                c.moveToPosition(i);
                Intent intent=new Intent(getActivity(),EventDetailActivity.class);
                intent.putExtra("CallingActivity", ImportantConstants.FAVOURITE_ACTIVITY);
                intent.putExtra("EventId",c.getString(c.getColumnIndexOrThrow("event_id")));
                intent.putExtra("EventImageUrl",c.getString(c.getColumnIndexOrThrow("event_large_image")));
                intent.putExtra("EventTitle",c.getString(c.getColumnIndexOrThrow("event_title")));
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                else
                    startActivity(intent);
            }
        });

        getActivity().getSupportLoaderManager().initLoader(0,null,this);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri= EventContract.EventEntry.CONTENT_URI;
        return new CursorLoader(getActivity(),uri,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount()==0)
        {
            if(favouriteEventListView.getVisibility()==View.VISIBLE)
                favouriteEventListView.setVisibility(View.GONE);
            if(emptyView!=null && emptyView.getVisibility()==View.GONE){
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(R.string.no_favorites_added);
            }
        }
        else{
            if(favouriteEventListView.getVisibility()==View.GONE)
                favouriteEventListView.setVisibility(View.VISIBLE);
            if(emptyView!=null && emptyView.getVisibility()==View.VISIBLE)
                emptyView.setVisibility(View.GONE);
        }
        if(adapter!=null && data!=null)
            adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(adapter!=null)
            adapter.swapCursor(null);
    }
}
