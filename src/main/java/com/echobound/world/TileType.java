package com.echobound.world;

public enum TileType {
    EMPTY(' '),
    SOLID('#'),
    ONE_WAY('-'),
    HAZARD('^'),
    SPAWN('S');

    private final char symbol;

    TileType(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    public boolean isSolid() {
        return this == SOLID;
    }

    public boolean isOneWay() {
        return this == ONE_WAY;
    }

    public boolean isHazard() {
        return this == HAZARD;
    }

    public static TileType fromChar(char c) {
        for (TileType t : values()) {
            if (t.symbol == c) {
                return t;
            }
        }
        return EMPTY;
    }
}
