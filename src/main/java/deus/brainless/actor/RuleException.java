package deus.brainless.actor;

public class RuleException extends RuntimeException {
    public RuleException(String actionName, String name) {
        super("In Action " + actionName + "the Rule with name" + name + "failed");
    }

    public RuleException(String actionName, int index) {
        super("In Action " + actionName + "the Rule at index" + index + "failed");
    }
}
