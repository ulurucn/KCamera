package vip.frendy.base;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by frendy on 2018/5/4.
 */

public class Device {

    //返回1GB/2GB/3GB/4GB
    public static int getTotalRam(Context context){
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0 ;
        try{
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader,8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(firstLine != null){
            totalRam = (int)Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }

        return totalRam;
    }
}
