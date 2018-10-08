package lt.auciunas.tadas.testCaseCreator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import lt.auciunas.tadas.testCaseCreator.exceptions.FileNotSupportedException;
import lt.auciunas.tadas.testCaseCreator.phpFile.TestFileFiller;
import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.ParsedTestFile;
import lt.auciunas.tadas.testCaseCreator.phpFile.parser.OriginalFileParser;
import lt.auciunas.tadas.testCaseCreator.phpFile.parser.TestFileParser;
import lt.auciunas.tadas.testCaseCreator.phpFile.validator.FileValidator;
import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.ParsedSourceFile;
import lt.auciunas.tadas.testCaseCreator.phpFile.TestFileCreator;
import org.jetbrains.annotations.NotNull;

public class TestFileAction extends AnAction {

    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project != null) {
            VirtualFile file = event.getData(PlatformDataKeys.VIRTUAL_FILE);

            if (!isFileValid(file)) {
                return;
            }

            TestFileCreator phpTestFileCreator = new TestFileCreator(file);
            try {
                VirtualFile createdFile = phpTestFileCreator.createTestFile();

                OriginalFileParser phpParser = new OriginalFileParser(file);
                ParsedSourceFile parsedSrcFile = phpParser.parseFile();

                TestFileParser testFileParser = new TestFileParser(parsedSrcFile);
                ParsedTestFile parsedTestFile = testFileParser.parseTestFileContents();

                TestFileFiller testFileFiller = new TestFileFiller(createdFile, parsedTestFile);
                testFileFiller.clearTestCaseSetUp();
                testFileFiller.fillFile();

                new OpenFileDescriptor(project, createdFile).navigate(true);
            } catch (Exception e) {
                Messages.showMessageDialog(e.getMessage(), "Project", Messages.getInformationIcon());
            }
        } else {
            Messages.showMessageDialog("No project", "Project", Messages.getWarningIcon());
        }
    }

    private boolean isFileValid(VirtualFile file) {
        FileValidator phpFileValidator = new FileValidator(file);

        try {
            phpFileValidator.validateFile();
        } catch (FileNotSupportedException | Exception e) {
            Messages.showMessageDialog(e.getMessage(), "Project", Messages.getInformationIcon());

            return false;
        }

        return true;
    }

}