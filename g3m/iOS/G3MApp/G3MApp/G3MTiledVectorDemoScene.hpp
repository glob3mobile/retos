//
//  G3MTiledVectorDemoScene.hpp
//  G3MApp
//
//  Created by Diego Gomez Deck on 5/17/14.
//

#ifndef __G3MApp__G3MTiledVectorDemoScene__
#define __G3MApp__G3MTiledVectorDemoScene__

#include "G3MDemoScene.hpp"
class TiledVectorLayer;

class G3MTiledVectorDemoScene : public G3MDemoScene {
private:
  TiledVectorLayer* _tiledVectorLayer;

protected:
  void rawActivate(const G3MContext* context);

  void rawSelectOption(const std::string& option,
                       int optionIndex);


public:
  G3MTiledVectorDemoScene(G3MDemoModel* model) :
  G3MDemoScene(model, "Vector Tiles", "", 0),
  _tiledVectorLayer(NULL)
  {
    _options.push_back("Pinkish");
    _options.push_back("Greenish");
    _options.push_back("Rainbow");
  }

  void deactivate(const G3MContext* context);

};

#endif
