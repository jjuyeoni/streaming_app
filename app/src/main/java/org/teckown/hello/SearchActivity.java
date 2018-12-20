package org.teckown.hello;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatDrawableManager;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText param;
    int num = 3;
    WebView myWebView;
    ListView searchlist;
    static DrawableManager DM = new DrawableManager();
    final String serverKey = "AIzaSyDYQy1GKyWfPm9ZostsAYWOTvAmrBwtvVQ";
    ArrayList<listViewItem> sdata = new ArrayList<listViewItem>();
    AsyncTask<?,?,?> searchTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.video_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        param = (EditText)findViewById(R.id.edTxtParam);
        //웹뷰
        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setVisibility(View.INVISIBLE);

        searchlist = (ListView) findViewById(R.id.listview);
        searchlist.setVisibility(View.INVISIBLE);

        spinner.setOnItemSelectedListener(this);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        num = pos;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void onClick(View v){
        if( num == 0 ) {
            myWebView.setVisibility(View.INVISIBLE);
            searchlist.setVisibility(View.VISIBLE);
            Log.d("Youtube", "유튜브 선택됨");
            searchTask = new searchTask().execute();
        }
        else if( num == 1) {
            searchlist.setVisibility(View.INVISIBLE);
            myWebView.setVisibility(View.VISIBLE);
            myWebView.loadUrl("https://tv.kakao.com/search?q="+param.getText());
        }
        else if( num == 2 ) {
            Log.d("트위치", "선택");
            Toast.makeText(getApplicationContext(),
                    "트위치", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(),
                    "에러", Toast.LENGTH_SHORT).show();
    }


    private class searchTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           // progressDialog = ProgressDialog.show(SearchActivity.this, "...", "검색중");
        }
        @Override
        protected Void doInBackground(Void... params) {
            Log.d("Youtube", "doinBack");
            try {
                paringJsonData(getUtube(param.getText()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override

        protected void onPostExecute(Void result) {
            Log.d("Youtube", "onPostExecute");
            ListViewAdapter mAdapter = new ListViewAdapter(SearchActivity.this, R.layout.listview_search, sdata);
            searchlist.setAdapter(mAdapter);
            mAdapter.setList(sdata);
            Log.d("Youtube", "어댑터에 뿌리기");
        }
        public JSONObject getUtube(Editable q) {
            HttpGet httpGet = new HttpGet(
                    "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=7&key=" + serverKey + "&q=" + q
            );
            Log.d("Youtube", "httpGEt: " + httpGet);
// part(snippet), q(검색값) , key(서버키)
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                Log.d("Youtube", "2try");
                response = client.execute(httpGet);
                Log.d("Youtube", "http client");
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
// 파싱을 하면 여러가지 값을 얻을 수 있는데 필요한 값들을 세팅하셔서 사용하시면 됩니다.

        private void paringJsonData(JSONObject jsonObject) throws JSONException {
            Log.d("Youtube", "json 파서");
            sdata.clear();
            JSONArray contacts = jsonObject.getJSONArray("items");
            for (int i = 0; i < contacts.length(); i++) {
                Log.d("Youtube", "for");
                JSONObject c = contacts.getJSONObject(i);

                String title = c.getJSONObject("snippet").getString("title");
                String titleUTF8 = "";
                // 제목 urf8설정
                try {
                    titleUTF8 = new String(title.getBytes("8859_1"), "utf-8");
                    Log.d("Youtube", "for1");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String user = c.getJSONObject("snippet").getString("channelTitle");
                Log.d("Youtube", "for2");
                String userUTF8 = "";
                //user 이름 utf8설정
                try {
                    userUTF8 = new String(user.getBytes("8859_1"), "utf-8");
                    Log.d("Youtube", "for3");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                JSONObject jsonOb = c.getJSONObject("id");
                Log.d("Youtube", "for4");
                String url = ""; //채널 url 채널 나옴
                String vUrl = ""; //영상 url 영상 나옴
                if ( jsonOb.has("videoId") ){ //video일 경우
                    Log.d("Youtube", "for video");
                    String id = jsonOb.getString("videoId");
                    url = "https://www.youtube.com/watch?v=" + id;
                    Log.d("Youtube", "id" + id);
                    Log.d("Youtube", "url" + url);
                }
                else { //채널일 경우
                    Log.d("Youtube", "for");
                    String id = jsonOb.getString("channelId");
                    url = "https://www.youtube.com/channel/" + id;
                    Log.d("Youtube", "id" + id);
                    Log.d("Youtube", "url" + url);
                }

                String date = c.getJSONObject("snippet").getString("publishedAt") // 등록날짜
                      .substring(0, 10);
                String imgUrl = c.getJSONObject("snippet")
                        .getJSONObject("thumbnails").getJSONObject("default")
                        .getString("url"); // 썸내일 이미지 URL값
                Log.d("Youtube", "제목 : " + title);
                Log.d("Youtube", "URL: " + imgUrl);
                sdata.add(new listViewItem(titleUTF8, userUTF8, date, url, imgUrl));//title user time url thumnail
                Log.d("Youtube", "성공!!");
            }
        }
    }
}

