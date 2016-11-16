package cm.frame.httputils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HttpPublisher {
    /**
     * 访问网络失败
     */
    public static final int NETWORK_ERROR = 0X1101;

    /**
     * Http访问异常
     */
    public static final int HTTP_ERROR = 0X1111;

    private Context mContext = null;
    private OkHttpClient client = null;
    private Map<String, String> mHttpToken = new HashMap<>();
    private static final String DEFAULT_TOKEN_NAME = "accessToken";
    private static final String TAG = "HttpPubliser";

    private HttpCallback defaultCallBack = new HttpCallback() {
        @Override
        public void onFailure(HttpMethod method, String tag, IOException e) {
            String msg = tag;
            if (method.getParam() != null && !method.getParam().isEmpty()) {
                msg = msg + ":" + method.getParam().toString();
            }
            Log.e(TAG, msg, e);
        }

        @Override
        public void onSuccess(HttpMethod method, String tag) {
            Log.d(TAG, tag + ":" + method.toJSONString());
        }
    };

    public class JsonObjectCallback implements Callback {
        private HttpMethod method;
        private String eventTag;
        private HttpCallback callback;

        public JsonObjectCallback(String tag, HttpMethod method, HttpCallback callback) {
            this.eventTag = tag;
            this.method = method;
            this.callback = callback;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            JSONObject data = new JSONObject();
            data.put("errCode", HTTP_ERROR);
            if (call.isCanceled()) {
                data.put("errName", "Http访问被取消");
            } else {
                data.put("errName", "Http访问异常");
            }
            method.put(data);
            defaultCallBack.onFailure(method, eventTag, e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            JSONObject data = JSON.parseObject(response.body().string());
            defaultCallBack.onSuccess(method.put(data), eventTag);
        }
    }

    private static class Holder {
        private static final HttpPublisher INSTANCE = new HttpPublisher();
    }

    private HttpPublisher() {
    }

    public static final HttpPublisher getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * init HttpPuclisher
     *
     * @param context
     * @param okHttpClient
     */
    public void initialize(Context context, OkHttpClient okHttpClient) {
        mContext = context.getApplicationContext();
        client = okHttpClient;
    }

    /**
     * 设置统一回调
     *
     * @param defaultCallBack
     */
    public void setDefaultCallBack(HttpCallback defaultCallBack) {
        if (defaultCallBack != null) {
            this.defaultCallBack = defaultCallBack;
        }
    }

    /**
     * 设置请求token
     *
     * @param token
     */
    public void setToken(String token) {
        if (null != token && !token.isEmpty())
            mHttpToken.put(DEFAULT_TOKEN_NAME, token);
    }

    /**
     * 设置请求token
     *
     * @param name  key
     * @param token value
     */
    public void setToken(String name, String token) {
        if (null != name && null != token && !token.isEmpty() && !name.isEmpty()) {
            mHttpToken.put(name, token);
        }
    }

    public boolean checkNetworkState() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 发送请求，采用默认回调
     *
     * @param method 参数
     * @param tag    标签
     */
    public void sendRequest(final HttpMethod method, final String tag) {
        sendRequest(method, tag, defaultCallBack);
    }

    /**
     * 发送请求，自定议回调
     *
     * @param method   参数
     * @param tag      标签
     * @param callback 回调，为空时使用默认回调
     */
    public void sendRequest(final HttpMethod method, final String tag, HttpCallback callback) {
        if (callback == null) {
            callback = defaultCallBack;
        }
        // 判断网络是否可用
        // 网络不可用时不进行网络连接
        if (!checkNetworkState()) {
            JSONObject data = new JSONObject();
            data.put("errCode", NETWORK_ERROR);
            data.put("errName", "网络异常");
            method.put(data);
            callback.onFailure(method, tag, null);
            return;
        }
        FormBody.Builder formBuilder = new FormBody.Builder();
        Map<String, Object> map = method.getParam();
        if (map != null && !map.isEmpty()) {
            for (String key : map.keySet()) {
                formBuilder.add(key, method.getParam().get(key).toString());
            }
        }
        Request request = new Request.Builder()
                .url(method.getUrl())
                .post(formBuilder.build())
                .tag(tag)
                .build();
        client.newCall(request).enqueue(new JsonObjectCallback(tag, method, callback));
    }

    /**
     * 发送带Token的请求,使用默认回调
     *
     * @param method
     * @param tag
     */
    public void sendRequestWithToken(HttpMethod method, String tag) {
        for (String key : mHttpToken.keySet()) {
            method.getParam().put(key, mHttpToken.get(key));
        }
        sendRequest(method, tag, defaultCallBack);
    }

    /**
     * 发送带Token的请求,使用默认回调
     *
     * @param method
     * @param tag
     */
    public void sendRequestWithToken(HttpMethod method, String tag, HttpCallback callback) {
        for (String key : mHttpToken.keySet()) {
            method.getParam().put(key, mHttpToken.get(key));
        }
        sendRequest(method, tag, callback);
    }

    /**
     * 取消所有
     */
    public void cancelAll() {
        client.dispatcher().cancelAll();
    }

    /**
     * 根据TAG取消
     *
     * @param tag
     */
    public void cancel(String tag) {

        if (TextUtils.isEmpty(tag)) {
            return;
        }
        for (Call call : client.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }
}