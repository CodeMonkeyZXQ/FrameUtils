package cm.frame.frameutils;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import cm.frame.httputils.HttpCallback;
import cm.frame.httputils.HttpMethod;
import cm.frame.httputils.HttpPublisher;

public class MainActivity extends AppCompatActivity {

    TextView mTvTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTvTip = (TextView)findViewById(R.id.tv_tip);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Map map = new HashMap();
                map.put("appId", "1001");
                HttpMethod method = new HttpMethod("http://120.25.64.229:8389/biz/brand/getall.do",map,"This is tag",new HttpCallback() {
                    @Override
                    public void onFailure(HttpMethod method) {
                        Snackbar.make(view, "onFailure:"+method.toJSONString(), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }

                    @Override
                    public void onSuccess(HttpMethod method) {
                        Snackbar.make(view, "onSuccess:"+method.toJSONString()+method.data().toJSONString(), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        mTvTip.setText(method.data().toJSONString());
                    }
                });
                HttpPublisher.getInstance().sendRequest(method);
                HttpPublisher.getInstance().sendRequest(method.setCallback(null));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
