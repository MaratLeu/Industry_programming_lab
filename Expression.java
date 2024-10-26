import java.util.*;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Expression {
    public static ArrayList<String> evaluateLines(ArrayList<String> lines) {
        ArrayList<String> results = new ArrayList<>();
        for (String line : lines) {
            List<Expression.Lexeme> lexemes = Analyse(line);
            Expression.LexemeBuffer lexemeBuffer = new Expression.LexemeBuffer(lexemes);
            results.add(String.valueOf(expr(lexemeBuffer)));
        }
        return results;
    }

    public enum LexemeType {
        OPEN_BRACKET, CLOSE_BRACKET,
        PLUS, MINUS,
        MULTIPLY, DIVIDE,
        NUMBER, EOF
    }

    public static class Lexeme {
        Expression.LexemeType type;
        String lexeme;

        public Lexeme(Expression.LexemeType type, String lexeme) {
            this.type = type;
            this.lexeme = lexeme;
        }

        public Lexeme(Expression.LexemeType type, Character lexeme) {
            this.type = type;
            this.lexeme = lexeme.toString();
        }

        public String toString() {
            return "Lexeme{" + "type=" + type + ", lexeme='" + lexeme + '\'' + '}';
        }
    }

    public static class LexemeBuffer {
        private int pos;

        public List<Expression.Lexeme> lexemes;

        public LexemeBuffer(List<Expression.Lexeme> lexemes) {
            this.lexemes = lexemes;
        }

        public Expression.Lexeme next() {
            return lexemes.get(pos++);
        }

        public void back() {
            pos--;
        }

        public int getPos() {
            return pos;
        }
    }

    public static List<Expression.Lexeme> Analyse(String expression) {
        ArrayList<Expression.Lexeme> lexemes = new ArrayList<>();
        int pos = 0;
        while (pos != expression.length()) {
            char c = expression.charAt(pos);
            switch (c) {
                case '(':
                    lexemes.add(new Expression.Lexeme(Expression.LexemeType.OPEN_BRACKET, c));
                    pos++;
                    continue;
                case ')':
                    lexemes.add(new Expression.Lexeme(Expression.LexemeType.CLOSE_BRACKET, c));
                    pos++;
                    continue;
                case '+':
                    lexemes.add(new Expression.Lexeme(Expression.LexemeType.PLUS, c));
                    pos++;
                    continue;
                case '-':
                    lexemes.add(new Expression.Lexeme(Expression.LexemeType.MINUS, c));
                    pos++;
                    continue;
                case '*':
                    lexemes.add(new Expression.Lexeme(Expression.LexemeType.MULTIPLY, c));
                    pos++;
                    continue;
                case '/':
                    lexemes.add(new Expression.Lexeme(Expression.LexemeType.DIVIDE, c));
                    pos++;
                    continue;
                default:
                    if (c >= '0' && c <= '9') {
                        StringBuilder sb = new StringBuilder();
                        do {
                            sb.append(c);
                            pos++;
                            if (pos >= expression.length()) {
                                break;
                            }
                            c = expression.charAt(pos);
                        } while(c >= '0' && c <= '9');
                        lexemes.add(new Expression.Lexeme(Expression.LexemeType.NUMBER, sb.toString()));
                    }
                    else {
                        if (c != ' ') {
                            throw new RuntimeException("Unexpected character: '" + c + "'");
                        }
                        pos++;
                    }
            }
        }
        lexemes.add(new Expression.Lexeme(Expression.LexemeType.EOF, ""));
        return lexemes;
    }

    public static int expr(Expression.LexemeBuffer lexemes) {
        Expression.Lexeme lexeme = lexemes.next();
        if (lexeme.type == Expression.LexemeType.EOF) {
            return 0;
        }
        else {
            lexemes.back();
            return plusminus(lexemes);
        }
    }

    public static int plusminus(Expression.LexemeBuffer lexemes) {
        int value = multdiv(lexemes);
        while (true) {
            Expression.Lexeme lexeme = lexemes.next();
            switch (lexeme.type) {
                case PLUS:
                    value += multdiv(lexemes);
                    break;
                case MINUS:
                    value -= multdiv(lexemes);
                    break;
                default:
                    lexemes.back();
                    return value;
            }
        }
    }

    public static int multdiv(Expression.LexemeBuffer lexemes) {
        int value = factor(lexemes);
        while (true) {
            Expression.Lexeme lexeme = lexemes.next();
            switch (lexeme.type) {
                case MULTIPLY:
                    value *= factor(lexemes);
                    break;
                case DIVIDE:
                    value /= factor(lexemes);
                    break;
                default:
                    lexemes.back();
                    return value;
            }
        }
    }

    public static int factor(Expression.LexemeBuffer lexemes) {
        Expression.Lexeme lexeme = lexemes.next();
        switch (lexeme.type) {
            case NUMBER:
                return Integer.parseInt(lexeme.lexeme);
            case OPEN_BRACKET:
                int value = expr(lexemes);
                lexeme = lexemes.next();
                if (lexeme.type != Expression.LexemeType.CLOSE_BRACKET) {
                    throw new RuntimeException("Unexpected token: " + lexeme.lexeme + " at position " + lexemes.getPos());
                }
                return value;
            default:
                throw new RuntimeException("Unexpected token: " + lexeme.lexeme + " at position " + lexemes.getPos());
        }
    }

    // Подсчет с помощью регулярных выражений
    private static final Map<String, Integer> operatorPrecedence = new HashMap<>() {{
        put("*", 2);
        put("/", 2);
        put("+", 1);
        put("-", 1);
    }};

    private static double regex(String expression) {
        expression = expression.replaceAll("\s+", "");
        Deque<Double> operands = new ArrayDeque<>();
        Deque<String> operators = new ArrayDeque<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c)) {
                int j = i + 1;
                while (j < expression.length() && (Character.isDigit(expression.charAt(j)) || expression.charAt(j) == '.')) {
                    j++;
                }
                operands.push(Double.parseDouble(expression.substring(i, j)));
                i = j - 1;
            } else if (c == '(') {
                operators.push("(");
            } else if (c == ')') {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    processOperator(operands, operators);
                }
                operators.pop(); // Remove '('
            } else if (operatorPrecedence.containsKey(String.valueOf(c))) {
                while (!operators.isEmpty() && operatorPrecedence.get(String.valueOf(c)) <= operatorPrecedence.get(operators.peek())) {
                    processOperator(operands, operators);
                }
                operators.push(String.valueOf(c));
            }
        }

        while (!operators.isEmpty()) {
            processOperator(operands, operators);
        }

        return operands.pop();
    }

    private static void processOperator(Deque<Double> operands, Deque<String> operators) {
        String operator = operators.pop();
        double num2 = operands.pop();
        double num1 = operands.pop();
        double result = performOperation(num1, operator, num2);
        operands.push(result);
    }

    private static double performOperation(double num1, String operator, double num2) {
        return switch (operator) {
            case "+" -> num1 + num2;
            case "-" -> num1 - num2;
            case "*" -> num1 * num2;
            case "/" -> {
                if (num2 == 0) {
                    throw new IllegalArgumentException("Division by zero");
                }
                yield num1 / num2;
            }
            default -> throw new UnsupportedOperationException("Operator not supported: " + operator);
        };
    }

    public static ArrayList<String> evaluate_with_regex(ArrayList<String> expressions) {
        ArrayList<String> results = new ArrayList<>();
        for (String expression : expressions) {
            var result = regex(expression);
            results.add(String.valueOf(result));
        }
        return results;
    }

    // Подсчет с помощью библиотеки
    public static ArrayList<String> evaluate_with_library(ArrayList<String> expressions) {
        ArrayList<String> results = new ArrayList<>();
        for (String expression : expressions) {
            net.objecthunter.exp4j.Expression exp = new ExpressionBuilder(expression).build();
            var result = exp.evaluate();
            results.add(String.valueOf(result));
        }
        return results;
    }
}
