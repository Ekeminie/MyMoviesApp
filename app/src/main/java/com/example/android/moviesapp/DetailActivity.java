package com.example.android.moviesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.moviesapp.adapter.TrailerAdapter;
import com.example.android.moviesapp.api.Client;
import com.example.android.moviesapp.api.Service;
import com.example.android.moviesapp.data.FavoriteDbHelper;
import com.example.android.moviesapp.model.Movie;
import com.example.android.moviesapp.model.Trailer;
import com.example.android.moviesapp.model.TrailerResponse;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;

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

import static android.provider.BaseColumns._ID;
import static com.example.android.moviesapp.data.FavoriteContract.FavoriteEntry.COLUMN_MOVIEID;
import static com.example.android.moviesapp.data.FavoriteContract.FavoriteEntry.COLUMN_PLOT_SYNOPSIS;
import static com.example.android.moviesapp.data.FavoriteContract.FavoriteEntry.COLUMN_POSTER_PATH;
import static com.example.android.moviesapp.data.FavoriteContract.FavoriteEntry.COLUMN_TITLE;
import static com.example.android.moviesapp.data.FavoriteContract.FavoriteEntry.COLUMN_USERRATING;
import static com.example.android.moviesapp.data.FavoriteContract.FavoriteEntry.TABLE_NAME;

/**
 * Created by Delight on 08/05/2018.
 */

public class DetailActivity extends AppCompatActivity {



    TextView nameOfMovie, plotSynopsis, userRating, releaseDate;
    ImageView imageView;
    private RecyclerView recyclerView;
    private TrailerAdapter adapter;
    private List<Trailer> trailerList;
    private List<Movie> movieList;
     private FavoriteDbHelper favoriteDbHelper;
    private Movie favorite;
    private final AppCompatActivity activity = DetailActivity.this;
    Movie movie;
    String thumbnail, movieName, synopsis, rating, dateOfRelease;
    int movie_id;
    private SQLiteDatabase mDB;

    int cacheSize = 10 * 1024 * 1024 * 5;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final FavoriteDbHelper favoriteDbHelper = new FavoriteDbHelper(this);
       mDB= favoriteDbHelper.getWritableDatabase();





        imageView = findViewById(R.id.thumbnail_image_header);
        //nameOfMovie = findViewById(R.id.movietitle);
        plotSynopsis = findViewById(R.id.plot_synopsis);
        userRating = findViewById(R.id.userr_rating);
        releaseDate = findViewById(R.id.release_datee);


        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra("movies")){

//             thumbnail = getIntent().getExtras().getString("poster_path");
//             movieName = getIntent().getExtras().getString("original_title");
//             synopsis = getIntent().getExtras().getString("overview");
//             rating = getIntent().getExtras().getString("vote_average");
//             dateOfRelease = getIntent().getExtras().getString("release_date");

            movie = getIntent().getParcelableExtra("movies");

            thumbnail = movie.getPosterPath();
            movieName = movie.getOriginalTitle();
            synopsis = movie.getOverview();
            rating = Double.toString(movie.getVoteAverage());
            dateOfRelease = movie.getReleaseDate();
            movie_id = movie.getId();

            String poster = "http://image.tmdb.org/t/p/w500" + thumbnail;

            Glide.with(this)
                    .load(poster)
                    .placeholder(R.drawable.load)
                    .into(imageView);

//            nameOfMovie.setText(movieName);
            plotSynopsis.setText(synopsis);
            userRating.setText(rating);
            releaseDate.setText(dateOfRelease);

            ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar)).setTitle(movieName);
        }else{
            Toast.makeText(this, "No API Data", Toast.LENGTH_SHORT).show();
        }

        MaterialFavoriteButton materialFavoriteButton = findViewById(R.id.favorite_button);

        if(Exists(movieName)){
            materialFavoriteButton.setFavorite(true);
            materialFavoriteButton.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
                @Override
                public void onFavoriteChanged(MaterialFavoriteButton materialFavoriteButton, boolean b) {
                    if (b == true){
                        saveFavorite();
                        Snackbar.make(materialFavoriteButton, "Added to Favourite", Snackbar.LENGTH_SHORT).show();
                    }else {
                        FavoriteDbHelper favoriteDbHelper = new FavoriteDbHelper(DetailActivity.this);
                        favoriteDbHelper.deleteFavorite(movie_id);
                        Snackbar.make(materialFavoriteButton, "Removed from Favourite", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }

        else {
            materialFavoriteButton.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
                @Override
                public void onFavoriteChanged(MaterialFavoriteButton materialFavoriteButton, boolean b) {
                    if (b == true){
                        saveFavorite();
                        Snackbar.make(materialFavoriteButton, "Added to Favourite", Snackbar.LENGTH_SHORT).show();
                    }else {
                        int movie_id = getIntent().getExtras().getInt("id");
                        FavoriteDbHelper favoriteDbHelper = new FavoriteDbHelper(DetailActivity.this);
                        favoriteDbHelper.deleteFavorite(movie_id);
                        Snackbar.make(materialFavoriteButton, "Removed from Favourite", Snackbar.LENGTH_SHORT).show();
                    }



                }
            });
        }

       /* SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        materialFavoriteButton.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
            @Override
            public void onFavoriteChanged(MaterialFavoriteButton materialFavoriteButton, boolean b) {
                if(b){
                    SharedPreferences.Editor editor = getSharedPreferences("com.example.android.moviesapp.DetailActivity", MODE_PRIVATE).edit();
                    editor.putBoolean("Favorite Added ", true);
                    editor.commit();
                    saveFavorite();
                    Snackbar.make(materialFavoriteButton, "Added to Favouite", Snackbar.LENGTH_SHORT ).show();

                } else {
                    int movie_id = getIntent().getExtras().getInt("id");
                    favoriteDbHelper = new FavoriteDbHelper(DetailActivity.this);
                    favoriteDbHelper.deleteFavorite(movie_id);
                    SharedPreferences.Editor editor = getSharedPreferences("com.example.android.moviesapp.DetailActivity", MODE_PRIVATE).edit();
                    editor.putBoolean("Favorite Removed", true);
                    editor.commit();
                    Snackbar.make(materialFavoriteButton, "Removed From Favourite", Snackbar.LENGTH_SHORT).show();
                }
            }
        });*/

            initViews();
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return  activeNetworkInfo  != null && activeNetworkInfo.isConnected();


    }


    public boolean Exists(String searchItem){
        String [] projection = {
                _ID,
                COLUMN_MOVIEID,
                COLUMN_TITLE,
                COLUMN_USERRATING,
                COLUMN_POSTER_PATH,
                COLUMN_PLOT_SYNOPSIS
        };
        String selection = COLUMN_TITLE + " =?";
        String[] selectionArgs = {searchItem};
        String limit = "1";

        Cursor cursor = mDB.query(TABLE_NAME, projection, selection, selectionArgs, null, null, null, limit);
        boolean exists = (cursor.getCount() > 0 );
        cursor.close();
        return exists;
    }


    private void initCollapsingToolbar(){
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(" ");
        AppBarLayout appBarLayout =  findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener(){
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset){
                if (scrollRange == -1){
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0){
                    collapsingToolbarLayout.setTitle(getString(R.string.movie_detail));
                    isShow = true;
                }else if (isShow){
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }


    private void initViews(){

        trailerList = new ArrayList<>();
        adapter = new TrailerAdapter(this, trailerList);
        recyclerView = findViewById(R.id.recycler_view1);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        loadJson();
    }


    private void loadJson(){
        int movie_id = getIntent().getExtras().getInt("id");
        try{
            if(BuildConfig.The_movies_db_api.isEmpty()){
                Toast.makeText(getApplicationContext(), "Please Obtain an Api Key from your Service Provider", Toast.LENGTH_SHORT).show();
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
                                int maxStale = 60 * 60 * 24 * 28;
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
//                     Client client = new Client();
//            Service apiService=  client.getClient().create(Service.class);
            Call<TrailerResponse> call = apiService.getMovieTrailer(movie_id, BuildConfig.The_movies_db_api);
            call.enqueue(new Callback<TrailerResponse>() {
                @Override
                public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {
                    List<Trailer> trailer = response.body().getResults();
                    recyclerView.setAdapter(new TrailerAdapter(getApplicationContext(), trailer));
                    recyclerView.smoothScrollToPosition(0);
                }

                @Override
                public void onFailure(Call<TrailerResponse> call, Throwable t) {
                    Log.d("Error", "Error while Running");
                    Toast.makeText(DetailActivity.this, "Error while fetching Trailer info ",  Toast.LENGTH_SHORT).show();

                }
            });
        }catch (Exception e){
            Log.d("Error", "Error");
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveFavorite(){
        favoriteDbHelper = new FavoriteDbHelper(activity);
        favorite = new Movie();

//        int movie_id = getIntent().getExtras().getInt("id");
//        String rate = getIntent().getExtras().getString("vote_average");
//                String poster = getIntent().getExtras().getString("poster_path");
//
//
//        favorite.setId(movie_id);
//        favorite.setOriginalTitle(movieName);
//        favorite.setPosterPath(poster);
//        favorite.setVoteAverage(Double.parseDouble(rate));
//        favorite.setOverview(synopsis);

        Double rate = movie.getVoteAverage();
        favorite.setId(movie_id);
        favorite.setOriginalTitle(movieName);
        favorite.setPosterPath(thumbnail);
        favorite.setVoteAverage(rate);
        favorite.setOverview(synopsis);


        favoriteDbHelper.addFavorite(favorite);



        }
   }



