package com.vegle.supriod;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class UserinfoActivity extends AppCompatActivity {
    Info info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        info = new Info();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.infotoolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setFitsSystemWindows(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        load_info();
    }
    public void load_info(){
        TextView macaddr = (TextView)findViewById(R.id.macaddr);
        TextView serviceIp = (TextView)findViewById(R.id.serviceIP);
        TextView userIp = (TextView)findViewById(R.id.userIP);
        SharedPreferences preferences = getSharedPreferences("userinfo",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        macaddr.setText(info.getMacadd());
        serviceIp.setText(info.getSeverIp());
        userIp.setText(preferences.getString("userip",""));
    }
}
