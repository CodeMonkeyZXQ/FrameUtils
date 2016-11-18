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

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.R.attr.tag;


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

    private Consumer consumer = new Consumer<HttpMethod>() {
        @Override
        public void accept(HttpMethod method) {
            if (method.getError() != null) {
                method.getCallback().onFailure(method);
            } else {
                method.getCallback().onSuccess(method);
            }
        }
    };

    private HttpCallback defaultCallBack = new HttpCallback() {
        @Override
        public void onFailure(HttpMethod method) {
            String msg = method.getTag();
            if (method.getParam() != null && !method.getParam().isEmpty()) {
                msg = msg + ":" + method.getParam().toString();
            }
            Log.e(TAG, msg, method.getError());
        }

        @Override
        public void onSuccess(HttpMethod method) {
            Log.d(TAG, method.getTag() + ":" + method.getParam().toString());
            Log.d(TAG, method.getTag() + ":" + method.data().toString());
        }
    };

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
     * 发送请求，自定议回调
     *
     * @param method   参数
     */
    public void sendRequest(HttpMethod method) {
        HttpMethod newMethod = new HttpMethod(method.getUrl(), method.getParam(), method.getTag(),method.getCallback());
        Flowable.just(newMethod).map(new Function<HttpMethod, HttpMethod>() {
            @Override
            public HttpMethod apply(HttpMethod httpMethod) throws Exception {
                if (httpMethod.getCallback() == null) {
                    httpMethod.setCallback(defaultCallBack);
                }

                // 判断网络是否可用
                // 网络不可用时不进行网络连接
                if (!checkNetworkState()) {
                    JSONObject data = new JSONObject();
                    data.put("errCode", NETWORK_ERROR);
                    data.put("errName", "网络异常");
                    httpMethod.put(data);
                    httpMethod.setError(new Exception("NetWork Error!"));
                    return httpMethod;
                }
                FormBody.Builder formBuilder = new FormBody.Builder();
                Map<String, Object> map = httpMethod.getParam();
                if (map != null && !map.isEmpty()) {
                    for (String key : map.keySet()) {
                        formBuilder.add(key, httpMethod.getParam().get(key).toString());
                    }
                }
                final Request request = new Request.Builder()
                        .url(httpMethod.getUrl())
                        .post(formBuilder.build())
                        .tag(tag)
                        .build();
                Response response = null;
                JSONObject data = null;
                try {
                    response = client.newCall(request).execute();
                    data = JSON.parseObject(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                    httpMethod.setError(e);
                }
                return httpMethod.put(data);
            }
        })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(consumer);
    }

    /**
     * 发送带Token的请求,使用默认回调
     *
     * @param method
     */
    public void sendRequestWithToken(HttpMethod method) {
        for (String key : mHttpToken.keySet()) {
            method.getParam().put(key, mHttpToken.get(key));
        }
        sendRequest(method);
    }

    /**
     * 发送带Token的请求,使用默认回调
     *
     * @param method
     */
    public void sendRequestWithToken(HttpMethod method, HttpCallback callback) {
        for (String key : mHttpToken.keySet()) {
            method.getParam().put(key, mHttpToken.get(key));
        }
        sendRequest(method);
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