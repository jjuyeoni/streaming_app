package org.teckown.hello;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText param;
    int num = 3;

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
            Toast.makeText(getApplicationContext(), "유투브", Toast.LENGTH_SHORT).show();
            searchYoutube();
        }
        else if( num == 1) {
            WebView myWebView = (WebView) findViewById(R.id.webview);
            myWebView.loadUrl("https://tv.kakao.com/search?q="+param.getText());
        }
        else if( num == 2 )
            Toast.makeText(getApplicationContext(),
                    "트위치", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(),
                    "에러", Toast.LENGTH_SHORT).show();
    }

    public void searchYoutube() {

    }
}
