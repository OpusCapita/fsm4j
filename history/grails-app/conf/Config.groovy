log4j = {
    root {
        warn 'stdout'
    }
    trace 'jdbc.sqltiming'
}

plugin.platformCore.events.disabled = true

grails.mime.types = [
        json: ['application/json', 'text/json']
]
