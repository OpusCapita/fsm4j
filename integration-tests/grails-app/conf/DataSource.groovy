hibernate {
    cache.use_second_level_cache = false
    cache.use_query_cache = false
    naming_strategy = com.jcatalog.util.database.hibernate.ImprovedNamingStrategyWithAnnotations
}

environments {
    test {
        dataSource {
            dbCreate = ""
            username = System.getProperty('db.username')
            password = System.getProperty('db.password')
            adminUsername = System.getProperty('db.admin.username')
            adminPassword = System.getProperty('db.admin.password')
            url = System.getProperty('db.url')
            adminUrl = System.getProperty('db.admin.url')
            switch (System.getProperty('db')) {
                case 'mysql':
                    driverClassName = "com.mysql.jdbc.Driver"
                    dialect = com.jcatalog.util.database.CustomMySQL5InnoDBDialect
                    break
                case 'sqlserver':
                    driverClassName = "net.sourceforge.jtds.jdbc.Driver"
                    dialect = com.jcatalog.util.database.UnicodeSQLServerDialect
                    break
                case 'oracle':
                    driverClassName = "oracle.jdbc.OracleDriver"
                    dialect = org.hibernate.dialect.Oracle10gDialect
                    break
                default:
                    break
            }
        }
    }
}
