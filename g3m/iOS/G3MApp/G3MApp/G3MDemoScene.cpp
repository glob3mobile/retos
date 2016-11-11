//
//  G3MDemoScene.cpp
//  G3MApp
//
//  Created by Diego Gomez Deck on 11/16/13.
//

#include "G3MDemoScene.hpp"

#include "G3MDemoModel.hpp"

void G3MDemoScene::deactivate(const G3MContext* context) {
  _model->reset();

  _selectedOptionIndex = -1;
}

void G3MDemoScene::activate(const G3MContext* context) {
  rawActivate(context);
}

void G3MDemoScene::activateOptions(const G3MContext* context) {
  if (_autoselectOptionIndex >= 0) {
    if (_options.size() > _autoselectOptionIndex) {
      selectOption(_options[_autoselectOptionIndex]);
    }
  }
}


int G3MDemoScene::getOptionIndex(const std::string& option) const {
  const int optionsSize = _options.size();
  for (int i = 0; i < optionsSize; i++) {
    if (_options[i] == option) {
      return i;
    }
  }

  return -1;
}

void G3MDemoScene::selectOption(const std::string& option) {
  const int optionIndex = getOptionIndex(option);
  if (optionIndex != _selectedOptionIndex) {
    if (optionIndex >= 0) {
      _selectedOptionIndex = optionIndex;

      rawSelectOption(option, optionIndex);

      _model->onChangeSceneOption(this, option, optionIndex);
    }
  }
}
