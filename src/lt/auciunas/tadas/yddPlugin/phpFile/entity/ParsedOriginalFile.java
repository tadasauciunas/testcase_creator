package lt.auciunas.tadas.yddPlugin.phpFile.entity;

import java.util.*;

public class ParsedOriginalFile {

    private ArrayList<String> usages = new ArrayList<>();
    private Map<String, String> dependencies = new LinkedHashMap<>();
    private String originalNamespace, testFileClassDefinition, originalClassName;

    public ArrayList<String> getUsages() {
        return usages;
    }

    public void addUsage(String usage) {
        this.usages.add(usage);
    }

    public String getOriginalNamespace() {
        return originalNamespace;
    }

    public void setOriginalNamespace(String originalNamespace) {
        this.originalNamespace = originalNamespace;
    }

    public String getOriginalClassName() {
        return originalClassName;
    }

    public void setOriginalClassName(String originalClassName) {
        this.originalClassName = originalClassName;
    }

    public String getTestFileClassDefinition() {
        return testFileClassDefinition;
    }

    public void setTestFileClassDefinition(String testFileClassDefinition) {
        this.testFileClassDefinition = testFileClassDefinition;
    }

    public Map<String, String> getDependencies() {
        if (this.dependencies == null) {
            this.dependencies = new LinkedHashMap<>();
        }
        return dependencies;
    }

    public void addDependency(String key, String value) {
        this.dependencies.put(key, value);
    }
}
