NotificationLog
===============

Use an expanded notification as a makeshift Android log output console.

![Screenshots][1]

The design goals of this project are as follows: 
+ Provide an on-device view of explicit log output during app development.
+ Do not disrupt the normal lifecycle of the app (no app switching).
+ Transparent implementation (using the same API as [android.util.Log][3], easily disabled).
+ Minimal setup.

This library does not currently output the system LogCat, only messages explicitly logged within the app. The status of this project should be considered "experimental".

Usage
-----

Include the jar file (run `ant jar` to build from source, or download it [here][2]) in your project. NotificationLog has the same API as [android.util.Log][3]. You should switch your imports to `com.readystatesoftware.notificationlog.Log`.

By default, NotificationLog behaves exactly the same as [android.util.Log][3]. To enable the additional notification logging, you must first initialize the class for notifications. You might want to do that in your Application class like so:

````java
@Override
public void onCreate() {
    super.onCreate();
    if (BuildConfig.DEBUG) {
        Log.initialize(this);
    }
}
````

Once initialized, the `Log.v()` `Log.d()` `Log.i()` `Log.w()` `Log.e()` and `Log.wtf()` will write to the notification. This can be enabled or disabled at any time using the `Log.setNotificationsEnabled()` method.

Advanced Integration
--------------------

To display a more detailed view of the log buffer and provide additional filter, level and clear actions, you must add the following to your manifest:

````xml
<activity 
    android:name="com.readystatesoftware.notificationlog.LogActivity" 
    android:exported="true"
    android:launchMode="singleTop" />
```` 

Toasts
------

You can have log messages additionally appear as Toasts by using the `Log.setToastsEnabled()` method. This is disabled by default and should not be enabled during any performance critical code. Toast notifications will not appear when the Log methods are called off of the main thread.

Credits
-------

Author: [Jeff Gilfelt](https://github.com/jgilfelt)

License
-------

    Copyright 2013 readyState Software Ltd

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.




 [1]: https://raw.github.com/jgilfelt/NotificationLog/master/screenshot.png
 [2]: https://raw.github.com/jgilfelt/NotificationLog/master/builds/notificationlog-0.1.0.jar
 [3]: https://developer.android.com/reference/android/util/Log.html
 
