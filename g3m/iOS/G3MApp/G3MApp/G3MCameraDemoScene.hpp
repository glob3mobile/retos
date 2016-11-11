//
//  G3MCameraDemoScene.hpp
//  G3MApp
//
//  Created by Diego Gomez Deck on 11/18/13.
//

#ifndef __G3MApp__G3MCameraDemoScene__
#define __G3MApp__G3MCameraDemoScene__

#include "G3MDemoScene.hpp"

class SGShape;

class G3MCameraDemoScene : public G3MDemoScene {
private:
  SGShape* _theSphynxShape;
  SGShape* _theEiffelTowerShape;
  SGShape* _arcDeTriompheShape;

protected:
  void rawActivate(const G3MContext* context);

  void rawSelectOption(const std::string& option,
                       int optionIndex);

public:
  G3MCameraDemoScene(G3MDemoModel* model) :
  G3MDemoScene(model, "Camera", "<select model>", -1),
  _theSphynxShape(NULL),
  _theEiffelTowerShape(NULL),
  _arcDeTriompheShape(NULL)
  {
    _options.push_back("The Sphynx");
    _options.push_back("The Eiffel Tower");
    _options.push_back("Arc de Triomphe");
  }

  void setTheSphynxShape(SGShape* shape) {
    _theSphynxShape = shape;
  }

  void setTheEiffelTowerShape(SGShape* shape) {
    _theEiffelTowerShape = shape;
  }

  void setArcDeTriompheShape(SGShape* shape) {
    _arcDeTriompheShape = shape;
  }

};

#endif
