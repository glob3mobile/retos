//
//  CameraDoubleDragHandler.hpp
//  G3MiOSSDK
//
//  Created by Agustin Trujillo Pino on 28/07/12.
//

#ifndef G3MiOSSDK_CameraDoubleDragHandler
#define G3MiOSSDK_CameraDoubleDragHandler


#include "CameraEventHandler.hpp"
#include "Camera.hpp"


class CameraDoubleDragHandler: public CameraEventHandler {
    
public:
  CameraDoubleDragHandler()
  {
  }
  
  ~CameraDoubleDragHandler() {
#ifdef JAVA_CODE
  super.dispose();
#endif

  }
  
  
  bool onTouchEvent(const G3MEventContext *eventContext,
                    const TouchEvent* touchEvent,
                    CameraContext *cameraContext);
  
  void render(const G3MRenderContext* rc,
              CameraContext *cameraContext);
  
  void onDown(const G3MEventContext *eventContext,
              const TouchEvent& touchEvent,
              CameraContext *cameraContext);
  void onMove(const G3MEventContext *eventContext,
              const TouchEvent& touchEvent,
              CameraContext *cameraContext);
  void onUp(const G3MEventContext *eventContext,
            const TouchEvent& touchEvent,
            CameraContext *cameraContext);
  
  MutableVector3D _cameraPosition;
  MutableVector3D _cameraCenter;
  MutableVector3D _cameraUp;
  MutableVector2I _cameraViewPort;
  MutableMatrix44D _cameraModelViewMatrix;
  
};

#endif
