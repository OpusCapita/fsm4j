log4j = {
    root {
        error()
    }
    appenders {
        console name: 'stdout'
    }

    debug 'com.opuscapita'
    debug 'com.jcatalog'
}