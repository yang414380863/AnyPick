package com.yang.AnyPick.basic;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YanGGGGG on 2017/11/13.
 */

public class ActivityCollector {
    public static List<Activity> activities=new ArrayList<>();

    public static void addActivity(Activity activity){
        activities.add(activity);
    }

    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }

    public static void finishExcept(Activity a){
        for (Activity activity:activities){
            if (!activity.isFinishing()&&!activity.equals(a)){
                activity.finish();
            }
        }
    }
}
