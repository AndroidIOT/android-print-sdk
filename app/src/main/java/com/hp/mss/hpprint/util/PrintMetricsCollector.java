package com.hp.mss.hpprint.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentInfo;
import android.print.PrintJob;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.hp.mss.hpprint.model.PrintMetricsData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class PrintMetricsCollector extends Thread {

    public static final String PRINT_METRICS_PRODUCTION_SERVER = "https://print-metrics-w1.twosmiles.com/api/v1/mobile_app_metrics";
    public static final String PRINT_METRICS_TEST_SERVER = "http://print-metrics-test.twosmiles.com/api/v1/mobile_app_metrics";
    public static final String PRINT_METRICS_LOCAL_SERVER = "http://10.0.2.2:4567/api/v1/mobile_app_metrics";
    public static final String PRINT_METRICS_USER_NAME = "hpmobileprint";
    public static final String PRINT_METRICS_PASSWORD = "print1t";

    private static final String TAG = "PrintMetricsCollector";
    private static final int PRINT_JOB_WAIT_TIME = 1000;
    private static final int MILS = 1000;

    private PrintJob printJob;
    private Handler metricsHandler;
    private Activity hostActivity;

    private HashMap<String, String> combinedMetrics = new HashMap<String, String>();

    public PrintMetricsCollector(Activity activity, PrintJob printJob) {
        this.hostActivity = activity;
        this.printJob = printJob;
        this.metricsHandler = new Handler();
    }

    @Override
    public void run() {
        if (printJob == null ) {
            return;
        }
        if (isJobFailed(printJob)) {
            PrintMetricsData printMetricsData = new PrintMetricsData();
            if (printJob.isFailed()) {
                printMetricsData.printResult = PrintMetricsData.PRINT_RESULT_FAILED;
            } else if (printJob.isCancelled()) {
                printMetricsData.printResult = PrintMetricsData.PRINT_RESULT_CANCEL;
            }
            postMetrics(hostActivity.getApplicationContext(), printMetricsData);
            return;
        }

        if (hasJobInfo(printJob)) {

            PrintJobInfo printJobInfo = printJob.getInfo();
            PrintAttributes printJobAttributes = printJobInfo.getAttributes();
            PrinterId printerId = printJobInfo.getPrinterId();

            PrintMetricsData printMetricsData = new PrintMetricsData();
            printMetricsData.printResult = PrintMetricsData.PRINT_RESULT_SUCCESS;

            try {
                Method gdi = PrintJobInfo.class.getMethod("getDocumentInfo");
                PrintDocumentInfo printDocumentInfo = (PrintDocumentInfo) gdi.invoke(printJobInfo);
                Method gsn = PrinterId.class.getMethod("getServiceName");
                ComponentName componentName = (ComponentName) gsn.invoke(printerId);

                printMetricsData.printPluginTech = componentName.getPackageName();

                if (printDocumentInfo.getContentType() == PrintDocumentInfo.CONTENT_TYPE_DOCUMENT) {
                    printMetricsData.paperType = PrintMetricsData.CONTENT_TYPE_DOCUMENT;
                } else if (printDocumentInfo.getContentType() == PrintDocumentInfo.CONTENT_TYPE_PHOTO) {
                    printMetricsData.paperType = PrintMetricsData.CONTENT_TYPE_PHOTO;
                } else if (printDocumentInfo.getContentType() == PrintDocumentInfo.CONTENT_TYPE_UNKNOWN) {
                    printMetricsData.paperType = PrintMetricsData.CONTENT_TYPE_UNKNOWN;
                }

                printMetricsData.blackAndWhiteFilter = String.valueOf(printJobInfo.getAttributes().getColorMode());

                String width = Double.toString(printJobAttributes.getMediaSize().getWidthMils() / (float) MILS);
                String height = Double.toString(printJobAttributes.getMediaSize().getHeightMils() / (float) MILS);

                printMetricsData.paperSize = (width + " x " + height);
                printMetricsData.printerID = printerId.getLocalId();
                printMetricsData.numberOfCopy = String.valueOf(printJobInfo.getCopies());

                postMetrics(hostActivity.getApplicationContext(), printMetricsData);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                Log.e(TAG, "CollectionRunner", e);
            }

        } else {
            metricsHandler.postDelayed(this, PRINT_JOB_WAIT_TIME);
        }
    }

    private static boolean hasJobInfo(final PrintJob printJob) {
        return (printJob.isQueued() || printJob.isCompleted() || printJob.isStarted());
    }

    private static boolean isJobFailed(final PrintJob printJob) {
        return (printJob.isFailed() || printJob.isBlocked() || printJob.isCancelled());
    }

    private void postMetrics(final Context context, final PrintMetricsData data) {
        RequestQueue queue = Volley.newRequestQueue(context);
        addMetrics( (HashMap<String,String>)data.toMap() );

        StringRequest sr = new StringRequest(Request.Method.POST, getPrintMetricsServer(context), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("PrintMetricsUtil", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("PrintMetricsUtil", error.toString());
            }
        }){
            @Override
            protected Map<String,String> getParams(){

                Map<String,String> params = getMetricsParams();
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String authorizationString = "Basic " + Base64.encodeToString((PRINT_METRICS_USER_NAME + ":" + PRINT_METRICS_PASSWORD).getBytes(), Base64.NO_WRAP);

                Map<String,String> params = new HashMap<String, String>();

                params.put("Authorization", authorizationString);
                return params;
            }
        };
        queue.add(sr);

    }

    public static boolean isDebuggable(Context context) {
        return ( 0 != ( context.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) );
    }

    private static String getPrintMetricsServer(Context context) {
        return (isDebuggable(context) ? PRINT_METRICS_TEST_SERVER : PRINT_METRICS_PRODUCTION_SERVER);
    }

    public void addMetrics(HashMap metricsMap) {
        combinedMetrics.putAll(metricsMap);
    }

    private Map<String, String> getMetricsParams() {
        return combinedMetrics;
    }

}


