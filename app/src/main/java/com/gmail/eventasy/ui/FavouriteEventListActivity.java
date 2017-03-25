package com.gmail.eventasy.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.gmail.eventasy.R;

public class FavouriteEventListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_event_list);
        Toolbar toolbar=(Toolbar)findViewById(R.id.favourite_toolbar);
        if(toolbar!=null){
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeActionContentDescription(R.string.go_back_main_screen);
        }

        if(savedInstanceState==null){
            FavouriteEventListFragment fragment=new FavouriteEventListFragment();
            if(!fragment.isAdded()){
                getSupportFragmentManager()
                        .beginTransaction().add(R.id.activity_favourite_event_list,fragment)
                        .commit();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
