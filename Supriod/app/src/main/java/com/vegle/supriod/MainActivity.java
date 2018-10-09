package com.vegle.supriod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static DatagramSocket udpSocket;
    static Info info = new Info();
    public  static  int timeoutcut=0;
    TextView msgView;
    TextView UAEmsgView;
    static byte[] session ;
    EditText IDtxt;
    EditText PWDtxt;
    long firsttime = 0;
    BroadcastReceiver myReceive = null;
    Toolbar toolbar;
    Button login;
    Button downbtm;
    SharedPreferences save;
    SharedPreferences.Editor editor;
    CheckBox rmb;
    TextView t4;
    boolean check = false;
    boolean checkservice = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        save = getSharedPreferences("userinfo",MODE_PRIVATE);
        editor = save.edit();
        rmb = (CheckBox)findViewById(R.id.remember);
        msgView = (TextView)findViewById(R.id.textView3);
        UAEmsgView = (TextView)findViewById(R.id.MessageView) ;
        IDtxt = (EditText) findViewById(R.id.IDtext);
        PWDtxt = (EditText) findViewById(R.id.PWDtext);
        toolbar = (Toolbar)findViewById(R.id.toolbar2);
        toolbar.setFitsSystemWindows(true);
        login = (Button)findViewById(R.id.Loginbtm);
        downbtm = (Button)findViewById(R.id.down);
        t4 = (TextView)findViewById(R.id.textView4) ;
        Login_Click();
        Down_Click();
        creatmsgBCS();
        clickCheckBox();
        setinfo();

        toolbar.inflateMenu(R.menu.menu_activity);
        /*toolbar菜单的点击事件*/
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.setting:
                        Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                        startActivity(intent);
                       // Toast.makeText(MainActivity.this,"设置",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.userinfo:
                        Intent intent1 = new Intent(MainActivity.this,UserinfoActivity.class);
                        startActivity(intent1);
                        //Toast.makeText(MainActivity.this,"用户信息",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.errorinfo:
                        Intent intent2 = new Intent(MainActivity.this,ErrorInfoActivity.class);
                        startActivity(intent2);
                        break;
                }
                return false;
            }
        });

        if(info.getStatus() == 1){
            login.setEnabled(false);
            downbtm.setEnabled(true);
        }else{
            login.setEnabled(true);
            downbtm.setEnabled(false);
        }
        /*加载的时候如果checkbox是勾选状态，则读取配置文件中的登录信息*/
        if(save.getBoolean("CheckBox",false)){
            IDtxt.setText(save.getString("username"," "));
            PWDtxt.setText(save.getString("password"," "));
            rmb.setChecked(save.getBoolean("CheckBox",false));
        }

    }
    /*
    *   检查checkbox的勾选与否
    * */
    public void clickCheckBox(){
        rmb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rmb.isChecked()){
                    //Toast.makeText(MainActivity.this,"被选中",Toast.LENGTH_SHORT).show();
                    saveinfo();
                }else{
                    //Toast.makeText(MainActivity.this,"未被选中！",Toast.LENGTH_SHORT).show();
                    editor.putBoolean("CheckBox",false);//记住未勾选的状态，写入配置文件中
                    editor.commit();
                }
            }
        });
    }
    /*
    *   保存信息到配置文件中
    * */
    public void saveinfo(){
        if(!IDtxt.getText().toString().isEmpty())       //判断是否为空
        {
            editor.putString("username",IDtxt.getText().toString());
            editor.putString("password",PWDtxt.getText().toString());
            editor.putBoolean("CheckBox",rmb.isChecked());
            editor.commit();
            Toast.makeText(MainActivity.this,"保存成功！",Toast.LENGTH_SHORT).show();
        }else {
            rmb.setChecked(false);
            Toast.makeText(MainActivity.this,"如果要保存账号和密码，则账号和密码不能为空！",Toast.LENGTH_SHORT).show();
        }

    }
    /*
    *
    *       设置info类的信息，从配置文件中读取
    * */
    public void setinfo(){
        info.setusername(save.getString("username",""));
        info.setpassword(save.getString("password",""));
        info.setlocalIp(save.getString("userip",""));
            info.setmacadd(new Decrypt_Operation().decrypt(save.getString("macpwd", "0")).trim());

        //Toast.makeText(MainActivity.this,"username:"+info.getUsername()+"\n"+"PWD:"+
               // info.getPWD(),Toast.LENGTH_SHORT).show();
    }

    //"action"广播接收器注册
    public void creatmsgBCS(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("action");
        myReceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkservice = intent.getExtras().getBoolean("alive");//获得alive的值
                msgView.setText(intent.getStringExtra("tag"));//获取tag标签的信息
                //检查button的enable的标记btnenflag
                if(intent.getExtras().getInt("btnenflag") == 1)
                {
                    login.setEnabled(false);
                    downbtm.setEnabled(true);

                }else {
                    login.setEnabled(true);
                    downbtm.setEnabled(false);
                }
                if(intent.getBooleanExtra("clean",false)){
                    msgView.setText("欢迎使用");
                    //UAEmsgView.setText("");
                }
            }
        };
        registerReceiver(myReceive,filter);//注册广播
    }
/*
*
*
*       下线事件
*
* */
    public  void Down_Click(){

        downbtm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("notiService");
                intent.putExtra("thread",false);
                sendBroadcast(intent);
            }
        });

    }
    /*
    *
    *
    *           登陆事件
    *           用Toast来展示结果
    *           反馈信息
    *
    * */
    public void Login_Click(){
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Toast.makeText(MainActivity.this,"Test",Toast.LENGTH_SHORT).show();
                UploadClick();

            }
        });
    }
/*
*           上线按钮点击事件
*
* */
    public void UploadClick() {
        //Button loginbtm = (Button) findViewById(R.id.Loginbtm);
        final TextView textView = (TextView) findViewById(R.id.MessageView);
        info.setusername(IDtxt.getText().toString());
        info.setpassword(PWDtxt.getText().toString());
        textView.setText("正在上线...");
        //textView.setText( "\n用户名：" + info.getUsername() + "\n密码：" + info.getPWD());
        if (IDtxt.getText().toString().isEmpty() || PWDtxt.getText().toString().isEmpty()) {
            //Toast.makeText(MainActivity.this, "用户名：" + info.getUsername() + "\n密码：" + info.getPWD(), Toast.LENGTH_SHORT).show();
            textView.setText("请输入用户名和密码");

        } else {
            if(check==false){

                //在子线程中执行上线和打开服务的功能
               new Thread(new Runnable() {
                    @Override
                    public void run() {
                       //
                        check = true;           //线程是否在运行的标记位
                        load_udpSocket();
                        byte[] session = upload();  //执行上线函数，并且返回session
                        timeoutcut = 0;
                        close_udpSocket();
                        if (info.getStatus() == 1) {
                            Intent intent = new Intent(MainActivity.this, mainService.class);
                            //intent.putExtra("session", session);
                            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            /*try {
                                breathing(session);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                            startService(intent);//打开服务

                        }
                        check = false;
                    }
                }).start();
                }else{
                Toast.makeText(MainActivity.this,"正在连接远程服务器，请稍等...",Toast.LENGTH_SHORT).show();

            }
            }
    }


/*
*
*
*       上线函数
*
* */
    private byte[] upload(){
        InetAddress severip = null;
        try {
            severip = InetAddress.getByName(info.getSeverIp());
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
        DatagramPacket pushdata;
        byte[] conetpacket = conetPacket();
        byte[] buffer = new byte[4096];
        pushdata = new DatagramPacket(conetpacket,conetpacket.length,severip,3848);
        try{
            udpSocket.send(pushdata);
            DatagramPacket recvdata = new DatagramPacket(buffer,buffer.length);
            udpSocket.receive(recvdata);
            int buffersize = recvdata.getLength();
            final byte[] recvpacket = decrypt(Arrays.copyOf(buffer,buffersize));
            byte[] recvmd5 = new byte[16];
            for(int i=2;i<18;i++){
                recvmd5[i-2] = recvpacket[i];
                recvpacket[i] = 0;

            }
            int sessionLen = recvpacket[22];

            if(compare_Md5(recvmd5,getMd5(recvpacket))){
                byte[] session = new byte[sessionLen];
                for(int i=0,j=23;j<sessionLen+23;i++,j++){
                    session[i] = recvpacket[j];
                    recvpacket[j] = 0;
                }

                UAEmsgView.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            UAEmsgView.setText(show_message(recvpacket));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                });
                info.setStatus(recvpacket[20]);
               // Thread.currentThread().interrupt();
                this.session = session;
                info.session = this.session;
                return session;

            }
        }catch (final IOException e){
            timeoutcut++;   //判断上线连接出错次数，连续出错3次以上。则停止上线
            if(timeoutcut<=3){
                UAEmsgView.post(new Runnable() {
                    @Override
                    public void run() {
                        UAEmsgView.setText("连接失败！第"+Integer.toString(timeoutcut)+"次重连");
                    }
                });
                upload();
            }
            else {
                timeoutcut = 0;
                UAEmsgView.post(new Runnable() {
                    @Override
                    public void run() {
                        UAEmsgView.setText("连接远程服务器失败！请检查网络是否连接！" + e.getMessage());
                    }
                });
            }
        }
        return null;
    }
    /*
    *
    *
    *              初始化套接字
    *
    *
    * */
    private void load_udpSocket(){
        try {
            udpSocket = new DatagramSocket(null);
            udpSocket.setReuseAddress(true);
            udpSocket.setSoTimeout(5000);
            udpSocket.bind(new InetSocketAddress(3848));
        }catch (SocketException e){
            e.printStackTrace();
            /*Looper.prepare();
            Toast.makeText(this,"端口号被占用，无法登陆！",Toast.LENGTH_SHORT).show();
            Looper.loop();*/
        }
    }
    /*
 *
 *
 * 				消息打印函数
 * 				打印服务器返回信息
 *
 *
 */
    private static String show_message(byte[] recvPacket) throws UnsupportedEncodingException {
        int messid = -1;
        for(int i = 0;i<recvPacket.length;i++){
            if(recvPacket[i] == 11)
            {
                messid = i;
            }
        }
        int messlen = recvPacket[messid+1] & 0xFF;
        byte[] message = new byte[messlen];
        message = Arrays.copyOfRange(recvPacket, messid+2, messid+2+messlen);
        String m = new String(message,"GBK");
        return m;
    }
    /*
    *
    *
    *
    *               关闭套接字
    *
    *
    *
    * */
    private  void close_udpSocket(){
        udpSocket.close();
    }
    /*
    *
    *           创建一个connect包
    *
    * */
    public byte[] conetPacket() {
        int packetLen ;
        packetLen = 38+info.getUsername().length()+info.getPWD().length()+info.getDhcp_setting().length()
                +info.getLocalIP().length()+info.getSeverIp().length()+info.getVersion().length();
        byte[] packet = new byte[packetLen];
        byte i = -1;
        packet[++i] = 0x01;         //表示动作：连接
        for(;i<17;){
            packet[++i] = 0;        //空出16个位置，先置为0，后再存md5
        }
        packet[++i] = 0x07;
        packet[++i] = 0x08;
        String[] macs = info.getMacadd().split(":");
        for(String a:macs){
            packet[++i] = (byte)Integer.parseInt(a,16);
        }
        packet[++i] = 0x01;             //用户名的标志
        packet[++i] = (byte)(info.getUsername().length()+2);
        for(byte a:info.getUsername().getBytes()){
            packet[++i] = a;
        }
        packet[++i] = 0x02;             //密码的标志
        packet[++i] = (byte)(info.getPWD().length()+2);
        for(byte a:info.getPWD().getBytes()){
            packet[++i] = a;
        }
        packet[++i] = 0x09;             //localIp的标志
        packet[++i] = (byte)(info.getLocalIP().length()+2);
        for(byte a:info.getLocalIP().getBytes()){
            packet[++i] = a;
        }
        packet[++i] = 0x0a;             //type的标志
        packet[++i] = (byte)(info.getType().length()+2);
        for (byte a:info.getType().getBytes()){
            packet[++i] = a;
        }
        packet[++i] = 0x0e;            //dhcp_setting的标志
        packet[++i] = (byte)(info.getDhcp_setting().length()+2);
        for (byte a:info.getDhcp_setting().getBytes()){
            packet[++i] = a;
        }
        packet[++i] = 0x1f;            //version的标志
        packet[++i] = (byte)(info.getVersion().length()+2);
        for (byte a:info.getVersion().getBytes()){
            packet[++i] = a;
        }
        byte[] md5 = getMd5(packet);
        for(int n=0;n<md5.length;n++){
            packet[n+2]=md5[n];
        }
        packet = encrypt(packet);
        return  packet;
    }
    private static byte[] getMd5(byte[] md5){
        MessageDigest md5text = null;
        try {
            md5text = MessageDigest.getInstance("MD5");

        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        byte[] md5_packet = md5text.digest(md5);
        return md5_packet;
    }
    /*
	 *
	 *
	 *
	 * 				加密Packet
	 *
	 *
	 * */
    private static byte[] encrypt(byte[] packet) {
        byte[] encrypt_packet = new byte[packet.length];
        int i = 0;
        for (byte b : packet) {
            encrypt_packet[i++] = (byte) ((b & 0x80) >> 6 | (b & 0x40) >> 4 | (b & 0x20) >> 2 | (b & 0x10) << 2
                    | (b & 0x08) << 2 | (b & 0x04) << 2 | (b & 0x02) >> 1 | (b & 0x01) << 7);
        }
        return encrypt_packet;
    }

    /*
     *
     * 				解密算法
     *
     *
     * */
    private static byte[] decrypt(byte[] packet) {
        byte[] decrypt_packet = new byte[packet.length];
        int i = 0;
        for (byte b : packet) {
            decrypt_packet[i++] = (byte) ((b & 0x80) >> 7 | (b & 0x40) >> 2 | (b & 0x20) >> 2 | (b & 0x10) >> 2
                    | (b & 0x08) << 2 | (b & 0x04) << 4 | (b & 0x02) << 6 | (b & 0x01) << 1);
        }
        return decrypt_packet;
    }
    /*
	 *
	 *
	 * 				MD5比较算法
	 * 				比较两个md5的位数是否相同
	 * 				相同return true
	 * 				不同return false
	 *
	 *
	 * */
    private static boolean compare_Md5(byte[] n1,byte[] n2){
        boolean flag = true;
        for(int i = 0;i<n1.length;i++)
        {
            if(n1[i]!=n2[i]){
                flag = false;
                break;
            }
        }
        return flag;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //保存Activity的数据
        outState.putString("msg",UAEmsgView.getText().toString());
        outState.putString("username",info.getUsername());
        outState.putString("password",info.getPWD());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //恢复Activity的数据
        UAEmsgView.setText(savedInstanceState.getString("msg").toString());

        IDtxt.setText(savedInstanceState.getString("username").toString());
        PWDtxt.setText(savedInstanceState.getString("password").toString());
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
            //双击back退出程序的的功能
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                long secondtime = System.currentTimeMillis();
                if(secondtime - firsttime >2000){
                    Toast.makeText(MainActivity.this,"在按一次退出程序",Toast.LENGTH_LONG).show();
                    firsttime = secondtime;
                    return true;
                }else
                {
                    System.exit(0);
                }
                break;
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(rmb.isChecked()){
            saveinfo();
        }
        if(checkservice == false){
            Intent intent = new Intent(MainActivity.this,mainService.class);
            stopService(intent);

        }
        if(myReceive != null ) {
            unregisterReceiver(myReceive);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}
