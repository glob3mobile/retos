//
//  G3M3DModelDemoScene.hpp
//  G3MApp
//
//  Created by Diego Gomez Deck on 11/18/13.
//

#ifndef __G3MApp__G3M3DModelDemoScene__
#define __G3MApp__G3M3DModelDemoScene__

#include "G3MDemoScene.hpp"


class G3M3DModelDemoScene : public G3MDemoScene {
protected:
  void rawActivate(const G3MContext* context);

  void rawSelectOption(const std::string& option,
                       int optionIndex);


public:
  G3M3DModelDemoScene(G3MDemoModel* model) :
  G3MDemoScene(model, "3D Model", "", -1)
  {
  }

};

#endif
