#!groovy
import groovy.json.JsonSlurper

def jenkins = "10.100.192.200"
def prodMaster = "10.120.192.200"
def prodProxy = "10.120.192.200"

def repo = "bludek/click-paradise"
def service = "clickcount"
def servers

node {
    stage 'Check'
    def result = getJsonService("http://${jenkins}:8080/job/click-count/api/json")
    def lastSuccess = result.lastSuccessfulBuild.number
    println "Last successful build is : ${lastSuccess}"
    result = getJsonService("http://${jenkins}:8080/job/click-count/${lastSuccess}/api/json")
    def revision = result.actions.lastBuiltRevision.SHA1[0]
    println "Last successful revision is : ${revision}"

    stage 'Checkout'
    checkout(
        [$class: 'GitSCM',
        branches: [[name: "${revision}"]],
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
    dir('.') {stash name: 'compose', includes: '*.yml'}

    stage 'Maven Release'
    //extract the current version
    def pom = readFile 'pom.xml'
    def currentVersion = new XmlParser().parseText(pom).version.text()
    currentVersion = currentVersion.replaceAll('-SNAPSHOT', '')
    // TODO: perform maven release, upgrade version

    stage 'Docker release'
    docker.withRegistry('http://10.100.192.200:5000') {
        def img = docker.image("clickparadise/clickcount:jenkins-click-count-${lastSuccess}")
        img.push("production")
        img.push("${currentVersion}")
    }
}


node("prod") {
    stage name: 'Deployment', concurrency: 1
    def nextColor = deploy("prod", "${prodMaster}", "${service}")

    stage name: 'Post-Deployment', concurrency: 1
    postDeploy("prod", "${prodMaster}", "${prodProxy}", "${nextColor}", "${service}")
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

def getJsonService(url) {
    def serviceJson = "${url}".toURL().text
    def result = new JsonSlurper().parseText(serviceJson)
    return result
}
