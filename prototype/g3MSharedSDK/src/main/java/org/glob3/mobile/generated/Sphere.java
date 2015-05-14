package org.glob3.mobile.generated; 
//
//  Sphere.cpp
//  G3MiOSSDK
//
//  Created by Jose Miguel SN on 04/06/13.
//
//

//
//  Sphere.h
//  G3MiOSSDK
//
//  Created by Jose Miguel SN on 04/06/13.
//
//




public class Sphere extends BoundingVolume
{

  private Mesh _mesh;

  //Vector2I Sphere::projectedExtent(const G3MRenderContext* rc) const {
  //  int TODO_remove_this; // Agustin: no implementes este método que va a desaparecer
  //  return Vector2I::zero();
  //}
  
  private Mesh createWireframeMesh(Color color, short resolution)
  {
    final IMathUtils mu = IMathUtils.instance();
    final double delta = DefineConstants.PI / (resolution-1);
  
    // create vertices
    FloatBufferBuilderFromCartesian3D vertices = FloatBufferBuilderFromCartesian3D.builderWithFirstVertexAsCenter();
    for (int i = 0; i<2 *resolution-2; i++)
    {
      final double longitude = -DefineConstants.PI + i *delta;
      for (int j = 0; j<resolution; j++)
      {
        final double latitude = -DefineConstants.PI/2 + j *delta;
        final double h = mu.cos(latitude);
        final double x = h * mu.cos(longitude);
        final double y = h * mu.sin(longitude);
        final double z = mu.sin(latitude);
        vertices.add(new Vector3D(x,y,z).times(_radius).add(_center));
      }
    }
  
    // create border indices for vertical lines
    ShortBufferBuilder indices = new ShortBufferBuilder();
    for (short i = 0; i<2 *resolution-2; i++)
    {
      for (short j = 0; j<resolution-1; j++)
      {
        indices.add((short)(j+i *resolution));
        indices.add((short)(j+1+i *resolution));
      }
    }
  
    // create border indices for horizontal lines
    for (short j = 1; j<resolution-1; j++)
    {
      for (short i = 0; i<2 *resolution-3; i++)
      {
        indices.add((short)(j+i *resolution));
        indices.add((short)(j+(i+1)*resolution));
      }
    }
    for (short j = 1; j<resolution-1; j++)
    {
      final short i = (short)(2 *resolution-3);
      indices.add((short)(j+i *resolution));
      indices.add((short)(j));
    }
  
    Mesh mesh = new IndexedMesh(GLPrimitive.lines(), true, vertices.getCenter(), vertices.create(), indices.create(), 1, 1, new Color(color));
  
    if (vertices != null)
       vertices.dispose();
  
    return mesh;
  }


  public final Vector3D _center ;
  public final double _radius;
  public final double _radiusSquared;


  public Sphere(Vector3D center, double radius)
  {
     _center = new Vector3D(center);
     _radius = radius;
     _radiusSquared = radius * radius;
     _mesh = null;
  }

  public Sphere(Sphere that)
  {
     _center = new Vector3D(that._center);
     _radius = that._radius;
     _radiusSquared = that._radiusSquared;
     _mesh = null;
  }

  public final Vector3D getCenter()
  {
    return _center;
  }

  public final double getRadius()
  {
    return _radius;
  }

  public final double getRadiusSquared()
  {
    return _radiusSquared;
  }

  public final double projectedArea(G3MRenderContext rc)
  {
    return rc.getCurrentCamera().getProjectedSphereArea(this);
  }
//  Vector2I projectedExtent(const G3MRenderContext* rc) const;

  public final void render(G3MRenderContext rc, GLState parentState, Color color)
  {
    if (_mesh == null)
    {
      _mesh = createWireframeMesh(color, (short) 16);
    }
    _mesh.render(rc, parentState);
  }

  public final boolean touches(BoundingVolume that)
  {
    if (that == null)
    {
      return false;
    }
    return that.touchesSphere(this);
  }

  public final boolean touchesBox(Box that)
  {
    final Vector3D p = that.closestPoint(_center);
    final Vector3D v = p.sub(_center);
    return v.dot(v) <= (_radius * _radius);
  }
  public final boolean touchesFrustum(Frustum frustum)
  {
    // this implementation is not right exact, but it's faster.
    if (frustum.getNearPlane().signedDistance(_center) > _radius)
       return false;
    if (frustum.getFarPlane().signedDistance(_center) > _radius)
       return false;
    if (frustum.getLeftPlane().signedDistance(_center) > _radius)
       return false;
    if (frustum.getRightPlane().signedDistance(_center) > _radius)
       return false;
    if (frustum.getTopPlane().signedDistance(_center) > _radius)
       return false;
    if (frustum.getBottomPlane().signedDistance(_center) > _radius)
       return false;
    return true;
  }
  public final boolean touchesSphere(Sphere that)
  {
    final Vector3D d = _center.sub(that._center);
    final double squaredDist = d.dot(d);
    final double radiusSum = _radius + that._radius;
    return squaredDist <= (radiusSum * radiusSum);
  }

  public final BoundingVolume mergedWith(BoundingVolume that)
  {
    if (that == null)
    {
      return null;
    }
    return that.mergedWithSphere(this);
  }

  public final BoundingVolume mergedWithBox(Box that)
  {
    if (that.fullContainedInSphere(this))
    {
      return new Sphere(this);
    }
  
    final Vector3D upper = that.getUpper();
    final Vector3D lower = that.getLower();
  
    double minX = _center._x - _radius;
    if (lower._x < minX)
    {
       minX = lower._x;
    }
  
    double maxX = _center._x + _radius;
    if (upper._x > maxX)
    {
       maxX = upper._x;
    }
  
    double minY = _center._y - _radius;
    if (lower._y < minY)
    {
       minY = lower._y;
    }
  
    double maxY = _center._y + _radius;
    if (upper._y > maxY)
    {
       maxY = upper._y;
    }
  
    double minZ = _center._z - _radius;
    if (lower._z < minZ)
    {
       minZ = lower._z;
    }
  
    double maxZ = _center._z + _radius;
    if (upper._z > maxZ)
    {
       maxZ = upper._z;
    }
  
    return new Box(new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ));
  
    /* Diego: creo que este test ya no hace falta, porque el coste del método
     fullContainedInBox es casi tanto es casi similar a todo lo anterior
     if (fullContainedInBox(that)) {
     return new Box(*that);
     }
     if (that->fullContainedInSphere(this)) {
     return new Sphere(*this);
     }*/
  
  }
  public final BoundingVolume mergedWithSphere(Sphere that)
  {
    final double d = _center.distanceTo(that._center);
  
    if (d + that._radius <= _radius)
    {
      return new Sphere(this);
    }
    if (d + _radius <= that._radius)
    {
      return new Sphere(that);
    }
  
    final double radius = (d + _radius + that._radius) / 2.0;
    final Vector3D u = _center.sub(that._center).normalized();
    final Vector3D center = _center.add(u.times(radius - _radius));
  
    return new Sphere(center, radius);
  }

  public final boolean contains(Vector3D point)
  {
    return _center.squaredDistanceTo(point) <= _radiusSquared;
  }

  public final boolean fullContains(BoundingVolume that)
  {
    return that.fullContainedInSphere(this);
  }

  public final boolean fullContainedInBox(Box that)
  {
    final Vector3D upper = that.getUpper();
    final Vector3D lower = that.getLower();
    if (_center._x + _radius > upper._x)
       return false;
    if (_center._x - _radius < lower._x)
       return false;
    if (_center._y + _radius > upper._y)
       return false;
    if (_center._y - _radius < lower._y)
       return false;
    if (_center._z + _radius > upper._z)
       return false;
    if (_center._z - _radius < lower._z)
       return false;
    return true;
  }
  public final boolean fullContainedInSphere(Sphere that)
  {
    final double d = _center.distanceTo(that._center);
    return (d + _radius <= that._radius);
  }

  public final Sphere createSphere()
  {
    return new Sphere(this);
  }

}