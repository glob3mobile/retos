/************************************************************************************

Filename    :   MainActivity.java
Content     :   
Created     :   
Authors     :   

Copyright   :   Copyright 2014 Oculus VR, LLC. All Rights reserved.


*************************************************************************************/
package oculus;

import java.io.IOException;

import org.glob3.mobile.generated.AltitudeMode;
import org.glob3.mobile.generated.Angle;
import org.glob3.mobile.generated.BoxShape;
import org.glob3.mobile.generated.CameraDoubleTapHandler;
import org.glob3.mobile.generated.CameraRenderer;
import org.glob3.mobile.generated.CameraSingleDragHandler;
import org.glob3.mobile.generated.CircleShape;
import org.glob3.mobile.generated.DeviceAttitudeCameraHandler;
import org.glob3.mobile.generated.ElevationData;
import org.glob3.mobile.generated.EllipsoidShape;
import org.glob3.mobile.generated.GFont;
import org.glob3.mobile.generated.Geodetic2D;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.HUDAbsolutePosition;
import org.glob3.mobile.generated.HUDQuadWidget;
import org.glob3.mobile.generated.HUDRelativeSize;
import org.glob3.mobile.generated.HUDRenderer;
import org.glob3.mobile.generated.ICameraConstrainer;
import org.glob3.mobile.generated.IElevationDataListener;
import org.glob3.mobile.generated.ILocationModifier;
import org.glob3.mobile.generated.LabelImageBuilder;
import org.glob3.mobile.generated.Mark;
import org.glob3.mobile.generated.MarksRenderer;
import org.glob3.mobile.generated.Planet;
import org.glob3.mobile.generated.SGShape;
import org.glob3.mobile.generated.Sector;
import org.glob3.mobile.generated.ShapeLoadListener;
import org.glob3.mobile.generated.ShapesRenderer;
import org.glob3.mobile.generated.SingleBilElevationDataProvider;
import org.glob3.mobile.generated.TimeInterval;
import org.glob3.mobile.generated.URL;
import org.glob3.mobile.generated.Vector2I;
import org.glob3.mobile.generated.Vector3D;
import org.glob3.mobile.specific.G3MBuilder_Android;
import org.glob3.mobile.specific.G3MWidget_Android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.oculus.vrappframework.VrActivity;

public class MainActivity extends VrActivity implements TextureView.SurfaceTextureListener{
	public static final String TAG = "VrTemplate";
	
	public Camera camera;
	public TextureView cameraView;
	
	public class VrSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
		
		private View v;
		private SurfaceHolder surfaceHolder;
		
		public VrSurfaceView(Context context) {
			super(context);
			surfaceHolder = getHolder();
			getHolder().addCallback(this);
		}

		
		@SuppressWarnings("deprecation")
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			surfaceHolder = holder;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) { 
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) { 
			
		}
		
		Paint p = new Paint();
		DisplayMetrics displaymetrics = new DisplayMetrics();
		
		@Override
		public void draw(Canvas c){
			super.draw(c);
			doDraw(c);
		}
		
		Bitmap bitmap;
		
		public void doDraw(Bitmap bmap){
			bitmap = bmap.copy(bmap.getConfig(), true);
			Canvas canvas = surfaceHolder.lockCanvas();
            if (null != canvas)
            {
                synchronized(surfaceHolder){
                	doDraw(canvas);
                }
                
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
		}
		
		private void doDraw(Canvas canvas){
			if (bitmap != null){
				canvas.drawBitmap(bitmap,0,0,p);
			}
		}
		
	}

	/** Load jni .so on initialization */
	static {
		Log.d(TAG, "LoadLibrary");
		System.loadLibrary("ovrapp");
	}

	long appHolder = 0;
	
    public static native long nativeSetAppInterface( VrActivity act, String fromPackageNameString, String commandString, String uriString );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String commandString = VrActivity.getCommandStringFromIntent( intent );
		String fromPackageNameString = VrActivity.getPackageStringFromIntent( intent );
		String uriString = VrActivity.getUriStringFromIntent( intent );

		appHolder = nativeSetAppInterface( this, fromPackageNameString, commandString, uriString ); 
		setAppPtr( appHolder );
		
		oculusBridge();
		initGlobe();
    }   
    
    boolean started = false;
    boolean stopped = false;
    
    @Override
    protected void onStop(){
    	super.onStop();
    }
    
    @Override
    protected void onPause(){
    	if (started){
    		try {
    			cameraView.setSurfaceTextureListener(null);
    			camera.stopPreview();
    			camera.release();
    		}
    		catch(Exception E) {
    			E.printStackTrace();
    		}
    		
    		stopped = true;
    	}
    	
    	super.onPause();
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	
    	if (started && stopped){
    		resetEyes();
    		cameraView.setSurfaceTextureListener(this);
    		SurfaceTexture st = cameraView.getSurfaceTexture();
    		cameraView.getSurfaceTextureListener().onSurfaceTextureAvailable(
    				st,cameraView.getWidth() , cameraView.getHeight());
    		stopped = false;
    	}
    }
    
    @Override
    protected void onDestroy(){
        started = false;
    	super.onDestroy();
    }
    
    private RelativeLayout placeHolder;
    public VrSurfaceView leftEye, rightEye;
    LinearLayout linlayout;
    
    private void oculusBridge (){
    	/***
    	 * It will modify the standard Oculus scene to include a globe inside it ...
    	 */
    	
    	ViewGroup ct = (ViewGroup) this.findViewById(android.R.id.content);
		SurfaceView sw = (SurfaceView) ct.getChildAt(0);
		sw.setLayoutParams(new LinearLayout.LayoutParams(1,1,0));
		ct.removeView(sw);
		LayoutInflater l = getLayoutInflater();
		RelativeLayout r = new RelativeLayout(this);
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		// Align bottom and top
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		
		placeHolder = new RelativeLayout(this);
		placeHolder.setLayoutParams(params);
		
		linlayout = new LinearLayout(this);
		linlayout.setLayoutParams(params);
		linlayout.setOrientation(LinearLayout.HORIZONTAL);
		leftEye = new VrSurfaceView(this);
		rightEye = new VrSurfaceView(this);
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,1);
		leftEye.setLayoutParams(params2);
		rightEye.setLayoutParams(params2);
		leftEye.setWillNotDraw(false);
		rightEye.setWillNotDraw(false);
		linlayout.addView(leftEye);
		linlayout.addView(rightEye);
		
		r.addView(linlayout);
		r.addView(placeHolder);
		
		
		cameraView = new TextureView(getApplicationContext());
		cameraView.setSurfaceTextureListener(this);
		cameraView.setLayoutParams(params);
		cameraView.setZ(-100);
		r.addView(cameraView);
		
		
		r.addView(sw);
		
		setContentView(r);
    }
    
	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {

			Log.e("SURFACE TEXTURE","AVAILABLE");
			Log.e("SURFACE TEXTURE INITIAL SIZE", "W: "+width+",H: "+height);
			initCamera();
			try {
			camera.setPreviewTexture(surface);
			camera.startPreview();
	        started = true;
			} catch (IOException ioe) {}
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
	}
	
	Paint p = new Paint();
	DisplayMetrics displaymetrics = new DisplayMetrics();

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		 getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		 int width = displaymetrics.widthPixels / 2;
		 
		Bitmap src = cameraView.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
		Bitmap theEye = Bitmap.createBitmap(src, (width-1) - (width/2), 0, width, displaymetrics.heightPixels);
		leftEye.doDraw(theEye);
		rightEye.doDraw(theEye);
	}
	

    private void initCamera(){
    	camera = Camera.open();
    }
    
    private void resetEyes(){
    	linlayout.removeAllViews();
    	leftEye = new VrSurfaceView(this);
		rightEye = new VrSurfaceView(this);
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,1);
		leftEye.setLayoutParams(params2);
		rightEye.setLayoutParams(params2);
		leftEye.setWillNotDraw(false);
		rightEye.setWillNotDraw(false);
		linlayout.addView(leftEye);
		linlayout.addView(rightEye);
    }
    
    private G3MWidget_Android widget = null;
    //Marcas y formas
    private MarksRenderer marksRenderer = null;
    private ShapesRenderer shapesRenderer = null;
    //Elementos para elevs
    GeoJSONParser rt =  null;
    Sector gcSector = Sector.fromDegrees(27.7116484957735, -15.90589160041418, 28.225913322423995,
			-15.32910937385168);
	Vector2I gcExtent = new Vector2I(500,500);
	private ElevationData elevationData = null;
	
    
    private void initGlobe(){
    	G3MBuilder_Android builder = new G3MBuilder_Android(this);
		
		builder.setPlanet(Planet.createEarth());
		builder.setBackgroundColor(org.glob3.mobile.generated.Color.transparent());
			
		CameraRenderer cr = new CameraRenderer();
			
		cr.addHandler(new DeviceAttitudeCameraHandler(true, new ILocationModifier(){
			
			final double EYE_HEIGHT = 1.8;
			
			@Override
			public Geodetic3D modify(Geodetic3D location) {
				
				Geodetic2D geo2D = location.asGeodetic2D();
				
				if (elevationData != null)
					if (gcSector.contains(geo2D)){
						double hgt = elevationData.getElevationAt(geo2D);
						return new Geodetic3D(geo2D, hgt + EYE_HEIGHT);
					}
					
				return location;
			}
			
		}));
		cr.addHandler(new CameraSingleDragHandler(false));
		cr.addHandler(new CameraDoubleTapHandler());
		
		builder.setCameraRenderer(cr);
		
		final LabelImageBuilder labelBuilder = new LabelImageBuilder("glob3",               // text
                  GFont.monospaced(38), // font
                  6,                     // margin
                  org.glob3.mobile.generated.Color.yellow(),       // color
                  org.glob3.mobile.generated.Color.black(),        // shadowColor
                  3,                     // shadowBlur
                  1,                     // shadowOffsetX
                  -1,                    // shadowOffsetY
                  org.glob3.mobile.generated.Color.red(),          // backgroundColor
                  4,                     // cornerRadius
                  true                   // mutable
                  );

HUDQuadWidget label = new HUDQuadWidget(labelBuilder,
   new HUDAbsolutePosition(10),
   new HUDAbsolutePosition(10),
   new HUDRelativeSize(1, HUDRelativeSize.Reference.BITMAP_WIDTH),
   new HUDRelativeSize(1, HUDRelativeSize.Reference.BITMAP_HEIGHT) );

HUDRenderer hud = new HUDRenderer();
builder.addRenderer(hud);
hud.addWidget(label);
		
		
		marksRenderer = new MarksRenderer(false);
		builder.addRenderer(marksRenderer);
		shapesRenderer = new ShapesRenderer();
		builder.addRenderer(shapesRenderer);
		
		rt = new GeoJSONParser(builder.getDownloader());
		SingleBilElevationDataProvider elevationDataProviderTamaran =
				new SingleBilElevationDataProvider(
						new URL("file:///gc2.bil",false),
						gcSector,
						gcExtent);

		elevationDataProviderTamaran.requestElevationData(gcSector, gcExtent,10000,new MyElevationDataListener(),true);
		builder.getPlanetRendererBuilder().setElevationDataProvider(elevationDataProviderTamaran);
		builder.addCameraConstraint(new ICameraConstrainer(){
			
			boolean shouldDrawMarkers = true;
			@Override
			public void dispose() {}

			@Override
			public boolean onCameraChange(Planet planet, org.glob3.mobile.generated.Camera previousCamera,
					org.glob3.mobile.generated.Camera nextCamera) {

				labelBuilder.setText("H: "+ nextCamera.getHeading()._degrees+"P: "+nextCamera.getPitch()._degrees +",R: "+nextCamera.getRoll()._degrees + " ");
				
				
				if (rt.markers != null && widget != null && shouldDrawMarkers) {
					addMarkers();
					addSomeShapes();
					shouldDrawMarkers = false;
				}
				
				return false;
			}
			
		});

		widget = builder.createWidget();
		placeHolder.addView(widget);

		widget.setZOrderOnTop(true);
		widget.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		widget.getNextCamera().setFixedZNear(3);
		widget.getG3MWidget().getCurrentCamera().setFixedZNear(3);
    }
    
    private void addMarkers() {
    	for (int i=0; i<rt.markers.size(); i++){
    		Marker m = rt.markers.get(i);
    		Geodetic2D point = m.getPoint();
    		double hgt = 0;
    		if (gcSector.contains(point)){
    			hgt = elevationData.getElevationAt(point);
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
    		
    		marksRenderer.addMark(mark);
    	}
    	
    	
    }
    
    private void addSomeShapes(){
    	
    	Geodetic2D cylinderPos = Geodetic2D.fromDegrees(28.112306,-15.423633);//(28.072635, -15.451248);
    	Geodetic2D ballPos = Geodetic2D.fromDegrees(28.114289,-15.401381);
    	Geodetic2D boxPos = Geodetic2D.fromDegrees(27.906961,-15.443478);
    	double modelOffset = 1;
    	
    	double cylHgt = 0, ballHgt = 0, boxHgt = 0;


    	if (gcSector.contains(ballPos)){
			ballHgt = elevationData.getElevationAt(ballPos);
			if (ballHgt < 0 || Double.isNaN(ballHgt) || Double.isInfinite(ballHgt)) ballHgt = 0;
		}

    	cylHgt = elevationData.getElevationAt(cylinderPos);
		if (cylHgt < 0 || Double.isNaN(cylHgt) || Double.isInfinite(cylHgt)) cylHgt = 0;
		
		
		boxHgt = elevationData.getElevationAt(boxPos);
		if (boxHgt < 0 || Double.isNaN(boxHgt) || Double.isInfinite(boxHgt)) boxHgt = 0;
    	
    	BoxShape theBox = new BoxShape(new Geodetic3D(boxPos,boxHgt+modelOffset),
    			AltitudeMode.ABSOLUTE,
    			new Vector3D(1000,1000,1000),
    			1.0f, 
    			org.glob3.mobile.generated.Color.fromRGBA255(0, 0, 255, 220));
    	
    	CircleShape theCircle = new CircleShape(new Geodetic3D(cylinderPos,cylHgt+modelOffset),
    			AltitudeMode.ABSOLUTE, 
    			10, 
    			org.glob3.mobile.generated.Color.blue());

    	theCircle.setPitch(theCircle.getPitch().add(Angle.halfPi));
    	
    	
    	EllipsoidShape theBall = new EllipsoidShape(new Geodetic3D(ballPos,ballHgt+modelOffset),
    			AltitudeMode.ABSOLUTE, 
    			new Vector3D(1000,1000,1000),
    			(short) 10,
    			1f, 
    			false, 
    			false, 
    			org.glob3.mobile.generated.Color.green());
    	
    	EllipsoidShape theBall2 = new EllipsoidShape(new Geodetic3D(cylinderPos,cylHgt),
    			AltitudeMode.ABSOLUTE, 
    			new Vector3D(5,5,20),
    			(short) 10,
    			1f, 
    			false, 
    			false, 
    			org.glob3.mobile.generated.Color.red());
		theBall.setTranslation(-2,0,1);
		shapesRenderer.addShape(theBall);
    	

    	shapesRenderer.addShape(theBall2);
    	shapesRenderer.addShape(theBall);
    	
    	shapesRenderer.loadBSONSceneJS(new URL("file:///A320.bson", false), "file:///textures-A320/", false, 
    			new Geodetic3D(cylinderPos,1000),
                AltitudeMode.ABSOLUTE, new ShapeLoadListener() {

                   @Override
                   public void onBeforeAddShape(final SGShape shape) {
                      shape.setScale(20);
                      shape.setPitch(Angle.fromDegrees(90));
                   }

                   @Override
                   public void onAfterAddShape(final SGShape shape) {}

                   @Override
                   public void dispose() {}
                });
    }
    
    public class MyElevationDataListener implements IElevationDataListener {
    	
    	MyElevationDataListener(){}
    	
    	@Override
    	public void onData(Sector sector, Vector2I extent, ElevationData elevData){
    		elevationData = elevData;
    		elevationData._retain();
    		if (widget != null){

    			widget.getG3MWidget().getPlanetRenderer().setEnable(false);
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

