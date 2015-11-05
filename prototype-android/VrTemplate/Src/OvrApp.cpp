/************************************************************************************

Filename    :   OvrApp.cpp
Content     :   Trivial use of the application framework.
Created     :   
Authors     :   

Copyright   :   Copyright 2014 Oculus VR, LLC. All Rights reserved.

*************************************************************************************/

#include "OvrApp.h"
#include "GuiSys.h"
#include "OVR_Locale.h"

using namespace OVR;

extern "C" {

jlong Java_oculus_MainActivity_nativeSetAppInterface( JNIEnv * jni, jclass clazz, jobject activity,
		jstring fromPackageName, jstring commandString, jstring uriString )
{
	LOG( "nativeSetAppInterface" );
	return (new OvrTemplateApp::OvrApp())->SetActivity( jni, clazz, activity, fromPackageName, commandString, uriString );
}

float Java_oculus_MainActivity_getHeadSetYaw(JNIEnv * jni, jclass clazz) {
	return OvrTemplateApp::OvrApp::yaw;
}

float Java_oculus_MainActivity_getHeadSetRoll(JNIEnv * jni, jclass clazz) {
	return OvrTemplateApp::OvrApp::roll;
}

float Java_oculus_MainActivity_getHeadSetPitch(JNIEnv * jni, jclass clazz) {
	return OvrTemplateApp::OvrApp::pitch;
}

float Java_oculus_MainActivity_getHeadSetYawOffset(JNIEnv * jni, jclass clazz){
	return OvrTemplateApp::OvrApp::yawOffset;
}
}// extern "C"

namespace OvrTemplateApp
{

OvrApp::OvrApp()
	: GuiSys( OvrGuiSys::Create() )
	, Locale( NULL )
{
}

OvrApp::~OvrApp()
{
	OvrGuiSys::Destroy( GuiSys );
}

void OvrApp::PrintMessage(float time, const char * message){
	app->ShowInfoText(time,message);
}

void OvrApp::Configure( ovrSettings & settings )
{
	settings.PerformanceParms.CpuLevel = 2;
	settings.PerformanceParms.GpuLevel = 2;

	//app->ShowInfoText(5,"Allahu Akbar");
	//app->StartSystemActivity(PUI_GLOBAL_MENU);

	//Reducing time rate tracking to 1/30 (globe standards) and applying it to a Stick model.
	/*const ovrTracking baseTracking = vrapi_GetPredictedTracking( app->GetOvrMobile(), 1/30 );
	const ovrHeadModelParms headModelParms = vrapi_DefaultHeadModelParms();
	vrapi_ApplyHeadModel( &headModelParms, &baseTracking );*/
}

void OvrApp::OneTimeInit( const char * fromPackage, const char * launchIntentJSON, const char * launchIntentURI )
{
	auto java = app->GetJava();
	//¿Arranca con sonidito chorra?
	SoundEffectContext.reset( new ovrSoundEffectContext( *java->Env, java->ActivityObject ) );
	SoundEffectContext->Initialize();
	SoundEffectPlayer.reset( new OvrGuiSys::ovrDummySoundEffectPlayer() );

	Locale = ovrLocale::Create( *app, "default" );

	String fontName;
	GetLocale().GetString( "@string/font_name", "efigs.fnt", fontName );
	GuiSys->Init( this->app, *SoundEffectPlayer, fontName.ToCStr(), &app->GetDebugLines() );
        
	const OvrStoragePaths & paths = app->GetStoragePaths();
	//Busca direcciones a carpeta de assets de oculus, creo.
	Array<String> SearchPaths;
	paths.PushBackSearchPathIfValid( EST_SECONDARY_EXTERNAL_STORAGE, EFT_ROOT, "RetailMedia/", SearchPaths );
	paths.PushBackSearchPathIfValid( EST_SECONDARY_EXTERNAL_STORAGE, EFT_ROOT, "", SearchPaths );
	paths.PushBackSearchPathIfValid( EST_PRIMARY_EXTERNAL_STORAGE, EFT_ROOT, "RetailMedia/", SearchPaths );
	paths.PushBackSearchPathIfValid( EST_PRIMARY_EXTERNAL_STORAGE, EFT_ROOT, "", SearchPaths );

	const char * scenePath = "Oculus/tuscany2.ovrscene";
	String SceneFile;
	//Si está esa escena, la carga.
	if ( GetFullPath( SearchPaths, scenePath, SceneFile ) )
	{
		MaterialParms materialParms;
		materialParms.UseSrgbTextureFormats = false;
		Scene.LoadWorldModel( SceneFile.ToCStr(), materialParms );
		Scene.SetYawOffset( -M_PI * 0.5f );
	}
	else
	{
		LOG( "OvrApp::OneTimeInit SearchPaths failed to find %s", scenePath );
	}
}

void OvrApp::OneTimeShutdown()
{
	// Free GL resources
}

bool OvrApp::OnKeyEvent( const int keyCode, const int repeatCount, const KeyEventType eventType )
{
	if ( GuiSys->OnKeyEvent( keyCode, repeatCount, eventType ) )
	{
		//Aquí podríamos tratar eventos de teclado, imagino ...
		return true;
	}
	return false;
}

Matrix4f OvrApp::Frame( const VrFrame & vrFrame )
{
	// Player movement.
	Scene.Frame( vrFrame, app->GetHeadModelParms() );

	// Update GUI systems after the app frame, but before rendering anything.
	GuiSys->Frame( vrFrame, Scene.GetCenterEyeViewMatrix() );

	//Updating my headset data;
	yaw = Scene.GetEyeYaw();
	pitch = Scene.GetEyePitch();
	roll = Scene.GetEyeRoll();
	yawOffset = Scene.GetYawOffset();

	return Scene.GetCenterEyeViewMatrix();
}

Matrix4f OvrApp::DrawEyeView( const int eye, const float fovDegreesX, const float fovDegreesY, ovrFrameParms & frameParms )
{
	const Matrix4f eyeViewProjection = Scene.DrawEyeView( eye, fovDegreesX, fovDegreesY );

	frameParms.ExternalVelocity = Scene.GetExternalVelocity();

	GuiSys->RenderEyeView( Scene.GetCenterEyeViewMatrix(), eyeViewProjection );

	return eyeViewProjection;
}

} // namespace OvrTemplateApp
