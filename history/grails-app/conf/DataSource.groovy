dataSource {
    pooled = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}
hibernate {
    naming_strategy = com.jcatalog.util.database.hibernate.ImprovedNamingStrategyWithAnnotations
    cache.use_second_level_cache=true
    cache.provider_class='org.hibernate.cache.EhCacheProvider'
}
environments {
    test {
        dataSource {
            dbCreate = ''
            url = "jdbc:h2:mem:test;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
        }
    }
}
