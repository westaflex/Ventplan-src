
dataSource {
    driverClassName = 'org.sqlite.JDBC'
    pool {
        maxWait = 5000
        maxIdle = 5
        maxActive = 1
    }
}

environments {
    development {
        dataSource {
            url = 'jdbc:sqlite:../sql/ventplan.db'
        }
    }
    test {
        dataSource {
            url = 'jdbc:sqlite:../sql/ventplan.db'
        }
    }
    production {
        dataSource {
            // install4j
            url = 'jdbc:sqlite:lib/ventplan.db'
        }
    }
}
