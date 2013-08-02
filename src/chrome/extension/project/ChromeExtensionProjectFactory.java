/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chrome.extension.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Gennaro
 */
@ServiceProvider(service=ProjectFactory.class)
public class ChromeExtensionProjectFactory implements ProjectFactory {

    public static final String PROJECT_FILE = "manifest.json";

    /**
     * Tests to see if the specified directory is a Chrome Extension
     * project.
     * @param projectDirectory
     * @return 
     */
    @Override
    public boolean isProject(FileObject projectDirectory) {
        return projectDirectory.getFileObject(PROJECT_FILE) != null;
    }

    @Override
    public Project loadProject(FileObject dir, ProjectState state) throws IOException {
        return isProject(dir) ? new ChromeExtensionProject(dir, state) : null;
    }

    @Override
    public void saveProject(Project prjct) throws IOException, ClassCastException {
    }
    
}
