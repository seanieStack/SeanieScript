package tool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;


public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(1);
        }

        Path outputDir = Paths.get(args[0]);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        defineAst(
                outputDir,
                "Expr",
                List.of(
                        "Binary   : Expr left, Token operator, Expr right",
                        "Grouping : Expr expression",
                        "Literal  : Object value",
                        "Unary    : Token operator, Expr right"
                )
        );
    }

    private static void defineAst(Path dir, String baseName, List<String> types) throws IOException {
        Path filePath = dir.resolve(baseName + ".java");
        StringBuilder builder = new StringBuilder();

        builder.append("package interp;\n\n");
        builder.append("import java.util.List;\n");
        builder.append("import lombok.AllArgsConstructor;\n\n");
        builder.append("abstract class ").append(baseName).append(" {\n\n");

        defineVisitor(builder, baseName, types);

        for (String type : types) {
            String trimmedType = type.trim();

            String[] parts = trimmedType.split(":");
            String className = parts[0].trim();
            String fields = parts[1].trim();
            defineType(builder, baseName, className, fields);
        }

        builder.append("\n");

        builder.append("  abstract <R> R accept(Visitor<R> visitor);\n");

        builder.append("}\n");


        Files.writeString(filePath, builder.toString(), StandardCharsets.UTF_8);
    }

    private static void defineVisitor(StringBuilder builder, String baseName, List<String> types) {
        builder.append("  interface Visitor<R> {\n");
        for (String type : types){
            String typeName = type.split(":")[0].trim();
            builder.append("    R visit").append(typeName).append(baseName).append("(").append(typeName).append(" ").append(baseName.toLowerCase()).append(");\n");
        }
        builder.append("  }\n\n");
    }

    private static void defineType(StringBuilder builder, String baseName, String className, String fields) {
        builder.append("  @AllArgsConstructor\n");
        builder.append("  static class ").append(className).append(" extends ").append(baseName).append(" {\n");

        writeFields(builder, fields);

        builder.append("\n");
        builder.append("    @Override\n");
        builder.append("    <R> R accept(Visitor<R> visitor) {\n");
        builder.append("      return visitor.visit").append(className).append(baseName).append("(this);\n");
        builder.append("    }\n");

        builder.append("  }\n\n");
    }

    private static void writeFields(StringBuilder builder, String fields) {
        for (String field : fields.split(", ")) {
            builder.append("    final ").append(field).append(";\n");
        }
    }
}
