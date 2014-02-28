/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.templates;

import net.sf.saxon.type.StringConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Git based source control management service.
 *
 * @author Thomas Draier
 */
public class GitSourceControlManagement extends SourceControlManagement {


    private static final Logger logger = LoggerFactory.getLogger(GitSourceControlManagement.class);

    /**
     * Initializes an instance of this class.
     * @param executable the git executable
     */
    public GitSourceControlManagement(String executable) {
        super(executable);
    }

    @Override
    public void add(List<File> files) throws IOException {
        if (files.isEmpty()) {
            return;
        }

        String rootPath = rootFolder.getPath();
        List<String> args = new ArrayList<String>();
        args.add("add");
        for (File file : files) {
            if (file.getPath().equals(rootPath)) {
                args.add(".");
            } else {
                args.add(file.getPath().substring(rootPath.length() + 1));
            }
        }
        executeCommand(executable, args.toArray(new String[args.size()]));
        invalidateStatusCache();
    }

    @Override
    public void commit(String message) throws IOException {
        boolean commitRequired = checkCommit();
        if (commitRequired) {
            String branch = executeCommand(executable, new String[]{"symbolic-ref"," --short ","HEAD"}).out.trim();
            checkExecutionResult(executeCommand(executable, new String[]{"commit","-a","-m", message }));
            checkExecutionResult(executeCommand(executable, new String[]{"-c", "core.askpass=true","push","-u","origin",branch}));
        }
        invalidateStatusCache();
    }

    @Override
    protected Map<String, Status> createStatusMap() throws IOException {
        return createStatusMap(true);
    }

    private Map<String, Status> createStatusMap(boolean folder) throws IOException {
        Map<String, Status> newMap = new HashMap<String, Status>();
        List<String> paths = readLines(executeCommand(executable, new String[]{"rev-parse", "--show-prefix"}).out);
        String relPath = paths.isEmpty()?"":paths.get(0);
        ExecutionResult result = executeCommand(executable, new String[]{"status","--porcelain"});
        for (String line : readLines(result.out)) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            String path = line.substring(3);
            if (path.contains(" -> ")) {
                path = StringUtils.substringAfter(path, " -> ");
            }
            path = StringUtils.removeEnd(path, "/");
            path = StringUtils.removeStart(path, relPath);
            char indexStatus = line.charAt(0);
            char workTreeStatus = line.charAt(1);
            Status status = null;
            if (workTreeStatus == ' ') {
                if (indexStatus == 'M') {
                    status = Status.MODIFIED;
                } else if (indexStatus == 'A') {
                    status = Status.ADDED;
                } else if (indexStatus == 'D') {
                    status = Status.DELETED;
                } else if (indexStatus == 'R') {
                    status = Status.RENAMED;
                } else if (indexStatus == 'C') {
                    status = Status.COPIED;
                }
            } else if (workTreeStatus == 'M') {
                status = Status.MODIFIED;
            } else if (workTreeStatus == 'D') {
                if (indexStatus == 'D' || indexStatus == 'U') {
                    status = Status.UNMERGED;
                } else {
                    status = Status.DELETED;
                }
            } else if (workTreeStatus == 'A' || workTreeStatus == 'U') {
                status = Status.UNMERGED;
            } else if (workTreeStatus == '?') {
                status = Status.UNTRACKED;
            }
            if (status != null) {
                // put resource status
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                newMap.put(path, status);
                if (folder) {
                    // store intermediate folder status as MODIFIED 
                    StringBuilder subPath = new StringBuilder();
                    for (String segment : StringUtils.split(path, '/')) {
                        newMap.put(subPath.length() == 0 ? "/" : subPath.toString(), Status.MODIFIED);
                        subPath.append('/');
                        subPath.append(segment);
                    }
                }
            }
        }
        return newMap;
    }

    @Override
    public File getRootFolder() {
        return rootFolder;
    }

    @Override
    public String getURI() throws IOException {
        ExecutionResult result = executeCommand(executable, new String[]{"remote","-v"});
        String url = StringUtils.substringBefore(StringUtils.substringAfter(result.out,"origin"),"(").trim();
        if (!StringUtils.isEmpty(url)) {
            return "scm:git:"+url;
        }
        return null;
    }

    protected void getFromSCM(File workingDirectory, String uri, String branchOrTag) throws IOException {
        this.rootFolder = workingDirectory.getParentFile();
        ExecutionResult r = executeCommand(executable, new String[]{"-c", "core.askpass=true","clone",uri,workingDirectory.getName()});
        if (r.exitValue > 0) {
            throw new IOException(r.err);
        }
        this.rootFolder = workingDirectory;
        if (!StringUtils.isEmpty(branchOrTag)) {
            executeCommand(executable, new String[]{"checkout ",branchOrTag});
        }
        this.rootFolder = workingDirectory;
    }

    protected void sendToSCM(File workingDirectory, String url) throws IOException {
        int  MERGE_COMMAND_INDEX = 5;
        List<String[]> commands = Arrays.asList(
                new String[]{"init"},
                new String[]{"add","."},
                new String[]{"commit","-a","-m","First commit"},
                new String[]{"remote","add","origin",url},
                new String[]{"-c", "core.askpass=true","fetch"},
                new String[]{"merge","origin/master"},
                new String[]{"-c", "core.askpass=true","push","-u","origin","master"});

        this.rootFolder = workingDirectory;
        for (String[] command : commands) {
            logger.debug("executing command : {}", Arrays.toString(command));
            ExecutionResult res = executeCommand(executable, command);

            // if the remote repo is empty, the merge orgin/master fail but we have to continue
            // the merge is only used for repo with existing content
            if (!Arrays.equals(command,commands.get(MERGE_COMMAND_INDEX)) && res.exitValue > 0) {
                // an issue occurs during first commit
                // clean up
                executeCommand(executable,new String[]{"merge", "--abort"});
                File gitDir = new File(workingDirectory.getPath() + "/.git");
                if (gitDir.exists()) {
                    FileUtils.deleteDirectory(gitDir);
                }
                logger.error("unable to init git repository {} : {}", url, res.err);
                if (!StringUtils.isEmpty(res.out)) {
                    logger.error(res.out);
                }
                StringBuilder message = new StringBuilder();
                if (!StringUtils.isEmpty(res.err)) {
                    if (StringUtils.contains(res.err,"tree files would be overwritten")) {
                        // tree issue, unable to merge existing content
                        message.append("Unable to send sources to a non empty repository");
                    } else if (StringUtils.contains(res.err,"Repository not found")) {
                        // repo not found
                        message.append("Repository not found");
                    } else {
                        message.append("Repository not accessible, see the log for more information");
                    }
                } else {
                    message.append("Repository not accessible, see the log for more information");
                }
                throw new IOException("Unable to init git repository. " + message.toString());
            }
        }


    }

    protected void initWithWorkingDirectory(File workingDirectory) throws IOException {
        this.rootFolder = workingDirectory;
    }

    @Override
    public void markConflictAsResolved(File file) throws IOException {
        add(file);
    }

    @Override
    public void move(File src, File dst) throws IOException {
        if (src == null || dst == null) {
            return;
        }

        String rootPath = rootFolder.getPath();

        List<String> args = new ArrayList<String>();
        args.add("mv");
        if (src.getPath().equals(rootPath)) {
            args.add(".");
        } else {
            args.add(src.getPath().substring(rootPath.length() + 1));
        }
        if (dst.getPath().equals(rootPath)) {
            args.add(".");
        } else {
            args.add(dst.getPath().substring(rootPath.length() + 1));
        }
        executeCommand(executable, args.toArray(new String[args.size()]));
        invalidateStatusCache();
    }

    @Override
    public void remove(File file) throws IOException {
        if (file == null) {
            return;
        }

        String rootPath = rootFolder.getPath();

        List<String> args = new ArrayList<String>();
        args.add("rm");
        args.add("-f");
        if (file.getPath().equals(rootPath)) {
            args.add(".");
        } else {
            args.add(file.getPath().substring(rootPath.length() + 1));
        }
        executeCommand(executable, args.toArray(new String[args.size()]));
        invalidateStatusCache();
    }

    @Override
    public String update() throws IOException {
        StringBuilder out = new StringBuilder();
        out.append("[").append(executable).append(" stash clear").append("]:\n");
        ExecutionResult result = executeCommand(executable, new String[]{"stash", "clear"});
        out.append(result.out);
        out.append("\n");
        if (StringUtils.isNotEmpty(result.err)) {
            out.append(result.err).append("\n");
        }

        Map<String, Status> statusMap = createStatusMap(false);
        boolean stashRequired = statusMap.values().contains(Status.MODIFIED) || statusMap.values().contains(Status.ADDED)
                || statusMap.values().contains(Status.DELETED) || statusMap.values().contains(Status.RENAMED)
                || statusMap.values().contains(Status.COPIED) || statusMap.values().contains(Status.UNMERGED);
        if (stashRequired) {
            out.append("[").append(executable).append(" stash").append("]:\n");
            result = executeCommand(executable, new String[]{"stash"});
            out.append(result.out);
            out.append("\n");
            if (StringUtils.isNotEmpty(result.err)) {
                out.append(result.err).append("\n");
            }
        }
        out.append("[").append(executable).append(" pull --rebase").append("]:\n");
        ExecutionResult pullResult = executeCommand(executable, new String[]{"pull","--rebase"});
        out.append(pullResult.out);
        out.append("\n");
        if (StringUtils.isNotEmpty(pullResult.err)) {
            out.append(pullResult.err).append("\n");
        }
        ExecutionResult stashPopResult = null;
        if (stashRequired) {
            out.append("[").append(executable).append(" stash pop").append("]:\n");
            stashPopResult = executeCommand(executable, new String[]{"stash","pop"});
            out.append(stashPopResult.out);
            out.append("\n");
            if (StringUtils.isNotEmpty(stashPopResult.err)) {
                out.append(stashPopResult.err).append("\n");
            }
        }
        invalidateStatusCache();
        checkExecutionResult(pullResult);
        if (stashPopResult != null) {
            checkExecutionResult(stashPopResult);
        }

        return out.toString();
    }
}
