$.fn.cuteTime.settings = {
    refresh: -1,					// time in milliseconds before next refresh of page data; -1 == no refresh
    time_ranges: [
        {bound: Number.NEGATIVE_INFINITY,			// IMPORANT: bounds MUST be in ascending order, from negative infinity to positive infinity
            cuteness: 'le futur !',		unit_size: 0},
        {bound: 0,
            cuteness: 'à l\'instant',			unit_size: 0},
        {bound: 20 * 1000,
            cuteness: 'il y a quelques secondes',	unit_size: 0},
        {bound: 60 * 1000,
            cuteness: 'il y a une minute',		unit_size: 0},
        {bound: 60 * 1000 * 2,
            cuteness: 'il y a %CT% minutes',		unit_size: 60 * 1000},
        {bound: 60 * 1000 * 60,
            cuteness: 'il y a une heure',		unit_size: 0},
        {bound: 60 * 1000 * 60 * 2,
            cuteness: 'il y a %CT% heures',			unit_size: 60 * 1000 * 60},
        {bound: 60 * 1000 * 60 * 24,
            cuteness: 'hier',			unit_size: 0},
        {bound: 60 * 1000 * 60 * 24 * 2,
            cuteness: 'il y a %CT% jours',			unit_size: 60 * 1000 * 60 * 24},
        {bound: 60 * 1000 * 60 * 24 * 30,
            cuteness: 'le mois dernier',			unit_size: 0},
        {bound: 60 * 1000 * 60 * 24 * 30 * 2,
            cuteness: 'il y a %CT% mois',		unit_size: 60 * 1000 * 60 * 24 * 30},
        {bound: 60 * 1000 * 60 * 24 * 30 * 12,
            cuteness: 'l\'année dernière',			unit_size: 0},
        {bound: 60 * 1000 * 60 * 24 * 30 * 12 * 2,
            cuteness: 'il y a %CT% ans',			unit_size: 60 * 1000 * 60 * 24 * 30 * 12},
        {bound: Number.POSITIVE_INFINITY,
            cuteness: 'il y a très longtemps',		unit_size: 0}
    ]
};
