/**
 * Created by Ludo van den Boom <ludo@cubicphuse.nl> on 06/04/2017.
 */

package com.cubicphuse.RCTTorch;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Build;


import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class RCTTorchModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext myReactContext;
    private Boolean isTorchOn = false;
    private Camera camera;
    private CameraManager cameraManager; //

    public RCTTorchModule(ReactApplicationContext reactContext) {
        super(reactContext);

        // Need access to reactContext to check for camera
        this.myReactContext = reactContext;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.cameraManager = (CameraManager) this.myReactContext.getSystemService(Context.CAMERA_SERVICE);
        }
    }
    @Override
    public String getName() {
        return "RCTTorch";
    }

    @ReactMethod
    public void switchState(Boolean newState, Callback successCallback, Callback failureCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           
            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, newState);
                successCallback.invoke(true);
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                failureCallback.invoke("Error: " + errorMessage);
            }
        } else {
            Camera.Parameters params;

            if (newState && !isTorchOn) {
                camera = Camera.open();
                params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                camera.startPreview();
                isTorchOn = true;
            } else if (isTorchOn) {
                params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

                camera.setParameters(params);
                camera.stopPreview();
                camera.release();
                isTorchOn = false;
            }
        }
    }

    @ReactMethod
    public void getFlashIntensity(Callback successCallback, Callback failureCallback) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
         
        try {
            String cameraId = cameraManager.getCameraIdList()[0];

            // Check if the device supports the method (added in Android 13)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int flashIntensity = cameraManager.getTorchStrengthLevel(cameraId);
                successCallback.invoke(flashIntensity);
                
            } else {
                failureCallback.invoke("Flash intensity not supported on this Android version");
            }
        } catch (CameraAccessException e) {
            String errorMessage = e.getMessage();
            failureCallback.invoke("Error: " + errorMessage);
        }
    } else {
        failureCallback.invoke("Flash intensity not supported on this device");
    }
}

@ReactMethod
public void changeFlashlightStrength( int torchStrength, Callback successCallback, Callback failureCallback) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
             String cameraId = cameraManager.getCameraIdList()[0];
            // Check if the camera device supports flash unit strength control
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            Integer maxFlashStrength = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL);

            if (maxFlashStrength == null || maxFlashStrength <= 1) {
                failureCallback.invoke("Flash unit strength control not supported for this camera device");
                return;
            }

            // Check if the camera device has a flash unit
            Boolean hasFlashUnit = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            if (hasFlashUnit == null || !hasFlashUnit) {
                failureCallback.invoke("Camera device does not have a flash unit");
                return;
            }

            // Check if the torch is already ON with the same strength level
            if (isTorchOn(cameraId) && getCurrentTorchStrength(cameraId) == torchStrength) {
                successCallback.invoke("Flashlight strength set successfully");
                return;
            }

            // Turn ON the torch with the specified brightness level
            cameraManager.turnOnTorchWithStrengthLevel(cameraId, torchStrength);
            successCallback.invoke("Flashlight strength set successfully");
        } catch (CameraAccessException e) {
            failureCallback.invoke("Error setting flashlight strength: " + e.getMessage());
        }
    } else {
        failureCallback.invoke("Flashlight strength control not supported on this Android version");
    }
}

    // Method to check if the torch is currently ON
    private boolean isTorchOn(String cameraId) throws CameraAccessException {
        return cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) != null;
    }

    // Method to get the current torch strength level
    private int getCurrentTorchStrength(String cameraId) throws CameraAccessException {
        return cameraManager.getTorchStrengthLevel(cameraId);
    }
    
}
