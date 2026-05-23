package banana.republic.resource;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;


public class BankImpl implements Bank {

    private static final int MAX_PER_TYPE = 19;
    private final Map<ResourceType, Integer> resources;

    public BankImpl() {
        EnumMap<ResourceType, Integer> map = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            map.put(type, MAX_PER_TYPE);
        }
        this.resources = Collections.synchronizedMap(map);
    }

    @Override
    public boolean hasResource(ResourceType type, int amount) {
        if (amount < 0) {
            return false;
        }
        return resources.getOrDefault(type, 0) >= amount;
    }

    @Override
    public void takeResource(ResourceType type, int amount) {
        validateAmount(amount);
        synchronized (resources) {
            int current = resources.getOrDefault(type, 0);
            if (current < amount) {
                throw new IllegalArgumentException(String.format("Insufficient %s in bank. Requested: %d, Available: %d",type, amount, current));
            }
            int newAmount = current - amount;
            resources.put(type, newAmount);
            assert newAmount >= 0 : "Bank resource count should never go below 0 for " + type;
        }
    }

    @Override
    public void returnResource(ResourceType type, int amount) {
        validateAmount(amount);
        synchronized (resources) {
            int current = resources.getOrDefault(type, 0);
            if (current + amount > MAX_PER_TYPE) {
                throw new IllegalArgumentException(String.format("Cannot return %d %s. Would exceed max capacity of %d (current: %d)",amount, type, MAX_PER_TYPE, current));
            }
            int newAmount = current + amount;
            resources.put(type, newAmount);
            assert newAmount <= MAX_PER_TYPE : "Bank resource count should never exceed max for " + type;
        }
    }

    @Override
    public int getCount(ResourceType type) {
        return resources.getOrDefault(type, 0);
    }

    @Override
    public boolean canFulfillAll(Map<ResourceType, Integer> requests) {
        if (requests == null || requests.isEmpty()) {
            return true;
        }
        synchronized (resources) {
            for (Map.Entry<ResourceType, Integer> entry : requests.entrySet()) {
                ResourceType type = entry.getKey();
                Integer amount = entry.getValue();
                if (amount == null || amount < 0) {
                    return false;
                }
                if (resources.getOrDefault(type, 0) < amount) {
                    return false;
                }
            }
            return true;
        }
    }

    private void validateAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amount);
        }
    }
}
