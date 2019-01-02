package org.teckown.hello;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Intent intent;
    private SharedPreferences mPreferences;
    private static final String TASKS_URL = "https://avocadoapi.herokuapp.com/api/v1/sessions/verify";
    private static final String CHNNEL_URL = "https://avocadoapi.herokuapp.com/api/v1/channels/show";
    private static final String REMOVE_URL = "https://avocadoapi.herokuapp.com/api/v1/channels/destroy";

    private TabLayout tabLayout;
    private ViewPager viewPager;

    //파싱데이터 담기
    ArrayList<channelItem> sdata = new ArrayList<channelItem>();
    ListView channelList;
    channelAdapter mAdapter;

    AsyncTask<?,?,?> sendTask; //채널삭제
    HttpClient client;
    HttpPost post;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

//      create toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

//        channelList = (ListView)findViewById(R.id.mainlist);
//
//        //커스텀 어댑터
//        mAdapter = new channelAdapter(MainActivity.this, R.layout.listview_main, sdata);
//        channelList.setAdapter(mAdapter);

        // Initializing the TabLayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Tab One"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab Two"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab Three"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Initializing ViewPager
        viewPager = (ViewPager) findViewById(R.id.pager);

        // Creating TabPagerAdapter adapter
        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Set TabSelectedListener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPreferences.contains("AuthToken")) {
//            user login check
            loadTasksFromAPI(TASKS_URL);
//            user channel check
            getChannelFromAPI(CHNNEL_URL);
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    private void loadTasksFromAPI(String url) {
        GetTasksTask getTasksTask = new GetTasksTask(MainActivity.this);
        getTasksTask.setMessageLoading("Loading tasks...");
        getTasksTask.execute(url + "?auth_token=" + mPreferences.getString("AuthToken", ""));
    }

    private class GetTasksTask extends UrlJsonAsyncTask {
        public GetTasksTask(Context context) {
            super(context);
        }
        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            String response = null;
            JSONObject json = new JSONObject();

            try {
                try {
                    // setup the returned values in case
                    // something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");

                    // setup the request headers
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(post, responseHandler);
                    json = new JSONObject(response);

                }catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO", "" + e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON", "" + e);
            }
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getBoolean("success")) {
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putString("uid", json.getJSONObject("data").getString("user_id"));
                    editor.putString("email", json.getJSONObject("data").getString("email"));
                    editor.commit();
                }
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }

    private void getChannelFromAPI(String url) {
        GetChannelURL getChannelURL = new GetChannelURL(MainActivity.this);
        getChannelURL.setMessageLoading("Get your channel");
        getChannelURL.execute(url + "?user_id="+mPreferences.getString("uid",""));
    }

    private class GetChannelURL extends UrlJsonAsyncTask {
        public GetChannelURL(Context context) {
            super(context);
        }
        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            String response = null;
            JSONObject json = new JSONObject();

            try {
                try {
                    // setup the returned values in case
                    // something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");

                    // setup the request headers
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(post, responseHandler);
                    json = new JSONObject(response);

                }catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO", "" + e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON", "" + e);
            }
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            sdata.clear();
            try {
                if (json.getBoolean("success")) {
                    String info = json.getString("info");
                    Log.d("GetList", "info" + info);
                    JSONArray dataJson = json.getJSONArray("data");
                    for(int i = 0; i < dataJson.length(); i++) {
                        JSONObject data = dataJson.getJSONObject(i);
                        String id = data.getString("id");
                        Log.d("GetList", "id " + id);
                        String name = data.getString("name");
                        Log.d("GetList", "name " + name);
                        String imgUrl = data.getString("img");
                        Log.d("GetList", "img " + imgUrl);
                        String link = data.getString("link");
                        Log.d("GetList", "link " + link);
                        String platform = data.getString("platform_type");
                        if (platform.equals("t"))
                            platform = "트위치";
                        else if (platform.equals("k"))
                            platform = "카카오TV";
                        else if (platform.equals("y"))
                            platform = "유튜브";
                        Log.d("GetList", "platform " + platform);
                        sdata.add(new channelItem(id, name, imgUrl, link, platform));
                        Log.d("GetList", "저장 성공 " + i);
                    }
                    channelList.setAdapter(mAdapter);
                    mAdapter.setList(sdata);
                    Log.d("GetList", "setlist");
                }
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }

//    ABOUT TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_settings2:
                intent = new Intent(this, LogoutActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    //서버로 삭제할 채널정보 보내기 ###############################################################################
    private class sendTask extends AsyncTask<channelItem, Void, Void> {

        @Override
        protected Void doInBackground(channelItem... objects) {
            sendPost(objects[0]);
            return null;
        }
        public void sendPost(channelItem c) {
            client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), 30000); //타임아웃 설정
            String url = REMOVE_URL + "?auth_token=" + mPreferences.getString("AuthToken", "");
            Log.d("서버전송", "URL: " + url);
            post = new HttpPost(url);
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("AuthToken", mPreferences.getString("AuthToken", ""));
                jsonObject.put("id", c.getId());
                StringEntity entity = new StringEntity(jsonObject.toString(), HTTP.UTF_8);
                Log.d("서버전송", "try");
                //UrlEncodedFormEntity entity = new UrlEncodedFormEntity(post, "UTF-8");
                entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(entity);
                client.execute(post);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
    //리스트뷰 클릭
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final channelItem dto = sdata.get(position);
            Log.d("채널삭제", "onclick list");
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("채널 삭제")
                    .setMessage(dto.getName() + " 채널을 삭제할까요?")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("채널삭제", "확인버트 누르고");
                            sendTask = new sendTask().execute(dto);
                            mAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), "삭제성공", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    };

}
