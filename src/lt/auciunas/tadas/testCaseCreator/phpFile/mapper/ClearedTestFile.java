package lt.auciunas.tadas.testCaseCreator.phpFile.mapper;

import java.util.ArrayList;

/**
 * Mapper/bucket object for transferring data which was cleared from the existing test file,
 * and might be needed to reuse in the updated test file.
 */
public class ClearedTestFile {
    private ArrayList<String> usages = new ArrayList<>();

    public ArrayList<String> getUsages() {
        return this.usages;
    }

    public void addUsage(String dependency) {
        this.usages.add(dependency);
    }
}
