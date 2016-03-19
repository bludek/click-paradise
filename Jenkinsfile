#!groovy

def mvn(args) {
    sh "${tool 'maven.3.3.9'}/bin/mvn ${args}"
}

stage 'Dev'
node {
    checkout scm
    mvn '-o clean package'
    dir('target') {stash name: 'war', includes: 'x.war'}
}
