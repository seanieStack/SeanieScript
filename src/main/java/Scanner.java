import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords = Map.ofEntries(
            Map.entry("and", TokenType.AND),
            Map.entry("class", TokenType.CLASS),
            Map.entry("else", TokenType.ELSE),
            Map.entry("false", TokenType.FALSE),
            Map.entry("for", TokenType.FOR),
            Map.entry("fun", TokenType.FUN),
            Map.entry("if", TokenType.IF),
            Map.entry("nil", TokenType.NIL),
            Map.entry("or", TokenType.OR),
            Map.entry("print", TokenType.PRINT),
            Map.entry("return", TokenType.RETURN),
            Map.entry("super", TokenType.SUPER),
            Map.entry("this", TokenType.THIS),
            Map.entry("true", TokenType.TRUE),
            Map.entry("var", TokenType.VAR),
            Map.entry("while", TokenType.WHILE)
    );

    Scanner(String source){
        this.source = source;
    }

    List<Token> scanTokens(){
        while (!isAtEnd()){
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd(){
        return current >= source.length();
    }

    private void scanToken(){
        char c = advance();
        switch (c) {
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);
            case '-' -> addToken(TokenType.MINUS);
            case '+' -> addToken(TokenType.PLUS);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '*' -> addToken(TokenType.STAR);
            case '!' -> addToken(match('=') ? TokenType.BANG_EQUALS : TokenType.BANG);
            case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
            case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    multiLineComment();
                } else {
                    addToken(TokenType.SLASH);
                }
            }
            case ' ', '\r', '\t' -> {} // Ignore whitespace
            case '\n' -> line++;
            case '"' -> string();
            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    SeanieScript.error(line, "Unexpected character.");
                }
            }
        }
    }

    private char advance(){
        return source.charAt(current++);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected){
        if(isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek(){
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext(){
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private void multiLineComment(){
        while (peek() != '*' && peekNext() != '/' && !isAtEnd()){
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()){
            SeanieScript.error(line, "Unterminated comment");
        }
        advance();
        advance();
    }

    private void string(){
        while (peek() != '"' && !isAtEnd()){
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()){
            SeanieScript.error(line, "Unterminated string");
            return;
        }
        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }

    private void number(){
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())){

            do advance();
            while (isDigit(peek()));
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier(){
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private boolean isAlpha(char c){
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }
}
