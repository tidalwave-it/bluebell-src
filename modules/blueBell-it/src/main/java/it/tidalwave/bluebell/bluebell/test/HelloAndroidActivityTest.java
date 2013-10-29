package it.tidalwave.bluebell.bluebell.test;

import android.test.ActivityInstrumentationTestCase2;
import it.tidalwave.bluebell.mobile.SampleCameraActivity;

public class HelloAndroidActivityTest extends ActivityInstrumentationTestCase2<SampleCameraActivity> {

    public HelloAndroidActivityTest() {
        super("it.tidalwave.bluebell.bluebell", SampleCameraActivity.class);
    }

    public void testActivity() {
        SampleCameraActivity activity = getActivity();
        assertNotNull(activity);
    }
}

