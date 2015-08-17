# Quick Start Guide - Scroll below for more info

Note: If you are behind a proxy, please make sure Android Studio is capable of downloading build tools from the SDK Manager.  Depending on your configuration, additional downloads may be required.

***

1. Clone the repo (for this tutorial, we'll clone to our home folder)

    `git clone git@github.com:IPGPTP/DroidPrint ~/DroidPrint`

2. Run Android Studio.
3. Select `File | Open`.
4. Navigate to the `~/DroidPrint` directory and select the `PrintSDKSample` directory. Then click the `Choose` button.
    ![Load Sample App](https://s3-us-west-2.amazonaws.com/droidprint/images/PrintSDKSample.png)
5. Android Studio will alert you regarding any missing dependencies.  Click the appropriate link(s) to fix them.
    ![Build error](https://s3-us-west-2.amazonaws.com/droidprint/images/builderror.png)
6. Run the app!
    ![Running app](https://s3-us-west-2.amazonaws.com/droidprint/images/runapp.png)
7. At this point, you should have a working example for reference. To learn more, please check out the full [README](https://github.com/IPGPTP/DroidPrint) file on the project homepage.



# DroidPrint

A library to simplify development of printing in apps on Android, as well as providing an improved user experience.  This library serves as an interface to Google Cloud Print and various other Print Plugins and services.

Note: Print functionality only exists starting in Android API versions 19 and beyond. Devices with an older OS than KitKat will not be able to print using our library workflow.

Please see the [Quick Start](https://github.com/IPGPTP/DroidPrint/wiki/Quick-Start) page, if you want to get started right away.

## Contents

- [JavaDocs](##javadocs)
- [Installation](#installation)
- [Basic Usage](#basic-usage)
    + [ImageAsset](#imageasset)
    + [PrintItem](#printitem)
    + [printJobData](#printJobData)
- [Advanced](#advanced)
    + [Multiple Assets](#multiple-assets)
    + [Default Print Attributes](#default-print-attributes)
    + [Print Metrics](#print-metrics)
    + [Plugin Install Helper](#plugin-install-helper)
- [KitKat Print Preview](#kitkat-print-preview)
    + [Font](#font)
    + [Color](#color)
- [Layout Options](#layout-options)

## Overview

![Droid Print Stack](https://s3-us-west-2.amazonaws.com/droidprint/images/DroidPrintStack.png)

DroidPrint provides an interface that simplifies developer interaction with Android Print functionality.  It also provides the following features currently not offered by Android:

## Features

1. Kitkat print preview (Lollipop has its own print preview functionality)
2. Dialogs for assisting users in installing print plugins
3. More advanced [layouting features](#layout-options) than google's `PrintHelper`

## JavaDocs

The JavaDocs for this project are located can be found at [http://ipgptp.github.io/DroidPrint/javadocs/](http://ipgptp.github.io/DroidPrint/javadocs/). This includes complete documentation for all classes, methods, constants, you may need.

## Installation

### Minimum System Requirements

In order to use the SDK and run the PrintSDKSample app, you need the following SDK Platforms and Build-tools installed. Note: Android Studio should prompt you to install any missing tools and assist with installation.

* Android Studio
* Android SDK Platform-tools Version "22"
* Android SDK Build-tools Version "22"
* Android SDK Build-tools Version "21.1.2"
* Android Support Repository Version "16"
* Android Support Library Version "22.2.1"
* SDK Platform "22" (Lolipop)
* SDK Platform "21" (Lolipop)
* SDK Platform "19" (Kitkat)

The __DroidPrint__ library is not yet available publicly via maven or jcenter.
There are two ways to install the library in your application. One option is to download the source code and compile it into your application. The other option is to use the aar file.

1. [Install from source](https://github.com/IPGPTP/DroidPrint/wiki/Install-with-source-code)
2. [Install aar file](https://github.com/IPGPTP/DroidPrint/wiki/Install-With-.aar-File)

## Basic Usage

In order to use the __DroidPrint__ library, you need to first obtain the URI to the bitmap you want to print.
Once you have that, you are ready to invoke our print workflow.

### ImageAsset

One of the first objects you need to create in order to use our print workflow is the `ImageAsset` object. It tells the print sdk what you want to print.
You will need at least one but you can create as many as needed ([Multiple Assets](#multiple-assets)). You can use one of the following 3 methods to create the imageAsset.

To load an ImageAsset from resources:
```java
ImageAsset imageAsset4x6 = new ImageAsset(this, R.drawable.template4x6, ImageAsset.MeasurementUnits.INCHES, 4, 6);
```

To load an image from storage:
```java
ImageAsset assetdirectory = new ImageAsset(this, "oceanwave.jpeg", ImageAsset.MeasurementUnits.INCHES, 4, 6);
```

To load an ImageAsset from an existing `Bitmap` object:
```java
ImageAsset bitmapAsset = new ImageAsset(this, bitmap, ImageAsset.MeasurementUnits.INCHES, 4,5);
```

Or, if you already saved the bitmap into the internal storage location for your app:

```java
ImageAsset imageAsset4x6 = new ImageAsset(fileUriString, ImageAsset.MeasurementUnits.INCHES, 4, 6);
```

### PrintItem

Once you create an image asset, you need to associate it with a `PrintItem` object. This object provides a scaletype (layout) for the imageAsset and allows you to define what media size you want to associate with the `ImageAsset`. You will need to provide a `ScaleType` and the `ImageAsset` itself.

```java
PrintItem printItemDefault = new ImagePrintItem(PrintItem.ScaleType.CENTER, imageAsset4x6);
```

For more information, take a look in the Sample App or the JavaDocs.

### PrintJobData

After creating the `PrintItem`, you are ready to create the `PrintJobData` object which contains all the relevant info needed
to build the print.

```java
PrintJobData printJobData = new PrintJobData(activity, printItemDefault);
printJobData.setJobName("Example");
PrintUtil.setPrintJobData(printJobData);
```

### Print

Once you have created the `PrintJobData` object, you are ready to print.

```java
PrintUtil.print(activity);
```

## Advanced

If you would like more customization on what gets printed when users select certain print settings in the Android Print Dialog,
we optionally provide a way to give the `PrintJobData` multiple `PrintItems` that will be selected based on your user's media size selection as
well as orientation.

### Multiple Assets

#### OverView
Android's system print dialog gives the user the ability to select from an array of
paper sizes to print on. In order to accomodate for that, we give you the ability to print different assets on different paper sizes. By default, the `PrintItem` you pass into the constructor of `PrintJobData` will be used for any paper size the user selects. This library gives you the ability to create multiple `PrintItem`'s to override how your print looks when the user selects different paper sizes.

So to clarify, let's say you created `PrintJobData` with a 'PrintItem' that contained a '4x6' asset. If you hadn't added any other `PrintItem`'s to the `PrintJobData`, then no matter what paper size the user selects in the print dialog, the '4x6 ' asset will be used. However, if you want the user to print a different asset when they select a paper size of '8.5x11' then, you can create a separate `PrintItem` that will be able to handle prints to that paper size.

#### Usage
When creating an `PrintItem`, you can provide the constructor a MediaSize object that lets the Print Library know that you
would like the PrintItem to be used for a particular paper size selection. For example, if you did:

```java
PrintItem printItemLetter = new ImagePrintItem(PrintAttributes.MediaSize.NA_LETTER, scaleType, imageAsset4x5);
```

It would tell the Print Library that you want to use the `imageAsset4x5` whenever the user selects the letter size media in
the Android Print Dialog.

You must then add the printItem to the `PrintJobData` by invoking:

```java
printJobData.addPrintItem(printItemLetter);
```

### Default Print Attributes

`printJobData` takes in an Android standard [`PrintAttributes`](https://developer.android.com/reference/android/print/PrintAttributes.html) object which is used to set print dialog options.

```java
PrintAttributes printAttributes = new PrintAttributes.Builder()
        .setMediaSize(printItemLetter.getMediaSize())
        .build();
printJobData.setPrintDialogOptions(printAttributes);
```

### Print Metrics

Printing related metrics will be sent to HP server.  HP is collecting information like: wireless setup, printer id, number of pages, black and white filter, and print job status. See `PrintMetricsData` for details.

Above printing information is also available to your app. In order to allow us to post printing metrics data to your app, please implement `PrintUtil.PrintMetricsListener` in your calling activity.

```java
class YourCallingActivity extends ActionBarActivity implements PrintUtil.PrintMetricsListener {
	...
	@Override
    	public void onPrintMetricsDataPosted(PrintMetricsData printMetricsData) {
   			// Do what you want to do with available printMetricsData
    	}
	...
}
```

### Print Service Plugin Install Helper
Currently, the Android Framework requires customers to install a print service plugin for their printer on their device. Without the right plugin, the device will be unable to discover or use their printer.

In order to improve the customer's print experience, we have created a helper that guides them to the print plugin play store page. The helper works by displaying an alert dialog when the customer hits print. 

You can disable the  print plugin install helper by setting:

```java
PrintUtil.showPluginHelper = false;
```

## KitKat Print Preview (Lollipop has its own print preview functionality)

Whereas Lollipop offers print preview functionality, KitKat did not offer this feature. DroidPrint implements print preview on KitKat. You can customize the KitKat print preview to your liking by applying overrides to the library styles. Besides the font colors, the preview activity will match the theme you have set up for the rest of your application.

### Font

In your strings resource file:

```xml
<string name="lib_font">fonts/HPSimplified_BdIt.ttf</string>
```

### Color

In your styles resource file:

```xml
<style name="pagePreviewView">
    <item name="sizeFontColor">@android:color/white</item>
</style>
<style name="previewPrintInstructionsBG">
    <item name="android:background">@android:color/white</item>
</style>
<style name="previewPrintInstructionsText">
    <item name="android:textColor">@color/HPFontColorBlue</item>
</style>
<style name="previewSpinnerBG">
    <item name="android:background">@android:color/white</item>
</style>
<style name="previewPaperSizeSpinnerText">
    <item name="android:textColor">@color/HPFontColorBlue</item>
</style>
<style name="previewSupportText">
    <item name="android:textColor">@color/HPFontColorGrey</item>
</style>
```

## Layout Options

The following images demonstrate the effect of each of the Layout Options.

Here is the image we will use for the examples:
![puppies image](https://s3-us-west-2.amazonaws.com/droidprint/images/image.jpeg)

### Layout Options:
## Center
![Centered Image](https://s3-us-west-2.amazonaws.com/droidprint/images/centered.jpeg)

## Crop
![Cropped Image](https://s3-us-west-2.amazonaws.com/droidprint/images/full_page.jpeg)

## Fit
![Fit page](https://s3-us-west-2.amazonaws.com/droidprint/images/fit_page.jpeg)

## Top Left
![Top Left](https://s3-us-west-2.amazonaws.com/droidprint/images/top_left.jpeg)
