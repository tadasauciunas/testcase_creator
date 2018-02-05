package lt.auciunas.tadas.testCaseCreator.phpFile.mapper;

import java.util.ArrayList;

/**
 * Mapper/bucket object for keeping data which will be used to create the actual test file.
 */
public class ParsedTestFile {

    private ArrayList<String> usages = new ArrayList<>(),
            dependencyInitializations = new ArrayList<>(),
            dependencyDefinitions = new ArrayList<>();
    private String originalNamespace, testFileClassDefinition, originalClassDefinition,
            originalClassInitialization;

    public ArrayList<String> getUsages() {
        return usages;
    }

    public void setUsages(ArrayList<String> usages) {
        this.usages = usages;
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

    public String getTestFileClassDefinition() {
        return testFileClassDefinition;
    }

    public void setTestFileClassDefinition(String testFileClassDefinition) {
        this.testFileClassDefinition = testFileClassDefinition;
    }

    public ArrayList<String> getDependencyDefinitions() {
        return dependencyDefinitions;
    }

    public void addDependencyDefinition(String dependencyDefinitions) {
        if (this.dependencyDefinitions == null) {
            this.dependencyDefinitions = new ArrayList<>();
        }
        this.dependencyDefinitions.add(dependencyDefinitions);
    }

    public ArrayList<String> getDependencyInitializations() {
        return dependencyInitializations;
    }

    public void addDependencyInitialization(String dependencyInitialization) {
        if (this.dependencyInitializations == null) {
            this.dependencyInitializations = new ArrayList<>();
        }
        this.dependencyInitializations.add(dependencyInitialization);
    }

    public void setOriginalClassDefinition(String originalClassDefinition) {
        this.originalClassDefinition = originalClassDefinition;
    }

    public String getOriginalClassDefinition() {
        return this.originalClassDefinition;
    }

    public void setOriginalClassInitialization(String originalClassInitialization) {
        this.originalClassInitialization = originalClassInitialization;
    }

    public String getOriginalClassInitialization() {
        return this.originalClassInitialization;
    }
}
