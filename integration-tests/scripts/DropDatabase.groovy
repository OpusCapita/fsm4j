import com.jcatalog.maven.db.DatabaseManager
import com.jcatalog.maven.db.DatabaseManagerFactory
import com.jcatalog.maven.db.DefaultDatabaseManagerFactory

target(drop: 'Drop database') {
    def config = new ConfigSlurper("test").parse(new File('grails-app/conf/DataSource.groovy').toURL())
    def url = config.dataSource.url
    def username = config.dataSource.username
    def password = config.dataSource.password
    def adminUrl = config.dataSource.adminUrl
    def adminUsername = config.dataSource.adminUsername
    def adminPassword = config.dataSource.adminPassword
    def driver = config.dataSource.driverClassName
    String dbServer = System.getProperty('db')

    DatabaseManagerFactory databaseManagerFactory = new DefaultDatabaseManagerFactory(driver, adminUrl, adminUsername, adminPassword)
    DatabaseManager databaseManager = databaseManagerFactory.createDatabaseManager(dbServer)
    databaseManager.dropDatabase(url, username, password)
}

setDefaultTarget(drop)