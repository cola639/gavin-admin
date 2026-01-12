pipeline {
    // Run this pipeline on any available Jenkins agent/node
    agent any

    // Environment variables available to all stages
    environment {
        // Docker network name (frontend/backend containers can talk to each other on this network)
        NETWORK = 'admin'

        // Docker image + container name
        IMAGE_NAME = 'admin-server'

        // Built JAR file name (in target/)
        JAR_FILE = 'admin-server.jar'

        // Jenkins workspace directory
        WS = "${WORKSPACE}"

        // Spring profile / build arg (used by Dockerfile + app config)
        PROFILE = 'prod'

        // Mvn version
        NODE_IMAGE   = "maven:3-alpine"

        // Http port: the port your Spring Boot app listens on (matches application-prod.yml)
        HOST_HTTP_PORT = '8989'
        CONTAINER_HTTP_PORT = '80'

        // JVM port: the port your JVM is running on
        HOST_JVM_PORT = '8887'
        CONTAINER_JVM_PORT = '90'
    }

    stages {
        stage('1. Environment') {
            steps {
                sh 'pwd && ls -alh'   // Show current path and files
                sh 'printenv'         // Print environment variables
                sh 'docker version'   // Docker version check
                sh 'java -version'    // Java version check
                sh 'git --version'    // Git version check
            }
        }

        stage('2. Compile') {
            agent {
                docker {
                    image "${NODE_IMAGE}"    // Use Maven Docker image for build
                    args  '-v maven-repository:/root/.m2'   // Cache Maven dependencies
                }
            }
            steps {
                sh 'pwd && ls -alh'
                sh 'mvn -v'
                // Build and package (skip tests)
                sh 'cd ${WS} && mvn clean package -Dmaven.test.skip=true'
            }
        }

        stage('3. Build Image') {
            steps {
                sh 'pwd && ls -alh'
                sh 'echo ${WS}'
                sh 'ls -alh ${WS}/'
                sh 'ls -lah ${WS}/${IMAGE_NAME}/target/'

                // Build Docker image:
                // - Docker build context points to the target/ folder (where the JAR is)
                // - Dockerfile must be able to COPY the JAR from that context
                sh '''
                    docker build \
                      --build-arg PROFILE=${PROFILE} \
                      --build-arg JAR_FILE=${JAR_FILE} \
                      -t ${IMAGE_NAME} \
                      -f Dockerfile \
                      ${WS}/${IMAGE_NAME}/target/
                '''

                // Remove old container if it exists (ignore error if not found)
                sh 'docker rm -f ${IMAGE_NAME} || true'
            }
        }

        stage('4. Deploy') {
            steps {
                // Ensure the Docker network exists (create it if missing)
                sh 'docker network inspect ${NETWORK} >/dev/null 2>&1 || docker network create ${NETWORK}'

                // Option A (internal only): no port published to the host
                // sh 'docker run -d --net ${NETWORK} --name ${IMAGE_NAME} --restart always ${IMAGE_NAME}'

                // Option B (recommended if you need external access):
                // Publish host port -> container port (both are variables now)
                sh """
                docker run -d --net ${NETWORK} \
                  -p ${HOST_HTTP_PORT}:${CONTAINER_HTTP_PORT} \
                  -p ${HOST_JVM_PORT}:${CONTAINER_JVM_PORT} \
                  --name ${IMAGE_NAME} --restart always \
                  ${IMAGE_NAME}
                """

                // Option C (auto-publish a random host port to container port):
                // sh 'docker run -d --net ${NETWORK} -p ${CONTAINER_PORT} --name ${IMAGE_NAME} --restart always ${IMAGE_NAME}'
            }
        }
    }
}
