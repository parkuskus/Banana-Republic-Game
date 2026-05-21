package banana.republic.player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class Action {

    private final ActionType actionType;
    private final Map<String, Object> parameters;

    public Action(ActionType actionType) {
        if (actionType == null) {
            throw new IllegalArgumentException("ActionType cannot be null");
        }
        this.actionType = actionType;
        this.parameters = new HashMap<>();
    }

    public ActionType getActionType() {
        return actionType;
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public void setParameter(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Parameter key cannot be null");
        }
        parameters.put(key, value);
    }

    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public String toString() {
        return String.format("Action[type=%s, params=%s]", actionType, parameters.keySet());
    }
}
