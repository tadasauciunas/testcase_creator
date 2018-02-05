package lt.auciunas.tadas.testCaseCreator.phpFile.mapper;

import java.util.*;

/**
 * Mapper/bucket object necessary for transferring data which was parsed from the original file
 * and will be used for creating the test file.
 */
public class ParsedOriginalFile {

    private ArrayList<String> usages = new ArrayList<>();
    private Map<String, List<String>> dependencies = new LinkedHashMap<>();
    private String originalNamespace, originalClassName;

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

    public Map<String, List<String>> getDependencies() {
        if (this.dependencies == null) {
            this.dependencies = new LinkedHashMap<>();
        }
        return dependencies;
    }

    public void addDependency(String key, String value) {
        List<String> values = new ArrayList<>();

        if (this.dependencies.get(key) == null) {
            values.add(value);
        } else {
            values.addAll(this.dependencies.get(key));
            values.add(value);
        }

        this.dependencies.put(key, values);
    }
}
