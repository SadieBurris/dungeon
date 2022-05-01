package com.gigamonkeys.dungeon;

import static com.gigamonkeys.dungeon.CommandParser.*;
import static com.gigamonkeys.dungeon.Text.*;

import com.gigamonkeys.dungeon.CommandParser.Parse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represent the player.
 */
public class Player implements Location, Attack.Target {

  private final Map<String, PlacedThing> inventory = new HashMap<>();
  private Room room;
  private int hitPoints;

  public Player(Room start, int hitPoints) {
    this.room = start;
    this.hitPoints = hitPoints;
  }

  //////////////////////////////////////////////////////////////////////////////
  // Location implementation

  public Map<String, PlacedThing> locationMap() {
    return inventory;
  }

  //
  //////////////////////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////////////////////
  // Tracking and describing state changes.

  public static record State(int hitPoints) {}

  public State state() {
    return new State(hitPoints);
  }

  public Stream<String> stateChanges(State original) {
    int damage = original.hitPoints - hitPoints;
    return Stream.ofNullable(damage > 0 ? describeDamage(damage) : null);
  }

  //
  //////////////////////////////////////////////////////////////////////////////

  public Optional<Thing> roomThing(String name) {
    return room.thing(name);
  }

  public Optional<Thing> anyThing(String name) {
    return thing(name).or(() -> roomThing(name));
  }

  public void go(Door door) {
    room = door.from(room);
  }

  public Room room() {
    return room;
  }

  public void drop(Thing t) {
    room.drop(t);
  }

  public String inventory() {
    if (things().isEmpty()) {
      return "You've got nothing!";
    } else {
      var items = things().stream().map(t -> a(t.description())).toList();
      return new Text.Wrapped().add("You have").add(commify(items) + ".").toString();
    }
  }

  public int hitPoints() {
    return hitPoints;
  }

  public String applyAttack(Attack attack) {
    int damage = attack.damage();
    hitPoints -= damage;
    return "You take " + numberOf(damage, "hit point") + " of damage.";
  }

  public String who() {
    return "you";
  }

  public String describeDamage(int amount) {
    var status = hitPoints > 0 ? "You're down to " + hitPoints + "." : "You feel consciousness slipping away.";
    return "You take " + amount + plural(" hit point", amount) + " of damage. " + status;
  }

  public boolean alive() {
    return hitPoints > 0;
  }

  //////////////////////////////////////////////////////////////////////////////
  // Command action parsers.

  Action drop(String[] args) {
    var name = arg(args, 1).or("Drop what?");
    var thing = name.maybe(n -> thing(n)).or(n -> "No " + n + " to drop!");
    return thing.toAction(t -> Action.drop(this, t));
  }

  Action eat(String[] args) {
    var name = arg(args, 1).or("Eat what?");
    var thing = name.maybe(this::anyThing).or(n -> "No " + n + " here to eat.");
    return thing.toAction(food -> Action.eat(this, food));
  }

  Action go(String[] args) {
    var name = arg(args, 1).or("Go where?");
    var dir = name.maybe(Direction::fromString).or(n -> "Don't understand direction " + n + ".");
    var door = dir.maybe(room()::door).or(d -> "No door to the " + d + ".");
    return door.toAction(d -> Action.go(this, d));
  }

  Action look(String[] args) {
    return Action.look(this);
  }

  Action attack(String[] args) {
    var targetName = arg(args, 1).or("Attack what? And with what?");
    var with = arg(args, args.length - 2).expect("with").or("Don't understand ATTACK with no WITH.");
    var weaponName = arg(args, args.length - 1).or("Attack with what?");
    var target = targetName.maybe(this::implicitMonster).or(n -> "No " + n + " here to attack.");
    var weapon = weaponName.maybe(this::anyThing).or(n -> "No " + n + " here to attack with!");
    return with.toAction(e -> weapon.toAction(w -> target.toAction(t -> Action.attack(t, w))));
  }

  Action take(String[] args) {
    return listOfThings(args, 1).or("Take what?").toAction(ts -> Action.take(this, ts));
  }

  private Optional<Thing> implicitMonster(String name) {
    return name.equals("with") ? room().onlyMonster() : roomThing(name);
  }

  private Parse<List<Thing>, String[]> listOfThings(String[] args, int start) {
    var things = new ArrayList<Thing>();
    for (var i = start; i < args.length; i++) {
      var maybe = roomThing(args[i]);
      if (!maybe.isPresent()) {
        if (!args[i].equals("and")) {
          return new Parse<>(null, args, null);
        }
      } else {
        var thing = maybe.get();
        things.add(thing);
        thing.allThings().forEach(things::add);
      }
    }
    return new Parse<>(things, args, null);
  }
}
