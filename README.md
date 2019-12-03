![](https://repository-images.githubusercontent.com/211314521/7d7b8a80-e15c-11e9-994a-bf63dcf3792a)
# Engage Android Demo App

[![Kotlin Version](https://img.shields.io/badge/Kotlin-v1.3.50-blue.svg)](https://kotlinlang.org)

Getting Started
------------------------
This sample app is intended to provide guidance on how to use Engage SDK to create android apps that can make use of ProximiPro's Engage platform.
It demeonstrats step by step setup of Engage SDK and how one can configure it according to their needs. 

If you haven't checked out our SDK yet, check it out [here](https://verlos.github.io/engage-android-docs/).

Prerequisites
------------------------

### Android Studio IDE setup

For development, it requires `Android studio version 3.5` or above. The latest version can be downloaded from [here](https://developer.android.com/studio/).

### Libraries Used

* [Foundation][0] - Components for core system capabilities, Kotlin extensions, and support for multidex and automated testing.
  * [AppCompat][1] - Degrade gracefully on older versions of Android.
  * [Android KTX][2] - Write more concise, idiomatic Kotlin code.
* [Architecture][3] - A collection of libraries that help you design robust, testable, and maintainable apps. Start with classes for managing your UI component lifecycle and handling data persistence.
  * [Lifecycles][4] - Create a UI that automatically responds to lifecycle events.
  * [LiveData][5] - Build data objects that notify views when the underlying database changes.
* [UI][6] - Details on why and how to use UI Components in your apps - together or separate
  * [Animations & Transitions][7] - Move widgets and transition between screens.
  * [Fragment][8] - A basic unit of composable UI.
  * [Layout][9] - Lay out widgets using different algorithms.
* Third party
  * [Kotlin Coroutines][10] for managing background threads with simplified code and reducing needs for callbacks
  * [Retrofit][11] A type-safe HTTP client for Android and Java.
  * [SDP][12] An Android SDK that provides a new size unit - sdp (scalable dp). This size unit scales with the screen size.
  * [SSP][13] Variant of sdp project based on the sp size unit.
  * [Timber][14] A logger with a small, extensible API which provides utility on top of Android's normal Log class.
  * [Engage SDK][15] ProximiPro Engage SDK for demo purpose

[0]: https://developer.android.com/jetpack/components
[1]: https://developer.android.com/topic/libraries/support-library/packages#v7-appcompat
[2]: https://developer.android.com/kotlin/ktx
[3]: https://developer.android.com/jetpack/arch/
[4]: https://developer.android.com/topic/libraries/architecture/lifecycle
[5]: https://developer.android.com/topic/libraries/architecture/livedata
[6]: https://developer.android.com/guide/topics/ui
[7]: https://developer.android.com/training/animation/
[8]: https://developer.android.com/guide/components/fragments
[9]: https://developer.android.com/guide/topics/ui/declaring-layout
[10]: https://kotlinlang.org/docs/reference/coroutines-overview.html
[11]: https://square.github.io/retrofit/
[12]: https://github.com/intuit/sdp
[13]: https://github.com/intuit/ssp
[14]: https://github.com/JakeWharton/timber
[15]: https://verlos.github.io/engage-android-docs/

## How to setup project?

1. Clone this repository in a location of your choice, like your projects folder, using the following command over terminal:

```
git clone https://github.com/Verlos/engage-android-demo.git
```

2. Start Android Studio and go File/Open select project folder.

3. This demo app uses Firebase push notifications. therefore, it requires `google-services.json` file to be added at `app/google-services.json`. Create a sample project in firebase and register the package name of the app which is `com.proximipro.engagesdkdemo`. Download `google-services.json` file and put it in project's app directory.

4. Sync project and build it. It will take some time to build and download Gradle dependencies.

5. Once completed, there will be a message that says `"BUILD SUCCESSFUL"`.

6. Yup! You are all set now. To run just hit ► the (run) button.  🚀
