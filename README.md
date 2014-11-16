# Introduction

blueBell is a didactic application demonstrating design strategies with the Android platform. It is an application to
remotely control a Sony Camera, connected through a Wifi connection, by means of its exposed REST interface.

# Structure of the Project

## Module decomposition

The project is composed of three separate modules:

* **Sony Camera API**. It is code (heavily) refactored from a Sony example and wraps the Sony Camera API. It is interesting
  as an example on how to deal with a REST API (JSON and XML based) in an Android application. It also contains a simple
  client implementation of the SSDP protocol. This module is a plain Java 7 set of files, which demonstrates how part of
  the code of an Android application can be written in a reusable fashion.
* **Camera View**. It contains the basic abstractions of the blueBell application, in form of Presentation and Control 
  objects. It's again a plain Java 7 set of files, which demonstrates how part of an Android application can be written
  in a way so that they can be tested without resorting to the Android platform.
* **Application APK**. This module contains Android specific code and generates the final APK bundle.


## Structure of the application

The application is composed by two Activites:

* The **CameraDiscoveryPresentationActivity** makes it possible to pick a Wifi network and scans for the presence of Sony
  camera devices. This activity demonstrates:
    * how to interact with the Wifi system service;
    * how to interact with an Android activity (the Wifi configuration panel);
    * how to receive Broadcast Notification;
    * how to keep the state of an Activity;
    * how to write a simple adapter to populate a ``ListView``;
    * how to use an Intent to pass the control (with parameters) to another Activity.

* The **CameraViewPresentationActivity** connects to a camera device and, if it supports the "live view" feature, it 
  continuously displays the contents of the Electronic Viewfinder (EVF). It also shows the values of some camera 
  settings such as the F number, the shutter speed, the ISO speed rating and others; some of the settings can be also
  changed. It also allow to shoot a photo or to record a movie. This activity demonstrates:
    * how to create a custom Android UI component;
    * how to manage a Dialog;
    * how to customise an Activity so it runs at full screen

In general, the app is loosely designed after the Presentation Abstraction Control (PAC) architectural pattern:

  * components which interact with the user ("Presentations") are kept as simple and dumb as possible, without any
    logic. They mostly expose methods to render data and populate UI widgets and to capture the user's gestures and
    take care of any UI threading issue.
  * the presentation logic is encapsulated in controllers ("Controls").

Controls are split in two parts:

  * a superclass which contains all the code that can be written without depending on Android.
  * a specialised class which extends the abstract controller with the required Android-specific code.

The following points are likely to require specific Android code:

  * the use of ``Intent``s;
  * the use of string resources (``R.value.foobar``);
  * the use of system specific infrastructures, such as the ``BroadcastReceiver``;
  * the use of system services, such as ``WifiManager``;
 
Both Presentations and Controls are also described by interfaces to allow the creation of mocks that can be used in
automated testing. While at the present time there are no valid tests in this project, most of the presentation logic
can be automatically tested by means of pure Java classes, avoiding the cumbersome Android approach to tests.

Presentation interfaces contains one method for each interaction.

TODO: Activity is used only as a life-cycle controller.


# Remarks

## Code reusability 

Explain: try-with-resources java 6 vs 7

## Lombok annotations

Explain: main Lombok annotations

# AndroidUIThreadDecoratorFactory

TODO: explain



## OTHERS



# Notes:

This project uses Lombok. In a few words, it's a compiler enhancement that shortens some Java constructs by means of 
special annotations. For instance, ``@Getter private int foo;`` automatically generates a ``public int getFoo()`` getter method.
This requires some interaction with the IDE so it doesn't complain about syntax errors.

* If you work with NetBeans 7.3 and later, you don't need anything.

* If you work with Eclipse, you need to install the Lombok plugin as explained at http://projectlombok.org

* If you work with Android Studio, you need to go to File / Settings (Preferences on Mac OS X), go to the Plugin tab and
  install the Lombok plugin. You also need to go to the Compiler Settings, tab "Annotation Processors" and check
  "Enable annotation processors".


