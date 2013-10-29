package it.tidalwave.bluebell.bluebell.test;

import android.test.ActivityInstrumentationTestCase2;
import it.tidalwave.bluebell.bluebell.*;

public class HelloAndroidActivityTest extends ActivityInstrumentationTestCase2<HelloAndroidActivity> {

    public HelloAndroidActivityTest() {
        super("it.tidalwave.bluebell.bluebell", HelloAndroidActivity.class);
    }

    public void testActivity() {
        HelloAndroidActivity activity = getActivity();
        assertNotNull(activity);
    }
}

