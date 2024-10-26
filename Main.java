import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Input arithmetic expression: ");
            String expression;
            ArrayList<String> expressions = new ArrayList<>();
            do {
                expression = scanner.nextLine();
                if (!expression.isEmpty()) {
                    expressions.add(expression);
                }
            } while (!expression.isEmpty());

            // Посчитать выражение
            ReadWrite.write_plain_text("input.txt", expressions);
            ArrayList<String> expressions_input = ReadWrite.read_plain_text("input.txt");
            ArrayList<String> results = Expression.evaluateLines(expressions_input);
            ReadWrite.write_plain_text("output.txt", results);

            // Посчитать с использованием регулярных выражений
            ArrayList<String> results_regex = Expression.evaluate_with_regex(expressions_input);
            ReadWrite.write_plain_text("output_2.txt", results_regex);

            // Посчитать с использованием библиотеки
            ArrayList<String> results_library = Expression.evaluate_with_library(expressions_input);
            ReadWrite.write_plain_text("output_3.txt", results_library);

            // Архивировать и разархивировать в zip
            Archive.zip("input.txt", "archive.zip");
            Archive.unzip("archive.zip", "zip");

            // Архивировать и разархивировать в rar
            Archive.rar("input.txt", "archive.rar");
            Archive.unrar("archive.rar", "rar");

            // Шифрование расшифрование
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey key = keyGen.generateKey();
            byte[] encrypted = Encryption.encrypt("input.txt", "encrypt.txt", key);
            Encryption.decrypt("decrypt.txt", encrypted, key);

            // Чтение и запись XML-файл, JSON-файл, YAML-файл, HTML-файл, Protobuf-файл
            ArrayList<String> expressions_list = ReadWrite.read_plain_text("input.txt");
            ArrayList<String> results_list = ReadWrite.read_plain_text("output.txt");
            ArrayList<Zapis> zapises = new ArrayList<>();
            for (int i = 0; i < expressions_list.size(); i++) {
                String expr = expressions_list.get(i);
                String result = results_list.get(i);
                zapises.add(new Zapis(expr, result));
            }

            ReadWrite.write_xml("parse.xml", zapises);
            ReadWrite.read_xml("parse.xml", "xml_parser.txt");
            byte[] xml_encrypted = Encryption.encrypt("parse.xml", "encrypt.xml", key);
            Encryption.decrypt("decrypt.xml", xml_encrypted, key);

            ReadWrite.write_json("parse.json", zapises);
            ReadWrite.read_json("parse.json", "json_parser.txt");

            ReadWrite.write_yaml("parse.yaml", zapises);
            ReadWrite.read_yaml("parse.yaml", "yaml_parser.txt");

            ReadWrite.write_html("parse.html", zapises);
            ReadWrite.read_html("parse.html", "html_parser.txt");
            ReadWrite.write_protobuf("parse.proto", zapises);
            ReadWrite.read_protobuf("parse.proto", "protobuf_parser.txt");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}