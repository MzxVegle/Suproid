package com.vegle.supriod;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Vegle on 2017/10/27.
 */

public class ErrorInfoActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView errorview;
    Thread thread;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        toolbar = (Toolbar)findViewById(R.id.errtoolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        errorview = (TextView)findViewById(R.id.errview);
        errorview.setMovementMethod(new ScrollingMovementMethod());
        Button interrupt = (Button)findViewById(R.id.interup) ;
        Button clean = (Button)findViewById(R.id.clean);
        clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CatchError().clean();
            }
        });
        interrupt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thread.interrupt();
            }
        });
        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    Message msg = new Message();
                    handler.sendMessage(msg);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }

                }
            }
        });
        thread.start();

        //errorview.setScrollbarFadingEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
Handler handler = new Handler(){
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        errorview.setText(new CatchError().geterror());
        int offset = errorview.getLineCount()*errorview.getLineHeight();
        if(offset > errorview.getHeight()){
            errorview.scrollTo(0,offset-errorview.getHeight());
        }
    }
};
    @Override
    protected void onResume() {
        super.onResume();

    }
}
