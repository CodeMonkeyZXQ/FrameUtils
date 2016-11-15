package cm.frame.httputils;

import java.io.IOException;

/**
 * Created by zhouxiqing on 16-11-15.
 */
public abstract class HttpCallback {

    public abstract void onFailure(HttpMethod method,String tag,IOException e);

    public abstract void onSuccess(HttpMethod method,String tag);
}
