package org.teckown.hello;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Intent intent;
    private SharedPreferences mPreferences;
    private static final String TASKS_URL = "https://avocadoapi.herokuapp.com/api/v1/sessions/verify";
    //저장된 채널 불러오기
    AsyncTask<?,?,?> getChannelTask;
    //파싱데이터 담기
    ArrayList<channelItem> sdata = new ArrayList<channelItem>();
    ListView channelList;
    channelAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

//      create toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        channelList = (ListView)findViewById(R.id.mainlist);
        //커스텀 어댑터
        mAdapter = new channelAdapter(MainActivity.this, R.layout.listview_main, sdata);
        channelList.setAdapter(mAdapter);
        getChannelTask = new getChannelTask().execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPreferences.contains("AuthToken")) {
            loadTasksFromAPI(TASKS_URL);
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


    // 저장된 채널 리스트뷰 #################################################################################
    private class getChannelTask extends AsyncTask<Void, Void, Void> {
        //ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // progressDialog = ProgressDialog.show(SearchActivity.this, "...", "검색중");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("GetList", "doinBack");
            try {
                paringJsonData(getList());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override

        protected void onPostExecute(Void result) {
            Log.d("GetList", "onPostExecute");
            channelList.setAdapter(mAdapter);
            mAdapter.setList(sdata);
//            if (nullCheck == false) {
//                mAdapter.setList(sdata);
//                Log.d("GetList", "어댑터에 뿌리기");
//            }
            //else setText("해당 채널이 없습니다");
            //Log.d("Twitch", "검색 결과가 없음");
        }

        public JSONObject getList() {
            HttpGet httpGet = new HttpGet("https://avocadoapi.herokuapp.com/api/v1/channels/show?user_id=" + mPreferences.getString("user_id", ""));
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                Log.d("GetList", "2try");
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(stringBuilder.toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return jsonObject;
        }

        //json 파싱
        private void paringJsonData(JSONObject jsonObject) throws JSONException {
            Log.d("GetList", "json 파서");
            sdata.clear();
            String info = jsonObject.getString("info");
            boolean success = jsonObject.getBoolean("success");
            Log.d("GetList", "info" + info);
            Log.d("GetList", "success" + success);
            //여기까지 너무 잘뽑힘

            //여기서부터 안됨
            JSONArray dataJson = jsonObject.getJSONArray("data");
            Log.d("GetList", "data" + dataJson);

            /*for (int i = 0; i < dataJson.length(); i++) {
                Log.d("GetList", "for");
                JSONObject d = dataJson.getJSONObject(i);
                String id = d.getString("id");
                String name = d.getString("name");
                Log.d("GetList", "name : " + name);
                String imgUrl = d.getString("img");
                String link = d.getString("link");
                String platform_type = d.getString("platform_type");
                sdata.add(new channelItem(id, name, imgUrl, link, platform_type));
                Log.d("GetList", "저장 성공 " + i);
            }*/
        }
    }


}
