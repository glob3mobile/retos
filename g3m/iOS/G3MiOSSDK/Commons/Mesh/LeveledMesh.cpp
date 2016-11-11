//
//  LeveledMesh.cpp
//  G3MiOSSDK
//
//  Created by Jose Miguel SN on 16/04/13.
//
//

#include "LeveledMesh.hpp"

#include "Vector3D.hpp"

const Vector3D LeveledMesh::getVertex(size_t i) const {
  return _mesh->getVertex(i);
}
