//
//  FloatBufferBuilderFromCartesian2D.hpp
//  G3MiOSSDK
//
//  Created by José Miguel S N on 06/09/12.
//

#ifndef G3MiOSSDK_FloatBufferBuilderFromCartesian2D
#define G3MiOSSDK_FloatBufferBuilderFromCartesian2D

#include "Vector2D.hpp"
#include "Vector2F.hpp"
#include "FloatBufferBuilder.hpp"

class FloatBufferBuilderFromCartesian2D: public FloatBufferBuilder {
public:
  void add(const Vector2D& vector) {
    _values.push_back( (float) vector._x );
    _values.push_back( (float) vector._y );
  }
  
  void add(const Vector2F& vector) {
    _values.push_back( vector._x );
    _values.push_back( vector._y );
  }
  
  void add(float x, float y) {
    _values.push_back(x);
    _values.push_back(y);
  }

};

#endif
