package banana.republic.board;

import banana.republic.resource.ResourceType;

public enum TerrainType {
    FOREST(ResourceType.WOOD),
    HILL(ResourceType.BRICK),
    FIELD(ResourceType.WHEAT),
    MOUNTAIN(ResourceType.ORE),
    BANANA_PLANTATION(ResourceType.BANANA),
    DESERT(null);

    private final ResourceType resourceType;

    TerrainType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public boolean producesResource() {
        return resourceType != null;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }
}
