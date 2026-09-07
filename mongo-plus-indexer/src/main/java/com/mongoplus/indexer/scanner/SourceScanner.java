package com.mongoplus.indexer.scanner;

import com.mongoplus.indexer.MongoPlusIndexerConfig;
import com.mongoplus.indexer.model.MongoPlusApiIndex;
import com.mongoplus.indexer.type.TypeClassifier;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;

import javax.lang.model.element.Modifier;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/** 使用 JDK Compiler Tree API 扫描配置范围内的公开源码 API。 */
public final class SourceScanner {
    private final MongoPlusIndexerConfig config;
    private final TypeClassifier classifier = new TypeClassifier();
    private final Set<Path> primaryFiles = new LinkedHashSet<Path>();
    private final Set<Path> referencedFiles = new LinkedHashSet<Path>();

    public SourceScanner(MongoPlusIndexerConfig config) { this.config = config; }

    public MongoPlusApiIndex scan() throws IOException {
        if (config.isPipeline()) { return new PipelineScan().scan(); }
        collectPrimaryFiles();
        if (primaryFiles.isEmpty()) { throw new IllegalArgumentException("Primary Scan Scope 中没有 Java 源码"); }
        List<SourceType> types = parse(new ArrayList<Path>(primaryFiles));
        resolveRequiredTypes(types);
        List<SourceType> referencedTypes = referencedFiles.isEmpty()
                ? Collections.<SourceType>emptyList() : parse(new ArrayList<Path>(referencedFiles));
        return buildIndex(types, referencedTypes);
    }

    private void collectPrimaryFiles() throws IOException {
        for (String packageName : config.getPrimaryPackages()) {
            Path relative = packagePath(packageName);
            for (Path root : config.getSourceRoots()) {
                Path directory = root.resolve(relative);
                if (!Files.isDirectory(directory)) { continue; }
                try (Stream<Path> paths = Files.walk(directory)) {
                    paths.filter(path -> path.toString().endsWith(".java"))
                            .sorted().forEach(primaryFiles::add);
                }
            }
        }
    }

    private void resolveRequiredTypes(List<SourceType> types) {
        Set<String> required = new LinkedHashSet<String>(Arrays.asList("RegexOptions", "FieldChain"));
        for (SourceType type : types) {
            for (SourceMethod method : type.methods) {
                for (SourceParameter parameter : method.parameters) {
                    String kind = classifier.classify(parameter.type);
                    if ("ENUM".equals(kind) || "NESTED_FIELD_BUILDER".equals(kind)) {
                        required.add(TypeClassifier.simpleName(parameter.type));
                    }
                }
            }
        }
        for (String simpleName : required) {
            Path exact = locateImportedType(types, simpleName);
            if (exact == null) { exact = locateKnownType(simpleName); }
            if (exact != null && !primaryFiles.contains(exact)) { referencedFiles.add(exact); }
        }
    }

    private Path locateImportedType(List<SourceType> types, String simpleName) {
        for (SourceType type : types) {
            String imported = type.imports.get(simpleName);
            if (imported != null) {
                Path located = locateQualifiedName(imported);
                if (located != null) { return located; }
            }
        }
        return null;
    }

    private Path locateKnownType(String simpleName) {
        if ("FieldChain".equals(simpleName)) { return locateQualifiedName("com.mongoplus.function.FieldChain"); }
        if ("RegexOptions".equals(simpleName)) { return locateQualifiedName("com.mongoplus.enums.RegexOptions"); }
        return null;
    }

    private Path locateQualifiedName(String name) {
        int separator = name.lastIndexOf('.');
        Path relative = packagePath(name.substring(0, separator))
                .resolve(name.substring(separator + 1) + ".java");
        List<Path> roots = new ArrayList<Path>(config.getSourceRoots());
        Collections.sort(roots);
        for (Path root : roots) {
            Path candidate = root.resolve(relative);
            if (Files.isRegularFile(candidate)) { return candidate; }
        }
        return null;
    }

    private List<SourceType> parse(List<Path> files) throws IOException {
        files = new ArrayList<Path>(files);
        Collections.sort(files);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) { throw new IllegalStateException("当前运行环境不是完整 JDK，无法获得 JavaCompiler"); }
        StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        try {
            List<java.io.File> sourceFiles = new ArrayList<java.io.File>();
            for (Path file : files) { sourceFiles.add(file.toFile()); }
            Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(sourceFiles);
            JavacTask task = (JavacTask) compiler.getTask(null, manager, null,
                    Arrays.asList("-proc:none", "-Xlint:none"), null, units);
            DocTrees docTrees = DocTrees.instance(task);
            List<SourceType> result = new ArrayList<SourceType>();
            for (CompilationUnitTree unit : task.parse()) { parseUnit(unit, docTrees, result); }
            return result;
        } finally { manager.close(); }
    }

    private void parseUnit(CompilationUnitTree unit, DocTrees docTrees, List<SourceType> result) {
        Map<String, String> imports = new HashMap<String, String>();
        unit.getImports().forEach(item -> {
            String name = item.getQualifiedIdentifier().toString();
            imports.put(name.endsWith(".*") ? name : name.substring(name.lastIndexOf('.') + 1), name);
        });
        String packageName = unit.getPackageName() == null ? "" : unit.getPackageName().toString();
        for (Tree declaration : unit.getTypeDecls()) {
            if (!(declaration instanceof ClassTree)) { continue; }
            ClassTree classTree = (ClassTree) declaration;
            if (!classTree.getModifiers().getFlags().contains(Modifier.PUBLIC)) { continue; }
            SourceType type = new SourceType(packageName, classTree.getSimpleName().toString(),
                    classTree.getKind().name(), imports, documentation(docTrees, unit, classTree),
                    tags(docTrees, unit, classTree),
                    classTree.getModifiers().getFlags().contains(Modifier.ABSTRACT));
            classTree.getTypeParameters().forEach(parameter -> type.typeParameters.add(parameter.toString()));
            classTree.getModifiers().getAnnotations().forEach(annotation -> type.annotations.add(annotation.toString()));
            if (classTree.getExtendsClause() != null) { type.parents.add(simpleTree(classTree.getExtendsClause())); }
            for (Tree implemented : classTree.getImplementsClause()) { type.parents.add(simpleTree(implemented)); }
            boolean declaredConstructor = false;
            for (Tree member : classTree.getMembers()) {
                if (member instanceof MethodTree) {
                    MethodTree method = (MethodTree) member;
                    if ("<init>".contentEquals(method.getName())) {
                        addConstructor(type, method);
                        declaredConstructor = true;
                    } else {
                        addMethod(type, method, unit, docTrees);
                    }
                }
                if (classTree.getKind() == Tree.Kind.ENUM && member instanceof VariableTree) {
                    addEnumConstant(type, (VariableTree) member, unit, docTrees);
                }
            }
            if (!declaredConstructor && classTree.getKind() == Tree.Kind.CLASS) {
                type.constructors.add(SourceConstructor.implicitPublicNoArg());
            }
            result.add(type);
        }
    }

    private void addConstructor(SourceType type, MethodTree constructor) {
        Set<Modifier> flags = constructor.getModifiers().getFlags();
        SourceConstructor source = new SourceConstructor(visibility(flags), false);
        for (VariableTree parameter : constructor.getParameters()) {
            String parameterType = parameter.getType().toString();
            boolean varargs = parameter.toString().contains("...");
            source.parameters.add(new SourceParameter(parameter.getName().toString(), parameterType, varargs));
        }
        type.constructors.add(source);
    }

    private static String visibility(Set<Modifier> modifiers) {
        if (modifiers.contains(Modifier.PUBLIC)) { return "public"; }
        if (modifiers.contains(Modifier.PROTECTED)) { return "protected"; }
        if (modifiers.contains(Modifier.PRIVATE)) { return "private"; }
        return "package-private";
    }

    private void addMethod(SourceType type, MethodTree method, CompilationUnitTree unit, DocTrees docs) {
        Set<Modifier> flags = method.getModifiers().getFlags();
        if (flags.contains(Modifier.PRIVATE) || flags.contains(Modifier.PROTECTED)
                || (!flags.contains(Modifier.PUBLIC) && type.kind.indexOf("INTERFACE") < 0)) { return; }
        if ("<init>".contentEquals(method.getName())) { return; }
        SourceMethod source = new SourceMethod(method.getName().toString(),
                method.getReturnType() == null ? "" : method.getReturnType().toString());
        source.defaultMethod = flags.contains(Modifier.DEFAULT);
        source.staticMethod = flags.contains(Modifier.STATIC);
        source.deprecated = hasDeprecated(method);
        source.modifiers.addAll(flags);
        method.getModifiers().getAnnotations().forEach(annotation -> source.annotations.add(annotation.toString()));
        method.getTypeParameters().forEach(parameter -> source.typeParameters.add(parameter.toString()));
        source.doc = documentation(docs, unit, method);
        source.tags.putAll(tags(docs, unit, method));
        for (VariableTree parameter : method.getParameters()) {
            String parameterType = parameter.getType().toString();
            boolean varargs = parameter.toString().contains("...");
            source.parameters.add(new SourceParameter(parameter.getName().toString(), parameterType, varargs));
        }
        type.methods.add(source);
    }

    private void addEnumConstant(SourceType type, VariableTree variable, CompilationUnitTree unit, DocTrees docs) {
        if (!variable.getType().toString().equals(type.name)) { return; }
        Map<String, Object> constant = object();
        constant.put("name", variable.getName().toString());
        List<Object> arguments = new ArrayList<Object>();
        String initializer = variable.getInitializer() == null ? "" : variable.getInitializer().toString();
        int open = initializer.indexOf('('), close = initializer.lastIndexOf(')');
        if (open >= 0 && close > open) {
            String value = initializer.substring(open + 1, close).trim();
            if (!value.isEmpty()) { arguments.add(unquote(value)); }
        }
        constant.put("arguments", arguments);
        constant.put("description", documentation(docs, unit, variable).description);
        type.enumConstants.add(constant);
    }

    private MongoPlusApiIndex buildIndex(List<SourceType> types, List<SourceType> referencedTypes) {
        MongoPlusApiIndex index = new MongoPlusApiIndex(config.getMongoPlusVersion());
        for (String packageName : config.getPrimaryPackages()) { index.list("primaryPackages").add(packageName); }
        Map<String, List<MethodRecord>> families = new java.util.TreeMap<String, List<MethodRecord>>();
        Map<String, SourceType> byName = new HashMap<String, SourceType>();
        for (SourceType type : types) { byName.put(type.name, type); }
        for (SourceType type : types) {
            for (SourceMethod method : type.methods) {
                families.computeIfAbsent(method.name, key -> new ArrayList<MethodRecord>())
                        .add(new MethodRecord(type, method));
            }
            if (isWrapper(type)) { index.list("wrappers").add(wrapper(type)); }
        }
        for (Map.Entry<String, List<MethodRecord>> entry : families.entrySet()) {
            index.list("methodFamilies").add(family(entry.getKey(), entry.getValue(), types));
        }
        for (SourceType type : referencedTypes) {
            if ("RegexOptions".equals(type.name)) { index.list("types").add(enumType(type)); }
        }
        addSpecialTypes(index, referencedTypes);
        int overloads = 0;
        for (List<MethodRecord> records : families.values()) { overloads += records.size(); }
        Map<String, Object> stats = index.object("scanStatistics");
        stats.put("activelyScannedJavaTypes", types.size());
        stats.put("referencedTypesParsed", referencedTypes.size());
        stats.put("methodFamilyCount", families.size());
        stats.put("overloadCount", overloads);
        List<Object> referenced = new ArrayList<Object>();
        for (SourceType type : referencedTypes) { referenced.add(type.packageName + "." + type.name); }
        stats.put("referencedTypeNames", referenced);
        stats.put("fullProjectTraversal", false);
        return index;
    }

    private Map<String, Object> family(String name, List<MethodRecord> records, List<SourceType> types) {
        Collections.sort(records, Comparator.comparing(record -> record.method.signature()));
        Map<String, Object> family = object(); family.put("name", name);
        Set<String> descriptions = new LinkedHashSet<String>();
        Set<String> operators = new java.util.TreeSet<String>();
        Set<String> compositionSemantics = new java.util.TreeSet<String>();
        Set<String> aliases = new java.util.TreeSet<String>();
        List<Object> overloads = new ArrayList<Object>();
        for (MethodRecord record : records) {
            if (!record.method.doc.description.isEmpty()) { descriptions.add(record.method.doc.description); }
            operators.addAll(record.type.tags.getOrDefault("mongodbOperator", Collections.<String>emptyList()));
            operators.addAll(record.method.tags.getOrDefault("mongodbOperator", Collections.<String>emptyList()));
            compositionSemantics.addAll(record.method.tags.getOrDefault(
                    "mongoComposition", Collections.<String>emptyList()));
            aliases.addAll(record.type.tags.getOrDefault("aiAlias", Collections.<String>emptyList()));
            aliases.addAll(record.method.tags.getOrDefault("aiAlias", Collections.<String>emptyList()));
            overloads.add(overload(record, types));
        }
        family.put("description", descriptions.isEmpty() ? "" : descriptions.iterator().next());
        family.put("mongoOperators", new ArrayList<Object>(operators));
        family.put("compositionSemantics", new ArrayList<Object>(compositionSemantics));
        family.put("aliases", new ArrayList<Object>(aliases));
        family.put("overloads", overloads);
        return family;
    }

    private Map<String, Object> overload(MethodRecord record, List<SourceType> types) {
        SourceMethod method = record.method;
        Map<String, Object> value = object();
        value.put("signature", method.signature()); value.put("returnType", method.returnType);
        value.put("returnDescription", method.doc.returnDescription);
        value.put("declaredIn", record.type.packageName + "." + record.type.name);
        List<Object> available = new ArrayList<Object>();
        for (SourceType type : types) {
            if (isWrapper(type) && inherits(type, record.type.name, types, new LinkedHashSet<String>())) {
                available.add(type.packageName + "." + type.name);
            }
        }
        value.put("availableIn", available);
        value.put("modifiers", new ArrayList<Object>(method.modifiers));
        value.put("annotations", new ArrayList<Object>(method.annotations));
        value.put("typeParameters", new ArrayList<Object>(method.typeParameters));
        value.put("defaultMethod", method.defaultMethod); value.put("staticMethod", method.staticMethod);
        value.put("deprecated", method.deprecated);
        List<Object> parameters = new ArrayList<Object>();
        for (SourceParameter parameter : method.parameters) {
            Map<String, Object> item = object(); item.put("name", parameter.name); item.put("type", parameter.type);
            String description = method.doc.parameters.getOrDefault(parameter.name, "");
            item.put("semanticType", classifier.classify(parameter.type, method.name, parameter.name,
                    record.type.name, description));
            item.put("varargs", parameter.varargs);
            item.put("description", description); parameters.add(item);
        }
        value.put("parameters", parameters); return value;
    }

    private boolean inherits(SourceType type, String target, List<SourceType> types, Set<String> visited) {
        if (type.name.equals(target)) { return true; }
        if (!visited.add(type.name)) { return false; }
        for (String parent : type.parents) {
            String simple = TypeClassifier.simpleName(parent);
            if (simple.equals(target)) { return true; }
            for (SourceType candidate : types) {
                if (candidate.name.equals(simple) && inherits(candidate, target, types, visited)) { return true; }
            }
        }
        return false;
    }

    private Map<String, Object> wrapper(SourceType type) {
        Map<String, Object> value = object(); value.put("name", type.name);
        value.put("qualifiedName", type.packageName + "." + type.name);
        value.put("kind", type.kind.toLowerCase(java.util.Locale.ROOT));
        value.put("abstractType", type.abstractType);
        value.put("extendsOrImplements", new ArrayList<Object>(type.parents));
        List<SourceConstructor> constructors = new ArrayList<SourceConstructor>(type.constructors);
        Collections.sort(constructors, Comparator.comparing(SourceConstructor::signature));
        List<Object> constructorValues = new ArrayList<Object>();
        for (SourceConstructor constructor : constructors) {
            Map<String, Object> constructorValue = object();
            constructorValue.put("signature", type.name + constructor.signature());
            constructorValue.put("visibility", constructor.visibility);
            constructorValue.put("implicit", constructor.implicit);
            List<Object> parameters = new ArrayList<Object>();
            for (SourceParameter parameter : constructor.parameters) {
                Map<String, Object> item = object();
                item.put("name", parameter.name);
                item.put("type", parameter.type);
                item.put("varargs", parameter.varargs);
                parameters.add(item);
            }
            constructorValue.put("parameters", parameters);
            constructorValues.add(constructorValue);
        }
        value.put("constructors", constructorValues);
        return value;
    }

    private Map<String, Object> enumType(SourceType type) {
        Map<String, Object> value = object(); value.put("name", type.name);
        value.put("qualifiedName", type.packageName + "." + type.name); value.put("kind", "enum");
        value.put("description", type.doc.description); value.put("constants", type.enumConstants); return value;
    }

    private void addSpecialTypes(MongoPlusApiIndex index, List<SourceType> referencedTypes) {
        Map<String, Object> function = object(); function.put("name", "SFunction");
        function.put("semanticType", "LAMBDA_FIELD");
        function.put("description", "通过实体 getter 方法引用表示 MongoPlus 字段");
        function.put("usageExamples", Arrays.<Object>asList("User::getUserName", "User::getAge"));
        function.put("limitations", Arrays.<Object>asList("不能直接表达嵌套文档字段"));
        function.put("supportedSemanticTypes", Arrays.<Object>asList(
                "LAMBDA_FIELD", "CONDITION_GROUP_LAMBDA", "DOCUMENT_LAMBDA"));
        index.list("specialTypes").add(function);
        SourceType fieldChain = null;
        for (SourceType type : referencedTypes) { if ("FieldChain".equals(type.name)) { fieldChain = type; } }
        Map<String, Object> chain = object(); chain.put("name", "FieldChain");
        chain.put("semanticType", "NESTED_FIELD_BUILDER");
        chain.put("description", fieldChain == null ? "通过 Lambda getter 链安全构建 MongoDB 嵌套文档字段" : fieldChain.doc.description);
        List<Object> methods = new ArrayList<Object>();
        if (fieldChain != null) {
            for (SourceMethod method : fieldChain.methods) { methods.add(publicMethod(fieldChain, method)); }
        }
        chain.put("publicMethods", methods);
        Map<String, Object> usage = object();
        usage.put("normal", "FieldChain.of(User::getRole).then(Role::getRoleName).build()");
        usage.put("withDollarPrefix", "FieldChain.of(User::getRole).then(Role::getRoleName).build(true)");
        chain.put("usage", usage); index.list("specialTypes").add(chain);
        Map<String, Object> nested = object(); nested.put("id", "nested-field"); nested.put("name", "嵌套文档字段");
        nested.put("aliases", Arrays.<Object>asList("nested field", "内部文档", "嵌套字段"));
        nested.put("relatedTypes", Arrays.<Object>asList("FieldChain"));
        nested.put("stringExample", "role.roleName"); index.list("concepts").add(nested);
    }

    private Map<String, Object> publicMethod(SourceType type, SourceMethod method) {
        Map<String, Object> value = object();
        value.put("name", method.name);
        value.put("signature", method.signature());
        value.put("returnType", method.returnType);
        value.put("modifiers", new ArrayList<Object>(method.modifiers));
        value.put("annotations", new ArrayList<Object>(method.annotations));
        value.put("typeParameters", new ArrayList<Object>(method.typeParameters));
        List<Object> parameters = new ArrayList<Object>();
        for (SourceParameter parameter : method.parameters) {
            Map<String, Object> item = object();
            item.put("name", parameter.name);
            item.put("type", parameter.type);
            String description = method.doc.parameters.getOrDefault(parameter.name, "");
            item.put("semanticType", classifier.classify(parameter.type, method.name, parameter.name,
                    type.name, description));
            item.put("varargs", parameter.varargs);
            item.put("description", description);
            parameters.add(item);
        }
        value.put("parameters", parameters);
        return value;
    }

    private boolean isWrapper(SourceType type) { return type.name.endsWith("Wrapper"); }
    private boolean hasDeprecated(MethodTree method) {
        return method.getModifiers().getAnnotations().stream()
                .anyMatch(annotation -> annotation.getAnnotationType().toString().endsWith("Deprecated"));
    }
    private static Path packagePath(String packageName) {
        int classSeparator = packageName.lastIndexOf('.');
        if (classSeparator > 0 && Character.isUpperCase(packageName.charAt(classSeparator + 1))) {
            packageName = packageName.substring(0, classSeparator);
        }
        return java.nio.file.Paths.get(packageName.replace('.', java.io.File.separatorChar));
    }
    private static String simpleTree(Tree tree) { return tree.toString(); }
    private static String unquote(String value) {
        return value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")
                ? value.substring(1, value.length() - 1) : value;
    }
    private static Map<String, Object> object() { return new LinkedHashMap<String, Object>(); }

    private Documentation documentation(DocTrees trees, CompilationUnitTree unit, Tree tree) {
        DocCommentTree doc = trees.getDocCommentTree(TreePath.getPath(unit, tree));
        if (doc == null) { return new Documentation(); }
        Documentation result = new Documentation();
        result.description = normalize(doc.getFullBody().toString());
        for (DocTree block : doc.getBlockTags()) {
            if (block instanceof ParamTree) {
                ParamTree param = (ParamTree) block;
                result.parameters.put(param.getName().toString(), normalize(param.getDescription().toString()));
            } else if (block instanceof ReturnTree) {
                result.returnDescription = normalize(((ReturnTree) block).getDescription().toString());
            }
        }
        return result;
    }

    private Map<String, List<String>> tags(DocTrees trees, CompilationUnitTree unit, Tree tree) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        DocCommentTree doc = trees.getDocCommentTree(TreePath.getPath(unit, tree));
        if (doc == null) { return result; }
        for (DocTree block : doc.getBlockTags()) {
            if (block instanceof UnknownBlockTagTree) {
                UnknownBlockTagTree tag = (UnknownBlockTagTree) block;
                result.computeIfAbsent(tag.getTagName(), key -> new ArrayList<String>())
                        .add(normalize(tag.getContent().toString()));
            }
        }
        return result;
    }

    private static String normalize(String value) {
        String normalized = value.replaceAll("\\s+", " ").trim();
        StringBuilder decoded = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            if (normalized.charAt(i) == '\\' && i + 5 < normalized.length()
                    && normalized.charAt(i + 1) == 'u') {
                String hexadecimal = normalized.substring(i + 2, i + 6);
                try {
                    decoded.append((char) Integer.parseInt(hexadecimal, 16));
                    i += 5;
                    continue;
                } catch (NumberFormatException ignored) {
                    // 非法转义保持原样，交给 JSON writer 正常转义。
                }
            }
            decoded.append(normalized.charAt(i));
        }
        return decoded.toString();
    }


    /** 沿 Stage 继承链、显式 Expression 入口及公开签名收集依赖，不扫描包。 */
    private final class PipelineScan {
        private static final String EXPRESSION_SEMANTIC = "PIPELINE_EXPRESSION";
        private static final String FIELD_REFERENCE_CONCEPT = "PIPELINE_EXPRESSION_FIELD_REFERENCE";
        private static final String PARAMETER_TAG = "mongoParam";
        private boolean hasExpressionParameterEvidence;
        private final Set<String> stageParameterSemantics = new java.util.TreeSet<String>();
        private final Map<String, SourceType> loaded = new java.util.TreeMap<String, SourceType>();
        private final Set<String> hierarchy = new java.util.TreeSet<String>();
        private final Set<String> dependencies = new java.util.TreeSet<String>();
        private final Set<String> external = new java.util.TreeSet<String>();
        private final Map<String, List<MethodRecord>> families = new java.util.TreeMap<String, List<MethodRecord>>();

        MongoPlusApiIndex scan() throws IOException {
            String entry = MongoPlusIndexerConfig.PIPELINE_ENTRY_TYPE;
            if (load(entry) == null) { throw new IOException("找不到聚合入口源码: " + entry); }
            collectHierarchy(entry);
            for (String name : hierarchy) {
                SourceType type = loaded.get(name);
                for (SourceMethod method : type.methods) {
                    // 接口静态方法不通过子接口继承。
                    if (!method.staticMethod || name.equals(entry)) { select(type, method); }
                }
            }
            for (String root : config.getExpressionRoots()) {
                if (load(root) == null) { throw new IOException("找不到表达式入口源码: " + root); }
                // 与签名依赖共用闭包，保留构造器、父类型和 special type；每个类型只处理一次。
                dependencies.add(root);
            }
            Set<String> processed = new java.util.TreeSet<String>();
            while (!processed.containsAll(dependencies)) {
                Set<String> pending = new java.util.TreeSet<String>(dependencies);
                pending.removeAll(processed);
                for (String name : pending) {
                    processed.add(name);
                    SourceType type = load(name);
                    if (type == null) { continue; }
                    for (String parent : type.parents) { references(type, parent); }
                    for (String bound : type.typeParameters) { references(type, bound); }
                    for (SourceConstructor constructor : type.constructors) {
                        if (!"public".equals(constructor.visibility)) { continue; }
                        for (SourceParameter parameter : constructor.parameters) { references(type, parameter.type); }
                    }
                    for (SourceMethod method : type.methods) {
                        methodReferences(type, method);
                        if (!hierarchy.contains(name)) { select(type, method); }
                    }
                }
            }
            MongoPlusApiIndex index = new MongoPlusApiIndex(config.getMongoPlusVersion());
            index.asMap().put("entryType", entry);
            index.asMap().put("expressionRoots", config.getExpressionRoots());
            index.list("primaryPackages").add("com.mongoplus.aggregate");
            int overloadCount = 0;
            int stages = 0;
            int expressions = 0;
            for (Map.Entry<String, List<MethodRecord>> item : families.entrySet()) {
                List<MethodRecord> records = item.getValue();
                records.sort(Comparator.comparing((MethodRecord r) -> qualified(r.type))
                        .thenComparing(r -> r.method.signature()));
                boolean stage = item.getKey().startsWith("PIPELINE_STAGE:");
                Map<String, Object> value = family(records.get(0).method.name, records, Collections.<SourceType>emptyList());
                value.put("apiCategory", stage ? "PIPELINE_STAGE" : "PIPELINE_EXPRESSION");
                Set<String> mappings = new java.util.TreeSet<String>();
                List<Object> overloads = new ArrayList<Object>();
                for (MethodRecord record : records) {
                    mappings.addAll(mapping(record.type, record.method, stage ? "mongoStage" : "mongoExpression"));
                    overloads.add(evidence(record.type, record.method));
                }
                value.put("mongoStages", stage ? new ArrayList<String>(mappings) : Collections.emptyList());
                value.put("mongoExpressions", stage ? Collections.emptyList() : new ArrayList<String>(mappings));
                value.put("overloads", overloads);
                index.getMethodFamilies().add(value);
                overloadCount += records.size();
                if (stage) { stages++; } else { expressions++; }
            }
            Set<String> included = new java.util.TreeSet<String>(hierarchy);
            included.addAll(dependencies);
            for (String name : included) {
                SourceType type = loaded.get(name);
                if (type == null) { continue; }
                Map<String, Object> value = wrapper(type);
                value.put("abstractType", type.abstractType || type.kind.contains("INTERFACE"));
                value.put("description", type.doc.description);
                value.put("typeParameters", new ArrayList<String>(type.typeParameters));
                List<String> annotations = new ArrayList<String>(type.annotations);
                Collections.sort(annotations);
                value.put("annotations", annotations);
                List<String> parents = new ArrayList<String>(type.parents);
                Collections.sort(parents);
                value.put("extendsOrImplements", parents);
                value.put("imports", new java.util.TreeMap<String, String>(type.imports));
                List<Object> methods = new ArrayList<Object>();
                List<SourceMethod> sorted = new ArrayList<SourceMethod>(type.methods);
                sorted.sort(Comparator.comparing(SourceMethod::signature));
                for (SourceMethod method : sorted) {
                    if (hierarchy.contains(name) && !dependencies.contains(name)
                            && method.staticMethod && !name.equals(entry)) { continue; }
                    if (dependencies.contains(name) || !mapping(type, method, "mongoStage").isEmpty()
                            || !mapping(type, method, "mongoExpression").isEmpty()) {
                        methods.add(evidence(type, method));
                    }
                }
                value.put("publicMethods", methods);
                if ("ENUM".equals(type.kind)) {
                    List<Object> constants = new ArrayList<Object>(type.enumConstants);
                    constants.sort(Comparator.comparing(c -> ((Map<?, ?>) c).get("name").toString()));
                    value.put("constants", constants);
                }
                index.list("types").add(value);
            }
            // 只带入闭包实际引用的既有 special type / concept，不注入全局样例。
            MongoPlusApiIndex special = new MongoPlusApiIndex(config.getMongoPlusVersion());
            for (SourceType type : loaded.values()) {
                type.methods.sort(Comparator.comparing(SourceMethod::signature));
            }
            addSpecialTypes(special, new ArrayList<SourceType>(loaded.values()));
            for (Object item : special.list("specialTypes")) {
                Map<?, ?> value = (Map<?, ?>) item;
                for (String name : dependencies) {
                    if (name.equals("com.mongoplus.support.SFunction") && "SFunction".equals(value.get("name"))
                            || name.equals("com.mongoplus.function.FieldChain") && "FieldChain".equals(value.get("name"))) {
                        index.list("specialTypes").add(item);
                    }
                }
            }
            if (dependencies.contains("com.mongoplus.function.FieldChain")) {
                index.list("concepts").addAll(special.list("concepts"));
            }
            if (hasExpressionParameterEvidence) { index.list("concepts").add(expressionParameterConcept()); }
            for (String semantic : stageParameterSemantics) {
                index.list("concepts").add(StageParameterConcepts.concept(semantic));
            }
            Map<String, Object> stats = index.object("scanStatistics");
            Set<String> activeTypes = new java.util.TreeSet<String>(hierarchy);
            activeTypes.addAll(config.getExpressionRoots());
            stats.put("activelyScannedJavaTypes", activeTypes.size());
            stats.put("referencedTypesParsed", loaded.size() - activeTypes.size());
            stats.put("methodFamilyCount", families.size());
            stats.put("overloadCount", overloadCount);
            stats.put("pipelineStageCount", stages);
            stats.put("pipelineExpressionCount", expressions);
            stats.put("externalTypeReferences", new ArrayList<String>(external));
            stats.put("fullProjectTraversal", false);
            return index;
        }

        private void collectHierarchy(String name) throws IOException {
            if (!hierarchy.add(name)) { return; }
            SourceType type = load(name);
            if (type == null) { hierarchy.remove(name); return; }
            for (String parent : type.parents) {
                String resolved = resolve(type, parent.replaceAll("<.*", ""));
                if (resolved != null) { collectHierarchy(resolved); }
            }
        }

        private void select(SourceType type, SourceMethod method) throws IOException {
            for (String category : Arrays.asList("PIPELINE_STAGE", "PIPELINE_EXPRESSION")) {
                String tag = "PIPELINE_STAGE".equals(category) ? "mongoStage" : "mongoExpression";
                if (mapping(type, method, tag).isEmpty()) { continue; }
                families.computeIfAbsent(category + ":" + method.name, key -> new ArrayList<MethodRecord>())
                        .add(new MethodRecord(type, method));
                for (String bound : type.typeParameters) { references(type, bound); }
                methodReferences(type, method);
            }
        }

        private Set<String> mapping(SourceType type, SourceMethod method, String tag) {
            Set<String> values = new java.util.TreeSet<String>();
            List<String> raw = new ArrayList<String>(type.tags.getOrDefault(tag, Collections.<String>emptyList()));
            raw.addAll(method.tags.getOrDefault(tag, Collections.<String>emptyList()));
            for (String value : raw) {
                // 仅接受独立且完整的显式标记值，不从描述正文提取美元词。
                if (value.matches("\\$[A-Za-z][A-Za-z0-9]*")) { values.add(value); }
            }
            return values;
        }

        private void methodReferences(SourceType owner, SourceMethod method) throws IOException {
            references(owner, method.returnType);
            for (String bound : method.typeParameters) { references(owner, bound); }
            for (SourceParameter parameter : method.parameters) { references(owner, parameter.type); }
        }

        private void references(SourceType owner, String signatureType) throws IOException {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("[A-Za-z_$][A-Za-z0-9_$]*(?:\\.[A-Za-z_$][A-Za-z0-9_$]*)*").matcher(signatureType);
            while (matcher.find()) {
                String name = resolve(owner, matcher.group());
                if (name != null && !hierarchy.contains(name)) { dependencies.add(name); }
            }
        }

        private String resolve(SourceType owner, String token) throws IOException {
            if (token.isEmpty() || !Character.isUpperCase(token.charAt(0)) && token.indexOf('.') < 0) { return null; }
            Set<String> candidates = new java.util.TreeSet<String>();
            if (token.indexOf('.') >= 0) { candidates.add(token); }
            String imported = owner.imports.get(token);
            if (imported != null) { candidates.add(imported); }
            candidates.add(owner.packageName + "." + token);
            for (String name : owner.imports.values()) {
                if (name.endsWith(".*")) { candidates.add(name.substring(0, name.length() - 1) + token); }
            }
            String found = null;
            for (String candidate : candidates) {
                if (locateQualifiedName(candidate) != null) {
                    if (found != null && !found.equals(candidate)) {
                        throw new IOException("类型引用不明确: " + qualified(owner) + " -> " + token);
                    }
                    found = candidate;
                }
            }
            if (found == null && imported != null && !imported.startsWith("java.")) { external.add(imported); }
            return found;
        }

        private SourceType load(String name) throws IOException {
            if (!loaded.containsKey(name)) {
                Path path = locateQualifiedName(name);
                if (path == null) { return null; }
                for (SourceType type : parse(Collections.singletonList(path))) {
                    loaded.put(qualified(type), type);
                }
            }
            return loaded.get(name);
        }

        private Map<String, Object> evidence(SourceType owner, SourceMethod method) {
            Map<String, Object> value = overload(new MethodRecord(owner, method), Collections.<SourceType>emptyList());
            value.put("name", method.name);
            Set<String> available = new java.util.TreeSet<String>();
            available.add(qualified(owner));
            if (hierarchy.contains(qualified(owner)) && !method.staticMethod) {
                available.add(MongoPlusIndexerConfig.PIPELINE_ENTRY_TYPE);
            }
            value.put("availableIn", new ArrayList<String>(available));
            List<String> parameterTypes = new ArrayList<String>();
            for (SourceParameter parameter : method.parameters) { parameterTypes.add(parameter.type); }
            value.put("parameterTypes", parameterTypes);
            List<String> modifiers = new ArrayList<String>();
            for (Modifier modifier : method.modifiers) { modifiers.add(modifier.toString()); }
            Collections.sort(modifiers);
            value.put("modifiers", modifiers);
            value.put("mongoStages", new ArrayList<String>(mapping(owner, method, "mongoStage")));
            value.put("mongoExpressions", new ArrayList<String>(mapping(owner, method, "mongoExpression")));
            parameterSemantics(owner, method, value);
            return value;
        }

        /** 仅消费逐参数显式声明；泛型名、参数名和描述均不能产生聚合语义。 */
        @SuppressWarnings("unchecked")
        private void parameterSemantics(SourceType owner, SourceMethod method, Map<String, Object> evidence) {
            for (String raw : new java.util.TreeSet<String>(
                    method.tags.getOrDefault(PARAMETER_TAG, Collections.<String>emptyList()))) {
                String[] parts = raw.split(" ");
                if ((parts.length != 3 && parts.length != 4)
                        || !(EXPRESSION_SEMANTIC.equals(parts[1]) || StageParameterConcepts.contains(parts[1]))
                        || !("VALUE".equals(parts[2]) || "ELEMENT".equals(parts[2]))) {
                    throw new IllegalArgumentException("非法 @mongoParam: " + qualified(owner) + "#"
                            + method.signature() + " -> " + raw);
                }
                Map<String, Object> target = null;
                for (Object item : (List<?>) evidence.get("parameters")) {
                    Map<String, Object> parameter = (Map<String, Object>) item;
                    if (parts[0].equals(parameter.get("name"))) { target = parameter; break; }
                }
                if (target == null
                        || ("ELEMENT".equals(parts[2]) && !isExpressionElementContainer(owner, target))
                        || ("VALUE".equals(parts[2]) && Boolean.TRUE.equals(target.get("varargs")))) {
                    throw new IllegalArgumentException("@mongoParam 参数或作用范围不匹配: " + qualified(owner)
                            + "#" + method.signature() + " -> " + raw);
                }
                boolean expression = EXPRESSION_SEMANTIC.equals(parts[1]);
                String conceptRef = expression ? FIELD_REFERENCE_CONCEPT
                        : parts.length == 4 ? parts[3] : StageParameterConcepts.conceptId(parts[1]);
                if (target.containsKey("semanticEvidence") || !expression
                        && (!StageParameterConcepts.accepts(parts[1], (String) target.get("type"), parts[2])
                        || !StageParameterConcepts.acceptsConcept(parts[1], conceptRef))
                        || expression && parts.length != 3) {
                    throw new IllegalArgumentException("@mongoParam 冲突或 Java 表示不匹配: " + qualified(owner)
                            + "#" + method.signature() + " -> " + raw);
                }
                target.put("semanticType", parts[1]);
                target.put("semanticScope", parts[2]);
                target.put("conceptRef", conceptRef);
                Map<String, Object> source = object();
                source.put("source", "JAVADOC");
                source.put("tag", PARAMETER_TAG);
                source.put("value", raw);
                target.put("semanticEvidence", source);
                if (expression) { hasExpressionParameterEvidence = true; }
                else { stageParameterSemantics.add(conceptRef); }
            }
        }

        /** 只校验显式 ELEMENT 标签的容器结构；容器类型本身不产生任何语义。 */
        private boolean isExpressionElementContainer(SourceType owner, Map<String, Object> parameter) {
            String type = (String) parameter.get("type");
            if (Boolean.TRUE.equals(parameter.get("varargs")) || type.endsWith("[]")) { return true; }
            String raw = type.replaceAll("<.*", "");
            String qualifiedType = raw.contains(".") ? raw : owner.imports.get(raw);
            if (qualifiedType == null && owner.imports.containsKey("java.util.*")
                    && locateQualifiedName(owner.packageName + "." + raw) == null) {
                qualifiedType = "java.util." + raw;
            }
            return "java.util.List".equals(qualifiedType) || "java.util.Collection".equals(qualifiedType);
        }

        /** 固定的已审计表示契约；只随显式参数 evidence 输出，不增加任何扫描入口或映射。 */
        private Map<String, Object> expressionParameterConcept() {
            Map<String, Object> concept = object();
            concept.put("id", FIELD_REFERENCE_CONCEPT);
            concept.put("name", "聚合表达式参数的字段引用表示");
            concept.put("semanticType", EXPRESSION_SEMANTIC);
            concept.put("description", "已标记参数接收聚合表达式值；VALUE 指参数本身，ELEMENT 指数组、varargs 或集合的每个元素。"
                    + "String 表示以默认 BSON String codec 为前提，不保证任意值满足具体操作符的服务端约束。");
            Map<String, Object> field = object();
            field.put("javaType", "java.lang.String");
            field.put("prefix", "$");
            field.put("excludedPrefix", "$$");
            field.put("encoding", "UNCHANGED");
            field.put("example", "$amount");
            concept.put("fieldReference", field);
            Map<String, Object> variable = object();
            variable.put("javaType", "java.lang.String");
            variable.put("prefix", "$$");
            variable.put("encoding", "UNCHANGED");
            variable.put("sourceExample", "$$SEARCH_META");
            variable.put("bindingValidated", false);
            concept.put("variableReference", variable);
            Map<String, Object> literal = object();
            literal.put("javaType", "java.lang.String");
            literal.put("excludedPrefix", "$");
            literal.put("encoding", "UNCHANGED");
            literal.put("example", "amount");
            literal.put("automaticFieldPrefix", false);
            literal.put("automaticLiteralEscaping", false);
            literal.put("dollarPrefixedLiteralConstruction", "NOT_ESTABLISHED");
            concept.put("plainStringValue", literal);
            concept.put("bsonValueHandling", "Bson 转为 BSON document；其他非 null 值使用运行时类型对应的 codec。");
            List<Object> sources = new ArrayList<Object>();
            sources.add(semanticSource("mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java",
                    "group(TExpression,List<BsonField>); group(SFunction,BsonField...)",
                    "泛型 id 原样交给 Aggregates.group；Lambda id 经 getFieldNameLineOption 后进入同一泛型路径。"));
            sources.add(semanticSource("mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/Accumulators.java",
                    "accumulatorOperator; sum; avg", "参数保存在 BsonField 的 SimpleExpression 中。"));
            sources.add(semanticSource("mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/SimpleExpression.java",
                    "encodeValue", "null 写 BSON null；Bson 转 document；其余值委托运行时 codec。"));
            sources.add(semanticSource("mongo-plus-core/src/main/java/com/mongoplus/support/SFunction.java",
                    "getFieldNameLineOption", "返回美元前缀加实际字段名。"));
            sources.add(semanticSource("mongo-plus-core/src/main/java/com/mongoplus/conditions/operation/ConditionOperators.java",
                    "toDate; sum; add; substrBytes", "标记值直接放入 Document 或操作数数组；Lambda 路径使用美元前缀。"));
            sources.add(semanticSource("mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/Projections.java",
                    "computed; computedSearchMeta", "computed 原样接收 expression；computedSearchMeta 传入 $$SEARCH_META。"));
            sources.add(semanticSource("mongo-plus-core/src/main/java/com/mongoplus/aggregate/AggregateOptions.java",
                    "let", "契约明确使用双美元前缀访问聚合 let 变量。"));
            Map<String, Object> driver = object();
            driver.put("artifact", "org.mongodb:mongodb-driver-core:5.4.0");
            driver.put("symbols", "com.mongodb.client.model.Aggregates.GroupStage; com.mongodb.client.model.BuildersHelper.encodeValue");
            driver.put("mechanism", "GroupStage 将 id 编码到 $group._id；编码器按运行时类型委托 codec。");
            sources.add(driver);
            Map<String, Object> codec = object();
            codec.put("artifact", "org.mongodb:bson:5.4.0");
            codec.put("symbols", "org.bson.codecs.StringCodec.encode");
            codec.put("mechanism", "默认 STRING representation 调用 writer.writeString(value)，保留字符串内容。");
            sources.add(codec);
            concept.put("sourceEvidence", sources);
            return concept;
        }

        private Map<String, Object> semanticSource(String path, String symbols, String mechanism) {
            Map<String, Object> source = object();
            source.put("path", path);
            source.put("symbols", symbols);
            source.put("mechanism", mechanism);
            return source;
        }

        private String qualified(SourceType type) { return type.packageName + "." + type.name; }
    }

    private static final class SourceType {
        final String packageName; final String name; final String kind; final Map<String, String> imports;
        final Documentation doc; final Map<String, List<String>> tags;
        final boolean abstractType;
        final List<String> parents = new ArrayList<String>();
        final List<String> typeParameters = new ArrayList<String>();
        final List<String> annotations = new ArrayList<String>();
        final List<SourceMethod> methods = new ArrayList<SourceMethod>();
        final List<SourceConstructor> constructors = new ArrayList<SourceConstructor>();
        final List<Object> enumConstants = new ArrayList<Object>();
        SourceType(String packageName, String name, String kind, Map<String, String> imports,
                   Documentation doc, Map<String, List<String>> tags, boolean abstractType) {
            this.packageName = packageName; this.name = name; this.kind = kind;
            this.imports = new HashMap<String, String>(imports); this.doc = doc; this.tags = tags;
            this.abstractType = abstractType;
        }
    }
    private static final class SourceConstructor {
        final String visibility; final boolean implicit;
        final List<SourceParameter> parameters = new ArrayList<SourceParameter>();
        SourceConstructor(String visibility, boolean implicit) {
            this.visibility = visibility; this.implicit = implicit;
        }
        static SourceConstructor implicitPublicNoArg() { return new SourceConstructor("public", true); }
        String signature() {
            StringBuilder value = new StringBuilder("(");
            for (int i = 0; i < parameters.size(); i++) {
                if (i > 0) { value.append(", "); }
                SourceParameter parameter = parameters.get(i);
                value.append(parameter.varargs ? parameter.type.replaceFirst("\\[\\]$", "...") : parameter.type)
                        .append(' ').append(parameter.name);
            }
            return value.append(')').toString();
        }
    }
    private static final class SourceMethod {
        final String name; final String returnType; final List<SourceParameter> parameters = new ArrayList<SourceParameter>();
        final Set<Modifier> modifiers = new LinkedHashSet<Modifier>();
        final List<String> annotations = new ArrayList<String>();
        final List<String> typeParameters = new ArrayList<String>();
        final Map<String, List<String>> tags = new HashMap<String, List<String>>();
        Documentation doc = new Documentation(); boolean defaultMethod; boolean staticMethod; boolean deprecated;
        SourceMethod(String name, String returnType) { this.name = name; this.returnType = returnType; }
        String signature() {
            StringBuilder value = new StringBuilder(name).append('(');
            for (int i = 0; i < parameters.size(); i++) {
                if (i > 0) { value.append(", "); }
                SourceParameter parameter = parameters.get(i);
                value.append(parameter.varargs ? parameter.type.replaceFirst("\\[\\]$", "...") : parameter.type)
                        .append(' ').append(parameter.name);
            }
            return value.append(')').toString();
        }
    }
    private static final class SourceParameter {
        final String name; final String type; final boolean varargs;
        SourceParameter(String name, String type, boolean varargs) { this.name = name; this.type = type; this.varargs = varargs; }
    }
    private static final class Documentation {
        String description = ""; String returnDescription = ""; final Map<String, String> parameters = new HashMap<String, String>();
    }
    private static final class MethodRecord {
        final SourceType type; final SourceMethod method;
        MethodRecord(SourceType type, SourceMethod method) { this.type = type; this.method = method; }
    }
}
