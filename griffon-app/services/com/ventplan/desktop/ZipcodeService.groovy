package com.ventplan.desktop

class ZipcodeService {

    Map findVertreter(String zipcode) {
        def r = withSql { dataSourceName, sql ->
            sql.rows('SELECT * FROM handelsvertretung'
                     + ' WHERE plzvon <= ?.zipcode AND plzbis >= ?.zipcode',
                    [zipcode: zipcode])
        }
        r ? r[0] : null
    }

}
