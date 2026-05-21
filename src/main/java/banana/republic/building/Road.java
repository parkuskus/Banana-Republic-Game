package banana.republic.building;

import banana.republic.player.Player;


public class Road {

    private final Player owner;

    public Road(Player owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Road owner cannot be null");
        }
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return String.format("Road[owner=%s]", owner.getName());
    }
}
