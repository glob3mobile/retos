//
//  G3MVectorialDemoScene.h
//  G3MApp
//
//  Created by Diego Gomez Deck on 11/16/13.
//

#ifndef __G3MApp__G3MVectorialDemoScene__
#define __G3MApp__G3MVectorialDemoScene__

#include "G3MDemoScene.hpp"

class G3MVectorialDemoScene : public G3MDemoScene {
protected:
  void rawActivate(const G3MContext* context);

  void rawSelectOption(const std::string& option,
                       int optionIndex) {
    // do nothing, no options
  }

public:
  G3MVectorialDemoScene(G3MDemoModel* model) :
  G3MDemoScene(model, "Vectorial", "", -1)
  {
  }

};

#endif
