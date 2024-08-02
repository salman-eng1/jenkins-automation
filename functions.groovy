import groovy.json.JsonOutput

def readCommitAuthor() {
    sh '''#!/bin/bash
        git rev-parse HEAD | tr '\n' ' ' > gitCommit
        git show --format="%aN <%aE>" ${gitCommit} | head -1 | tr '\n' ' ' > gitCommitAuthor
    '''
    return readFile('gitCommitAuthor')
}

def durationTime(m1, m2) {
    int timecase = m2 - m1

    int seconds = (int) (timecase / 1000)
    int minutes = (int) (timecase / (60*1000))
    int hours = (int) (timecase / (1000*60*60))

    return hours.mod(24) + "h " + minutes.mod(60) + "m " + seconds.mod(60) + "s"
}

def findPodsFromName(String namespace, String name) {
    podsAndImagesRaw = sh(
        script: """
            kubectl get pods -n production --selector=app=market-review -o jsonpath='{.items[*].metadata.name}'

        """,
        returnStdout: true
    ).trim()
    wantedPods = podsAndImagesRaw.split('###')

    return wantedPods
}


def notifySlack(text, channel, attachments) {
    // Get your own slack webhook url and token
    def slackURL = 'https://hooks.slack.com/services/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'\
    def jenkinsIcon = 'https://a.slack-edge.com/205a/img/services/jenkins-ci_72.png'

    def payload = JsonOutput.toJson([
        text: text,
        channel: channel,
        username: "jenkins",
        icon_url: jenkinsIcon,
        attachments: attachments
    ])
    // Escape single quotes in the payload
    def escapedPayload = payload.replaceAll("'", "\\\\'")
    sh """
        curl -s -X POST ${slackURL} \
        -H 'Cache-Control: no-cache' \
        -H 'Content-Type: application/json;charset=UTF-8' \
        -d '${escapedPayload}'
    """}

return this
