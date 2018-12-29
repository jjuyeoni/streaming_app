package org.teckown.hello;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class channelAdapter extends BaseAdapter {
    ArrayList<channelItem> list;
    int layout;
    Context context;
    LayoutInflater inflater;

    public channelAdapter(Context context, int resource, ArrayList<channelItem> list) {
        this.context = context;
        this.layout = resource;
        this.list = list;
        inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    public void setList(ArrayList<channelItem> list) {
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String imgUrl;
        final int pos = position;

        if ( convertView == null) {
            convertView = inflater.inflate(layout, parent, false);
        }
        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        final ImageView image = (ImageView) convertView.findViewById(R.id.listImage) ;
        TextView nameTextView = (TextView) convertView.findViewById(R.id.listname) ;
        TextView platTextView = (TextView) convertView.findViewById(R.id.listplatform) ;

        // Data Set에서 position에 위치한 데이터 참조 획득
        channelItem channelItems = list.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        nameTextView.setText(channelItems.getName());
        platTextView.setText(channelItems.getPlatform_type());
        imgUrl = channelItems.getImg();
        Log.d("listAdapter", "URL: " + imgUrl);
        //URL이용해 이미지 불러오기 스레드
        Thread mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Log.d("listAdapter", "쓰레드");
                    URL url = new URL(imgUrl); // URL 주소를 이용해서 URL 객체 생성
                    Log.d("listAdapter", "url : " + url);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    image.setImageBitmap(bitmap); //   이미지 뷰에 지정할 Bitmap을 생성
                    Log.d("Youtube", "비트맵 생성");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        mThread.start();
        return convertView;
    }
}
