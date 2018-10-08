package lt.auciunas.tadas.testCaseCreator.phpFile.mapper;

import java.util.*;

/**
 * Mapper/bucket object necessary for transferring data which was parsed from the original file
 * and will be used for creating the test file.
 */
public class ParsedSourceFile {

    private List<Map<String, String>> dependencies = new ArrayList<>();
    private String sourceNamespace, sourceClassName;
    private Imports imports = new Imports();

    public Imports getImports() {
        return imports;
    }

    public String getSourceNamespace() {
        return sourceNamespace;
    }

    public void setSourceNamespace(String originalNamespace) {
        this.sourceNamespace = originalNamespace;
    }

    public String getSourceClassName() {
        return sourceClassName;
    }

    public void setSourceClassName(String originalClassName) {
        this.sourceClassName = originalClassName;
    }

    public List<Map<String, String>> getDependencies() {
        return dependencies;
    }

    public void addDependency(String key, String value) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(key, value);

        this.dependencies.add(map);
    }
}
