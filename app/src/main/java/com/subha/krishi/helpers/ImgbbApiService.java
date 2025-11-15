package com.subha.krishi.helpers;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ImgbbApiService {
    @Multipart
    @POST("1/upload")
    Call<ResponseBody> uploadImage(
            @Query("key") String apiKey,
            @Part MultipartBody.Part image
    );
}
