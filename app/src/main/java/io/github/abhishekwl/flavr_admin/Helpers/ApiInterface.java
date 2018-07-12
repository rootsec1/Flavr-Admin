package io.github.abhishekwl.flavr_admin.Helpers;

import io.github.abhishekwl.flavr_admin.Models.Food;
import io.github.abhishekwl.flavr_admin.Models.Hotel;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

  //HOTEL
  @FormUrlEncoded
  @POST("hotels/")
  Call<Hotel> createNewHotel(@Field("uid") String uid, @Field("name") String name, @Field("email") String email, @Field("phone") String phone, @Field("latitude") double latitude, @Field("longitude") double longitude);

  @GET("hotels/{uid}")
  Call<Hotel> getHotel(@Path("uid") String uid);

  @FormUrlEncoded
  @PUT("hotels/{uid}")
  Call<Hotel> updateHotel(@Path("uid") String uid, @Field("name") String name, @Field("email") String email, @Field("phone") String phone, @Field("latitude") double latitude, @Field("longitude") double longitude);

  //FOOD
  @FormUrlEncoded
  @POST("food/")
  Call<Food> createNewFoodItem(@Field("name") String name, @Field("cost") double cost, @Field("category") String category, @Field("hotel") String _id, @Field("image") String image);

  @GET("food/{id}")
  Call<ArrayList<Food>> getAllFoodItems(@Path("id") String id, @Query("uid") String uid);

  @GET("food/{_id}")
  Call<Food> getSpecificFoodItem(@Path("_id") String id);

  @FormUrlEncoded
  @PUT("hotels/{_id}")
  Call<Food> updateFoodItem(@Path("_id") String id, @Field("name") String name, @Field("cost") double cost, @Field("category") String category, @Field("image") String image);

  @DELETE("hotels/{_id}")
  Call<Food> deleteFoodItem(@Path("_id") String id);
}
