package com.mongoplus.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.printer.configuration.PrettyPrinterConfiguration;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 独立的源码声明索引器，不解析依赖、继承成员或生成代码。
 */
public final class ApiIndexer {
    private final JavaParser parser = new JavaParser(new ParserConfiguration()
            .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
            .setCharacterEncoding(StandardCharsets.UTF_8));
    private final List<Map<String, Object>> records = new ArrayList<>();
    private int scanned;
    private int parsed;
    private int failed;

    /**
     * 生成索引；有解析失败时仍输出成功记录，但进程返回非零状态。
     * @param args --root 仓库路径 --output 输出路径
     * @throws Exception 参数、文件或 POM 读取失败
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> options = new HashMap<>();
        if (args.length != 4) {
            throw new IllegalArgumentException("Usage: --root <repository> --output <directory>");
        }
        for (int i = 0; i < args.length; i += 2) {
            if (!Set.of("--root", "--output").contains(args[i]) || options.put(args[i], args[i + 1]) != null) {
                throw new IllegalArgumentException("Unknown or duplicate option: " + args[i]);
            }
        }
        ApiIndexer indexer = new ApiIndexer();
        indexer.generate(Path.of(options.get("--root")), Path.of(options.get("--output")));
        if (indexer.failed > 0) {
            System.exit(1);
        }
    }

    void generate(Path repository, Path output) throws Exception {
        Path root = repository.toRealPath();
        List<Path> modules = new ArrayList<>();
        collectModules(root, root, modules);
        for (Path module : modules.stream().sorted().toList()) {
            Path source = module.resolve("src/main/java");
            if (!Files.isDirectory(source)) {
                continue;
            }
            List<Path> files;
            try (var stream = Files.walk(source)) {
                files = stream.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".java"))
                        .filter(p -> !hasTarget(source.relativize(p))).sorted().toList();
            }
            System.out.println("Module " + relative(root, module) + ": " + files.size() + " Java files");
            for (Path file : files) {
                scan(root, module, file);
            }
        }
        records.sort(Comparator.<Map<String, Object>, String>comparing(r -> (String) r.get("module"))
                .thenComparing(r -> (String) r.get("package"))
                .thenComparing(r -> (String) r.get("qualifiedName"))
                .thenComparingInt(r -> "type".equals(r.get("recordType")) ? 0 : 1)
                .thenComparingInt(r -> (Integer) r.get("line")));
        Files.createDirectories(output);
        writeJsonl(output.resolve("source-index.jsonl"), records);
        writeJsonl(output.resolve("api-index.jsonl"), records.stream()
                .filter(r -> Boolean.TRUE.equals(r.get("apiVisible"))).toList());
        writeMarkdown(output.resolve("api-index.md"), records);
        StringBuilder directory = new StringBuilder("# MongoPlus 索引目录\n\n"
                + "[公开 API JSONL](api-index.jsonl) · [完整源码 JSONL](source-index.jsonl) · "
                + "[完整 Markdown](api-index.md)\n\n以下模块 Markdown 均包含内部实现记录。\n\n");
        var byModule = records.stream().collect(Collectors.groupingBy(r -> (String) r.get("module"),
                TreeMap::new, Collectors.toList()));
        for (var entry : byModule.entrySet()) {
            String modulePath = "modules/" + (".".equals(entry.getKey()) ? "_root" : entry.getKey()) + "/index.md";
            writeMarkdown(output.resolve(modulePath), entry.getValue());
            directory.append("- [").append(entry.getKey()).append("](").append(modulePath)
                    .append(")：").append(entry.getValue().size()).append(" 条记录\n");
        }
        Files.writeString(output.resolve("index.md"), directory, StandardCharsets.UTF_8);
        reportFile(output.resolve("index.md"), byModule.size());
        System.out.printf("Scanned: %d%nParsed: %d%nFailed: %d%nTypes: %d%nMethods: %d%nConstructors: %d%nFields: %d%n",
                scanned, parsed, failed, count("type"), count("method"), count("constructor"), count("field"));
    }

    private static void writeJsonl(Path path, List<Map<String, Object>> rows) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (var record : rows) {
                writer.write(mapper.writeValueAsString(record));
                writer.write('\n');
            }
        }
        reportFile(path, rows.size());
    }

    private void writeMarkdown(Path path, List<Map<String, Object>> rows) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, markdown(rows), StandardCharsets.UTF_8);
        reportFile(path, rows.size());
    }

    private static void reportFile(Path path, int count) throws IOException {
        System.out.printf("%s: %d records/entries, %d bytes%n", path.toAbsolutePath().normalize(), count, Files.size(path));
    }

    // 只读取实际 POM 的直接 modules，不加载父 POM、profile、插件或 Maven 依赖。
    private static void collectModules(Path root, Path module, List<Path> modules) throws Exception {
        module = module.toRealPath();
        if (!module.startsWith(root) || modules.contains(module)) {
            throw new IOException("Outside repository or cyclic module: " + module);
        }
        modules.add(module);
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        var project = factory.newDocumentBuilder().parse(module.resolve("pom.xml").toFile()).getDocumentElement();
        for (var child = project.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element element && "modules".equals(element.getTagName())) {
                var entries = element.getElementsByTagName("module");
                for (int i = 0; i < entries.getLength(); i++) {
                    collectModules(root, module.resolve(entries.item(i).getTextContent().trim()), modules);
                }
            }
        }
    }

    private void scan(Path root, Path module, Path file) {
        scanned++;
        try {
            var result = parser.parse(file);
            if (!result.isSuccessful() || result.getResult().isEmpty()) {
                failed++;
                System.err.println("Parse failed: " + file + "\n" + result.getProblems());
                return;
            }
            parsed++;
            var unit = result.getResult().orElseThrow();
            String packageName = unit.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");
            for (TypeDeclaration<?> type : unit.getTypes()) {
                extract(root, module, file, packageName, "", type);
            }
        } catch (IOException exception) {
            failed++;
            System.err.println("Parse failed: " + file + "\n" + exception.getMessage());
        }
    }

    private void extract(Path root, Path module, Path file, String pkg, String enclosing, TypeDeclaration<?> type) {
        String name = (enclosing.isEmpty() ? (pkg.isEmpty() ? "" : pkg + ".") : enclosing + ".") + type.getNameAsString();
        var record = base(root, module, file, pkg, name, type, "type");
        record.put("name", type.getNameAsString());
        record.put("kind", type instanceof ClassOrInterfaceDeclaration c ? (c.isInterface() ? "interface" : "class")
                : type instanceof EnumDeclaration ? "enum" : type instanceof AnnotationDeclaration ? "annotation" : "record");
        record.put("visibility", visibility(type));
        record.put("extends", type instanceof ClassOrInterfaceDeclaration c ? strings(c.getExtendedTypes()) : List.of());
        record.put("implements", type instanceof ClassOrInterfaceDeclaration c ? strings(c.getImplementedTypes())
                : type instanceof EnumDeclaration e ? strings(e.getImplementedTypes())
                : type instanceof RecordDeclaration r ? strings(r.getImplementedTypes()) : List.of());
        record.put("typeParameters", type instanceof ClassOrInterfaceDeclaration c ? strings(c.getTypeParameters())
                : type instanceof RecordDeclaration r ? strings(r.getTypeParameters()) : List.of());
        records.add(record);
        // 只遍历直接成员，避免把匿名类和局部类中的方法归到外层类型。
        for (BodyDeclaration<?> member : type.getMembers()) {
            if (member instanceof TypeDeclaration<?> nested) {
                extract(root, module, file, pkg, name, nested);
            } else if (member instanceof FieldDeclaration field) {
                for (VariableDeclarator variable : field.getVariables()) {
                    var fieldRecord = base(root, module, file, pkg, name, member, "field");
                    fieldRecord.put("name", variable.getNameAsString());
                    fieldRecord.put("type", variable.getType().toString());
                    fieldRecord.put("visibility", visibility(field));
                    fieldRecord.put("id", relative(root, module) + ":" + name + "#" + variable.getNameAsString());
                    fieldRecord.put("signature", field.getModifiers().stream()
                            .map(m -> m.getKeyword().asString() + " ").collect(Collectors.joining())
                            + variable.getType() + " " + variable.getNameAsString());
                    fieldRecord.put("variableLine", variable.getBegin().orElseThrow().line);
                    records.add(fieldRecord);
                }
            } else if (member instanceof MethodDeclaration || member instanceof ConstructorDeclaration) {
                CallableDeclaration<?> callable = (CallableDeclaration<?>) member;
                var method = base(root, module, file, pkg, name, member,
                        member instanceof ConstructorDeclaration ? "constructor" : "method");
                method.put("name", callable.getNameAsString());
                method.put("visibility", visibility(member));
                method.put("signature", signature(callable));
                method.put("id", relative(root, module) + ":" + name + "#" + callable.getSignature().asString());
                method.put("parameters", callable.getParameters().stream().map(p -> {
                    Map<String, Object> parameter = new LinkedHashMap<>();
                    parameter.put("name", p.getNameAsString());
                    parameter.put("type", p.getType().toString());
                    parameter.put("varArgs", p.isVarArgs());
                    parameter.put("annotations", annotations(p.getAnnotations()));
                    return parameter;
                }).toList());
                method.put("returnType", member instanceof MethodDeclaration m ? m.getType().toString() : null);
                method.put("typeParameters", strings(callable.getTypeParameters()));
                method.put("throws", strings(callable.getThrownExceptions()));
                records.add(method);
            } else if (member instanceof AnnotationMemberDeclaration a) {
                var method = base(root, module, file, pkg, name, member, "method");
                method.put("name", a.getNameAsString());
                method.put("visibility", "public");
                method.put("signature", a.getType() + " " + a.getNameAsString() + "()");
                method.put("id", relative(root, module) + ":" + name + "#" + a.getNameAsString() + "()");
                method.put("parameters", List.of());
                method.put("returnType", a.getType().toString());
                method.put("typeParameters", List.of());
                method.put("throws", List.of());
                method.put("defaultValue", a.getDefaultValue().map(Object::toString).orElse(null));
                records.add(method);
            }
        }
    }

    private static Map<String, Object> base(Path root, Path module, Path file, String pkg, String owner,
                                             BodyDeclaration<?> node, String recordType) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordType", recordType);
        result.put("module", relative(root, module));
        result.put("package", pkg);
        result.put("qualifiedName", owner);
        result.put("sourcePath", relative(root, file));
        result.put("line", node.getBegin().orElseThrow().line);
        result.put("endLine", node.getEnd().orElseThrow().line);
        result.put("apiVisible", apiVisible(node));
        result.put("javadoc", node instanceof NodeWithJavadoc<?> j ? j.getJavadocComment().map(c -> c.getContent()).orElse("") : "");
        result.put("annotations", annotations(((NodeWithAnnotations<?>) node).getAnnotations()));
        result.put("modifiers", node instanceof NodeWithModifiers<?> m
                ? m.getModifiers().stream().map(v -> v.getKeyword().asString()).toList() : List.of());
        return result;
    }

    private static String visibility(BodyDeclaration<?> node) {
        if (node instanceof NodeWithModifiers<?> modifiers) {
            String explicit = modifiers.getAccessSpecifier().asString();
            if (!explicit.isEmpty()) {
                return explicit;
            }
        }
        Node parent = node.getParentNode().orElse(null);
        if (node instanceof ConstructorDeclaration && parent instanceof EnumDeclaration) {
            return "private";
        }
        return parent instanceof ClassOrInterfaceDeclaration c && c.isInterface()
                || parent instanceof AnnotationDeclaration ? "public" : "package-private";
    }

    // 外层类型不可访问时，不把其中的 public/protected 成员暴露为公开 API。
    private static boolean apiVisible(BodyDeclaration<?> node) {
        if (!Set.of("public", "protected").contains(visibility(node))) {
            return false;
        }
        for (Node parent = node.getParentNode().orElse(null); parent != null;
             parent = parent.getParentNode().orElse(null)) {
            if (parent instanceof TypeDeclaration<?> type
                    && !Set.of("public", "protected").contains(visibility(type))) {
                return false;
            }
        }
        return true;
    }

    // AnnotationExpr 可能附带普通注释；关闭注释打印，不用正则破坏字符串参数。
    private static List<String> annotations(Collection<AnnotationExpr> nodes) {
        var configuration = new PrettyPrinterConfiguration().setPrintComments(false).setPrintJavadoc(false);
        return nodes.stream().map(node -> node.toString(configuration)).toList();
    }

    private static String signature(CallableDeclaration<?> method) {
        String modifiers = method.getModifiers().stream().map(m -> m.getKeyword().asString()).collect(Collectors.joining(" "));
        String generics = method.getTypeParameters().isEmpty() ? "" : "<" + String.join(", ", strings(method.getTypeParameters())) + "> ";
        String returnType = method instanceof MethodDeclaration m ? m.getType() + " " : "";
        List<String> parameters = new ArrayList<>();
        method.getReceiverParameter().ifPresent(p -> parameters.add(p.toString()));
        parameters.addAll(strings(method.getParameters()));
        return (modifiers.isEmpty() ? "" : modifiers + " ") + generics + returnType + method.getNameAsString()
                + "(" + String.join(", ", parameters) + ")"
                + (method.getThrownExceptions().isEmpty() ? "" : " throws " + String.join(", ", strings(method.getThrownExceptions())));
    }

    private String markdown(List<Map<String, Object>> rows) {
        StringBuilder out = new StringBuilder("# MongoPlus API 源码索引\n\n仅记录显式声明；类型名未做跨文件解析，不展开继承或 Lombok 成员。\n\n");
        out.append(String.format("全仓库扫描 %d；成功 %d；失败 %d。本页 %d 条完整源码记录（含内部实现）。\n\n",
                scanned, parsed, failed, rows.size()));
        String module = null;
        String pkg = null;
        for (var record : rows) {
            if (!Objects.equals(module, record.get("module"))) {
                module = (String) record.get("module");
                pkg = null;
                out.append("## 模块：").append(module).append("\n\n");
            }
            if (!Objects.equals(pkg, record.get("package"))) {
                pkg = (String) record.get("package");
                out.append("### 包：").append(pkg).append("\n\n");
            }
            if ("type".equals(record.get("recordType"))) {
                out.append("#### ").append(record.get("kind")).append(' ').append(record.get("qualifiedName")).append("\n\n")
                        .append("可见性：").append(record.get("visibility")).append("；泛型：").append(record.get("typeParameters"))
                        .append("；extends：").append(record.get("extends")).append("；implements：").append(record.get("implements")).append("\n\n");
            } else {
                out.append("##### ").append(record.get("recordType")).append(' ').append(record.get("name")).append("\n\n");
                fence(out, "java", (String) record.get("signature"));
                out.append("所属类型：").append(record.get("qualifiedName")).append("；可见性：").append(record.get("visibility")).append("\n\n");
            }
            out.append("源码：").append(record.get("sourcePath")).append(':').append(record.get("line"))
                    .append('-').append(record.get("endLine")).append("\n\n");
            if (!((List<?>) record.get("annotations")).isEmpty()) {
                fence(out, "java", String.join("\n", strings((List<?>) record.get("annotations"))));
            }
            if (!((String) record.get("javadoc")).isBlank()) {
                fence(out, "text", ((String) record.get("javadoc")).strip());
            }
        }
        return out.toString();
    }

    private static void fence(StringBuilder out, String language, String content) {
        String delimiter = "```";
        while (content.contains(delimiter)) {
            delimiter += "`";
        }
        out.append(delimiter).append(language).append('\n').append(content).append('\n').append(delimiter).append("\n\n");
    }

    private long count(String kind) {
        return records.stream().filter(r -> kind.equals(r.get("recordType"))).count();
    }

    private static List<String> strings(Collection<?> nodes) {
        return nodes.stream().map(Object::toString).toList();
    }

    private static String relative(Path root, Path path) {
        String value = root.relativize(path).toString().replace('\\', '/');
        return value.isEmpty() ? "." : value;
    }

    private static boolean hasTarget(Path path) {
        for (Path part : path) {
            if ("target".equals(part.toString())) {
                return true;
            }
        }
        return false;
    }
}
