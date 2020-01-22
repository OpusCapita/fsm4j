dataSource {
    pooled = false
    driverClassName = "com.mysql.jdbc.Driver"
    dbCreate = "create-drop"
    url = "jdbc:mysql://localhost:3306/test"
    username = "root"
    password = "root"
    dialect = 'com.jcatalog.util.database.CustomMySQL5InnoDBDialect'
}
//dataSource {
//    pooled = false
//    driverClassName = "org.h2.Driver"
//    dbCreate = "create-drop"
//    url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
//    username = "sa"
//    password = ""
//    dialect = 'org.hibernate.dialect.H2Dialect'
//}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
    naming_strategy = com.jcatalog.util.database.hibernate.ImprovedNamingStrategyWithAnnotations
}
