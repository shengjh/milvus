container('milvus-build-env') {
    timeout(time: 40, unit: 'MINUTES') {
        gitlabCommitStatus(name: 'Build Engine') {
            dir ("milvus_engine") {
                try {
                    checkout([$class: 'GitSCM', branches: [[name: "${SEMVER}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'SubmoduleOption',disableSubmodules: false,parentCredentials: true,recursiveSubmodules: true,reference: '',trackingSubmodules: false]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: "${params.GIT_USER}", url: "git@192.168.1.105:megasearch/milvus.git", name: 'origin', refspec: "+refs/heads/${SEMVER}:refs/remotes/origin/${SEMVER}"]]])

                    dir ("cpp") {
                        sh "git config --global user.email \"test@zilliz.com\""
                        sh "git config --global user.name \"test\""
                        withCredentials([usernamePassword(credentialsId: "${params.JFROG_USER}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                            sh "export JFROG_ARTFACTORY_URL='http://192.168.1.201:80/artifactory' && export JFROG_USER_NAME='${USERNAME}' && export JFROG_PASSWORD='${PASSWORD}' && ./build.sh -t ${params.BUILD_TYPE} -j"
                        }
                    }
                } catch (exc) {
                    updateGitlabCommitStatus name: 'Build Engine', state: 'failed'
                    throw exc
                }
            }
        }
    }
}
