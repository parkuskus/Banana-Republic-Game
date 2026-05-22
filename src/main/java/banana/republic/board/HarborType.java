package banana.republic.board;

import banana.republic.resource.ResourceType;

public enum HarborType {
    GENERIC_3TO1(3, null),
    BANANA_2TO1(2, ResourceType.BANANA),
    WOOD_2TO1(2, ResourceType.WOOD),
    BRICK_2TO1(2, ResourceType.BRICK),
    WHEAT_2TO1(2, ResourceType.WHEAT),
    ORE_2TO1(2, ResourceType.ORE);

    private final int defaultRatio;
    private final ResourceType specificResource;

    HarborType(int defaultRatio, ResourceType specificResource) {
        this.defaultRatio = defaultRatio;
        this.specificResource = specificResource;
    }

    public int getDefaultRatio() {
        return defaultRatio;
    }

    public ResourceType getSpecificResource() {
        return specificResource;
    }
}
