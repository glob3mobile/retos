//
//  G3MDemoBuilder.hpp
//  G3MApp
//
//  Created by Diego Gomez Deck on 11/14/13.
//

#ifndef __G3MApp__G3MDemoBuilder__
#define __G3MApp__G3MDemoBuilder__

class IG3MBuilder;
class LayerSet;
class G3MDemoModel;
class G3MDemoListener;


class G3MDemoBuilder {
private:
  G3MDemoListener* _listener;

  G3MDemoModel* _model;
  bool _initialized;

protected:
  G3MDemoBuilder(G3MDemoListener* listener);

  void build();

  virtual IG3MBuilder* getG3MBuilder() = 0;

public:
  virtual ~G3MDemoBuilder();

  G3MDemoModel* getModel();

};

#endif
