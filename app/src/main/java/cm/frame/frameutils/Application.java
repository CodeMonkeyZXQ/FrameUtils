package cm.frame.frameutils;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by zhouxiqing on 16-11-16.
 */
public class Application extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化Http请求
//        SSLParamsUtils.SSLParams sslParams = SSLParamsUtils.getSslSocketFactory(null, null, null);
//        CookieJarImpl cookieJar1 = new CookieJarImpl(new PersistentCookieStore(getApplicationContext()));
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .connectTimeout(20000L, TimeUnit.MILLISECONDS)
//                .readTimeout(20000L, TimeUnit.MILLISECONDS)
//                .cookieJar(cookieJar1)
//                .hostnameVerifier(new HostnameVerifier()
//                {
//                    @Override
//                    public boolean verify(String hostname, SSLSession session)
//                    {
//                        return true;
//                    }
//                })
//                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
//                .build();
//        mHttpPublisher = HttpPublisher.getInstance();
//        mHttpPublisher.initialize(getApplicationContext(),okHttpClient);
    }
}
