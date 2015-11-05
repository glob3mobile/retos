package oculus;

import org.glob3.mobile.generated.ILogger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.WindowManager;

/*
 * This class is intended to be used as a Northing corrector for 
 * the glasses, as heading appears to be relative to user's first position. 
 * Initially we assume we are going to correct once at the beginning, but this should be extended to cover
 * all cases (two or more people using same app, g.e.)
 */

public class MagnetoTracker  implements SensorEventListener {
	
	double orientedYaw;
	private SensorManager _sensorManager = null;	
	
	

	public MagnetoTracker(Context context) {
		_sensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		
		orientedYaw = Double.MIN_VALUE;
	}

	public void startTrackingDeviceOrientation() {

		if (!_sensorManager.registerListener(this,
				_sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_NORMAL)) {
			ILogger.instance().logError(
					"TYPE_ORIENTATION sensor not supported.");
		}
		
	}

	public void stopTrackingDeviceOrientation() {
		// TODO Auto-generated method stub
		_sensorManager.unregisterListener(this);
		orientedYaw = Double.MIN_VALUE;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		ILogger.instance().logInfo(
				"Sensor " + sensor.getName() + " changed accuracy to "
						+ accuracy);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ORIENTATION:
			orientedYaw = event.values[0];
			break;
		default:
			return;
		}
	}
		
	public double getOrientedYaw(){
		return orientedYaw;
	};
	
	public static double getRealYaw(double initialYaw, double initialRealYaw, double estimatedYaw){
		return initialRealYaw + (estimatedYaw - initialYaw);
	}


}
