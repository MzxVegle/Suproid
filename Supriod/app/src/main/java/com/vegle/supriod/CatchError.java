package com.vegle.supriod;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Vegle on 2017/10/27.
 */

public class CatchError {
    SharedPreferences sp;
    SharedPreferences.Editor editor ;
    static String error = "";
    public CatchError(){}
    public void CatchError(){
        sp = MyApp.getInstance().getSharedPreferences("ERROR", Context.MODE_APPEND|Context.MODE_PRIVATE);
        editor = sp.edit();

    }
    public  CatchError(String tag,Exception e){

        this.CatchError();
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String time = dateFormat.format(date);
        error =sp.getString("error","")+tag+time+e.toString()+"\n"+e.getMessage()+"\n--------------------------------------------------------------\n";
        editor.putString("error",error);
        editor.commit();
        //this.geterror();
    }
    public String geterror(){

        this.CatchError();
        //sp = MyApp.getInstance().getSharedPreferences("ERROR", Context.MODE_APPEND|Context.MODE_PRIVATE);
        return sp.getString("error","");
    }
public void clean(){
    this.CatchError();
    editor.clear().commit();
}

}
