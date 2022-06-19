package antifraud;

public enum UserRole {
    ADMINISTRATOR("ROLE_ADMINISTRATOR", false),
    MERCHANT("ROLE_MERCHANT", true),
    SUPPORT("ROLE_SUPPORT", true);

    private final String name;
    private final boolean changeable;

    UserRole(String name, boolean changeable) {
        this.name = name;
        this.changeable = changeable;
    }

    public String getName() {
        return name;
    }

    public boolean isChangeable() {
        return changeable;
    }
}

