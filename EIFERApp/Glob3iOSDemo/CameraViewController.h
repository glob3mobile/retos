//
//  CameraViewController.h
//  EIFER App
//
//  Created by Jose Miguel SN on 4/5/16.
//
//

#import <UIKit/UIKit.h>

#import <AVFoundation/AVFoundation.h>

//@import AVFoundation;
//@import UIKit;

@interface CameraViewController : UIViewController{
  AVCaptureSession *session;
  AVCaptureVideoPreviewLayer *captureVideoPreviewLayer;
  AVCaptureDevice *device;
}

-(void) enableVideo:(BOOL) enabled;

-(float) fieldOfViewInDegrees;

@end
