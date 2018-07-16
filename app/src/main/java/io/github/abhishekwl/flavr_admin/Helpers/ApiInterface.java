package io.github.abhishekwl.flavr_admin.Helpers;

import io.github.abhishekwl.flavr_admin.Models.Food;
import io.github.abhishekwl.flavr_admin.Models.Hotel;
import io.github.abhishekwl.flavr_admin.Models.Order;
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

  @GET("food/{hotelId}")
  Call<ArrayList<Food>> getAllFoodItems(@Path("hotelId") String hotelId, @Query("uid") String uid);

  @GET("food{hotelId}")
  Call<ArrayList<Food>> getFoodItemsInCategory(@Path("hotelId") String hotelId, @Query("uid") String uid, @Query("category") String category);

  @GET("food/{id}")
  Call<Food> getSpecificFoodItem(@Path("id") String id);

  @FormUrlEncoded
  @PUT("food/{id}")
  Call<Food> updateFoodItem(@Path("id") String id, @Field("name") String name, @Field("cost") double cost, @Field("category") String category, @Field("image") String image);

  @DELETE("food/{id}")
  Call<Food> deleteFoodItem(@Path("id") String id);

  //ORDERS
  @GET("orders/{id}")
  Call<ArrayList<Order>> getAllOrderItems(@Path("id") String hotelId, @Query("uid") String hotelUid);

  @GET("orders/{id}")
  Call<Order> getSpecificOrder(@Path("id") String orderId);

  @DELETE("orders/{id}")
  Call<Order> deleteOrder(@Path("id") String orderId);
}
