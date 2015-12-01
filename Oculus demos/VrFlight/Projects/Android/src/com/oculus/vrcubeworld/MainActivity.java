/************************************************************************************

Filename    :   MainActivity.java
Content     :   
Created     :   
Authors     :   

Copyright   :   Copyright 2014 Oculus VR, LLC. All Rights reserved.

*************************************************************************************/
package com.oculus.vrcubeworld;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.oculus.vrappframework.VrActivity;

import org.glob3.mobile.generated.AltitudeMode;
import org.glob3.mobile.generated.Angle;
import org.glob3.mobile.generated.Camera;
import org.glob3.mobile.generated.CameraDoubleTapHandler;
import org.glob3.mobile.generated.CameraRenderer;
import org.glob3.mobile.generated.CameraSingleDragHandler;
import org.glob3.mobile.generated.Color;
import org.glob3.mobile.generated.CompositeElevationDataProvider;
import org.glob3.mobile.generated.DeviceAttitudeCameraHandler;
import org.glob3.mobile.generated.ElevationData;
import org.glob3.mobile.generated.GEO2DCoordinatesData;
import org.glob3.mobile.generated.GEO2DLineRasterStyle;
import org.glob3.mobile.generated.GEOLineRasterSymbol;
import org.glob3.mobile.generated.GEOVectorLayer;
import org.glob3.mobile.generated.GFont;
import org.glob3.mobile.generated.Geodetic2D;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.HUDQuadWidget;
import org.glob3.mobile.generated.HUDRelativePosition;
import org.glob3.mobile.generated.HUDRelativeSize;
import org.glob3.mobile.generated.HUDRenderer;
import org.glob3.mobile.generated.ICameraConstrainer;
import org.glob3.mobile.generated.IElevationDataListener;
import org.glob3.mobile.generated.LabelImageBuilder;
import org.glob3.mobile.generated.LayerSet;
import org.glob3.mobile.generated.LayerTilesRenderParameters;
import org.glob3.mobile.generated.LevelTileCondition;
import org.glob3.mobile.generated.Mark;
import org.glob3.mobile.generated.MarksRenderer;
import org.glob3.mobile.generated.Planet;
import org.glob3.mobile.generated.Quality;
import org.glob3.mobile.generated.Sector;
import org.glob3.mobile.generated.SingleBilElevationDataProvider;
import org.glob3.mobile.generated.StrokeCap;
import org.glob3.mobile.generated.StrokeJoin;
import org.glob3.mobile.generated.TimeInterval;
import org.glob3.mobile.generated.URL;
import org.glob3.mobile.generated.Vector2I;
import org.glob3.mobile.generated.WMSLayer;
import org.glob3.mobile.generated.WMSServerVersion;
import org.glob3.mobile.specific.G3MBuilder_Android;
import org.glob3.mobile.specific.G3MWidget_Android;
import org.glob3.mobile.specific.MotionEventProcessor;

import java.util.ArrayList;
import java.util.LinkedList;

import com.oculus.vrcubeworldfw.R;

public class MainActivity extends VrActivity {

	public static final String TAG = "VrCubeWorld";

	private final boolean usingG3MEventController = false;

	private MotionEventProcessor processor = new MotionEventProcessor();
	private G3MWidget_Android widget = null;
	private long appPtr;

	/* Sobre elevaciones y ruta */
	private LayerSet layerSet;
	private GeoJSONParser rt;
	private GEOVectorLayer vectorialLayer = null;
	Sector gcSector = Sector.fromDegrees(27.7116484957735, -15.90589160041418, 28.225913322423995,
			-15.32910937385168);
	Vector2I gcExtent = new Vector2I(500,500);
	private ElevationData elevationData = null;
	private ElevationData elevationDataTamaran = null;

	private Sector tamaranSector = Sector.fromDegrees(27.734232482194695, -15.74588721413459, 28.11598082866073, -15.564356385426944);
	Vector2I tamaranExtent = new Vector2I(588,1413);

	private MarksRenderer marksRenderer = null;

	/* Sobre HUD */
	HUDRenderer hudRenderer;

	
	/** Load jni .so on initialization */
	static {
		Log.d( TAG, "LoadLibrary" );
		System.loadLibrary( "vrcubeworld" );
	}

	public static native long nativeSetAppInterface( VrActivity act, String fromPackageNameString, String commandString, String uriString );
	public static native float getHeadSetYaw();
	public static native float getHeadSetRoll();
	public static native float getHeadSetPitch();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d( TAG, "onCreate" );
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String commandString = VrActivity.getCommandStringFromIntent( intent );
		String fromPackageNameString = VrActivity.getPackageStringFromIntent( intent );
		String uriString = VrActivity.getUriStringFromIntent( intent );

		setAppPtr( nativeSetAppInterface( this, fromPackageNameString, commandString, uriString ) );

		oculusBridge();
		setGlobe(true);
	}

	@Override
	public boolean buttonEvent(
			int deviceId, int keyCode,
			boolean down, int repeatCount ) {

		if (down) {
			if ((keyCode & 0xFFFF) == KeyEvent.KEYCODE_BACK) {
				System.exit(0);
				return true;
			}
		}

		return super.buttonEvent(deviceId, keyCode, down, repeatCount);
	}

	private float lastX, lastY;
	private int forwardFactor = 0;
	private final int FORWARD_PERCENT = 2; //5;

	@Override
	public boolean dispatchTouchEvent (MotionEvent e){
		if (widget != null)
			if (usingG3MEventController) return widget.onTouchEvent(e);
			else {
				switch (e.getAction()) {
					case MotionEvent.ACTION_DOWN:
						lastX = e.getX();
						lastY = e.getY();
						break;
					case MotionEvent.ACTION_UP :
						if (e.getX() < lastX) {
							forwardFactor ++;
						}
						else if (e.getX() > lastX ){
							forwardFactor --;
						}
						break;
				}
				return false;
			}
		else return false;
	}

	private void oculusBridge (){
		/***
		 * It will modify the standard Oculus scene to include a globe inside it ...
		 */

		ViewGroup ct = (ViewGroup) this.findViewById(android.R.id.content);
		SurfaceView sw = (SurfaceView) ct.getChildAt(0);
		sw.setLayoutParams(new LinearLayout.LayoutParams(1,1,0));
		ct.removeView(sw);
		LayoutInflater l = getLayoutInflater();
		RelativeLayout r = (RelativeLayout) l.inflate(R.layout.globo, null);
		r.addView(sw);
		setContentView(r);
	}

	private void setGlobe(boolean withHeadset){

		G3MBuilder_Android builder = new G3MBuilder_Android(this);

		builder.setPlanet(Planet.createFlatEarth());

		CameraRenderer cr = new CameraRenderer();

		if (!withHeadset) cr.addHandler(new DeviceAttitudeCameraHandler(true));
		cr.addHandler(new CameraSingleDragHandler(false));
		cr.addHandler(new CameraDoubleTapHandler());

		builder.setCameraRenderer(cr);

		//Adding marks renderer
		marksRenderer = new MarksRenderer(false);
		builder.addRenderer(marksRenderer);

		//Adding cool init message;
		hudRenderer = new HUDRenderer();
		hudRenderer.addWidget(new HUDQuadWidget(
				new LabelImageBuilder("Usa el trackpad para moverte atrás o adelante",
						GFont.sansSerif(),
						32),
				new HUDRelativePosition(0.85f, HUDRelativePosition.Anchor.VIEWPORT_WIDTH, HUDRelativePosition.Align.LEFT),
				new HUDRelativePosition(0.5f, HUDRelativePosition.Anchor.VIEWPORT_HEIGHT, HUDRelativePosition.Align.ABOVE),
				new HUDRelativeSize(0.7f, HUDRelativeSize.Reference.VIEWPORT_WIDTH),
				new HUDRelativeSize(0.2f, HUDRelativeSize.Reference.VIEWPORT_HEIGHT)));
		builder.addRenderer(hudRenderer);

		CountDownTimer t = new CountDownTimer(5000,5000){


			@Override
			public void onTick(long millisUntilFinished) {}

			@Override
			public void onFinish() {
				hudRenderer.setEnable(false);
			}
		};
		t.start();

		//Adding layers
		layerSet = new LayerSet();
		layerSet.addLayer(new WMSLayer("WMS_OrtoExpress", new URL(
				"http://idecan1.grafcan.es/ServicioWMS/OrtoExpress?", false),
				WMSServerVersion.WMS_1_1_0, Sector.FULL_SPHERE, "image/jpeg", "EPSG:4326", "",
				false, new LevelTileCondition(0, 19), TimeInterval.fromDays(30), true));
		builder.getPlanetRendererBuilder().setLayerSet(layerSet);

		rt = new GeoJSONParser(builder.getDownloader());
		//Adding elevations:  Por ahora tamarán a secas
		SingleBilElevationDataProvider elevationDataProviderTamaran =
				new SingleBilElevationDataProvider(
						new URL("file:///34.bil",false),
						tamaranSector,
						tamaranExtent);

		//Adding providers ...
		SingleBilElevationDataProvider elevationDataProviderGC =
				new SingleBilElevationDataProvider(
						new URL("file:///gc2.bil",false),
						gcSector,
						gcExtent);

		CompositeElevationDataProvider edp = new CompositeElevationDataProvider();
		edp.addElevationDataProvider(elevationDataProviderTamaran);
		edp.addElevationDataProvider(elevationDataProviderGC);

		edp.requestElevationData(tamaranSector, tamaranExtent,10000,new MyElevationDataListener(1),true);
		edp.requestElevationData(gcSector,gcExtent,10000,new MyElevationDataListener(0),true);

		builder.getPlanetRendererBuilder().setElevationDataProvider(edp);

		//builder.getPlanetRendererBuilder().setElevationDataProvider(elevationDataProviderTamaran);

		builder.addCameraConstraint(new ICameraConstrainer(){

			boolean shouldDrawRoute = true;
			boolean shouldDrawMarkers = true;
			@Override
			public void dispose() {}

			@Override
			public boolean onCameraChange(Planet planet, Camera previousCamera,
										  Camera nextCamera) {
				if (rt.routes != null && widget != null && shouldDrawRoute) {
					//Cuando la ruta haya cargado, se pinta. Antes no.
					addRasterLineShapes(layerSet, rt.routes);
					shouldDrawRoute = false;
				}
				if (elevationDataTamaran != null && rt.markers != null && widget != null && shouldDrawMarkers) {
					//Cuando los markers hayan cargado, se pintan. Antes no.
					addMarkers();
					shouldDrawMarkers = false;
				}


				double zNear = 6;
				double hgtGround = getHeightFromGround(nextCamera);
				if (hgtGround < zNear){
					nextCamera.copyFrom(previousCamera);
					Geodetic3D pos = nextCamera.getGeodeticPosition();
					nextCamera.setGeodeticPosition(pos.asGeodetic2D(),pos._height+5);
				}
				return false;
			}

		});

		if (withHeadset)
			builder.addCameraConstraint(new ICameraConstrainer(){

				@Override
				public void dispose() {}

				@Override
				public boolean onCameraChange(Planet planet,
											  Camera previousCamera, Camera nextCamera) {

					nextCamera.setHeadingPitchRoll(
							Angle.fromRadians(getHeadSetYaw()),
							Angle.fromRadians(getHeadSetPitch()),
							Angle.fromRadians(getHeadSetRoll()).times(-1));

					if (!usingG3MEventController){
						if (forwardFactor != 0){
							if (hudRenderer.isEnable()) hudRenderer.setEnable(false);
							//Mover. Evitar clavarse en el piso.
							double gDistance;
							if (gcSector.contains(nextCamera.getGeodeticPosition().asGeodetic2D()))
								gDistance = Math.max(1,getHeightFromGround(nextCamera));
							else
								gDistance = nextCamera.getGeodeticPosition()._height;

							nextCamera.translateCamera(
									nextCamera.getViewDirection().normalized().
											times(forwardFactor * (FORWARD_PERCENT/100.0)
													* gDistance));
						}
					}

					return false;
				}

			});

		builder.getPlanetRendererBuilder().setQuality(Quality.QUALITY_HIGH);
		widget = builder.createWidget();

		final RelativeLayout placeHolder = (RelativeLayout) findViewById(R.id.g3mWidgetHolder);
		if (placeHolder != null)
			placeHolder.addView(widget);

		if (withHeadset) {
			widget.setCameraPosition(Geodetic3D.fromDegrees(27.759028,-15.57905,200));
		}
		widget.getG3MWidget().getCurrentCamera().setFixedZNear(5);
		widget.getNextCamera().setFixedZNear(5);

	}

	private double getHeightFromGround(Camera c){
		Geodetic3D pos = c.getGeodeticPosition();
		Geodetic2D pos2D = pos.asGeodetic2D();
		if (elevationDataTamaran != null && elevationData != null) {
			double hgt = 0;
			if (tamaranSector.contains(pos2D)){
				hgt = elevationDataTamaran.getElevationAt(pos2D);
			}
			else if (gcSector.contains(pos2D)){
				hgt = elevationData.getElevationAt(pos2D);
			}

			//Saving bil issues ...
			if (hgt < 0 || Double.isNaN(hgt) || Double.isInfinite(hgt)) {
				hgt = 0;
			}

			return pos._height - hgt;
		}
		else return pos._height;
	}

	private void addMarkers() {
		for (int i=0; i<rt.markers.size(); i++){
			Marker m = rt.markers.get(i);
			Geodetic2D point = m.getPoint();
			double hgt = 0;
			if (tamaranSector.contains(point)){
				hgt = elevationDataTamaran.getElevationAt(point);
				if (hgt < 0 || Double.isNaN(hgt) || Double.isInfinite(hgt)) hgt = 0;
			}

			Mark mark = new Mark(m.getName(),
					new URL(m.getIcon(),false),
					new Geodetic3D(point,hgt),
					AltitudeMode.ABSOLUTE,
					1000000,
					true,
					20
			);
			//mark.setOnScreenSizeOnPixels(50, 50); //Original: 50x50;
			marksRenderer.addMark(mark);
		}
	}

	private void addRasterLineShapes (LayerSet lyr, ArrayList<LinkedList<Geodetic2D>> rt){
		float dashLengths[] = {};
		int dashCount = 0;
		Color c = Color.red();
		ArrayList<LayerTilesRenderParameters> vectoParams = new ArrayList<LayerTilesRenderParameters>();
		vectoParams.add(widget.getG3MWidget().getPlanetRenderer().getLayerTilesRenderParameters());
		vectorialLayer = new GEOVectorLayer(vectoParams);
		for (int i=0; i< rt.size();i++){
			ArrayList<Geodetic2D> lineCoordinates = new ArrayList<Geodetic2D>(rt.get(i));
			GEO2DLineRasterStyle ls2 = new GEO2DLineRasterStyle(Color.black(), //const Color&     color,
					(float)4.0, //const float      width,
					StrokeCap.CAP_ROUND, // const StrokeCap  cap,
					StrokeJoin.JOIN_ROUND, //const StrokeJoin join,
					1,//const float      miterLimit,
					dashLengths,//float            dashLengths[],
					dashCount,//const int        dashCount,
					0);
			GEO2DLineRasterStyle ls = new GEO2DLineRasterStyle(c, //const Color&     color,
					(float)3.0, //const float      width,
					StrokeCap.CAP_ROUND, // const StrokeCap  cap,
					StrokeJoin.JOIN_ROUND, //const StrokeJoin join,
					1,//const float      miterLimit,
					dashLengths,//float            dashLengths[],
					dashCount,//const int        dashCount,
					0);
			vectorialLayer.addSymbol(new GEOLineRasterSymbol(
					new GEO2DCoordinatesData(lineCoordinates),
					ls2));
			vectorialLayer.addSymbol(new GEOLineRasterSymbol(
					new GEO2DCoordinatesData(lineCoordinates),
					ls));
		}
		lyr.addLayer(vectorialLayer);
	}

	/// Auxiliar classes - Elevs

	public class MyElevationDataListener implements IElevationDataListener {
		private int _zone;

		MyElevationDataListener(int zone){_zone = zone;}

		@Override
		public void onData(Sector sector, Vector2I extent, ElevationData elevData){
			if (_zone == 0){
				elevationData = elevData;
			}
			else{
				elevationDataTamaran = elevData;
			}

		}

		@Override
		public void onError(Sector sector,Vector2I extent) {}
		@Override
		public void onCancel(Sector sector, Vector2I extent) {}
		@Override
		public void dispose() {}
	};
}
