package interp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static interp.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords = Map.ofEntries(
            Map.entry("and", AND),
            Map.entry("class", CLASS),
            Map.entry("else", ELSE),
            Map.entry("false", FALSE),
            Map.entry("for", FOR),
            Map.entry("fun", FUN),
            Map.entry("if", IF),
            Map.entry("nil", NIL),
            Map.entry("or", OR),
            Map.entry("print", PRINT),
            Map.entry("return", RETURN),
            Map.entry("super", SUPER),
            Map.entry("this", THIS),
            Map.entry("true", TRUE),
            Map.entry("var", VAR),
            Map.entry("while", WHILE)
    );

    Scanner(String source){
        this.source = source;
    }

    List<Token> scanTokens(){
        while (!isAtEnd()){
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd(){
        return current >= source.length();
    }

    private void scanToken(){
        char c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);
            case '!' -> addToken(match('=') ? BANG_EQUALS : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    multiLineComment();
                } else {
                    addToken(SLASH);
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
        addToken(STRING, value);
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

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier(){
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
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
