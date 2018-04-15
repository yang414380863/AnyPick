package com.yang.AnyPick.basic;


import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by YanGGGGG on 2017/12/7.
 */

public class FileUtil {
    private static String path=MyApplication.getContext().getFilesDir().getPath();
    public static String readFileFromAssets(String fileName){
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bf=null;
        try {
            //获取assets资源管理器
            AssetManager assetManager = MyApplication.getContext().getAssets();
            //通过管理器打开文件并读取
            bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                if (bf!=null){
                    bf.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
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
    public static String writeFileToData(String username,String fileName,String content) {
        if (username.equals("")){
            username="visitor";
        }
        mkFile(path+"/"+username,fileName);
        String dirPath=path+"/"+username+"/"+fileName;
        File file=new File(dirPath);
        FileOutputStream outputStream=null;
        BufferedWriter writer=null;
        try {
            outputStream=new FileOutputStream(file);
            writer=new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(content);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                if (writer!=null){
                    writer.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return "success";
    }
    public static String readFileFromData(String username,String fileName){
        if (username.equals("")){
            username="visitor";
        }
        String dirPath=path+"/"+username+"/"+fileName;
        File file=new File(dirPath);
        FileInputStream inputStream=null;
        BufferedReader reader=null;
        StringBuilder content=new StringBuilder();
        try {
            inputStream=new FileInputStream(file);
            reader =new BufferedReader(new InputStreamReader(inputStream));
            String line="";
            while ((line=reader.readLine())!=null){
                content.append(line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                if (reader!=null){
                    reader.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return content.toString();
    }
    public static String[] showFileFromData(String dirName){
        if (dirName.equals("")){
            dirName="visitor";
        }
        String dirPath=path+"/"+dirName;
        String[] files =new File(dirPath).list();
        if (files==null){
            return null;
        }
        for(String file:files){
            LogUtil.d("file name: "+file);
        }
        return files;
    }

    private static File mkFile(String filePath,String fileName){
        File file = null;
        mkdir(filePath);
        try {
            file = new File(filePath+"/"+fileName);
            if (!file.exists()){
                file.createNewFile();
                LogUtil.d("filePath：" + filePath +"fileName:"+fileName);
            }else {
                LogUtil.d("file: " + fileName+" existed");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return file;
    }
    private static void mkdir(String dirName){
        File file = null;
        try {
            file = new File(dirName);
            if (!file.exists()){//判断指定的路径或者指定的目录文件是否已经存在。
                file.mkdir();//建立文件夹
                LogUtil.d("new dir: " + dirName);
            }else {
                LogUtil.d("dir: " + dirName+" existed");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}


