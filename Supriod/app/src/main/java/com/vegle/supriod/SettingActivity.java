package com.vegle.supriod;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {
    Info info;
    SharedPreferences preferences ;
    SharedPreferences.Editor editor ;
    EditText IP;
    EditText Macpwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        info = new Info();
        preferences = getSharedPreferences("userinfo",MODE_PRIVATE);
        editor = preferences.edit();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar settingtoolbar = (Toolbar) findViewById(R.id.settingtoolbar);
        settingtoolbar.setNavigationIcon(R.drawable.ic_back);
        settingtoolbar.inflateMenu(R.menu.menu_setting);
        Macpwd = (EditText)findViewById(R.id.macpwd) ;
        IP = (EditText)findViewById(R.id.setuserIP);
        Macpwd.setText(preferences.getString("macpwd","").toString().trim());
        IP.setText(preferences.getString("userip","").toString().trim());
        settingtoolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.save:
                        Toast.makeText(getApplicationContext(),"保存",Toast.LENGTH_SHORT).show();
                        save();
                        break;
                }
                return false;
            }
        });
        settingtoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
public void save(){
    editor.putString("userip",IP.getText().toString().trim());
    editor.putString("macpwd",Macpwd.getText().toString().trim());
    editor.commit();
    info.setmacadd(new Decrypt_Operation().decrypt(preferences.getString("macpwd","0")).trim());
    info.setlocalIp(preferences.getString("userip"," ").trim());
    Toast.makeText(getApplicationContext(),"保存成功！",Toast.LENGTH_SHORT).show();
}
}
