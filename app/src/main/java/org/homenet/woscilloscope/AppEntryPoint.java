package org.homenet.woscilloscope;

import android.app.Application;
import android.content.res.Configuration;

/**
 * Created by bbiggu on 2015. 11. 2..
 */
public class AppEntryPoint extends Application {
    // Debugging
    private final static String TAG = AppEntryPoint.class.getSimpleName(); //"AppEntryPoint";
    private static final boolean D = true;
    //
    public long starttime = 0;
    public long endtime = 0;

    @Override
    public void onCreate() {
        // Called when the application is starting, before any activity, service, or
        // receiver objects (excluding content providers) have been created.
        // Implementations should be as quick as possible (for example using lazy initialization of state)
        // since the time spent in this function directly impacts the performance of
        // starting the first activity, service, or receiver in a process.
        // If you override this method, be sure to call super.onCreate().
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        // This method is for use in emulated process environments.
        // It will never be called on a production Android device,
        // where processes are removed by simply killing them;
        // no user code (including this callback) is executed when doing so.
        super.onTerminate();
    }

    @Override
    public void onLowMemory () {
        // This is called when the overall system is running low on memory,
        // and actively running processes should trim their memory usage.
        // While the exact point at which this will be called is not defined,
        // generally it will happen when all background process have been killed.
        // That is, before reaching the point of killing processes hosting service
        // and foreground UI that we would like to avoid killing.
        //
        // You should implement this method to release any caches or other
        // unnecessary resources you may be holding on to.
        // The system will perform a garbage collection for you after returning from this method.
        super.onLowMemory();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Called by the system when the device configuration changes while your component is running.
        // Note that, unlike activities, other components are never restarted when a configuration changes:
        // they must always deal with the results of the change, such as by re-retrieving resources.
        //
        // At the time that this function has been called, your Resources object will have been
        // updated to return resource values matching the new configuration.
        //
        // For more information, read Handling Runtime Changes.
        super.onConfigurationChanged(newConfig);
    }
}
