import com.sun.source.tree.*;
import com.sun.source.util.*;
import javax.lang.model.element.Modifier;
import javax.tools.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.*;

/** Read-only source inventory using the JDK parser; no dependency resolution or source writes. */
public class SourceInventory {
    static Path root;
    static final List<Object> files = new ArrayList<>(), types = new ArrayList<>();
    static Map<String,Object> map(Object... pairs) {
        Map<String,Object> m = new LinkedHashMap<>();
        for(int i=0;i<pairs.length;i+=2)m.put((String)pairs[i],pairs[i+1]);
        return m;
    }
    static String json(Object o) {
        if(o==null)return "null";
        if(o instanceof Number || o instanceof Boolean)return o.toString();
        if(o instanceof Map){List<String>x=new ArrayList<>();((Map<?,?>)o).forEach((k,v)->x.add(json(k)+":"+json(v)));return "{"+String.join(",",x)+"}";}
        if(o instanceof Iterable){List<String>x=new ArrayList<>();for(Object v:(Iterable<?>)o)x.add(json(v));return "["+String.join(",",x)+"]";}
        StringBuilder b=new StringBuilder("\"");
        for(char c:o.toString().toCharArray())switch(c){case '\\':b.append("\\\\");break;case '"':b.append("\\\"");break;case '\n':b.append("\\n");break;case '\r':b.append("\\r");break;case '\t':b.append("\\t");break;default:if(c<32)b.append(String.format("\\u%04x",(int)c));else b.append(c);}
        return b.append('"').toString();
    }
    static List<String> annotations(ModifiersTree m){return m.getAnnotations().stream().map(Object::toString).collect(Collectors.toList());}
    static String doc(Object d){
        if(d==null)return "null";
        String s=d.toString();
        int at=s.indexOf("@mongo");if(at>=0)s=s.substring(0,at);
        return s;
    }
    public static void main(String[] args)throws Exception{
        root=Paths.get(args[0]).toAbsolutePath().normalize();
        List<Path> sources=new ArrayList<>();
        try(Stream<Path> dirs=Files.list(root)){
            for(Path d:dirs.filter(Files::isDirectory).sorted().collect(Collectors.toList())){
                if(!d.getFileName().toString().startsWith("mongo-plus-"))continue;
                Path main=d.resolve("src/main/java");
                if(Files.isDirectory(main))try(Stream<Path>s=Files.walk(main)){sources.addAll(s.filter(p->p.toString().endsWith(".java")).sorted().collect(Collectors.toList()));}
            }
        }
        Path legacyTool=root.resolve("tools/mongoplus-api-indexer/src/main/java");
        if(Files.isDirectory(legacyTool))try(Stream<Path>s=Files.walk(legacyTool)){sources.addAll(s.filter(p->p.toString().endsWith(".java")).sorted().collect(Collectors.toList()));}
        JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
        StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,StandardCharsets.UTF_8);
        JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none"),null,fm.getJavaFileObjectsFromPaths(sources));
        Trees trees=Trees.instance(task); DocTrees docs=DocTrees.instance(task);
        for(CompilationUnitTree unit:task.parse()){
            Path p=Paths.get(unit.getSourceFile().toUri());
            String rel=root.relativize(p).toString().replace('\\','/');
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(p));
            StringBuilder hex=new StringBuilder();for(byte x:digest)hex.append(String.format("%02x",x));
            files.add(map("path",rel,"sha256",hex.toString()));
            new TreePathScanner<Void,Void>() {
                Deque<String> owners=new ArrayDeque<>(); Deque<Boolean> visible=new ArrayDeque<>();
                @Override public Void visitClass(ClassTree t,Void v){
                    if(t.getSimpleName().length()==0)return null;
                    boolean parent=visible.isEmpty()||visible.peek();
                    boolean pub=t.getModifiers().getFlags().contains(Modifier.PUBLIC)||(getCurrentPath().getParentPath().getLeaf() instanceof ClassTree && ((ClassTree)getCurrentPath().getParentPath().getLeaf()).getKind()==Tree.Kind.INTERFACE);
                    String name=(owners.isEmpty()?unit.getPackageName().toString():owners.peek())+"."+t.getSimpleName();
                    owners.push(name);visible.push(pub&&parent);
                    if(pub&&parent){
                        long start=trees.getSourcePositions().getStartPosition(unit,t);
                        List<Object> methods=new ArrayList<>(),fields=new ArrayList<>();
                        boolean iface=t.getKind()==Tree.Kind.INTERFACE||t.getKind()==Tree.Kind.ANNOTATION_TYPE;
                        for(Tree member:t.getMembers()){
                            if(member instanceof MethodTree){MethodTree m=(MethodTree)member;
                                if(!(m.getModifiers().getFlags().contains(Modifier.PUBLIC)||(iface&&!m.getModifiers().getFlags().contains(Modifier.PRIVATE))))continue;
                                long pos=trees.getSourcePositions().getStartPosition(unit,m);
                                TreePath mp=new TreePath(getCurrentPath(),m);
                                methods.add(map("name",m.getName().toString(),"return",String.valueOf(m.getReturnType()),"parameters",m.getParameters().stream().map(Object::toString).collect(Collectors.toList()),"typeParameters",m.getTypeParameters().stream().map(Object::toString).collect(Collectors.toList()),"default",m.getDefaultValue()==null?null:m.getDefaultValue().toString(),"annotations",annotations(m.getModifiers()),"modifiers",m.getModifiers().getFlags().stream().map(Object::toString).collect(Collectors.toList()),"line",unit.getLineMap().getLineNumber(pos),"javadoc",doc(docs.getDocCommentTree(mp))));
                            }else if(member instanceof VariableTree){VariableTree f=(VariableTree)member;
                                if(f.getModifiers().getFlags().contains(Modifier.PUBLIC)||iface||t.getKind()==Tree.Kind.ENUM)fields.add(map("name",f.getName().toString(),"type",String.valueOf(f.getType()),"initial",f.getInitializer()==null?null:f.getInitializer().toString(),"annotations",annotations(f.getModifiers())));
                            }
                        }
                        types.add(map("name",name,"simpleName",t.getSimpleName().toString(),"kind",t.getKind().toString(),"module",rel.substring(0,rel.indexOf("/src/")),"path",rel,"line",unit.getLineMap().getLineNumber(start),"annotations",annotations(t.getModifiers()),"modifiers",t.getModifiers().getFlags().stream().map(Object::toString).collect(Collectors.toList()),"imports",unit.getImports().stream().map(Object::toString).collect(Collectors.toList()),"typeParameters",t.getTypeParameters().stream().map(Object::toString).collect(Collectors.toList()),"extends",String.valueOf(t.getExtendsClause()),"implements",t.getImplementsClause().stream().map(Object::toString).collect(Collectors.toList()),"methods",methods,"fields",fields,"javadoc",doc(docs.getDocCommentTree(getCurrentPath()))));
                    }
                    super.visitClass(t,v);owners.pop();visible.pop();return null;
                }
            }.scan(unit,null);
        }
        List<String> errors=diagnostics.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).map(Object::toString).collect(Collectors.toList());
        if(!errors.isEmpty())throw new IllegalStateException(errors.toString());
        Files.write(Paths.get(args[1]),json(map("parser","JDK Compiler Tree API, parse-only, public visibility including interface members","files",files,"types",types,"parseErrors",errors)).getBytes(StandardCharsets.UTF_8));
        System.out.println("Scanned "+files.size()+" source files; "+types.size()+" public types; annotations "+types.stream().map(x->(Map<?,?>)x).filter(x->x.get("kind").equals("ANNOTATION_TYPE")).count());
        fm.close();
    }
}
