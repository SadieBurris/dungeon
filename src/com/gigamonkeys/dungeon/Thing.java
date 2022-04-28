package com.gigamonkeys.dungeon;

import static com.gigamonkeys.dungeon.Text.*;
import static com.gigamonkeys.dungeon.Location.PlacedThing;

import java.util.*;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The interface for all the things in the Dungeon other than the
 * Player, Rooms, and Doors. We don't distinguish between different
 * kinds of things by class because ultimately it seems better to
 * actually have to deal with the player applying any verb to any
 * thing.
 */
public class Thing implements Items.ActualLocation {

  static class Monster extends Thing {

    private final String deadDescription;

    Monster(String name, String liveDescription, String deadDescription, int hitPoints, boolean isPortable) {
      super(name, liveDescription, isPortable, true, hitPoints);
      this.deadDescription = deadDescription;
    }

    public String description() {
      return alive() ? super.description() : deadDescription;
    }
  }

  static class Furniture extends Thing {

    Furniture(String name, String description) {
      super(name, description, false, false, 0);
    }
  }

  static class Weapon extends Thing {

    private final Attack attack;

    Weapon(String name, String description, Attack attack) {
      super(name, description, true, false, 0);
      this.attack = attack;
    }

    public Attack attack() {
      return attack;
    }
  }

  static class Food extends Thing {

    private final String eat;

    Food(String name, String description, String eat) {
      super(name, description, true, false, 0);
      this.eat = eat;
    }

    public String eat() {
      return destroy(eat);
    }
  }

  private final Items things = new Items(this);

  private final String name;
  private final String description;
  private final boolean isPortable;
  private final boolean isMonster;

  private int hitPoints;
  private Optional<Location> location = Optional.empty();

  Thing(String name, String description, boolean isPortable, boolean isMonster, int hitPoints) {
    this.name = name.toUpperCase();
    this.description = description;
    this.hitPoints = hitPoints;
    this.isPortable = isPortable;
    this.isMonster = isMonster;
  }

  public String name() {
    return name;
  }

  public String description() {
    return description;
  }

  public String describeThings() {
    var desc = new ArrayList<String>();
    things()
      .stream()
      .map(pt -> pt.where() + " the " + name() + " is " + a(pt.thing().description()) + ".")
      .forEach(desc::add);

    things().stream().map(PlacedThing::thing).map(Thing::describeThings).forEach(desc::add);

    return String.join(" ", desc);
  }

  public String eat() {
    return "Yuck. You can't eat " + a(description()) + ".";
  }

  public boolean isPortable() {
    return isPortable;
  }

  public boolean isMonster() {
    return isMonster;
  }

  public int hitPoints() {
    return hitPoints;
  }

  public void takeDamage(int damage) {
    hitPoints -= damage;
  }

  public Attack attack() {
    return new Attack.Useless(a(description()) + " is not an effective weapon.");
  }

  /**
   * Is the thing alive?
   */
  public boolean alive() {
    return hitPoints() > 0;
  }

  /**
   * Destroy the thing by unlinking it from any location and return a
   * description of the destruction.
   */
  public String destroy(String s) {
    location().ifPresent(l -> l.removeThing(this));
    return s;
  }

  /**
   * Apply an attack to this thing as a target.
   */
  public String applyAttack(Attack attack) {
    if (isMonster()) {
      takeDamage(attack.damage());
      return (
        "After " +
        attack.damage() +
        " points of damage, the " +
        name() +
        " is " +
        (alive() ? "wounded but still alive. And now it's mad." : "dead. Good job, murderer.")
      );
    } else {
      return "I don't know why you're attacking an innocent " + name() + ".";
    }
  }

  /**
   * Can this thing be taken from it's current location. It needs to be both
   * inherently portable and the location needs to allow it to be taken.
   */
  public boolean canBeTaken() {
    return isPortable() && location().map(l -> l.canTake(this)).orElse(false);
  }

  ////////////////////////////////////////////////////////////////////////////
  // Movement

  public final Optional<Location> location() {
    return location;
  }

  public final void moveTo(Location location) {
    this.location = Optional.of(location);
  }

  public final void clearLocation() {
    location = Optional.empty();
  }

  //////////////////////////////////////////////////////////////////////////////
  // Location implementation -- things can contain things.

  public void placeThing(Thing thing, String where) {
    things.placeThing(thing, where);
  }

  public void removeThing(Thing thing) {
    things.removeThing(thing);
  }

  public Optional<Thing> thing(String name) {
    return things.thing(name);
  }

  public Collection<PlacedThing> things() {
    return things.things();
  }

  public Stream<PlacedThing> allThings() {
    return things.allThings();
  }

  public boolean canTake(Thing thing) {
    return !alive();
  }

  //
  //////////////////////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////////////////////
  // Action events

  public Stream<Action> onTurn(Action.Turn a) {
    return Stream.empty();
  }

  public Stream<Action> onPlayerAttack(Action.PlayerAttack a) {
    return Stream.empty();
  }

  public Stream<Action> onEnter(Action.Go a) {
    return Stream.empty();
  }

  public Stream<Action> onTake(Action.Take a) {
    return Stream.empty();
  }

  public Stream<Action> onDrop(Action.Drop a) {
    return Stream.empty();
  }

  public Stream<Action> onLook(Action.Look a) {
    return Stream.empty();
  }

  public Stream<Action> onEat(Action.Eat a) {
    return Stream.empty();
  }

  public Stream<Action> onSay(Action.Say a) {
    return Stream.empty();
  }
}
