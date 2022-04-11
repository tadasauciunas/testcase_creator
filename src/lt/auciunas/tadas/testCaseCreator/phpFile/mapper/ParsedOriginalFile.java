package lt.auciunas.tadas.testCaseCreator.phpFile.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParsedOriginalFile {

    private ArrayList<String> usages = new ArrayList<>();
    private Map<String, List<String>> dependencies = new LinkedHashMap<>();
    private String originalNamespace, originalClassName;

    public ArrayList<String> getUsages() {
        return this.usages;
    }

    public void addUsage(String usage) {
        this.usages.add(usage);
    }

    public String getOriginalNamespace() {
        return this.originalNamespace;
    }

    public void setOriginalNamespace(String originalNamespace) {
        this.originalNamespace = originalNamespace;
    }

    public String getOriginalClassName() {
        return this.originalClassName;
    }

    public void setOriginalClassName(String originalClassName) {
        this.originalClassName = originalClassName;
    }

    public Map<String, List<String>> getDependencies() {
        if (this.dependencies == null) {
            this.dependencies = new LinkedHashMap<>();
        }

        return this.dependencies;
    }

    public void addDependency(String key, String value) {
        List<String> values = new ArrayList<>();

        if (this.dependencies.get(key) == null) {
            values.add(value);
        } else {
            values.addAll((Collection)this.dependencies.get(key));
            values.add(value);
        }

        this.dependencies.put(key, values);
    }
}
