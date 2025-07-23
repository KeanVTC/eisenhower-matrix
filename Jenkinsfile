pipeline {
    agent any

    environment {
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
        PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/KeanVTC/eisenhower-matrix.git'
            }
        }

        stage('Build Backend') {
            steps {
                sh 'mvn package -DskipTests' // ⬅️ Removed 'clean'
            }
        }

        stage('Run Backend Tests') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Done') {
            steps {
                echo '✅ Build and tests complete.'
            }
        }
    }
}
