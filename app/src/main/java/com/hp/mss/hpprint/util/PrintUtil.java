//
// Hewlett-Packard Company
// All rights reserved.
//
// This file, its contents, concepts, methods, behavior, and operation
// (collectively the "Software") are protected by trade secret, patent,
// and copyright laws. The use of the Software is governed by a license
// agreement. Disclosure of the Software to third parties, in any form,
// in whole or in part, is expressly prohibited except as authorized by
// the license agreement.
//

package com.hp.mss.hpprint.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;

import com.google.android.gms.analytics.Tracker;
import com.hp.mss.hpprint.activity.PrintPreview;
import com.hp.mss.hpprint.adapter.HPPrintDocumentAdapter;
import com.hp.mss.hpprint.model.ApplicationMetricsData;

import java.util.HashMap;


public class PrintUtil {

    public static final String IMAGE_SIZE_4x5 = "4x5";
    public static final String IMAGE_SIZE_4x6 = "4x6";
    public static final String IMAGE_SIZE_5x7 = "5x7";

    public static boolean is4x5media;

    static final String HP_PRINT_PLUGIN_PACKAGE_NAME = "com.hp.android.printservice";
    private static final String SHOW_4X5_MESSAGE_KEY = "com.hp.mss.hpprint.Show4x5DialogMessage";
    private static final String GOOGLE_STORE_PACKAGE_NAME = "com.android.vending";
    private static final String PRINT_DATA_STRING = "PRINT_DATA_STRING";
    public static final String PRINT_JOB_DATA = "PRINT_JOB_DATA";
    private static final int MILS = 1000;
    private static final int CURRENT_PRINT_PACKAGE_VERSION_CODE = 62; //Updated as of May 15,2015

    private static HashMap<String,String> appMetrics;

    public enum PackageStatus {
        INSTALLED_AND_NOT_UPDATED,
        INSTALLED_AND_UPDATED,
        INSTALLED_AND_ENABLED,
        INSTALLED_AND_DISABLED,
        NOT_INSTALLED
    }

    private static com.hp.mss.hpprint.model.PrintJob printJob;

    public static void print(Activity activity){
        if (appMetrics == null || appMetrics.isEmpty()) {
            appMetrics = (new ApplicationMetricsData(activity.getApplicationContext())).toMap();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createPrintJob(activity);
        } else {
            startPrintPreviewActivity(activity);
        }

    }

    public static void createPrintJob(Activity activity) {
        PrintManager printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter adapter = new HPPrintDocumentAdapter(activity, printJob, false);

        PrintJob androidPrintJob = printManager.print(printJob.getJobName(), adapter, printJob.getPrintDialogOptions());

        PrintMetricsCollector collector = new PrintMetricsCollector(activity, androidPrintJob);
        collector.addMetrics(appMetrics);
        collector.run();
    }

    public static void startPrintPreviewActivity(Activity activity) {
        Intent intent = new Intent(activity, PrintPreview.class);
        intent.putExtra(PRINT_JOB_DATA, printJob);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        activity.startActivity(intent);
    }

    public static void setPrintJob(com.hp.mss.hpprint.model.PrintJob printJobData){
        printJob = printJobData;
    }

    public static void setTracker(Tracker tracker) {
        GAUtil.setTracker(tracker);
    }

    public static void setApplicationMetrics(HashMap<String,String> map) {
        appMetrics = map;
    }

    public static PackageStatus checkGooglePlayStoreStatus(Activity activity) {
        return checkPackageStatus(activity, GOOGLE_STORE_PACKAGE_NAME);
    }

    public static PackageStatus checkPackageStatus(Activity activity, String packageName) {
        if (activity == null || packageName == null)
            return PackageStatus.NOT_INSTALLED;

        PackageManager packageManager = activity.getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            //if we reach below this line, then the package is installed. Otheriwse, the catch block is executed.

            if (packageName.equals(PrintUtil.HP_PRINT_PLUGIN_PACKAGE_NAME) && deviceAlwaysReturnsPluginEnabled()) {
                //Because of a bug either in the Nexus (Lollipop) or in the HP Print Plugin code that always says the app is enabled
                // even if it is disabled, we will return INSTALLED_AND_DISABLED all the time. Note that the
                // Text in the UI telling people to enable the plugin is properly worded in light of this bug.
                return PackageStatus.INSTALLED_AND_DISABLED;
            }

            if (appInfo.enabled)
                return PackageStatus.INSTALLED_AND_ENABLED;
            else
                return PackageStatus.INSTALLED_AND_DISABLED;

        } catch (PackageManager.NameNotFoundException e) {
            return PackageStatus.NOT_INSTALLED;
        }
    }

    private static boolean deviceAlwaysReturnsPluginEnabled() {
        return (Build.MODEL.contains("Nexus") && Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) || //Nexus Lollipop
                (Build.MODEL.equalsIgnoreCase("SM-N900") && Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) || //Samsung Note 3 KitKit
                (Build.MODEL.equalsIgnoreCase("GT-I9500") && Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT); //Samsung Galaxy S4
    }

}
