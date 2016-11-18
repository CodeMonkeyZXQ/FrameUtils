package cm.frame.httputils;

/**
 * Created by zhouxiqing on 16-11-15.
 */
public abstract class HttpCallback {

    public abstract void onFailure(HttpMethod method);

    public abstract void onSuccess(HttpMethod method);
}
