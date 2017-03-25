package com.gmail.eventasy.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static com.gmail.eventasy.ImportantConstants.IMAGE_SIZE;
import static com.gmail.eventasy.ImportantConstants.LANDSCAPE_COLUMNS;
import static com.gmail.eventasy.ImportantConstants.PORTRAIT_COLUMNS;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,ItemClickListener,SharedPreferences.OnSharedPreferenceChangeListener {

    int searchRadius;
    int maxPageNumber;
    String location;
    String sortOrder;

    Toolbar toolbar;
    FloatingActionButton fab;
    EventResponse eventResponse;
    EventListAdapter eventListAdapter;
    SharedPreferences sharedPreferences;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    private EndlessRecyclerViewScrollListener scrollListener;

    TextView emptyView;
    ProgressBar spinner;
    RecyclerView eventListStaggeredView;
    TextView currentLocationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //saving the list of events already fetched.
        if(savedInstanceState==null || !savedInstanceState.containsKey("EventList"))
        {
            eventResponse=new EventResponse();
            maxPageNumber=0;
        }
        else{
            eventResponse=savedInstanceState.getParcelable("EventList");
            maxPageNumber=savedInstanceState.getInt("MaxPageNumber");
        }

        setContentView(R.layout.activity_main);
        initViews();
        setSupportActionBar(toolbar);

        sharedPreferences=PreferenceManager .getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        //setting up the floating action button and navigation drawer.
        setupFab();
        setupNavigationDrawer();

        eventListStaggeredView.setHasFixedSize(true);
        int noOfColumns=getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT?PORTRAIT_COLUMNS:LANDSCAPE_COLUMNS;
        staggeredGridLayoutManager=new StaggeredGridLayoutManager(noOfColumns,StaggeredGridLayoutManager.VERTICAL);
        eventListStaggeredView.setLayoutManager(staggeredGridLayoutManager);

        scrollListener=new EndlessRecyclerViewScrollListener(staggeredGridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Toast toast=Toast.makeText(getApplicationContext(),getResources().getString(R.string.loading),Toast.LENGTH_SHORT);
                toast.show();
                if(page>maxPageNumber)
                    maxPageNumber=page;
                else
                    maxPageNumber+=1;
                fetchEventsAccordingToLocation(maxPageNumber);
            }
        };

        eventListStaggeredView.addOnScrollListener(scrollListener);
        eventListAdapter=new EventListAdapter(this,eventResponse);
        eventListStaggeredView.setAdapter(eventListAdapter);
        eventListAdapter.setClickListener(this);
    }

    private void initViews(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        currentLocationTextView=(TextView)findViewById(R.id.current_location);
        eventListStaggeredView=(RecyclerView)findViewById(R.id.main_event_list);
        emptyView=(TextView)findViewById(R.id.main_empty_view);
        spinner=(ProgressBar)findViewById(R.id.main_event_list_progress_bar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentLocationTextView.setText(getResources().getString(R.string.current_location)+" "+sharedPreferences.getString(getString(R.string.pref_location_key),getString(R.string.default_location)));
        //events are fetched only if the current list of events is empty.
        if(eventResponse.getAllEvents().getEventList().size()==0){
            if(Utility.isNetworkAvailable(this))
                fetchEventsAccordingToLocation(1);
            else{
                eventListStaggeredView.setVisibility(GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(getString(R.string.no_network_available));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("EventList",eventResponse);
        outState.putInt("MaxPageNumber",maxPageNumber);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_categories){
            Intent intent=new Intent(getApplicationContext(),CategoryActivity.class);
            //applying window transition animations only if SDK version>=LOLLIPOP
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            else
                startActivity(intent);
        }
        else if (id == R.id.nav_favourites) {
            Intent intent=new Intent(getApplicationContext(),FavouriteEventListActivity.class);
            //applying window transition animations only if SDK version>=LOLLIPOP
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            else
                startActivity(intent);
        }
        else if(id==R.id.nav_settings){
            Intent intent=new Intent(this,SettingsActivity.class);
            //applying window transition animations only if SDK version>=LOLLIPOP
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            else
                startActivity(intent);
        }
        else if(id==R.id.nav_rate){
            final String appPackageName=getPackageName();
            try{
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=" + appPackageName)));
            }
            catch (android.content.ActivityNotFoundException e){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
        else if (id == R.id.nav_share) {
            final String appPackageName=getPackageName();
            try{
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,"Install the app Eventasy: http://play.google.com/store/apps/details?id=" + appPackageName);
                startActivity(Intent.createChooser(intent,"Share link:"));
            }
            catch (Exception e){
            }
        }
        else if (id == R.id.nav_send) {
            Intent emailIntent=new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",getString(R.string.email_id),null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.email_subject));
            startActivity(Intent.createChooser(emailIntent,getString(R.string.send_email)));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fetchEventsAccordingToLocation(final int pageNo){
        setViewsVisibility();
        accessSharedPreferenceValues();

        ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);
        Call<EventResponse> call=apiInterface.getPreferredLocationEventList(Utility.getDateRangeString(120),location,searchRadius,sortOrder,Utility.getStartDate(),pageNo,IMAGE_SIZE, BuildConfig.EVENT_API_KEY);
        call.enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if(response.isSuccessful()){
                    eventResponse.setTotalItems(response.body().getTotalItems());
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

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                spinner.setVisibility(GONE);
                if(pageNo==1){
                    eventListStaggeredView.setVisibility(GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText(getString(R.string.error_fetching_data));
                }
            }
        });
    }

    private void setupFab(){
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getBaseContext(),SearchInputActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupNavigationDrawer(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onClick(View view, int position) {
        Event event=eventResponse.getAllEvents().getEventList().get(position);
        Intent intent=new Intent(this,EventDetailActivity.class);
        intent.putExtra("CallingActivity", ImportantConstants.MAIN_ACTIVITY);
        intent.putExtra("EventDetailObject",event);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        else
            startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        eventResponse.getAllEvents().setEventList(new ArrayList<Event>());
        eventListStaggeredView.setVisibility(GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setViewsVisibility(){
        if(spinner.getVisibility()== GONE || spinner.getVisibility()==View.INVISIBLE){
            spinner.setVisibility(View.VISIBLE);
        }
        if(emptyView.getVisibility()==View.VISIBLE)
            emptyView.setVisibility(GONE);

        if(eventListStaggeredView.getVisibility()==GONE || eventListStaggeredView.getVisibility()==View.INVISIBLE)
            eventListStaggeredView.setVisibility(View.VISIBLE);
    }

    private void accessSharedPreferenceValues(){
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        location=sharedPreferences.getString(getString(R.string.pref_location_key),getString(R.string.default_location));
        sortOrder=sharedPreferences.getString(getString(R.string.pref_sort_by_key),getString(R.string.default_sort_by));
        searchRadius=Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_radius_key),getString(R.string.default_search_radius)));
        location.replaceAll("\\s","+");
    }
}
