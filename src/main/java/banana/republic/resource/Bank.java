package banana.republic.resource;

import java.util.Map;


public interface Bank {

  
    boolean hasResource(ResourceType type, int amount);

    void takeResource(ResourceType type, int amount);


    void returnResource(ResourceType type, int amount);


    int getCount(ResourceType type);

    boolean canFulfillAll(Map<ResourceType, Integer> requests);
}
