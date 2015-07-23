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

package com.hp.mss.hpprint.activity;


import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.hp.mss.hpprint.R;
import com.hp.mss.hpprint.model.PrintItem;
import com.hp.mss.hpprint.model.PrintJobData;
import com.hp.mss.hpprint.model.PrintMetricsData;
import com.hp.mss.hpprint.util.FontUtil;
import com.hp.mss.hpprint.util.PrintPluginHelper;
import com.hp.mss.hpprint.util.PrintUtil;
import com.hp.mss.hpprint.util.SnapShotsMediaPrompt;
import com.hp.mss.hpprint.view.PagePreviewView;

import java.util.HashMap;

public class PrintPreview extends AppCompatActivity {
    private static final String HP_ANDROID_MOBILE_SITE = "http://www8.hp.com/us/en/ads/mobility/overview.html?jumpid=va_r11400_eprint";

    HashMap<String,PrintAttributes.MediaSize> spinnerMap = new HashMap<>();
    private PagePreviewView previewView;
    private boolean disableMenu = false;

    private float paperWidth;
    private float paperHeight;

    PrintJobData printJobData;
    String spinnerSelectedText;

    private static final PrintAttributes.MediaSize[] defaultMediaSizes = {
        PrintAttributes.MediaSize.NA_LETTER,
        PrintAttributes.MediaSize.NA_INDEX_4X6,
        new PrintAttributes.MediaSize("na_5x7_5x7in", "android", 5000, 7000)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_print_preview);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        printJobData = PrintUtil.getPrintJobData();

        initializeSpinnerData();

        previewView = (PagePreviewView) findViewById(R.id.preview_image_view);

        setPreviewViewLayoutProperties();

        TextView linkTextView = (TextView) findViewById(R.id.ic_printing_support_link);
        linkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAboutLinkClicked(view);
            }
        });

        ((TextView) findViewById(R.id.paper_size_title)).setTypeface(FontUtil.getDefaultFont(this));
        ((TextView) findViewById(R.id.print_preview_support_title)).setTypeface(FontUtil.getDefaultFont(this));
        ((TextView) findViewById(R.id.ic_printing_support_link)).setTypeface(FontUtil.getDefaultFont(this));
    }

    private void initializeSpinnerData(){
        Spinner sizeSpinner = (Spinner) findViewById(R.id.paper_size_spinner);

        String[] spinnerArray;
        if(printJobData.numPrintItems() == 0){
            spinnerArray = new String[defaultMediaSizes.length];
            for (int i = 0; i < defaultMediaSizes.length; i++) {
                String text = getSpinnerText(defaultMediaSizes[i]);
                spinnerMap.put(text, defaultMediaSizes[i]);
                spinnerArray[i] = text;
            }
        } else {
            spinnerArray = new String[printJobData.numPrintItems()];
            int i = 0;
            for (PrintAttributes.MediaSize mediaSize: printJobData.getPrintItems().keySet()) {
                String text = getSpinnerText(mediaSize);
                spinnerMap.put(text, mediaSize);
                spinnerArray[i++] = text;
            }
        }

        ArrayAdapter<String> adapter =new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(adapter);

        if(printJobData.getPrintDialogOptions() != null) {
            PrintAttributes.MediaSize mediaSize = printJobData.getPrintDialogOptions().getMediaSize();
            String text = getSpinnerText(mediaSize);
            sizeSpinner.setSelection(adapter.getPosition(text));
        }
        setSizeSpinnerListener(sizeSpinner);
    }

    private String getSpinnerText(PrintAttributes.MediaSize mediaSize){
        String widthText = fmt(mediaSize.getWidthMils() / 1000f);
        String heightText = fmt(mediaSize.getHeightMils() / 1000f);
        return String.format("%s x %s", widthText, heightText);
    }

    private static String fmt(double d)
    {
        if(d == (long) d)
            return String.format("%d",(long)d);
        else
            return String.format("%s",d);
    }

    private void setPreviewViewLayoutProperties() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        if (outMetrics.widthPixels <= outMetrics.heightPixels) { //screen in portrait mode
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) previewView.getLayoutParams();
            int size = outMetrics.widthPixels;
            params.width = size;
            params.height = size;
            previewView.setLayoutParams(params);
        } else { //screen in landscape mode
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) previewView.getLayoutParams();
            params.width = (int) (outMetrics.widthPixels / 2f);
            params.height = outMetrics.heightPixels;
            previewView.setLayoutParams(params);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_print_preview, menu);
        menu.findItem(R.id.action_print).setEnabled(!disableMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        disableMenu = true;
        invalidateOptionsMenu();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_print) {
            if (paperWidth == 4 && paperHeight == 5) {
                SnapShotsMediaPrompt.SnapShotsPromptListener snapShotsPromptListener =
                        new SnapShotsMediaPrompt.SnapShotsPromptListener() {
                            @Override
                            public void SnapShotsPromptOk() {
                                if (PrintUtil.showPluginHelper) {
                                    showPluginHelper();
                                } else {
                                    doPrint();
                                }
                            }

                            public void SnapShotsPromptCancel() {
                                disableMenu = false;
                                invalidateOptionsMenu();
                            }
                        };
                SnapShotsMediaPrompt.displaySnapShotsPrompt(this, snapShotsPromptListener);
            } else {
                if (PrintUtil.showPluginHelper) {
                    showPluginHelper();
                } else {
                    doPrint();
                }
            }
            return true;
        } else if (id == android.R.id.home) {
            super.onBackPressed();
            disableMenu = false;
            invalidateOptionsMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        previewView.setPhoto(null);
        //todo: delete printjob when done
//        if (photoFileName != null) {
//            File photoFile = new File(photoFileName);
//            if (photoFile.exists())
//                photoFile.deleteOnExit();
//        }
    }

    public void onAboutLinkClicked(View view) {
        Intent mobileSiteIntent = new Intent(Intent.ACTION_VIEW);
        mobileSiteIntent.setData(Uri.parse(HP_ANDROID_MOBILE_SITE));
        startActivity(mobileSiteIntent);
    }

    public void setSizeSpinnerListener(Spinner sizeSpinner) {
        sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerSelectedText = (String) parent.getItemAtPosition(position);

                PrintItem printItem = printJobData.getPrintItem(spinnerMap.get(spinnerSelectedText));

                paperWidth = printItem.getMediaSize().getWidthMils() / 1000f;
                paperHeight = printItem.getMediaSize().getHeightMils() / 1000f;

                previewView.setPageSize(paperWidth, paperHeight);
                new PagePreviewView.ImageLoaderTask(PrintPreview.this).execute(new PagePreviewView.LoaderParams(printItem, previewView));

                PrintUtil.is4x5media = paperHeight == 5 && paperWidth == 4;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void doPrint() {

        PrintAttributes printAttributes = printJobData.getPrintDialogOptions();
        if(printAttributes == null){
            printAttributes = new PrintAttributes.Builder()
                    .setMediaSize(spinnerMap.get(spinnerSelectedText))
                    .build();
        } else {
             printAttributes = new PrintAttributes.Builder()
                    .setColorMode(printJobData.getPrintDialogOptions().getColorMode())
                    .setMediaSize(spinnerMap.get(spinnerSelectedText))
                    .setMinMargins(printJobData.getPrintDialogOptions().getMinMargins())
                    .setResolution(printJobData.getPrintDialogOptions().getResolution())
                    .build();
        }

        printJobData.setPrintDialogOptions(printAttributes);
        PrintUtil.setPrintJobData(printJobData);
        PrintUtil.createPrintJob(this);

        disableMenu = false;
        invalidateOptionsMenu();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setPreviewViewLayoutProperties();
    }

    public void returnPrintDataToPreviousActivity(PrintMetricsData data) {
        Intent callingIntent = new Intent();
        callingIntent.putExtra(PrintMetricsData.class.toString(), data);
        setResult(RESULT_OK, callingIntent);
        finish();
    }

    public void showPluginHelper() {
        PrintPluginHelper.PluginHelperListener printPluginListener = new PrintPluginHelper.PluginHelperListener() {
            @Override
            public void printPluginHelperSkippedByPreference() {
                doPrint();
            }

            @Override
            public void printPluginHelperSkipped() {
                doPrint();
            }

            @Override
            public void printPluginHelperSelected() {
            }

            @Override
            public void printPluginHelperCanceled() {
            }
        };
        PrintPluginHelper.showPluginHelper(this, printPluginListener);
    }

}
