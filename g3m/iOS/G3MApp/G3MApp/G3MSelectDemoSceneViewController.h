//
//  G3MSelectDemoSceneViewController.h
//  G3MApp
//
//  Created by Diego Gomez Deck on 11/14/13.
//

#import <UIKit/UIKit.h>

class G3MDemoModel;


@interface G3MSelectDemoSceneViewController : UITableViewController {
  G3MDemoModel* _demoModel;
}

-(void)setDemoModel:(G3MDemoModel*) demoModel;

@property (weak, nonatomic) UIPopoverController* popoverController;

- (IBAction)changeDemo:(UIButton *)sender;

@end
