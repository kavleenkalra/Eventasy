package com.gmail.eventasy.ui;

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
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
import com.gmail.eventasy.retrofit.model.Event;
import com.gmail.eventasy.retrofit.model.EventResponse;
import com.gmail.eventasy.retrofit.rest.ApiClient;
import com.gmail.eventasy.retrofit.rest.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static com.gmail.eventasy.ImportantConstants.LANDSCAPE_COLUMNS;
import static com.gmail.eventasy.ImportantConstants.PORTRAIT_COLUMNS;

public class SearchableActivity extends AppCompatActivity implements ItemClickListener{

    int maxPageNumber;

    String query;
    Toolbar toolbar;
    RecyclerView searchListView;
    TextView emptyView;
    ProgressBar spinner;
    EventListAdapter adapter;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    EventResponse eventResponse;
    private EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null || !savedInstanceState.containsKey("EventList")){
            eventResponse=new EventResponse();
            maxPageNumber=0;
        }
        else{
            eventResponse=savedInstanceState.getParcelable("EventList");
            maxPageNumber=savedInstanceState.getInt("MaxPageNumber");
        }

        setContentView(R.layout.activity_searchable);
        initViews();
        if(toolbar!=null){
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeActionContentDescription(R.string.go_back_main_screen);
        }

        searchListView.setHasFixedSize(true);
        int noOfColumns=getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT?PORTRAIT_COLUMNS:LANDSCAPE_COLUMNS;
        staggeredGridLayoutManager=new StaggeredGridLayoutManager(noOfColumns,StaggeredGridLayoutManager.VERTICAL);
        searchListView.setLayoutManager(staggeredGridLayoutManager);

        scrollListener=new EndlessRecyclerViewScrollListener(staggeredGridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Toast toast=Toast.makeText(getApplicationContext(),getResources().getString(R.string.loading),Toast.LENGTH_SHORT);
                toast.show();
                if(page>maxPageNumber)
                    maxPageNumber=page;
                else
                    maxPageNumber+=1;
                fetchSearchResults(query,page);
            }
        };

        searchListView.addOnScrollListener(scrollListener);
        adapter=new EventListAdapter(getApplicationContext(),eventResponse);
        searchListView.setAdapter(adapter);
        adapter.setClickListener(this);
    }

    private void initViews(){
        toolbar=(Toolbar)findViewById(R.id.searchable_toolbar);
        searchListView=(RecyclerView)findViewById(R.id.search_recycler_view);
        emptyView=(TextView)findViewById(R.id.empty_search_list_view);
        spinner=(ProgressBar)findViewById(R.id.search_list_progress_bar);
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(eventResponse.getAllEvents().getEventList().size()==0){
            if(Utility.isNetworkAvailable(this))
                handleIntent(getIntent());
            else{
                searchListView.setVisibility(GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(getString(R.string.no_network_available));
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){
        if(intent.ACTION_SEARCH.equals(intent.getAction())){
            query=intent.getStringExtra(SearchManager.QUERY);
            query.replaceAll("\\s","+");
            fetchSearchResults(query,1);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("EventList",eventResponse);
        outState.putInt("MaxPageNumber",maxPageNumber);
        super.onSaveInstanceState(outState);
    }

    private void fetchSearchResults(String query,final int pageNo){

        setViewsVisibility();
        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);

        Call<EventResponse> call=apiInterface.getSearchResults(query, Utility.getDateRangeString(120),Utility.getStartDate(),pageNo,ImportantConstants.IMAGE_SIZE,BuildConfig.EVENT_API_KEY);
        call.enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if(response.isSuccessful()){
                    eventResponse.setTotalItems(response.body().getTotalItems());
                    if(eventResponse.getTotalItems()>0){
                        eventResponse.getAllEvents().getEventList().addAll(response.body().getAllEvents().getEventList());
                        if(adapter!=null)
                            adapter.notifyDataSetChanged();
                    }
                    else{
                        if(eventResponse.getAllEvents().getEventList().size()==0){
                            searchListView.setVisibility(GONE);
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText(R.string.no_results_found);
                        }
                    }
                    spinner.setVisibility(GONE);
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                spinner.setVisibility(GONE);
                if(pageNo==1){
                    searchListView.setVisibility(GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText(getString(R.string.error_fetching_data));
                }
            }
        });
    }

    @Override
    public void onClick(View view, int position) {
        Event event=eventResponse.getAllEvents().getEventList().get(position);
        Intent intent=new Intent(getApplicationContext(),EventDetailActivity.class);
        intent.putExtra("CallingActivity", ImportantConstants.SEARCH_ACTIVITY);
        intent.putExtra("EventDetailObject",event);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        else
            startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }

    private void setViewsVisibility(){
        if(spinner.getVisibility()== GONE || spinner.getVisibility()==View.INVISIBLE)
            spinner.setVisibility(View.VISIBLE);

        if(emptyView.getVisibility()==View.VISIBLE)
            emptyView.setVisibility(GONE);

        if(searchListView.getVisibility()==GONE || searchListView.getVisibility()==View.INVISIBLE)
            searchListView.setVisibility(View.VISIBLE);
    }
}
