package com.gmail.eventasy.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.eventasy.BuildConfig;
import com.gmail.eventasy.EndlessRecyclerViewScrollListener;
import com.gmail.eventasy.ImportantConstants;
import com.gmail.eventasy.ItemClickListener;
import com.gmail.eventasy.R;
import com.gmail.eventasy.Utility;
import com.gmail.eventasy.adapters.EventListAdapter;
import com.gmail.eventasy.retrofit.model.Category;
import com.gmail.eventasy.retrofit.model.Event;
import com.gmail.eventasy.retrofit.model.EventResponse;
import com.gmail.eventasy.retrofit.rest.ApiClient;
import com.gmail.eventasy.retrofit.rest.ApiInterface;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static com.gmail.eventasy.ImportantConstants.IMAGE_SIZE;
import static com.gmail.eventasy.ImportantConstants.LANDSCAPE_COLUMNS;
import static com.gmail.eventasy.ImportantConstants.PORTRAIT_COLUMNS;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventListFragment extends Fragment implements ItemClickListener,SharedPreferences.OnSharedPreferenceChangeListener{

    int searchRadius;
    int maxPageNumber;
    String location;
    String sortOrder;

    RecyclerView eventListStaggeredView;
    TextView emptyView;
    ProgressBar spinner;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    EventListAdapter eventListAdapter;
    EventResponse eventResponse;
    Category categoryObj;
    SharedPreferences sharedPreferences;

    private EndlessRecyclerViewScrollListener scrollListener;

    public EventListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null || !savedInstanceState.containsKey("EventList")){
            eventResponse=new EventResponse();
            maxPageNumber=0;
        }
        else{
            eventResponse=savedInstanceState.getParcelable("EventList");
            maxPageNumber=savedInstanceState.getInt("MaxPageNumber");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //events are fetched only if the current list of events is empty.
        if(eventResponse.getAllEvents().getEventList().size()==0){
            if(Utility.isNetworkAvailable(getContext())){
                if(categoryObj!=null)
                    fetchEventList(categoryObj.getCategoryId(),1);
            }
            else{
                eventListStaggeredView.setVisibility(GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(getString(R.string.no_network_available));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("EventList",eventResponse);
        outState.putInt("MaxPageNumber",maxPageNumber);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Bundle arguments=getArguments();
        if(arguments!=null){
            categoryObj=arguments.getParcelable("CategoryObject");
        }

        View rootView=inflater.inflate(R.layout.fragment_event_list, container, false);
        initViews(rootView);

        sharedPreferences=PreferenceManager .getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        eventListStaggeredView.setHasFixedSize(true);
        int noOfColumns=getActivity().getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT?PORTRAIT_COLUMNS:LANDSCAPE_COLUMNS;
        staggeredGridLayoutManager=new StaggeredGridLayoutManager(noOfColumns,StaggeredGridLayoutManager.VERTICAL);
        eventListStaggeredView.setLayoutManager(staggeredGridLayoutManager);
        scrollListener=new EndlessRecyclerViewScrollListener(staggeredGridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Toast toast=Toast.makeText(getContext(),getResources().getString(R.string.loading),Toast.LENGTH_SHORT);
                toast.show();
                if(page>maxPageNumber)
                    maxPageNumber=page;
                else
                    maxPageNumber+=1;
                fetchEventList(categoryObj.getCategoryId(),page);
            }
        };

        eventListStaggeredView.addOnScrollListener(scrollListener);
        eventListAdapter=new EventListAdapter(getContext(),eventResponse);
        eventListStaggeredView.setAdapter(eventListAdapter);
        eventListAdapter.setClickListener(this);

        return rootView;
    }

    private void initViews(View rootView){
        eventListStaggeredView=(RecyclerView)rootView.findViewById(R.id.event_list_recycler_view);
        emptyView=(TextView)rootView.findViewById(R.id.empty_event_list_view);
        spinner=(ProgressBar)rootView.findViewById(R.id.event_list_progress_bar);
    }

    public void fetchEventList(String categoryId, final int pageNo){
        setViewsVisibility();
        accessSharedPreferenceValues();

        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);
        Call<EventResponse> call=apiInterface.getEventListAccordingToCategory(categoryId,Utility.getDateRangeString(120),location,searchRadius,sortOrder,Utility.getStartDate(),pageNo,IMAGE_SIZE,BuildConfig.EVENT_API_KEY);
        call.enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if(response.isSuccessful()){
                    eventResponse.setTotalItems(response.body().getTotalItems());
                    Activity activity=getActivity();
                    if(activity!=null && isAdded()){
                        if(eventResponse.getTotalItems()>0){
                            eventResponse.getAllEvents().getEventList().addAll(response.body().getAllEvents().getEventList());
                            if(eventListAdapter!=null)
                                eventListAdapter.notifyDataSetChanged();
                        }
                        else{
                            if(eventResponse.getAllEvents().getEventList().size()==0){
                                eventListStaggeredView.setVisibility(GONE);
                                emptyView.setVisibility(View.VISIBLE);
                                emptyView.setText(R.string.no_results_found);
                            }
                        }
                        spinner.setVisibility(GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                Activity activity=getActivity();
                if(activity!=null && isAdded()){
                    if(pageNo==1){
                        eventListStaggeredView.setVisibility(GONE);
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText(getString(R.string.error_fetching_data));
                    }
                    spinner.setVisibility(GONE);
                }
            }
        });
    }


    @Override
    public void onClick(View view, int position) {
        Event event=eventResponse.getAllEvents().getEventList().get(position);
        Intent intent=new Intent(getActivity(),EventDetailActivity.class);
        intent.putExtra("CallingActivity", ImportantConstants.EVENT_LIST_ACTIVITY);
        intent.putExtra("EventDetailObject",event);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        else
            startActivity(intent);
    }

    private void setViewsVisibility(){
        if(spinner.getVisibility()== GONE || spinner.getVisibility()==View.INVISIBLE)
            spinner.setVisibility(View.VISIBLE);

        if(emptyView.getVisibility()==View.VISIBLE)
            emptyView.setVisibility(GONE);

        if(eventListStaggeredView.getVisibility()==GONE || eventListStaggeredView.getVisibility()==View.INVISIBLE)
            eventListStaggeredView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        eventResponse.getAllEvents().setEventList(new ArrayList<Event>());
        eventListStaggeredView.setVisibility(GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void accessSharedPreferenceValues(){
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getContext());
        location=sharedPreferences.getString(getString(R.string.pref_location_key),getString(R.string.default_location));
        sortOrder=sharedPreferences.getString(getString(R.string.pref_sort_by_key),getString(R.string.default_sort_by));
        searchRadius=Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_radius_key),getString(R.string.default_search_radius)));
        location.replaceAll("\\s","+");
    }
}
