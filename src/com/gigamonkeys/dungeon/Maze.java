package com.gigamonkeys.dungeon;

import static com.gigamonkeys.dungeon.Direction.*;
import java.util.stream.Stream;

/**
 * The Maze.
 */
public class Maze {

  /**
   * Build the maze and return the first room.
   */
  public Room build() {
    // Rooms
    var entry = new Room("a dusty entryway to a castle");
    var kitchen = new Room("what appears to be a kitchen");
    var blobbyblobLair = new Room("the lair of a horrible creature");
    var dining = new Room("a grand dining room with a crystal chandelier and tapestries on the walls");
    var storeroom = new Room("a storeroom");
    var hall = new Room("a long hallway");
    var throneRoom = new Room("a massive throneroom");
    var pit = new Room("a pit of nothing");

    // Doors
    entry.connect("oaken door", kitchen, EAST);
    entry.connect("dank tunnel", blobbyblobLair, SOUTH);
    kitchen.connect("swinging door", dining, EAST);
    kitchen.connect("wooden door", storeroom, SOUTH);
    dining.connect("golden archway", hall, NORTH);

    // Furniture
    var pedestal = new Thing.Furniture("pedestal", "stone pedestal");
    var table = new Thing.Furniture("table", "wooden table");
    var tray = new Thing.Furniture("tray", "TV tray");
    
    var painting = new Thing.Furniture("painting", "painting of a famous artist in their bedroom") {
      boolean open = false;

      @Override
      public String description() {
        if(!open) {
          return super.description() + ", a shut door visable at the back of the room";
        } else {
          return super.description() + ", an open door visable at the back of the room";
        }
      }

      public String open() {
        if(!open) {
          open = true;
          hall.connect("small hole in the painting", throneRoom, NORTH);
          return "The door in the painting opens, behind which you can see a throne. It almost looks real.";
        } else {
          return "You tear the doorframe off its hinges. Good job. What have you accomplished?";
        }
      }
    };

    var gnitniap = new Thing.Furniture("painting", "painting of a living room with an open door at the back leading to what looks to be a bedroom");

    var trapdoor = new Thing.Furniture("trapdoor", "slightly moldy trapdoor") {
      boolean open = false;

      @Override
      public String description() {
        return (open ? "an open " : "a closed ") + super.description();
      }

      public String open() {
        if (!open) {
          open = true;
          return "The trapdoor creaks open, revealing a staircase down.";
        } else {
          return "The " + name() + " is already open.";
        }
      }

      public String close() {
        if (open) {
          open = false;
          return "The trapdoor closes with a heavy thud.";
        } else {
          return "The " + name() + " is already closed.";
        }
      }
    };

    var throne = new Thing.Furniture("throne", "massive throne with ornate carvings intricately drawn into its golden crest") {
      @Override
      public Stream<Action> onTalk(Action.Talk a) {
        if(a.what().toUpperCase().contains("FROBNICATE")) {
          //throneRoom.placeThing(trapdoor, "on the only clean part of the floor where the throne used to lay");
          return Stream.of(new Action.Move(this, throneRoom, "the back of"));
        }
        return Stream.empty();
        //location().ifPresent(l -> moveTo(l, "around the room"));
      }
    };

    var treasureChest = new Thing.Furniture("chest", "wooden treasure chest") {
      boolean open = false;

      @Override
      public boolean isMonster() {
        var b = super.isMonster();
        return b;
      }

      @Override
      public String description() {
        return descriptor() + " " + super.description();
      }

      private String descriptor() {
        return open ? (things().isEmpty() ? "empty" : "open") : "closed";
      }

      @Override
      public String describeThings() {
        return open ? super.describeThings() : "";
      }

      public String open() {
        if (!open) {
          open = true;
          return "The chest lid opens with a creak. " + describeThings();
        } else {
          return "The " + name() + " is already open.";
        }
      }

      public String close() {
        if (open) {
          open = false;
          return "The chest snaps shut.";
        } else {
          return "The " + name() + " is already closed.";
        }
      }
    };

    var jeweledDagger = new Thing.Weapon("dagger", "jeweled dagger", new Attack.Simple("Stabby, stab, stab.", 1));

    // Things
    var ring = new Thing.Weapon(
      "ring",
      "ring of great power",
      new Attack.Full(
        "A sphere of light emanates from the ring",
        1000,
        (t -> " blasting " + t.who() + " to smithereens.")
      )
    );

    var axe = new Thing.Weapon("axe", "heavy dwarven axe", new Attack.Simple("You swing your axe and connect!", 2)) {
      public String eat() {
        return "Axes are not good for eating. Now your teeth hurt and you are no less hungry.";
      }
    };

    var sword = new Thing.Weapon(
      "sword",
      "broadsword with a rusty iron hilt",
      new Attack.Simple("Oof, this sword is heavy but you manage to swing it.", 5)
    ) {
      public String eat() {
        return "What are you, a sword swallower?! You can't eat a sword.";
      }
    };

    var bread = new Thing.Food("bread", "loaf of bread", "Ah, delicious. Could use some mayonnaise though.");

    var sandwich = new Thing.Food(
      "sandwich",
      "ham and cheese sandwich",
      "Mmmm, tasty. But I think you got a spot of mustard on your tunic."
    );

    // Monsters
    var blobbyblob = new Thing.Monster("blobbyblob", 7) {
      @Override
      public String description() {
        return alive()
          ? name() + ", a gelatenous mass with too many eyes and an odor of jello casserole gone bad"
          : hitPoints() > -100 ? "dead " + name() + " decaying into puddle of goo" : "a spattering of blobbyblob bits";
      }

      @Override
      public String eat() {
        return alive()
          ? "Are you out of your mind?! This is a live and jiggling " + name() + "."
          : hitPoints() < -100
            ? "The " +
            name() +
            " is blasted all over the room. There is nothing to eat unless you have a squeege and a straw."
            : super.destroy(
              "Ugh. This is worse than the worst jello casserole you have ever tasted. But it does slightly sate your hunger."
            );
      }

      @Override
      public String destroy(String s) {
        location().ifPresent(l -> moveTo(l, "around the room"));
        return "";
      }

      @Override
      public Attack attack() {
        return new Attack.Simple("The blobbyblob extrudes a blobby arm and smashes at you!", 3);
      }

      @Override
      public Stream<Action> onTurn(Action.Turn a) {
        return streamIf(alive(), new Action.Attack(a.player(), this));
      }
    };

    var pirate = new Thing.Monster(
      "pirate",
      "pirate with a wooden leg and an eye patch",
      "dead pirate with his eye patch askew",
      10,
      false
    ) {
      @Override
      public Stream<Action> onEnter(Action.Go a) {
        return streamIf(alive(), new Action.Say(this, "Arr, matey!"));
      }

      @Override
      public Stream<Action> onTake(Action.Take a) {
        return streamIf(
          alive() && a.taking(thing("parrot")),
          new Action.Say(this, "Oi, ye swarthy dog! Hands off me parrot!")
        );
      }

      @Override
      public Stream<Action> onTalk(Action.Talk a) {
        return streamIf(
          alive() && a.what().toUpperCase().contains("MAGIC WORD"),
          new Action.Say(this, "Arr, the magic word be 'Frobnicate'!")
        );
      }
    };

    var parrot = new Thing.Monster("parrot", "green and blue parrot with a tiny eye patch", "dead parrot", 5, true) {
      @Override
      public Stream<Action> onDrop(Action.Drop a) {
        return a.thing().name().equals("bread") ? Stream.of(new Action.Move(this, a.thing(), "on")) : Stream.empty();
      }

      @Override
      public String moveTo(Location location, String place) {
        var s = super.moveTo(location, place);
        return location == bread ? "The " + name() + " flies down and starts eating the bread." : s;
      }
    };

    // Place things
    pedestal.placeThing(ring, "on");
    pirate.placeThing(parrot, "on the right shoulder of");
    dining.placeThing(pirate, "in the middle of the room");
    kitchen.placeThing(table, "against the wall");
    table.placeThing(bread, "on");
    blobbyblobLair.placeThing(axe, "on the floor");
    storeroom.placeThing(treasureChest, "against the wall");
    treasureChest.placeThing(jeweledDagger, "inside");
    blobbyblobLair.placeThing(blobbyblob, "across from you");
    entry.placeThing(pedestal, "in the center of the room");
    entry.placeThing(tray, "by the door");
    tray.placeThing(sandwich, "on");
    dining.placeThing(sword, "propped against a wall");
    hall.placeThing(painting, "covering the north wall");
    throneRoom.placeThing(gnitniap, "on the south wall");
    throneRoom.placeThing(throne, "in the center of the room");

    return entry;
  }

  private static <T> Stream<T> streamIf(boolean test, T v) {
    return Stream.ofNullable(test ? v : null);
  }
}
