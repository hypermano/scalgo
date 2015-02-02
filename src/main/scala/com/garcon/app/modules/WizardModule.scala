package com.garcon.app.modules

import scaldi.Module
import com.garcon.app.services.WizardService
import com.garcon.app.services.WizardService
import com.garcon.app.services.PersistentWizardService
import com.garcon.app.services.MockWizardService
import scaldi.Condition

class WizardModule extends Module {
  val isTest = Condition(true)
  
  bind [WizardService] to new PersistentWizardService
  bind [WizardService] to new MockWizardService when isTest
}