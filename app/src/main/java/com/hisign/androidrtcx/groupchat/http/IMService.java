package com.hisign.androidrtcx.groupchat.http;

import com.hisign.androidrtcx.groupchat.pj.BaseIMResponse;
import com.hisign.broadcastx.pj.Stuff;

import java.util.List;
import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

public interface IMService {
    @GET("stuff/stuffHistory")
    Observable<BaseIMResponse<List<Stuff>>> stuffHistory(@QueryMap Map<String, Object> maps);
}
