# Introduction

blueBell is a didactic application demonstrating design strategies with the Android platform. It is an application to
remotely control a Sony Camera, connected through a Wifi connection, by means of its exposed REST interface.

If you want to quickly download the sources and compile them, please refer to the chapter "Compiling the project with
and IDE" below.


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

There's a test module too, but it is just a stub at the moment.

## Use of the Presentation Abstraction Control (PAC) pattern

Some say that an ``Activity`` is a controller object, but others disagree. Indeed, an ``Activity`` is bound to the
Android life-cycle, and from this respect acts as a controller. But it also interacts with the User Interface, both in
setting its state and registering callbacks. So, I'd rather say it's a "life-cycle" controller or a "navigation" 
controller. It makes sense to split the presentation logic into a separate class for complying with the 
[Separation of Concerns (SoC) principle](http://en.wikipedia.org/wiki/Separation_of_concerns) and its specialisation
[Separation Presentation](http://martinfowler.com/eaaDev/SeparatedPresentation.html).

The general design of blueBell refers to the Presentation Abstraction Control (PAC) architectural pattern:

  * components which interact with the user (**Presentations**) are kept as simple and dumb as possible, without any
    logic. They mostly expose methods to render data and populate UI widgets and to capture the user's gestures and
    take care of any UI threading issue. In blueBell, activities are PAC presentations. Their interface is modelled
    upon methods with represents a single interaction with the UI with a well defined semantics; for instance 
    ``public void renderRecStartStopButtonAsStop()``. Their implementation is straightforward, such as just changing
    the properties of a UI widget. They are usually just a few lines of code.

    Presentations are responsible for taking care of Android UI threading issues (see below).
   
  * the presentation logic is encapsulated in controllers (**Controls**). In blueBell, these are separated, mostly plain
    Java classes. They are indeed split in two parts:
      * a superclass which contains all the code that can be written without depending on Android;
      * a specialised class which extends the abstract controller with the required Android-specific code.

    The following points are likely to require specific Android code:
      * the use of ``Intent``s;
      * the use of string resources (``R.value.foobar``);
      * the use of system specific infrastructures, such as the ``BroadcastReceiver``;
      * the use of system services, such as ``WifiManager``.

    Since Android forbids networking code to be called from the UI thread, and blueBell is mostly about interacting with
    a remote service hosted on the camera, controllers execute most of their jobs in a background thread.

The general rationale behind this approach is explained in [this blog post](http://tidalwave.it/fabrizio/blog/beyond-mvc-pac-presentation-model-dci/),
even though there's no use of DCI in blueBell. A common benefit is testing: since both Presentations and Controls are 
described by interfaces, it is possible the creation of mocks that can be used in automated testing. While at the present 
time there are no valid tests in this project, most of the presentation logic can be automatically tested by means of 
pure Java classes, avoiding the cumbersome Android approach to tests.

Given the very nature of Android, which is not plain Java, the application of PAC raises some more problems than with
a JSE or JEE application. Some trade-offs are to be applied.


## Structure of the application

The application is composed by two Activites:

* The ``CameraDiscoveryPresentationActivity`` makes it possible to pick a Wifi network and scans for the presence of Sony
  camera devices. 
  This activity (together with its control object) demonstrates:
    * how to interact with the Wifi system service;
    * how to interact with a system activity (the Wifi configuration panel);
    * how to receive a Broadcast Notification;
    * how to preserve the state of an Activity;
    * how to write a simple adapter to populate a ``ListView``;
    * how to use an ``Intent`` to communicate with another ``Activity``.

* The ``CameraPresentationActivity`` connects to a camera device and, if it supports the "live view" feature, it 
  continuously displays the contents of the Electronic Viewfinder (EVF). It also shows the values of some camera 
  settings such as the F number, the shutter speed, the ISO speed rating and others; some of the settings can be also
  changed. It also allow to shoot a photo or to record a movie. 
  This activity (together with its control object) demonstrates:
    * how to create an ``Activity`` that interacts with a remote service (REST in this case);
    * how to customise an ``Activity`` so it runs at full screen;
    * how to create a custom Android UI component;
    * how to manage a ``Dialog``.

## Taking care of the UI threading model

The UI threading requirements for Android, which mandate that widgets are only touched by the same thread which creates
the Activity that contains them, are taken care of by a single class named ``AndroidUIThreadDecoratorFactory``. It 
creates a decorator of the Presentation and its Control always interact with the Presentation by means of its decorator.
The decorator invokes the original method in the special UI thread. ``AndroidUIThreadDecoratorFactory`` relies on a
well known Java technique. More information are in inline comments inside the source code.

# Remarks

The whole project is compiled in Java 7, which is compatible with recent releases of Android. Most Java syntactic 
features, such as the diamond operator, are thus allowed. A feature that is not allowed is the "try-with-resources"
because it requires a specific Java 7 runtime support that is missing in Android.

## JSR-305 annotations

The app makes use of some annotations defined in JSR-305. They are ``@NonNull``, ``@Nullable``, ``@CheckForNull`` and
at this level of discussion they can be just considered as a support for documentation, as they declare which reference
variables are allowed to hold ``null`` values.

## Lombok annotations

[Lombok](http://projectlombok.org) is an APT (Annotation Processor Tool) which reduces the amount of code in some 
trivial parts by making use of annotations. For instance, ``@Getter private int foo;`` automatically generates a 
``public int getFoo()`` getter method.

The used annotations are:

* ``@Getter`` and ``@Setter`` to generate getter and setter methods;
* ``@RequiredArgsConstructors`` to generate a constructor with initialises the class fields;
* ``@Slf4j`` to transparently inject a ``log`` field which is used for logging (SLF4J is used).
* ``@Cleanup`` to correctly close I/O resources (given that try-with-resources can't be used - see above).

While Lombok works transparently when the application is compiled from the command line, some additional work is
required to avoid false syntax errors in the IDE.
This might requires some special configuration in the IDE so it doesn't complain about syntax errors.

* If you work with NetBeans 7.3 and later, you don't need anything.

* If you work with Eclipse, you need to install the Lombok plugin as explained at http://projectlombok.org

* If you work with Android Studio, you need to go to File / Settings (Preferences on Mac OS X), go to the Plugin tab and
  install the Lombok plugin. You also need to go to the Compiler Settings, tab "Annotation Processors" and check
  "Enable annotation processors".


# Compiling the project with an IDE

blueBell is being developed with Maven, which is not an obvious choice for most people. In particular, it is not the 
default tool suggested by Google. Google's Android Studio, in fact, uses Gradle. For this reason, the project has been
structured so it can work with both tools:

 * files ``pom.xml`` are related to Maven;
 * files ``*.gradle``, ``local.properties`` and ``*.iml`` are related to Gradle and Android Studio.

If you're opening the project with a Maven enabled IDE such as NetBeans and Eclipse, you don't have to do nothing
special (Gradle and Android Studio are just ignored). If you're opening the project with Android Studio, you should use
the menu "File / Import Project..." rather than "Open...". Even in this case, you might get some Maven warnings, since
Android Studio has some Maven support embedded. Just ignore them. Perhaps, the best thing that you can do when working
with Android Studio is to delete the ``pom.xml`` files before opening the project.

Please also read the chapter above related to Lombok.