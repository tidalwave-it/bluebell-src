package it.tidalwave.bluebell.bluebell.test;

import android.test.ActivityInstrumentationTestCase2;
import it.tidalwave.bluebell.cameradiscovery.impl.android.CameraDiscoveryPresentationActivity;

public class HelloAndroidActivityTest extends ActivityInstrumentationTestCase2<CameraDiscoveryPresentationActivity> {

    public HelloAndroidActivityTest() {
//        super("it.tidalwave.bluebell.bluebell", CameraDiscoveryPresentationActivity.class);
        super(CameraDiscoveryPresentationActivity.class);
    }

    public void testActivity() {
        CameraDiscoveryPresentationActivity activity = getActivity();
        assertNotNull(activity);
    }
}

