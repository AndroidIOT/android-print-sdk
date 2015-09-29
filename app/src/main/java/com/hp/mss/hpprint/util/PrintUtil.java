/*
 * Hewlett-Packard Company
 * All rights reserved.
 *
 * This file, its contents, concepts, methods, behavior, and operation
 * (collectively the "Software") are protected by trade secret, patent,
 * and copyright laws. The use of the Software is governed by a license
 * agreement. Disclosure of the Software to third parties, in any form,
 * in whole or in part, is expressly prohibited except as authorized by
 * the license agreement.
 */

package com.hp.mss.hpprint.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;

import com.hp.mss.hpprint.activity.PrintPreview;
import com.hp.mss.hpprint.adapter.HPPrintDocumentAdapter;
import com.hp.mss.hpprint.model.PrintJobData;
import com.hp.mss.hpprint.model.PrintMetricsData;

/**
 * In order to print, you need to call the print(Activity) method in this class. It automatically creates
 * the print preview activity for KitKat devices. It also helps you install/detect print plugins.
 * <p>
 * You will need to set the printJobData in order to invoke the print method.
 */
public class PrintUtil {
    public static final String PLAY_STORE_PRINT_SERVICES_URL = "https://play.google.com/store/apps/collection/promotion_3000abc_print_services";
    private static final String HAS_METRICS_LISTENER = "has_metrics_listener";
    private static final int START_PREVIEW_ACTIVITY_REQUEST = 100;

    private static PrintJobData printJobData;
    protected static PrintMetricsListener metricsListener;
    public static boolean is4x5media;
    public static final String mediaSize4x5Label = "4 x 5";

    /**
     * Set this to false to disable plugin helper dialogs.
     */
    public static boolean showPluginHelper = true;

    /**
     * Call to start the HP Print SDK print flow.
     * @param activity The calling activity.
     */
    public static void print(Activity activity){
        metricsListener = null;

        if (checkIfActivityImplementsInterface(activity, PrintUtil.PrintMetricsListener.class)) {
            metricsListener = (PrintMetricsListener) activity;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (PrintUtil.showPluginHelper) {
                showPluginHelper(activity);
            } else {
                createPrintJob(activity);
            }
        } else {
            startPrintPreviewActivity(activity);
        }
    }

    /**
     * Directly create the android PrintJob. This should not be needed except for special circumstances.
     * Please use the {@link #print(Activity)} method.
     * @param activity The calling activity.
     */
    public static void createPrintJob(final Activity activity) {
        PrintManager printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter adapter = new HPPrintDocumentAdapter(activity, printJobData, false);

        final PrintJob androidPrintJob = printManager.print(printJobData.getJobName(), adapter, printJobData.getPrintDialogOptions());

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PrintMetricsCollector collector = new PrintMetricsCollector(activity, androidPrintJob);
                collector.start();
            }
        });
    }

    /**
     * Sets the printJobData.
     * @param printJobData The print job data object that you want to use.
     */
    public static void setPrintJobData(PrintJobData printJobData){
        PrintUtil.printJobData = printJobData;
    }

    /**
     * Gets the printJobData.
     * @return The printJobData that is set using {@link #setPrintJobData(PrintJobData)} otherwise it will return null.
     */
    public static PrintJobData getPrintJobData(){
        return printJobData;
    }

    /**
     * This interface exists in order to pass print metrics back to the calling activity.
     * In order to receive print metrics, you must implement this interface in your activity that calls {@link #print(Activity)}.
     */
    public interface PrintMetricsListener {
        /**
         * This method, when implemented allows you to access data in the PrintMetricsData class.
         * @param printMetricsData The print metrics data.
         */
        void onPrintMetricsDataPosted(PrintMetricsData printMetricsData);
    }

    private static void showPluginHelper(final Activity activity) {
        final PrintPluginHelper.PluginHelperListener printPluginListener = new PrintPluginHelper.PluginHelperListener() {
            @Override
            public void printPluginHelperSkippedByPreference() {
                createPrintJob(activity);
            }

            @Override
            public void printPluginHelperSkipped() {
                createPrintJob(activity);
            }

            @Override
            public void printPluginHelperSelected() {
            }

            @Override
            public void printPluginHelperCanceled() {
            }
        };
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PrintPluginHelper.showPluginHelper(activity, printPluginListener);
            }
        });
    }

    private static void startPrintPreviewActivity(Activity activity) {
        Intent intent = new Intent(activity, PrintPreview.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        boolean has_metrics_listener = (metricsListener != null) ? true : false;
        intent.putExtra(HAS_METRICS_LISTENER, has_metrics_listener);

        activity.startActivityForResult(intent, START_PREVIEW_ACTIVITY_REQUEST);
    }

    private static boolean checkIfActivityImplementsInterface(Activity theActivity, Class theInterface){
        for (Class i : theActivity.getClass().getInterfaces())
            if (i.toString().equals(theInterface.toString()))
                return true;
        return false;
    }

}
