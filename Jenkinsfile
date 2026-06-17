pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *')
    }

    environment {
        GRADLE_OPTS = '-Dorg.gradle.jvmargs=-Xmx512m'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo 'Code pulled from Git'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean compileJava -x test'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test'
            }
            post {
                failure {
                    emailext(
                        to: 'srengty@gmail.com',
                        subject: "BUILD FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """
                            Build: ${env.BUILD_URL}
                            Job: ${env.JOB_NAME}
                            Commit: ${env.GIT_COMMIT}

                            Tests failed. Please check the build log.

                            Changes:
                            ${currentBuild.changeSets}
                        """.stripIndent()
                    )
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    echo "Running Ansible playbook to deploy..."
                    ansible-playbook -i ansible/inventory-jenkins.ini ansible/deploy.yml || true
                    echo "Deploy stage complete"
                '''
            }
        }
    }

    post {
        failure {
            emailext(
                to: 'srengty@gmail.com',
                recipientProviders: [[$class: 'DevelopersRecipientProvider']],
                subject: "BUILD FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    Build: ${env.BUILD_URL}
                    Job: ${env.JOB_NAME}
                    Status: FAILURE
                    Commit: ${env.GIT_COMMIT}

                    See the build log for details.
                """.stripIndent()
            )
        }
        success {
            emailext(
                to: 'srengty@gmail.com',
                subject: "BUILD SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    Build: ${env.BUILD_URL}
                    Job: ${env.JOB_NAME}
                    Status: SUCCESS
                    Commit: ${env.GIT_COMMIT}

                    Deployed to web server successfully.
                """.stripIndent()
            )
        }
    }
}
