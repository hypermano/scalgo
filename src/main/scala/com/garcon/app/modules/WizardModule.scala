package com.garcon.app.modules

import scaldi.Module
import com.garcon.app.services.WizardService
import com.garcon.app.services.WizardService
import com.garcon.app.services.PersistentWizardService
import com.garcon.app.services.MockWizardService
import scaldi.Condition
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

class WizardModule extends Module {
  val conf = ConfigFactory.load()
  
  val isTest = Condition(conf.getBoolean("env.isTest"))
  
  bind [WizardService] to new PersistentWizardService
  bind [WizardService] to new MockWizardService when isTest
}