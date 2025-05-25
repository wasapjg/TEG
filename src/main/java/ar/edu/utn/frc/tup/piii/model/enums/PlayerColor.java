package ar.edu.utn.frc.tup.piii.model.enums;

public enum PlayerColor {
    RED("#FF0000"),
    BLUE("#0000FF"),
    GREEN("#008000"),
    YELLOW("#FFFF00"),
    BLACK("#000000"),
    PURPLE("#800080");

    private final String hexColor;

    PlayerColor(String hexColor) {
        this.hexColor = hexColor;
    }

    public String getHexColor() {
        return hexColor;
    }
}
