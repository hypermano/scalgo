package com.garcon.app.data

import scala.util.Random

/**
 *
 */
abstract class Construct {
  def attack(other: Construct) = {
    val availableAttacks = attackingParts.filter(!_.isDestroyed)
    val targetParts = List.fill(availableAttacks.size)(other.randomPart)
    val attackPairs = availableAttacks.zip(targetParts)    
    
    for ((attacker, Some(attackee)) <- attackPairs) {
      attacker.attack(attackee)
    }
  }
  
  def isDestroyed: Boolean
  
  protected def availableParts: List[ConstructPart]
  protected val attackingParts: List[AttackPart]
  
  protected[data] def randomPart: Option[ConstructPart] = {
    if (availableParts.isEmpty) 
      None 
    else 
      Some(availableParts(Random.nextInt(availableParts.size)))
  }
}

/**
 *
 */
trait ConstructPart {
  def name: String
  def isDestroyed: Boolean
  def status: Double
  def damage: DamageReport
  def mitigation: MitigationReport
  def retaliation: DamageReport
  def absorb(report: DamageReport, from: AttackPart)
}

trait DefensePart extends ConstructPart
trait AttackPart extends ConstructPart {
  def attack(other: ConstructPart) = {
    if (isDestroyed) {
      throw new IllegalArgumentException("Attack part is destroyed")
    }
    
    other.absorb(damage, this)
    wear()
    
    absorb(other.retaliation, this)
  }
  
  def wear() = {}
}

/**
 *
 */
abstract class AbstractConstructPart(
    protected[data] var integrity: Double,
    _mitigation: Double,
    _damage: Double) extends ConstructPart {
  
  def isDestroyed: Boolean = integrity <= 0
  def status: Double = integrity
  
  def damage: DamageReport = DamageReport(_damage)
  def mitigation: MitigationReport = MitigationReport(0)
  def retaliation: DamageReport = DamageReport(0)
  def absorb(report: DamageReport, from: AttackPart) = {
    val damage = (report.factor(mitigation)).physical max 0
    println(s"${from.name} strikes ${this.name} for $damage")
    integrity -= damage
  }
}

/**
 *
 */
abstract class AbstractAttackPart(
    integrity: Double = 0.0,
    mitigation: Double = 0.0,
    _damage: Double = 0.0) 
    extends AbstractConstructPart(integrity, mitigation, _damage) with AttackPart {
}

/**
 *
 */
abstract class AbstractDefensePart(
    integrity: Double = 0.0,
    mitigation: Double = 0.0) 
    extends AbstractConstructPart(integrity, mitigation, 0.0) with DefensePart {
  
}

/**
 *
 */
sealed trait CombatReport

/**
 *
 */
case class MitigationReport(physhical: Double = 0) extends CombatReport {
  def factor(other: CombatReport): MitigationReport = {
    other match {
      case MitigationReport(x) => MitigationReport(this.physhical + x)
      case _ => this
    }
  }
}

/**
 *
 */
case class DamageReport(physical: Double) extends CombatReport {
  def factor(other: CombatReport): DamageReport = {
    other match {
      case MitigationReport(x) => DamageReport((this.physical) - x max 0)
      case DamageReport(x) => DamageReport(this.physical + x)
      case _ => this
    }
  }
  
  def +(other: DamageReport) = DamageReport(physical + other.physical)
  def -(other: DamageReport) = DamageReport((physical - other.physical) max 0)
}

/**
 *
 */
case class HumanoidConstruct(left: AttackPart, right: AttackPart, body: DefensePart)  
  extends Construct {
  
  def isDestroyed: Boolean = body.isDestroyed
  
  protected def availableParts = List(left, right, body).filter(!_.isDestroyed)
  
  protected val attackingParts = List(left, right)
  
  def status: (Double, Double, Double) = (left.status, right.status, body.status)
  
}

/**
 *
 */
class StandardHull extends AbstractDefensePart(10, 0) {
  def name: String = "Hull" 
}

/**
 *
 */
class StandardFist extends AbstractAttackPart(5, 0, 3) {
  def name: String = "Fist"
}

/**
 *
 */
class PartDecorator(protected val decorated: ConstructPart) extends ConstructPart {
  def isDestroyed: Boolean = decorated.isDestroyed
  def status: Double = decorated.status
  def name: String = decorated.name
  def damage: DamageReport = decorated.damage
  def mitigation: MitigationReport = decorated.mitigation
  def retaliation: DamageReport = decorated.retaliation
  def absorb(report: DamageReport, from: AttackPart) = decorated.absorb(report, from)
}

/**
 *
 */
object RockForged {
  def apply(decorated: ConstructPart) = new RockForged(decorated)
}

/**
 *
 */
class RockForged(decorated: ConstructPart) extends PartDecorator(decorated) with AttackPart {
  override def name: String = s"Rockforged ${decorated.name}"
  override def damage: DamageReport = decorated.damage factor DamageReport(1)
}

/**
 *
 */
object GlassGlazed {
  def apply(decorated: AbstractConstructPart) = new GlassGlazed(decorated)
}

/**
 *
 */
class GlassGlazed(decorated: AbstractConstructPart) extends PartDecorator(decorated) with DefensePart {
  override def name: String = s"Glass-glazed ${decorated.name}"
  
  override def absorb(report: DamageReport, from: AttackPart) = {
    decorated.absorb(DamageReport(decorated.integrity), from)
  } 
}

/**
 *
 */
object IronHardened {
  def apply(decorated: ConstructPart) = new IronHardened(decorated)
}

/**
 *
 */
class IronHardened(decorated: ConstructPart) extends PartDecorator(decorated) with DefensePart {
  override def name: String = s"Iron-hardened ${decorated.name}"
  override def mitigation: MitigationReport = MitigationReport(2.5)
}

/**
 *
 */
object Spiked {
  def apply(decorated: ConstructPart) = new Spiked(decorated)
}

/**
 *
 */
class Spiked(decorated: ConstructPart) extends PartDecorator(decorated) with DefensePart with AttackPart {
  override def name: String = s"Spiky ${decorated.name}"
  override def retaliation: DamageReport = DamageReport(.5)
  override def damage: DamageReport = decorated.damage factor DamageReport(.25)
}

/**
 *
 */
object ConstructTest {
  def test() = {
    val payloads: List[(AttackPart, AttackPart, DefensePart)] = 
      List(
          (new StandardFist, new StandardFist(), new StandardHull()),
          (RockForged(new StandardFist), new StandardFist(), new StandardHull()),
          (new StandardFist(), new StandardFist(), GlassGlazed(new StandardHull())),
          (new StandardFist, new StandardFist(), IronHardened(new StandardHull())),
          (new StandardFist, new StandardFist(), Spiked(new StandardHull())),
          (new StandardFist, Spiked(RockForged(new StandardFist())), new StandardHull())
      )

    for ((left, right, body) <- payloads) {
      val humanoid = HumanoidConstruct(left, right, body)
      println(s"Before combat: ${humanoid.status}")
      humanoid.attack(humanoid)
      println(s"After attack:  ${humanoid.status}")
      println("\n")
    }
  }
}

trait ReportValue {
  def value: Double
}
case class FixedValue(v: Double) extends ReportValue {
  def value: Double = v
}
//case class DiceValue(x: Int, dice: Int, modifier: Double = 0) extends ReportValue
//case class RelativeValue(percentage: Double) extends ReportValue
trait AbilityType
case class Physical(damage: ReportValue) extends AbilityType
case class Ability(types: AbilityType*) {
  def activate: DamageReport = {
    var ret = DamageReport(0)
    for (t <- types) {
      t match {
        case Physical(x) => ret = ret factor DamageReport(x.value)
      }
    }
    ret
  }
}

case class ConstructState(hp: Int) {
  def apply(damage: DamageReport) = {}
}

trait AdvConstructPart {
  def state: ConstructState
  def attack(): DamageReport = DamageReport(0)
  def mitigation: DamageReport = DamageReport(0)
  def retaliation: DamageReport = DamageReport(0)
  def brace(damage: DamageReport, attacker: AdvConstructPart): DamageReport = {
    damage.factor(mitigation)
  }
  def impact(damage: DamageReport) = {
    state.apply(damage)
  } 
}