package banana.republic.board;

import banana.republic.resource.ResourceType;

public class HexTile {

    private final int id;
    private final TerrainType terrainType;
    private final NumberToken numberToken;
    private boolean hasRobber;
    private final int column;
    private final int row;

    public HexTile(int id, TerrainType terrainType, NumberToken numberToken,
                   boolean hasRobber, int column, int row) {
        this.id = id;
        this.terrainType = terrainType;
        this.numberToken = numberToken;
        this.hasRobber = hasRobber;
        this.column = column;
        this.row = row;
    }

    public int getId() {
        return id;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public NumberToken getNumberToken() {
        return numberToken;
    }

    public boolean hasRobber() {
        return hasRobber;
    }

    public void setRobber(boolean present) {
        this.hasRobber = present;
    }

    public ResourceType getResourceType() {
        if (terrainType == null) {
            throw new IllegalStateException(
                "HexTile id=" + id + " tidak memiliki terrainType"
            );
        }
        return terrainType.getResourceType();
    }

    public boolean canProduce() {
        if (terrainType == null || hasRobber) {
            return false;
        }
        return terrainType.producesResource();
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }
}
