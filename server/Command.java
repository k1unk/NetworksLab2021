public enum Command {

    GREETING('g'),
    TEXT('t'),
    CLOSE('c'),
    FINISH_SERVER('f');

    private final char symbol;

    Command(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }
}
