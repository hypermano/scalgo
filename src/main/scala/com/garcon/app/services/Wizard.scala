package com.garcon.app.services

import com.garcon.app.data.Wizard
import scala.util.Try
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._
import scaldi.{Injectable, Injector}

trait WizardService {
  def getAll(): List[Wizard]
}

class PersistentWizardService extends WizardService {

  def getAll(): List[Wizard] = {
    val mongoClient = MongoClient()
    
    val wizards = Try(
        mongoClient("test")("wizards")
        .find()
        .map( dbo => Try(grater[Wizard].asObject(dbo)).getOrElse(Wizard("")) )
        .toList
       )
    
    mongoClient.close()
    
    wizards.getOrElse(List.empty);
  }
  
}

class MockWizardService extends WizardService {
  
  def getAll(): List[Wizard] = {
    List(Wizard("Mockintus", List("The Fraud")))
  }
  
}