package com.mongoplus.indexer;

import com.mongoplus.indexer.json.JsonWriter;
import com.mongoplus.indexer.model.MongoPlusApiIndex;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** 仅消费正式 Index 的变量身份及作用域契约；不构造表达式 API，也不连接 MongoDB。 */
public final class VariableBindingScopeSelfTest {
    private static final String CAPABILITY = "VARIABLE_BINDING_SCOPE_V1";
    private static final String SIGNATURE =
            "lookup(String from, List<Variable<TExpression>> letList, Aggregate<?> aggregate, String as)";
    private static int cases;

    private VariableBindingScopeSelfTest() { }

    public static void main(String[] args) throws Exception {
        MongoPlusIndexerConfig config = MongoPlusIndexerConfig.forPipelineProject(Path.of(args[0])).build();
        MongoPlusIndexer generator = new MongoPlusIndexer(config);
        MongoPlusApiIndex index = generator.generate();
        require(((List<?>) index.asMap().get("requiredCapabilities")).contains(CAPABILITY), "缺少正式 scope capability");
        Map<?, ?> target = index.getMethodFamilies().stream().map(f -> (Map<?, ?>) f)
                .flatMap(f -> ((List<?>) f.get("overloads")).stream())
                .map(m -> (Map<?, ?>) m).filter(m -> SIGNATURE.equals(m.get("signature"))).findFirst().orElseThrow();
        verify(index, target);
        require(json(index).equals(json(generator.generate())), "两次正式生成一致");
        fixtures(config.getConstructionArtifactRepository());
        System.out.println("VariableBindingScopeSelfTest PASSED: " + cases
                + " assertions; BOUND_VARIABLE; UNBOUND_VARIABLE; nearest shadowing; restored parent; "
                + "parent initializers; inherited body; outer/sibling isolation; explicit catalogs; deterministic");
    }

    private static void verify(MongoPlusApiIndex index, Map<?, ?> target) {
        Map<?, ?> rules = concept(index, CAPABILITY);
        Map<?, ?> expression = concept(index, "PIPELINE_EXPRESSION_FIELD_REFERENCE");
        EvidenceConsumer consumer = new EvidenceConsumer(rules, contract(expression, "variableReference"));
        Map<?, ?> scope = contract(target, "variableScope");
        Map<?, ?> definitions = parameter(target, (String) contract(scope, "declarationOwner").get("parameter"));
        Map<?, ?> body = parameter(target, (String) contract(scope, "body").get("parameter"));
        require(contract(definitions, "variableDeclaration").get("scopeRef").equals(
                contract(body, "variableEnvironment").get("scopeRef")), "声明与 body 指向同一 scope");
        require("ENTRY_KEY".equals(contract(contract(definitions, "variableDeclaration"), "declarationName")
                .get("source")), "复用 ENTRY_KEY 名称");
        List<?> roles = (List<?>) contract(contract(definitions, "entryConstruction"), "constructor").get("parameters");
        require(roles.stream().map(r -> (Map<?, ?>) r).anyMatch(r -> "ENTRY_KEY".equals(r.get("input"))
                && "VARIABLE_NAME".equals(r.get("semanticType"))), "复用正式 key 语义");
        Scope root = consumer.root(true, true, true, true, List.of(), List.of());
        LinkedHashMap<String, String> entries = new LinkedHashMap<>();
        entries.put("userId", "$userId");
        Map<?, ?> field = contract(expression, "fieldReference");
        require(entries.get("userId").startsWith((String) field.get("prefix"))
                && !entries.get("userId").startsWith((String) field.get("excludedPrefix")), "复用字段引用表示");
        Scope outer = consumer.enter(scope, "/0", root, entries);
        Map<String, Object> outerId = outer.declarations.get("userId");
        Binding bound = consumer.resolve(outer, "$$userId");
        require("BOUND_VARIABLE".equals(bound.status) && outerId.equals(bound.identity), "正常绑定到 declaration identity");
        Binding dotted = consumer.resolve(outer, "$$userId.name");
        require("userId".equals(dotted.name) && "name".equals(dotted.path)
                && outerId.equals(dotted.identity), "referenceName 与 accessPath 分离，绑定身份不变");
        require(consumer.resolve(outer, "$$userId.name.first").path.equals("name.first"), "完整保留 accessPath");
        require("UNBOUND_VARIABLE".equals(consumer.resolve(outer, "$$missing").status), "完整环境正式未绑定");
        require("UNBOUND_VARIABLE".equals(consumer.resolve(root, "$$userId").status), "outer 不可见");
        require("UNBOUND_VARIABLE".equals(consumer.resolve(
                consumer.enter(scope, "/1", root, Map.of()), "$$userId").status), "sibling 不可见");
        Map<?, ?> inheritedEdge = index.getMethodFamilies().stream().map(f -> (Map<?, ?>) f)
                .flatMap(f -> ((List<?>) f.get("overloads")).stream()).map(m -> (Map<?, ?>) m)
                .flatMap(m -> ((List<?>) m.get("parameters")).stream()).map(p -> (Map<?, ?>) p)
                .filter(p -> p.get("variableEnvironment") instanceof Map)
                .map(p -> (Map<?, ?>) p.get("variableEnvironment"))
                .filter(e -> "ENCLOSING_ENVIRONMENT".equals(e.get("source"))).findFirst().orElseThrow();
        Scope inherited = consumer.inherit(inheritedEdge, outer);
        require(outerId.equals(consumer.resolve(inherited, "$$userId").identity), "嵌套 body 继承父声明");
        Scope inner = consumer.enter(scope, "/0/body/1", outer, Map.of("userId", "$$userId"));
        Map<String, Object> innerId = inner.declarations.get("userId");
        require(!outerId.equals(innerId) && innerId.equals(consumer.resolve(inner, "$$userId").identity), "最近声明 shadowing");
        require(outerId.equals(consumer.resolve(consumer.initializer(scope, inner), "$$userId").identity),
                "inner initializer 使用父 scope");
        require("UNBOUND_VARIABLE".equals(consumer.resolve(consumer.initializer(scope, outer), "$$userId").status),
                "initializer 不能引用自身声明");
        require(outerId.equals(consumer.resolve(consumer.exit(scope, inner), "$$userId").identity), "退出 inner 恢复 outer");
        require(consumer.enter(scope, "/0", root, entries).declarations.get("userId").equals(outerId), "同一结构身份稳定");
        require(!consumer.enter(scope, "/2", root, entries).declarations.get("userId").equals(outerId), "复用 body 不混淆调用身份");
        LinkedHashMap<String, String> dependentEntries = new LinkedHashMap<>(entries);
        dependentEntries.put("second", "$$userId");
        Scope simultaneous = consumer.enter(scope, "/3", root, dependentEntries);
        require("UNBOUND_VARIABLE".equals(consumer.resolve(consumer.initializer(scope, simultaneous), "$$userId").status),
                "后续 initializer 也不可见同一 declaration scope 内的早期条目");
        require(Integer.valueOf(1).equals(simultaneous.declarations.get("second").get("entryOrdinal")), "输入顺序定位声明身份");
        for (int flag = 0; flag < 4; flag++) {
            Scope incomplete = consumer.root(flag != 0, flag != 1, flag != 2, flag != 3, List.of(), List.of());
            require("INCOMPLETE_VARIABLE_ENVIRONMENT".equals(consumer.resolve(
                    consumer.enter(scope, "/0", incomplete, entries), "$$missing").status), "不完整环境不能伪造未绑定");
        }
        require("UNBOUND_VARIABLE".equals(consumer.resolve(root, "$$SEARCH_META").status), "sourceExample 不是 system 声明");
        Scope missingProof = consumer.root(true, true, true, true, List.of(), List.of());
        missingProof.evidence.remove("sourceEvidence");
        require("INCOMPLETE_VARIABLE_ENVIRONMENT".equals(consumer.resolve(missingProof, "$$missing").status),
                "只有 complete 标志、没有正式来源仍不能断言未绑定");
        Scope explicit = consumer.root(true, true, true, true, List.of("externalName"), List.of("SYSTEM_FIXTURE"));
        require("BOUND_VARIABLE".equals(consumer.resolve(explicit, "$$externalName").status), "external 必须显式声明");
        require("BOUND_VARIABLE".equals(consumer.resolve(explicit, "$$SYSTEM_FIXTURE").status), "system 必须显式声明");
        for (String invalid : List.of("$$", "$$.name", "$$userId.")) {
            require("INVALID_VARIABLE_REFERENCE".equals(consumer.resolve(outer, invalid).status), "空名称/路径拒绝");
        }
        Map<?, ?> typedView = index.list("types").stream().map(t -> (Map<?, ?>) t)
                .flatMap(t -> ((List<?>) t.get("publicMethods")).stream()).map(m -> (Map<?, ?>) m)
                .filter(m -> target.get("declaredIn").equals(m.get("declaredIn"))
                        && target.get("signature").equals(m.get("signature"))).findFirst().orElseThrow();
        require(scope.equals(typedView.get("variableScope")), "两个正式视图一致");
    }

    /** 无关 Stage/API/参数/字段名仍使用同一契约；删标签或缺依赖时拒绝，类标签不传播。 */
    private static void fixtures(Path repository) throws Exception {
        Path root = Files.createTempDirectory("variable-scope-fixture-");
        try {
            String entry = " * @mongoParam declarations VARIABLE_DEFINITION ELEMENT\n"
                    + " * @mongoEntryConstruction declarations constructor=com.mongodb.client.model.Variable"
                    + " artifact=org.mongodb:mongodb-driver-core:5.4.0 key=0 value=1 keySemantic=VARIABLE_NAME"
                    + " valueSemantic=PIPELINE_EXPRESSION result=VARIABLE_DEFINITION\n"
                    + " * @mongoEntryConstructionSource declarations artifact=org.mongodb:mongodb-driver-core:5.4.0"
                    + " symbols=constructor mechanism=Fixture audited constructor\n"
                    + " * @mongoTypedContainer declarations input=DOCUMENT_ENTRIES order=INPUT target=java.util.List\n"
                    + " * @mongoObjectField declarations field=definitions\n"
                    + " * @mongoObjectFieldSource declarations path=fixture symbols=gather mechanism=Fixture entries\n"
                    + " * @mongoParam branch PIPELINE VALUE\n"
                    + " * @mongoObjectField branch field=sequence\n"
                    + " * @mongoObjectFieldSource branch path=fixture symbols=gather mechanism=Fixture body\n";
            String scope = " * @mongoVariableScope declarations=declarations body=branch parent=ENCLOSING"
                    + " initializer=PARENT inheritance=LEXICAL shadowing=NEAREST exit=RESTORE_PARENT\n";
            String source = " * @mongoVariableScopeSource reference=fixture:scope symbols=gather"
                    + " mechanism=Explicit fixture lexical semantics\n";
            String fixture = "package com.mongoplus.aggregate; public interface Aggregate<C> {\n/**\n"
                    + " * @mongoStage $opaque\n" + entry + scope + source + " */\n"
                    + "<Renamed> C gather(java.util.List<com.mongodb.client.model.Variable<Renamed>> declarations,"
                    + "java.util.List<? extends org.bson.conversions.Bson> branch);\n}";
            String environment = " * @mongoVariableEnvironment branch source=ENCLOSING inheritance=LEXICAL exit=RESTORE_PARENT\n";
            String inherited = "/**\n * @mongoStage $route\n * @mongoParam branch PIPELINE VALUE\n"
                    + " * @mongoObjectField branch field=sequence\n"
                    + " * @mongoObjectFieldSource branch path=fixture symbols=descend mechanism=Fixture body\n"
                    + environment + source + " */\nC descend(java.util.List<? extends org.bson.conversions.Bson> branch);\n";
            String withInherited = fixture.replace("\n}", "\n" + inherited + "}");
            MongoPlusApiIndex index = generate(root, withInherited, repository);
            Map<?, ?> family = index.getMethodFamilies().stream().map(f -> (Map<?, ?>) f)
                    .filter(f -> "gather".equals(f.get("name"))).findFirst().orElseThrow();
            Map<?, ?> method = (Map<?, ?>) ((List<?>) family.get("overloads")).get(0);
            verify(index, method);
            for (String broken : List.of(fixture.replace(scope, ""), fixture.replace(source, ""),
                    fixture.replace("initializer=PARENT", "initializer=SELF"),
                    fixture.replace("body=branch", "body=absent"),
                    fixture.replace("declarations=declarations", "declarations=branch"),
                    fixture.replace("parent=ENCLOSING", "parent=GLOBAL"),
                    fixture.replace("shadowing=NEAREST", "shadowing=FIRST"),
                    fixture.replace("inheritance=LEXICAL", "inheritance=ALL"),
                    fixture.replace("exit=RESTORE_PARENT", "exit=EXPORT"),
                    fixture.replace(scope, scope + scope),
                    fixture.replace("keySemantic=VARIABLE_NAME", "keySemantic=OUTPUT_FIELD_NAME"),
                    fixture.replace(" * @mongoObjectField branch field=sequence\n", "")
                            .replace(" * @mongoObjectFieldSource branch path=fixture symbols=gather mechanism=Fixture body\n", ""))) {
                boolean rejected = false;
                try { generate(root, broken, repository); } catch (IllegalArgumentException expected) { rejected = true; }
                require(rejected, "非法/不完整 scope 标签必须拒绝");
            }
            String untagged = fixture.replace(scope + source, "");
            MongoPlusApiIndex absent = generate(root, untagged, repository);
            require(!((List<?>) absent.asMap().get("requiredCapabilities")).contains(CAPABILITY), "无标签不注入 scope capability");
            String classOnly = untagged.replace("public interface", "/**\n" + scope + source + " */\npublic interface");
            require(!((List<?>) generate(root, classOnly, repository).asMap().get("requiredCapabilities")).contains(CAPABILITY),
                    "类标签不传播");
            for (String broken : List.of(withInherited.replace(environment, environment + environment),
                    withInherited.replace("source=ENCLOSING", "source=GLOBAL"),
                    withInherited.replace(" * @mongoParam branch PIPELINE VALUE\n", ""),
                    withInherited.replace(scope, scope + environment))) {
                boolean rejected = false;
                try { generate(root, broken, repository); } catch (IllegalArgumentException expected) { rejected = true; }
                require(rejected, "继承 environment 缺证据、冲突或重复时拒绝");
            }
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path file : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(file); }
            }
        }
    }

    private static MongoPlusApiIndex generate(Path root, String source, Path repository) throws Exception {
        Path file = root.resolve("com/mongoplus/aggregate/Aggregate.java");
        Files.createDirectories(file.getParent());
        Files.writeString(file, source);
        return new MongoPlusIndexer(MongoPlusIndexerConfig.builder().addSourceRoot(root).pipeline(true)
                .constructionArtifactRepository(repository).build()).generate();
    }

    /** 测试消费者按 Index 规则解释引用和环境；正式生成器没有输入 pipeline 的绑定执行职责。 */
    private static final class EvidenceConsumer {
        private final Map<?, ?> rules;
        private final Map<?, ?> reference;

        private EvidenceConsumer(Map<?, ?> rules, Map<?, ?> reference) {
            this.rules = rules;
            this.reference = reference;
            require("VARIABLE_REFERENCE".equals(reference.get("semanticType")), "正式引用角色");
            require(CAPABILITY.equals(reference.get("bindingContractRef")), "引用关联绑定契约");
            require("NEAREST_SCOPE_THEN_PARENT".equals(contract(rules, "binding").get("lookup")), "正式查找次序");
        }

        private Scope root(boolean graph, boolean lexical, boolean external, boolean system, List<String> externalNames,
                           List<String> systemNames) {
            Map<?, ?> environment = contract(rules, "environmentEvidence");
            require("EXPLICIT_ONLY".equals(environment.get("externalVariables"))
                    && "EXPLICIT_ONLY".equals(environment.get("systemVariables"))
                    && Boolean.FALSE.equals(environment.get("implicitSystemCatalog")), "不注入 external/system");
            Scope root = new Scope(null, completeness(graph, lexical, external, system, "TEST_INPUT_ENVIRONMENT"));
            for (String name : externalNames) { root.declarations.put(name, catalog("EXTERNAL_VARIABLE", name)); }
            for (String name : systemNames) {
                require(!root.declarations.containsKey(name), "同 scope 名称必须唯一");
                root.declarations.put(name, catalog("SYSTEM_VARIABLE", name));
            }
            return root;
        }

        private Map<String, Object> catalog(String kind, String name) {
            Map<String, Object> identity = new LinkedHashMap<>();
            identity.put("kind", kind);
            identity.put("declarationName", name);
            Map<String, Object> declaration = Map.of("declarationName", name, "declarationIdentity", identity,
                    "sourceEvidence", Map.of("source", "EXPLICIT_TEST_CATALOG"));
            for (Object key : (List<?>) contract(rules, "environmentEvidence").get("catalogEntryFields")) {
                require(declaration.containsKey(key), "external/system 条目具备正式来源与身份字段");
            }
            return identity;
        }

        private Map<String, Object> completeness(boolean graph, boolean declarations, boolean external,
                                                 boolean system, String source) {
            Map<String, Object> evidence = new LinkedHashMap<>();
            evidence.put("scopeGraphComplete", graph);
            evidence.put("declarationsComplete", declarations);
            evidence.put("externalCatalogComplete", external);
            evidence.put("systemCatalogComplete", system);
            Map<String, Object> proofs = new LinkedHashMap<>();
            for (Object key : (List<?>) contract(rules, "environmentEvidence").get("completenessFields")) {
                proofs.put(key.toString(), Map.of("source", source));
            }
            evidence.put("sourceEvidence", proofs);
            return evidence;
        }

        private boolean complete(Scope environment) {
            if (!(environment.evidence.get("sourceEvidence") instanceof Map<?, ?> sources)) { return false; }
            for (Object key : (List<?>) contract(rules, "environmentEvidence").get("completenessFields")) {
                if (!Boolean.TRUE.equals(environment.evidence.get(key)) || !(sources.get(key) instanceof Map<?, ?> proof)
                        || !proof.containsKey("source")) { return false; }
            }
            return true;
        }

        private Scope inherit(Map<?, ?> edge, Scope parent) {
            require("INHERIT_ENVIRONMENT".equals(edge.get("operation"))
                    && "ENCLOSING_ENVIRONMENT".equals(edge.get("source"))
                    && "LEXICAL_PARENT_CHAIN".equals(edge.get("inheritance"))
                    && "RESTORE_PARENT".equals(edge.get("scopeExit"))
                    && edge.get("sourceEvidence") instanceof List<?>, "继承边必须来自正式声明");
            return new Scope(parent, completeness(true, true, true, true, "FORMAL_INHERITANCE_EDGE"));
        }

        private Scope enter(Map<?, ?> scope, String ownerPath, Scope parent, Map<String, String> entries) {
            require("ENCLOSING_ENVIRONMENT".equals(contract(scope, "parentScope").get("source")), "正式 parent 来源");
            require("LEXICAL_PARENT_CHAIN".equals(contract(scope, "inheritance").get("rule")), "正式 inheritance");
            require("DECLARATION_SCOPE".equals(contract(scope, "body").get("environment")), "body 使用新 scope");
            Map<?, ?> owner = contract(scope, "declarationOwner");
            Map<?, ?> identityRule = contract(contract(scope, "declaration"), "declarationIdentity");
            Map<String, Object> values = Map.of("apiRef", owner.get("apiRef"), "ownerNodePath", ownerPath,
                    "declarationParameter", owner.get("parameter"));
            Scope result = new Scope(parent, completeness(true, true, true, true, "FORMAL_SCOPE_CONTRACT"));
            int ordinal = 0;
            for (String name : entries.keySet()) {
                Map<String, Object> identity = new LinkedHashMap<>();
                for (Object component : (List<?>) identityRule.get("components")) {
                    String key = component.toString();
                    identity.put(key, "entryOrdinal".equals(key) ? ordinal : values.get(key));
                    require(identity.get(key) != null, "身份组件必须来自正式结构");
                }
                result.declarations.put(name, identity);
                ordinal++;
            }
            return result;
        }

        private Scope initializer(Map<?, ?> scope, Scope entered) {
            require("PARENT_SCOPE".equals(contract(scope, "initializerEnvironment").get("source"))
                    && Boolean.FALSE.equals(contract(scope, "initializerEnvironment").get("currentDeclarationsVisible")),
                    "initializer 环境排除当前所有声明");
            return entered.parent;
        }

        private Scope exit(Map<?, ?> scope, Scope entered) {
            require("RESTORE_PARENT".equals(contract(scope, "scopeExit").get("operation"))
                    && Boolean.FALSE.equals(contract(scope, "scopeExit").get("exportDeclarations")), "退出不泄漏声明");
            return entered.parent;
        }

        private Binding resolve(Scope environment, String input) {
            Map<?, ?> extraction = contract(reference, "nameExtraction");
            require("REMOVE_PREFIX_SPLIT_FIRST".equals(extraction.get("operation"))
                    && Boolean.FALSE.equals(extraction.get("trim")), "正式名称提取规则");
            String prefix = (String) reference.get("prefix");
            String delimiter = (String) extraction.get("delimiter");
            if (!input.startsWith(prefix)) { return new Binding("INVALID_VARIABLE_REFERENCE", null, null, null); }
            String suffix = input.substring(prefix.length());
            int split = suffix.indexOf(delimiter);
            String name = split < 0 ? suffix : suffix.substring(0, split);
            String path = split < 0 ? null : suffix.substring(split + delimiter.length());
            if (name.isEmpty() || path != null && path.isEmpty()) {
                return new Binding((String) extraction.get("invalidResult"), name, path, null);
            }
            boolean complete = true;
            for (Scope cursor = environment; cursor != null; cursor = cursor.parent) {
                complete &= complete(cursor);
                if (cursor.declarations.containsKey(name)) {
                    return new Binding(complete ? (String) contract(rules, "binding").get("boundResult")
                            : (String) contract(rules, "binding").get("incompleteResult"), name, path,
                            complete ? cursor.declarations.get(name) : null);
                }
            }
            return new Binding((String) contract(rules, "binding").get(complete ? "unboundResult" : "incompleteResult"),
                    name, path, null);
        }
    }

    private static final class Scope {
        private final Scope parent;
        private final Map<String, Object> evidence;
        private final Map<String, Map<String, Object>> declarations = new LinkedHashMap<>();

        private Scope(Scope parent, Map<String, Object> evidence) { this.parent = parent; this.evidence = evidence; }
    }

    private record Binding(String status, String name, String path, Map<String, Object> identity) { }

    private static Map<?, ?> parameter(Map<?, ?> method, String name) {
        return ((List<?>) method.get("parameters")).stream().map(p -> (Map<?, ?>) p)
                .filter(p -> name.equals(p.get("name"))).findFirst().orElseThrow();
    }

    private static Map<?, ?> concept(MongoPlusApiIndex index, String id) {
        return index.list("concepts").stream().map(c -> (Map<?, ?>) c)
                .filter(c -> id.equals(c.get("id"))).findFirst().orElseThrow();
    }

    private static Map<?, ?> contract(Map<?, ?> value, String key) {
        require(value.get(key) instanceof Map, "缺少正式契约: " + key);
        return (Map<?, ?>) value.get(key);
    }

    private static String json(MongoPlusApiIndex index) throws Exception {
        StringWriter writer = new StringWriter();
        JsonWriter.write(index.asMap(), writer);
        return writer.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
        cases++;
    }
}
