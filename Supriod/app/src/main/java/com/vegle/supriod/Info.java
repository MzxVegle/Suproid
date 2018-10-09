package com.vegle.supriod;

/**
 * Created by Vegle on 2017/9/29.
 */

public class Info {
    public static String username ="";
    public static String password ="";
    public static String localIp = "10.1.133.18";
    public static String severIp = "192.168.220.10";
    public static String macadd = "";//"1C:B7:2C:9B:6D:F8" ;
    public static byte status = 0 ;
    public static String version = "3.8.7";
    public static String type = "internet";
    public static String dhcp_setting = "0";
    public static int index = 0x01000000;
    public static byte[] session;
    private static byte[] block = { 0x2a, 0x06, 0, 0, 0, 0,
            0x2b, 0x06, 0, 0, 0, 0,
            0x2c, 0x06, 0, 0, 0, 0,
            0x2d, 0x06,0, 0, 0, 0,
            0x2e, 0x06, 0, 0, 0, 0,
            0x2f, 0x06, 0, 0, 0, 0 };
    /*
    *
    *
    *                  设置信息
    *
    *
    * */
    public void setusername(String username){
        this.username = username;
    }
    public void setpassword(String password){
        this.password = password;
    }
    public void setlocalIp(String localIp){
        this.localIp = localIp;
    }
    public void setseverIp(String severIp){
        this.severIp = severIp;
    }
    public void setmacadd(String macadd){
        this.macadd = macadd;
    }
    public void setStatus(byte status){   this.status = status;}
    /*
    *
    *
    *                   返回信息
    *
    *
    * */
    public String getUsername(){
        return this.username;
    }
    public String getPWD(){
        return this.password;
    }
    public String getLocalIP(){
        return this.localIp;}
    public String getSeverIp(){
        return this.severIp;}
    public String getMacadd(){
        return this.macadd;}
    public byte getStatus(){
        return this.status;}
    public String getVersion(){
        return this.version;}
    public String getType(){
        return this.type;}
    public String getDhcp_setting(){
        return this.dhcp_setting;}
    public byte[] getBlock(){
        return  this.block;}
}
