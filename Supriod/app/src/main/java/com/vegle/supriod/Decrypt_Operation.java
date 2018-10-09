package com.vegle.supriod;

/**
 * Created by Vegle on 2017/10/23.
 */

public class Decrypt_Operation {
    public String decrypt(String macaddr) {
        String[] sp = macaddr.split("-");
        String[] realmac = new String[sp.length];
        int[] num = new int[sp.length];
        for(int i = 0;i<sp.length;i++){
            num[i] = Integer.valueOf(sp[i]);

        }
        System.out.println();
        int c = 0;
        for(int i :num){
            num[c] =(i>>3);

            c++;
        }

        StringBuffer buffer = new StringBuffer();
        for(int i = 0;i<sp.length;i++){
            realmac[i] = String.format("%x",num[i]);
            buffer.append(realmac[i]+":");
        }
        return buffer.substring(0,buffer.length()-1).toString();
    }
}
