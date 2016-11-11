//
//  G3M3DSymbologyDemoScene.hpp
//  G3MApp
//
//  Created by Diego Gomez Deck on 11/18/13.
//

#ifndef __G3MApp__G3M3DSymbologyDemoScene__
#define __G3MApp__G3M3DSymbologyDemoScene__

#include "G3MDemoScene.hpp"


class G3M3DSymbologyDemoScene : public G3MDemoScene {
private:
protected:
  void rawActivate(const G3MContext* context);

  void rawSelectOption(const std::string& option,
                       int optionIndex);

public:
  G3M3DSymbologyDemoScene(G3MDemoModel* model) :
  G3MDemoScene(model, "3D Symbology", "", -1)
  {
  }

};

#endif
