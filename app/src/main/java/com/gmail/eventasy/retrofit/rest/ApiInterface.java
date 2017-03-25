package com.gmail.eventasy.retrofit.rest;

import com.gmail.eventasy.retrofit.model.CategoryResponse;
import com.gmail.eventasy.retrofit.model.Event;
import com.gmail.eventasy.retrofit.model.EventResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by kakalra on 12/14/2016.
 */

public interface ApiInterface {

    @GET("events/get")
    Call<Event> getEventDetail(@Query("id") String id,
                               @Query("app_key") String apiKey);

    @GET("events/search")
    Call<EventResponse> getPreferredLocationEventList(@Query("date") String date,
                                             @Query("location") String location,
                                             @Query("within") int radius,
                                             @Query("sort_order") String sortType,
                                             @Query("after_start_date") String afterStartDate,
                                             @Query("page_number") int pageNumber,
                                             @Query("image_sizes") String imageSize,
                                             @Query("app_key") String apiKey);

    @GET("categories/list")
    Call<CategoryResponse> getCategories(@Query("app_key") String apiKey);

    @GET("events/search")
    Call<EventResponse> getEventListAccordingToCategory(@Query("category") String category,
                                                        @Query("date") String date,
                                                        @Query("location") String location,
                                                        @Query("within") int radius,
                                                        @Query("sort_order") String sortType,
                                                        @Query("after_start_date") String afterStartDate,
                                                        @Query("page_number") int pageNumber,
                                                        @Query("image_sizes") String imageSize,
                                                        @Query("app_key") String apiKey);

    @GET("events/search")
    Call<EventResponse> getSearchResults(@Query("q") String query,
                                         @Query("date") String date,
                                         @Query("after_start_date") String afterStartDate,
                                         @Query("page_number") int pageNumber,
                                         @Query("image_sizes") String imageSize,
                                         @Query("app_key") String apiKey);

}
