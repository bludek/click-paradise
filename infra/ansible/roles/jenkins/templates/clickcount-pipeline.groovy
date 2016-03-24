#!groovy
import groovy.json.JsonSlurper

def uatMaster = "10.110.192.200"
def uatProxy = "10.110.192.200"

def repo = "bludek/click-paradise"
def service = "clickcount"
def servers

node {
    // Mark the code checkout 'stage'....
    stage 'Checkout'
    // servers = load 'servers.groovy'
    checkout(
        [$class: 'GitSCM',
        branches: [[name: '*/master']],
        doGenerateSubmoduleConfigurations: false,
        extensions: [[
            $class: 'CloneOption',
            noTags: false,
            reference: '',
            shallow: true
        ]],
        submoduleCfg: [],
        userRemoteConfigs: [[url: 'https://github.com/bludek/click-paradise.git']]  ]
    )

    // Mark the code build 'stage'....
    stage 'Build'
    // Run the maven build
    mvn 'clean package'
    dir('.') {stash name: 'compose', includes: '*.yml'}
    step([$class: 'Fingerprinter', targets: 'target/*.war'])

    stage 'QA'
    parallel(longerTests: {
        runTests(servers, 30)
    }, quickerTests: {
        runTests(servers, 20)
    })

    docker.withRegistry('http://10.100.192.200:5000') {
        stage 'Build Docker image'
        def img = docker.build("clickparadise/clickcount:${env.BUILD_TAG}")

        stage name: 'Promote Image', concurrency: 1
        // All the tests passed. We can now retag and push the 'latest' image.
        img.push('staging');
    }

}

node("uat") {
    stage name: 'UAT Deployment', concurrency: 1
    def nextColor = deploy("uat", "${uatMaster}", "${service}")

    stage name: 'UAT Post-Deployment', concurrency: 1
    postDeploy("uat", "${uatMaster}", "${uatProxy}", "${nextColor}", "${service}")
}

def mvn(args) {
    sh "${tool 'M3'}/bin/mvn ${args}"
}

def runTests(servers, duration) {
    node {

    }
}

def deploy(id, master, service) {
    currentColor = getCurrentColor(master, service)
    nextColor = getNextColor(currentColor)
    instances = getInstances(master, service)

    unstash 'compose'
    env.DOCKER_HOST = "tcp://${master}:4000"
    sh "docker-compose -f docker-compose-${id}.yml pull app-${nextColor}"
    sh "docker-compose -f docker-compose-${id}.yml rm -f app-${nextColor}"
    sh "docker-compose -f docker-compose-${id}.yml up -d app-${nextColor}"
    sh "docker-compose -f docker-compose-${id}.yml scale app-${nextColor}=${instances}"
    sh "curl -X PUT -d $instances http://${master}:8500/v1/kv/${service}/instances"
    return nextColor
}

def postDeploy(id, master, proxy, nextColor, service) {
    address = getAddress(master, service, nextColor)
    waitFor(address)
    sh "curl -X PUT -d ${nextColor} http://${master}:8500/v1/kv/${service}/color"
    updateProxy(id, proxy)
    if (currentColor != "") {
        println "New version is ready, stop the old one"
        env.DOCKER_HOST = "tcp://${master}:4000"
        sh "docker-compose -f docker-compose-${id}.yml stop app-${currentColor}"
    }
}

def updateProxy(id, proxy) {
    node("${id}-lb") {
        env.DOCKER_HOST = "tcp://${proxy}:2375"
        sh "docker kill -s HUP ${id}_nginx_1" // reload config
    }
}

def waitFor(address) {
    def isUp = false
    def tryCount = 1;
    println "Waiting for app to boot"
    while (!(isUp || tryCount == 10)) {
        def req = "curl -X GET ${address}/clickCount/rest/healthcheck".execute().text
        isUp = req == "ok"
        if (!isUp) {
            def proc = "sleep 3".execute()
            proc.waitFor()
        }
        tryCount++
    }
    if (!isUp) error ("Application does not start in time (30s)")
}

def getCurrentColor(swarmMaster, service) {
    try {
        return "http://${swarmMaster}:8500/v1/kv/${service}/color?raw".toURL().text
    } catch(e) {
        return ""
    }
}

def getNextColor(currentColor) {
    if (currentColor == "blue") {
        return "green"
    } else {
        return "blue"
    }
}

def getAddress(swarmMaster, service, color) {
    def serviceJson = "http://${swarmMaster}:8500/v1/catalog/service/${service}-${color}".toURL().text
    def result = new JsonSlurper().parseText(serviceJson)[0]
    def isWeb = result.ServiceID.contains("8080")
    if (!isWeb)
        result = new JsonSlurper().parseText(serviceJson)[1]
    return result.ServiceAddress + ":" + result.ServicePort
}

def getInstances(swarmMaster, service) {
    return 1
}
