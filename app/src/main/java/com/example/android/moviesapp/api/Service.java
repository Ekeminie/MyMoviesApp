package com.example.android.moviesapp.api;

import com.example.android.moviesapp.model.MoviesResponse;
import com.example.android.moviesapp.model.TrailerResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Delight on 08/05/2018.
 */

public interface Service{

    String api = "=3f8271315ebf1732ac9451c3d85d579c";

    @GET("movie/popular")
    Call<MoviesResponse> getPopularMovies(@Query("api_key") String apiKey);

    @GET("movie/top_rated")
    Call<MoviesResponse> getTopRatedMovies(@Query("api_key") String apiKey);


    @GET("movie/{movie_id}/videos")
    Call<TrailerResponse> getMovieTrailer(@Path("movie_id") int id, @Query("api_key") String apiKey);


}
