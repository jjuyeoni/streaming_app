package org.teckown.hello;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
import java.util.HashMap;

public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText param;
    int num = 3;
    WebView myWebView;
    ListView searchlist;
    ListViewAdapter mAdapter;
    static DrawableManager DM = new DrawableManager();
    final String serverKey = "AIzaSyDYQy1GKyWfPm9ZostsAYWOTvAmrBwtvVQ";  //유튜브api 서버키
    final String twitchKey = "nh1k78l9z6ohg6md25a56ewdeks48u";            //트위치키
    private String serverUrl = "https://avocadoapi.herokuapp.com/api/v1/channels/save?auth_token="; //채널저장url
    private SharedPreferences mPreferences;

    //파싱데이터 담기
    ArrayList<listViewItem> sdata = new ArrayList<listViewItem>();

    AsyncTask<?,?,?> searchTask; //유튜브 검색
    AsyncTask<?,?,?> searchTaskTwitch; //트위치 검색
    AsyncTask<?,?,?> sendTask; //채널저장 ( android -> server)
    AsyncTask<?,?,?> searchTaskKakao; //카카오 검색

    HttpClient client;
    HttpPost post;
    boolean nullCheck = false;

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
        spinner.setOnItemSelectedListener(this);

        param = (EditText)findViewById(R.id.edTxtParam);
        //웹뷰
        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setVisibility(View.INVISIBLE);
        //검색결과 리스트뷰
        searchlist = (ListView) findViewById(R.id.listview);
        searchlist.setVisibility(View.INVISIBLE);
        //커스텀 어댑터
        mAdapter = new ListViewAdapter(SearchActivity.this, R.layout.listview_search, sdata);
        searchlist.setAdapter(mAdapter);
        //리스트뷰 클릭 리스너
        searchlist.setOnItemClickListener(onItemClickListener);
        //유저정보
        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        num = pos;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    //플랫폼별 채널 검색 버튼 onClick ##########################################################################
    public void onClick(View v){
        if( num == 0 ) {
            myWebView.setVisibility(View.INVISIBLE);
            searchlist.setVisibility(View.VISIBLE);
            Log.d("Youtube", "유튜브 선택됨");
            searchTask = new searchTask().execute();
        }
        else if( num == 1) {
            myWebView.setVisibility(View.INVISIBLE);
            searchlist.setVisibility(View.VISIBLE);
            //myWebView.loadUrl("https://tv.kakao.com/search?q="+param.getText());
            searchTaskKakao = new searchTaskKakao().execute();
        }
        else if( num == 2 ) {
            myWebView.setVisibility(View.INVISIBLE);
            searchlist.setVisibility(View.VISIBLE);
            Log.d("트위치", "선택됨");
            searchTaskTwitch = new searchTaskTwitch().execute();
        }
        else
            Toast.makeText(getApplicationContext(),
                    "ERROR", Toast.LENGTH_SHORT).show();
    }

    //카카오 크롤링 api##########################################################################################
    private class searchTaskKakao extends AsyncTask<Void, Void, Void> {

        //ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // progressDialog = ProgressDialog.show(SearchActivity.this, "...", "검색중");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("Kakao", "doinBack");
            try {
                paringJsonData(getKakao(param.getText()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override

        protected void onPostExecute(Void result) {
            Log.d("Kakao", "onPostExecute");
//            ListViewAdapter mAdapter = new ListViewAdapter(SearchActivity.this, R.layout.listview_search, sdata);
            searchlist.setAdapter(mAdapter);
            if (nullCheck == false) {
                mAdapter.setList(sdata);
                Log.d("Kakao", "어댑터에 뿌리기");
            }
            //else setText("해당 채널이 없습니다");
            //Log.d("Twitch", "검색 결과가 없음");
        }

        public JSONObject getKakao(Editable q) {
            HttpPost httpPost = new HttpPost("https://avocadoapi.herokuapp.com/api/v1/channels/kakaoSearch?word=" + q);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                Log.d("Kakao", "2try");
                response = client.execute(httpPost);
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
            Log.d("Kakao", "json 파서");
            sdata.clear();
            String info = jsonObject.getString("info");
            boolean success = jsonObject.getBoolean("success");
            Log.d("Kakao", "info" + info);
            Log.d("Kakao", "success" + success);
            JSONObject dataJson = jsonObject.getJSONObject("data");

            JSONArray c_name = dataJson.getJSONArray("c_name");
            JSONArray c_link = dataJson.getJSONArray("c_link");
            JSONArray c_img = dataJson.getJSONArray("c_img");
            JSONArray c_platform_type = dataJson.getJSONArray("c_platform_type");

            for (int i = 0; i < c_name.length(); i++) {
                Log.d("Kakao", "c_name" + c_name.getString(i));
                Log.d("Kakao", "c_link" + c_link.getString(i));
                Log.d("Kakao", "c_img" + c_img.getString(i));
                String name = c_name.getString(i);
                String nameUTF = "";
                try {
                    nameUTF = new String(name.getBytes("8859_1"), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                sdata.add(new listViewItem("null",nameUTF, "null", c_link.getString(i), "https:" + c_img.getString(i), "k"));//순서 title user time url thumnail platform
                Log.d("Kakao", "저장 성공 " + i);
            }

        }
    }

    //트위치 api   ####################################################################################
    private class searchTaskTwitch extends AsyncTask<Void, Void, Void> {

        //ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // progressDialog = ProgressDialog.show(SearchActivity.this, "...", "검색중");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("Twitch", "doinBack");
            try {
                paringJsonData(getTwitch(param.getText()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override

        protected void onPostExecute(Void result) {
            Log.d("Twitch", "onPostExecute");
//            ListViewAdapter mAdapter = new ListViewAdapter(SearchActivity.this, R.layout.listview_search, sdata);
            searchlist.setAdapter(mAdapter);
            Log.d("Twitch", "nullcheck : " + nullCheck );
            if (nullCheck == false) {
                mAdapter.setList(sdata);
                Log.d("Twitch", "어댑터에 뿌리기");
            }
            else {
                Toast.makeText(getApplicationContext(),
                        "해당 채널이 없습니다.", Toast.LENGTH_SHORT).show();
                nullCheck = false;
            }
        }

        public JSONObject getTwitch(Editable q) {
            HttpGet httpGet = new HttpGet(// part(snippet), q(검색값) , key(서버키)
                    "https://api.twitch.tv/kraken/search/channels?query=" + q +"&client_id=" + twitchKey + "&limit=1"
            );
            Log.d("Twitch", "httpGEt: " + httpGet);

            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                Log.d("Twitch", "2try");
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
            Log.d("Twitch", "json 파서");
            sdata.clear();
            String total = jsonObject.getString("_total");
            Log.d("Twitch", "total " + total);
            if (total.equals("0")) {
                nullCheck = true;
            }
            else {
                JSONArray contacts = jsonObject.getJSONArray("channels");
                for (int i = 0; i < contacts.length(); i++) {
                    Log.d("Twitch", "for");
                    JSONObject c = contacts.getJSONObject(i);

                    String user = c.getString("display_name");
                    String userName = "";
                    // 제목 urf8설정
                    try {
                        userName = new String(user.getBytes("8859_1"), "utf-8");
                        Log.d("Twitch", "for1");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    String userID = c.getString("name");
                    Log.d("Twitch", "for2");
                    String url = "https://www.twitch.tv/" + userID;

                    String date = c.getString("created_at") // 등록날짜
                            .substring(0, 10);
                    String imgUrl = c.getString("profile_banner"); // 프로필 이미지 URL값
                    Log.d("Twitch", "URL: " + imgUrl);
                    sdata.add(new listViewItem(userID, userName, date, url, imgUrl, "t"));//순서 title user time url thumnail
                    Log.d("Twitch", "성공!!");
                }
            }
        }
    }

    //Youtube api   ###################################################################################
    private class searchTask extends AsyncTask<Void, Void, Void> {

        //ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           // progressDialog = ProgressDialog.show(SearchActivity.this, "...", "검색중");
        }
        @Override
        protected Void doInBackground(Void... params) {
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
//            ListViewAdapter mAdapter = new ListViewAdapter(SearchActivity.this, R.layout.listview_search, sdata);
            searchlist.setAdapter(mAdapter);
            mAdapter.setList(sdata);
            Log.d("Youtube", "nullcheck : " + nullCheck );
            if (nullCheck == false) {
                mAdapter.setList(sdata);
                Log.d("Youtube", "어댑터에 뿌리기");
            }
            else {
                Toast.makeText(getApplicationContext(),
                        "해당 채널이 없습니다.", Toast.LENGTH_SHORT).show();
                nullCheck = false;
            }
        }
        public JSONObject getUtube(Editable q) {
            HttpGet httpGet = new HttpGet(// part(snippet), q(검색값) , key(서버키)
                    "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=7&key=" + serverKey + "&q=" + q
            );
            Log.d("Youtube", "httpGEt: " + httpGet);

            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();
            try {
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
            Log.d("Youtube", "json 파서");
            sdata.clear();
            String totalResults = jsonObject.getJSONObject("pageInfo").getString("totalResults");

            Log.d("Youtube", "total : " + totalResults);
            if (totalResults.equals("0")) {
                nullCheck = true;
            }
            else {
                Log.d("Youtube", "else");
                JSONArray contacts = jsonObject.getJSONArray("items");
                for (int i = 0; i < contacts.length(); i++) {
                    JSONObject c = contacts.getJSONObject(i);

                    String title = c.getJSONObject("snippet").getString("title");
                    String titleUTF8 = "";
                    // 제목 urf8설정
                    try {
                        titleUTF8 = new String(title.getBytes("8859_1"), "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    String user = c.getJSONObject("snippet").getString("channelTitle");
                    String userUTF8 = "";
                    //user 이름 utf8설정
                    try {
                        userUTF8 = new String(user.getBytes("8859_1"), "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    JSONObject jsonOb = c.getJSONObject("id");
                    String url = ""; //채널 url 채널 나옴
                    String vUrl = ""; //영상 url 영상 나옴
                    if (jsonOb.has("videoId")) { //video일 경우
                        String id = jsonOb.getString("videoId");
                        url = "https://www.youtube.com/watch?v=" + id;
                    } else { //채널일 경우
                        String id = jsonOb.getString("channelId");
                        url = "https://www.youtube.com/channel/" + id;
                    }

                    String date = c.getJSONObject("snippet").getString("publishedAt") // 등록날짜
                            .substring(0, 10);
                    String imgUrl = c.getJSONObject("snippet")
                            .getJSONObject("thumbnails").getJSONObject("default")
                            .getString("url"); // 썸내일 이미지 URL값
                    Log.d("Youtube", "제목 : " + title);
                    Log.d("Youtube", "URL: " + imgUrl);
                    sdata.add(new listViewItem(titleUTF8, userUTF8, date, url, imgUrl, "y"));//순서 title user time url thumnail platform
                    Log.d("Youtube", "성공!!");
                }
            }
        }
    }

    //서버로 저장할 채널정보 보내기 ###############################################################################
    private class sendTask extends AsyncTask<listViewItem, Void, Void> {

        @Override
        protected Void doInBackground(listViewItem... objects) {
            sendPost(objects[0]);
            return null;
        }
        public void sendPost(listViewItem list) {
            client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), 30000); //타임아웃 설정
            serverUrl += mPreferences.getString("AuthToken", ""); //토큰값 추가
            Log.d("서버전송", "URL: " + serverUrl);
            post = new HttpPost(serverUrl);
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("AuthToken", mPreferences.getString("AuthToken", ""));
                jsonObject.put("name", list.getUser());
                jsonObject.put("img", list.getThumnail());
                jsonObject.put("link", list.getUrl());
                jsonObject.put("platform_type", list.getPlatform());
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

    //리스트뷰 클릭시 채널저장 다이얼로그    #################################################################
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final listViewItem dto = sdata.get(position);
            Log.d("서버전송", "onclick list");
            AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
            builder.setTitle("채널 등록")
                    .setMessage(dto.getUser() + " 채널을 등록할까요?")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("서버전송", "확인버트 누르고");
                            sendTask = new sendTask().execute(dto);
                            Toast.makeText(getApplicationContext(), "등록성공", Toast.LENGTH_LONG).show();
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

