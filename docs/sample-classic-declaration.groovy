// Import the utility functionality.
import jobs.generation.Utilities;

// Defines a the new of the repo, used elsewhere in the file
def project = QualifiedRepoName
def branch = BranchName

// Generate the builds for debug and release, commit and PRJob
[true, false].each { isPR -> // Defines a closure over true and false, value assigned to isPR
    ['Debug', 'Release'].each { configuration ->
        
        // Determine the name for the new job.  The first parameter is the project,
        // the second parameter is the base name for the job, and the last parameter
        // is a boolean indicating whether the job will be a PR job.  If true, the
        // suffix _prtest will be appended.
        def newJobName = Utilities.getFullJobName(project, configuration, isPR)
        
        // Define build string
        def buildString = """call \"C:\\Program Files (x86)\\Microsoft Visual Studio 14.0\\Common7\\Tools\\VsDevCmd.bat\" && build.cmd ${configuration}"""

        // Create a new job with the specified name.  The brace opens a new closure
        // and calls made within that closure apply to the newly created job.
        def newJob = job(newJobName) {
            // This opens the set of build steps that will be run.
            steps {
                // Indicates that a batch script should be run with the build string (see above)
                // Also available is:
                // shell (for unix scripting)
                batchFile(buildString)
            }
        }
        
        Utilities.setMachineAffinity(newJob, 'Windows_NT', 'latest-or-auto')
        
        // This call performs remaining common job setup on the newly created job.
        // It does the following:
        //   1. Sets up source control for the project.
        //   2. Adds standard options for build retention and timeouts
        //   3. Adds standard parameters for PR and push jobs.
        //      These allow PR jobs to be used for simple private testing, for instance.
        // See the documentation for this function to see additional optional parameters.
        Utilities.standardJobSetup(newJob, project, isPR, "*/${branch}")
        
        // The following two calls add triggers for push and PR jobs
        // In Github, the PR trigger will appear as "Windows Debug" and "Windows Release" and will be run
        // by default
        if (isPR) {
            Utilities.addGithubPRTriggerForBranch(newJob, branch, "Windows ${configuration}")
        }
        else {
            Utilities.addGithubPushTrigger(newJob)
        }
    }
}