package chrome.extension.project;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Gennaro
 */
public class ChromeExtensionProject implements Project {
    private final FileObject projectDir;
    private Lookup lkp;

    public ChromeExtensionProject(FileObject projectDir) {
        this.projectDir = projectDir;
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    @Override
    public Lookup getLookup() {
        if(lkp == null) {
            lkp = Lookups.fixed(new Object[] {
                this,
                new Info(),
                new ChromeExtensionProjectLogicalView(this),
                new ChromeExtensionActionProvider(),
                new ChromeExtensionProjectCopyOperation(),
                new ChromeExtensionProjectDeleteOperation(),
                new ChromeExtensionProjectMoveOrRenameOperation()
            });
        }
        return lkp;
    }

    private final class Info implements ProjectInformation {

        @StaticResource()
        public static final String CUSTOMER_ICON = "chrome/extension/project/icon.png";

        @Override
        public String getName() {
            return getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public Icon getIcon() {
            return new ImageIcon(ChromeExtensionProject.class.getResource("icon.png"));

            // new ImageIcon(CUSTOMER_ICON) does not display the image when selecting files.
//            return new ImageIcon(CUSTOMER_ICON);
        }

        @Override
        public Project getProject() {
            return ChromeExtensionProject.this;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pl) {
            // Do nothing, won't change.
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pl) {
            // Do nothing, won't change.
        }
    }

    class ChromeExtensionProjectLogicalView implements LogicalViewProvider {

        @StaticResource()
        public static final String CUSTOMER_ICON = "chrome/extension/project/icon.png";

        private final ChromeExtensionProject project;

        public ChromeExtensionProjectLogicalView(ChromeExtensionProject project) {
            this.project = project;
        }

        @Override
        public Node createLogicalView() {
            try {
                //Obtain the project directory's node:
                FileObject projectDirectory = project.getProjectDirectory();
                DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
                Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
                //Decorate the project directory's node:
                return new ProjectNode(nodeOfProjectFolder, project);
            }
            catch (DataObjectNotFoundException donfe) {
                Exceptions.printStackTrace(donfe);
                //Fallback-the directory couldn't be created -
                //read-only filesystem or something evil happened
                return new AbstractNode(Children.LEAF);
            }
        }

        private final class ProjectNode extends FilterNode {

            final ChromeExtensionProject project;

            public ProjectNode(Node node, ChromeExtensionProject project) throws DataObjectNotFoundException {
                super(node,
                        new FilterNode.Children(node),
                        new ProxyLookup(
                        new Lookup[] {
                            Lookups.singleton(project),
                            node.getLookup()
                        }));
                this.project = project;
            }

            @Override
            public Action[] getActions(boolean context) {
                return new Action[] {
                    CommonProjectActions.newFileAction(),
                    CommonProjectActions.renameProjectAction(),
                    CommonProjectActions.moveProjectAction(),
                    CommonProjectActions.copyProjectAction(),
                    CommonProjectActions.deleteProjectAction(),
                    CommonProjectActions.closeProjectAction()
                };
            }

            @Override
            public Image getIcon(int type) {
                return ImageUtilities.loadImage(CUSTOMER_ICON);
            }

            @Override
            public Image getOpenedIcon(int type) {
                return getIcon(type);
            }

            @Override
            public String getDisplayName() {
                return project.getProjectDirectory().getName();
            }
        }

        @Override
        public Node findPath(Node node, Object o) {
            // unimplemented for now.
            return null;
        }
    }

    class ChromeExtensionActionProvider implements ActionProvider {

        @Override
        public String[] getSupportedActions() {
            return new String[]{
                ActionProvider.COMMAND_RENAME,
                ActionProvider.COMMAND_MOVE,
                ActionProvider.COMMAND_COPY,
                ActionProvider.COMMAND_DELETE
            };
        }
        @Override
        public void invokeAction(String string, Lookup lkp) throws IllegalArgumentException {
            if (string.equalsIgnoreCase(ActionProvider.COMMAND_RENAME)) {
                DefaultProjectOperations.performDefaultRenameOperation(
                        ChromeExtensionProject.this,
                        "");
            }
            if (string.equalsIgnoreCase(ActionProvider.COMMAND_MOVE)) {
                DefaultProjectOperations.performDefaultMoveOperation(
                        ChromeExtensionProject.this);
            }
            if (string.equalsIgnoreCase(ActionProvider.COMMAND_COPY)) {
                DefaultProjectOperations.performDefaultCopyOperation(
                        ChromeExtensionProject.this);
            }
            if (string.equalsIgnoreCase(ActionProvider.COMMAND_DELETE)) {
                DefaultProjectOperations.performDefaultDeleteOperation(
                        ChromeExtensionProject.this);
            }
        }
        @Override
        public boolean isActionEnabled(String command, Lookup lookup) throws IllegalArgumentException {

            if ((command.equals(ActionProvider.COMMAND_RENAME))) {
                return true;
            } else if ((command.equals(ActionProvider.COMMAND_MOVE))) {
                return true;
            } else if ((command.equals(ActionProvider.COMMAND_COPY))) {
                return true;
            } else if ((command.equals(ActionProvider.COMMAND_DELETE))) {
                return true;
            }
            return false;
        }
    }

    private final class ChromeExtensionProjectMoveOrRenameOperation implements MoveOrRenameOperationImplementation {
        @Override
        public List<FileObject> getMetadataFiles() {
            return new ArrayList<FileObject>();
        }
        @Override
        public List<FileObject> getDataFiles() {
            ArrayList<FileObject> toReturn = new ArrayList<FileObject>();
            for(FileObject o : projectDir.getChildren()) {
                toReturn.add(o);
            }
            return toReturn;
        }
        @Override
        public void notifyRenaming() throws IOException {
        }
        @Override
        public void notifyRenamed(String nueName) throws IOException {
        }
        @Override
        public void notifyMoving() throws IOException {
        }
        @Override
        public void notifyMoved(Project original, File originalPath, String nueName) throws IOException {
        }
    }

    private final class ChromeExtensionProjectCopyOperation implements CopyOperationImplementation {
        @Override
        public List<FileObject> getMetadataFiles() {
            return new ArrayList<FileObject>();
        }
        @Override
        public List<FileObject> getDataFiles() {
            return new ArrayList<FileObject>();
        }
        @Override
        public void notifyCopying() throws IOException {
        }
        @Override
        public void notifyCopied(Project prjct, File file, String string) throws IOException {
        }
    }

    private final class ChromeExtensionProjectDeleteOperation implements DeleteOperationImplementation {
        @Override
        public List<FileObject> getMetadataFiles() {
            return new ArrayList<FileObject>();
        }
        @Override
        public List<FileObject> getDataFiles() {
            ArrayList<FileObject> toReturn = new ArrayList<FileObject>();
            toReturn.addAll(Arrays.asList(projectDir.getChildren()));
            return toReturn;
        }
        @Override
        public void notifyDeleting() throws IOException {
        }
        @Override
        public void notifyDeleted() throws IOException {
        }
    }
}
