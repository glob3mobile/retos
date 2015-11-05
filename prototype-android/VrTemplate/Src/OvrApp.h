/************************************************************************************

Filename    :   OvrApp.h
Content     :   Trivial use of the application framework.
Created     :   
Authors     :   

Copyright   :   Copyright 2014 Oculus VR, LLC. All Rights reserved.

************************************************************************************/

#ifndef OVRAPP_H
#define OVRAPP_H

#include "App.h"
#include "VrApi.h"
#include "SceneView.h"
#include "SoundEffectContext.h"
#include <memory>
#include "GuiSys.h"

namespace OvrTemplateApp
{

class OvrApp : public OVR::VrAppInterface
{
public:
						OvrApp();
	virtual				~OvrApp();

	virtual void 		Configure( OVR::ovrSettings & settings );
	virtual void		OneTimeInit( const char * fromPackage, const char * launchIntentJSON, const char * launchIntentURI );
	virtual void		OneTimeShutdown();
	virtual bool 		OnKeyEvent( const int keyCode, const int repeatCount, const OVR::KeyEventType eventType );
	virtual OVR::Matrix4f Frame( const OVR::VrFrame & vrFrame );
	virtual OVR::Matrix4f DrawEyeView( const int eye, const float fovDegreesX, const float fovDegreesY, ovrFrameParms & frameParms );

	class OVR::ovrLocale &	GetLocale() { return *Locale; }
	void PrintMessage(float time, const char * message);

	//Moved to public, just for reasons ...
	OVR::OvrSceneView		Scene;

	static float yaw, yawOffset;
	static float pitch;
	static float roll;

private:
	std::unique_ptr<OVR::ovrSoundEffectContext> SoundEffectContext;
	std::unique_ptr<OVR::OvrGuiSys::SoundEffectPlayer> SoundEffectPlayer;
	OVR::OvrGuiSys *		GuiSys;
	class OVR::ovrLocale *	Locale;


};

float OvrTemplateApp::OvrApp::yaw = 0;
float OvrTemplateApp::OvrApp::yawOffset = 0;
float OvrTemplateApp::OvrApp::pitch = 0;
float OvrTemplateApp::OvrApp::roll = 0;
} // namespace OvrTemplateApp

#endif	// OVRAPP_H
