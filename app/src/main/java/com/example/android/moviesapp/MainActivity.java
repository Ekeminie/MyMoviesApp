package com.example.android.moviesapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.moviesapp.adapter.MoviesAdapter;
import com.example.android.moviesapp.api.Client;
import com.example.android.moviesapp.api.Service;
import com.example.android.moviesapp.data.FavoriteDbHelper;
import com.example.android.moviesapp.model.Movie;
import com.example.android.moviesapp.model.MoviesResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    TextView failure;
    private RecyclerView recyclerView;
    private MoviesAdapter moviesAdapter;
    private List<Movie> movieList;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static final String LOG_TAG = MoviesAdapter.class.getName();
    private FavoriteDbHelper favoriteDbHelper;

    private AppCompatActivity activity = MainActivity.this;
    int cacheSize = 10 * 1024 * 1024 * 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//
//        failure = findViewById(R.id.failure_view);


        initViews();


    }


    public Activity getActivity() {
        Context context = this;
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private void initViews() {


        recyclerView = findViewById(R.id.recycler_view);

        movieList = new ArrayList<>();
        moviesAdapter = new MoviesAdapter(this, movieList);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        } else recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(moviesAdapter);
        moviesAdapter.notifyDataSetChanged();
        favoriteDbHelper = new FavoriteDbHelper(activity);

        swipeRefreshLayout = findViewById(R.id.main_content);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_orange_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initViews();
                Toast.makeText(MainActivity.this, "Movies Refreshed", Toast.LENGTH_SHORT).show();
            }
        });


        checkSortOrder();
    }


    private void initViews2() {

           // failure.setVisibility(failure.GONE);
                recyclerView = findViewById(R.id.recycler_view);
        movieList = new ArrayList<>();
        moviesAdapter = new MoviesAdapter(this, movieList);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        } else{ recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
    }
    recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(moviesAdapter);
        moviesAdapter.notifyDataSetChanged();
        favoriteDbHelper= new FavoriteDbHelper(activity);

        getAllFavorite();

    }
    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return  activeNetworkInfo  != null && activeNetworkInfo.isConnected();


    }
    private void loadJson() {
        try {
            if (BuildConfig.The_movies_db_api.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please Obtain an Api Key from the required database", Toast.LENGTH_SHORT).show();

                return;
            }

//            Cache cache = new Cache(getCacheDir(), cacheSize);
//            OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                    .cache(cache)
//                    .addInterceptor(new Interceptor() {
//                        @Override
//                        public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
//                            Request request = chain.request();
//                            if (!isNetworkAvailable()){
//                                int maxStale = 60 * 60 * 24 * 28;
//                                request = request
//                                        .newBuilder()
//                                        .header("Cache-Control", "public, only-if-cached, max-stale" + maxStale)
//                                        .build();
//                            }
//                            return chain.proceed(request);
//
//                        }
//                    }).build();
//
//
//            Retrofit.Builder builder = new Retrofit.Builder()
//                    .baseUrl("http://api.themoviedb.org/3/")
//                    .client(okHttpClient)
//                    .addConverterFactory(GsonConverterFactory.create());
//
//            Retrofit retrofit = builder.build();
//            Service apiService = retrofit.create(Service.class);
            Client Client = new Client();
           Service apiService =
                    Client.getClient().create(Service.class);
            Call<MoviesResponse> call = apiService.getPopularMovies(BuildConfig.The_movies_db_api);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = response.body().getResults();
                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }



                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", "Error ");//t.getMessage()
                    Toast.makeText(MainActivity.this, "Error Fetching Data!", Toast.LENGTH_SHORT).show();
                   // failure.setVisibility(failure.VISIBLE);


                }
            });
        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

    }


    private void loadJson1() {
        try {
            if (BuildConfig.The_movies_db_api.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please Obtain an Api Key from the required database", Toast.LENGTH_SHORT).show();
                return;
            }

            Cache cache = new Cache(getCacheDir(), cacheSize);
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                            Request request = chain.request();
                            if (!isNetworkAvailable()){
                                int maxStale = 60 * 60 * 24 * 24;
                                request = request
                                        .newBuilder()
                                        .header("Cache-Control", "public, only-if-cached, max-stale" + maxStale)
                                        .build();
                            }
                            return chain.proceed(request);

                        }
                    }).build();


            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl("http://api.themoviedb.org/3/")

                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create());

            Retrofit retrofit = builder.build();
            Service apiService = retrofit.create(Service.class);
//
//            Client Client = new Client();
//            Service apiService =
//                    Client.getClient().create(Service.class);
            Call<MoviesResponse> call = apiService.getTopRatedMovies(BuildConfig.The_movies_db_api);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = response.body().getResults();
                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }




                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", "Error ");//t.getMessage()
                    Toast.makeText(MainActivity.this, "Error Fetching Data!", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(LOG_TAG, "Preferences Updated");
        checkSortOrder();
    }

    private void checkSortOrder() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sortOrder = sharedPreferences.getString(
                this.getString(R.string.pref_sort_order_key),
                this.getString(R.string.pref_most_popular)
        );
        if (sortOrder.equals(this.getString(R.string.pref_most_popular))) {
            Log.d(LOG_TAG, "Sorting by Most Popular");
            loadJson();
        } else if (sortOrder.equals(this.getString(R.string.favorite))) {

        Log.d(LOG_TAG, "Sorting by Favorites");
        initViews2();
    }else

    {
        Log.d(LOG_TAG, "Sorting by Vote Average");
        loadJson1();
    }

}

    @Override
    protected void onResume() {
        super.onResume();
        if(movieList.isEmpty()){
            checkSortOrder();
        }else{
            checkSortOrder();

        }
    }

    private void getAllFavorite(){

        new  AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void...params){
                   movieList.clear();
                   movieList.addAll(favoriteDbHelper.getAllFavorite());
                   return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                moviesAdapter.notifyDataSetChanged();
            }
        }.execute();
    }
}


