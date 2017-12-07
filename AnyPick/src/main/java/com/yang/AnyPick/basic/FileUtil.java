package com.yang.AnyPick.basic;


import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by YanGGGGG on 2017/12/7.
 */

public class FileUtil {
        public static String readFileFromAssets(String fileName){
            StringBuilder stringBuilder = new StringBuilder();
            try {
                //获取assets资源管理器
                AssetManager assetManager = MyApplication.getContext().getAssets();
                //通过管理器打开文件并读取
                BufferedReader bf = new BufferedReader(new InputStreamReader(
                        assetManager.open(fileName)));
                String line;
                while ((line = bf.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stringBuilder.toString();
        }
    public static String[] getListFromAssets(String path){
        try{
            AssetManager assetManager = MyApplication.getContext().getAssets();
            return assetManager.list(path);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}


