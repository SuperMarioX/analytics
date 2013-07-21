var gadgets = gadgets || {};

gadgets.i18n = gadgets.i18n || {};

//鍒濆鍖�
//gadgets.i18n.NumberFormatConstants = 


/**
 * Base Function 0
 * The mapping of currency symbol through intl currency code.
 */

gadgets.i18n.CurrencyCodeMap = {
    'USD': '$',
    'ARS': '$',
    'AWG': '\u0192',
    'AUD': '$',
    'BSD': '$',
    'BBD': '$',
    'BEF': '\u20A3',
    'BZD': '$',
    'BMD': '$',
    'BOB': '$',
    'BRL': 'R$',
    'BRC': '\u20A2',
    'GBP': '\u00A3',
    'BND': '$',
    'KHR': '\u17DB',
    'CAD': '$',
    'KYD': '$',
    'CLP': '$',
    'CNY': '\u00A5',
    'COP': '\u20B1',
    'CRC': '\u20A1',
    'CUP': '\u20B1',
    'CYP': '\u00A3',
    'DKK': 'kr',
    'DOP': '\u20B1',
    'XCD': '$',
    'EGP': '\u00A3',
    'SVC': '\u20A1',
    'EUR': '\u20AC',
    'XEU': '\u20A0',
    'FKP': '\u00A3',
    'FJD': '$',
    'FRF': '\u20A3',
    'GIP': '\u00A3',
    'GRD': '\u20AF',
    'GGP': '\u00A3',
    'GYD': '$',
    'NLG': '\u0192',
    'HKD': '$',
    'INR': '\u20A8',
    'IRR': '\uFDFC',
    'IEP': '\u00A3',
    'IMP': '\u00A3',
    'ILS': '\u20AA',
    'ITL': '\u20A4',
    'JMD': '$',
    'JPY': '\u00A5',
    'JEP': '\u00A3',
    'KPW': '\u20A9',
    'KRW': '\u20A9',
    'LAK': '\u20AD',
    'LBP': '\u00A3',
    'LRD': '$',
    'LUF': '\u20A3',
    'MTL': '\u20A4',
    'MUR': '\u20A8',
    'MXN': '$',
    'MNT': '\u20AE',
    'NAD': '$',
    'NPR': '\u20A8',
    'ANG': '\u0192',
    'NZD': '$',
    'OMR': '\uFDFC',
    'PKR': '\u20A8',
    'PEN': 'S/.',
    'PHP': '\u20B1',
    'QAR': '\uFDFC',
    'RUB': '\u0440\u0443\u0431',
    'SHP': '\u00A3',
    'SAR': '\uFDFC',
    'SCR': '\u20A8',
    'SGD': '$',
    'SBD': '$',
    'ZAR': 'R',
    'ESP': '\u20A7',
    'LKR': '\u20A8',
    'SEK': 'kr',
    'SRD': '$',
    'SYP': '\u00A3',
    'TWD': '\u5143',
    'THB': '\u0E3F',
    'TTD': '$',
    'TRY': '\u20A4',
    'TRL': '\u20A4',
    'TVD': '$',
    'UYU': '\u20B1',
    'VAL': '\u20A4',
    'VND': '\u20AB',
    'YER': '\uFDFC',
    'ZWD': '$'
};


/**
 * Base Function 1
 * @fileoverview Functions for dealing with Date formatting
 */


/**
 * DateTime formatting functions following the pattern specification as defined
 * in JDK, ICU and CLDR, with minor modification for typical usage in JS.
 * Pattern specification: (Refer to JDK/ICU/CLDR)
 * <pre>
 * Symbol Meaning Presentation        Example
 * ------   -------                 ------------        -------
 * G        era designator          (Text)              AD
 * y#       year                    (Number)            1996
 * Y*       year (week of year)     (Number)            1997
 * u*       extended year           (Number)            4601
 * M        month in year           (Text & Number)     July & 07
 * d        day in month            (Number)            10
 * h        hour in am/pm (1~12)    (Number)            12
 * H        hour in day (0~23)      (Number)            0
 * m        minute in hour          (Number)            30
 * s        second in minute        (Number)            55
 * S        fractional second       (Number)            978
 * E        day of week             (Text)              Tuesday
 * e*       day of week (local 1~7) (Number)            2
 * D*       day in year             (Number)            189
 * F*       day of week in month    (Number)            2 (2nd Wed in July)
 * w*       week in year            (Number)            27
 * W*       week in month           (Number)            2
 * a        am/pm marker            (Text)              PM
 * k        hour in day (1~24)      (Number)            24
 * K        hour in am/pm (0~11)    (Number)            0
 * z        time zone               (Text)              Pacific Standard Time
 * Z        time zone (RFC 822)     (Number)            -0800
 * v        time zone (generic)     (Text)              Pacific Time
 * g*       Julian day              (Number)            2451334
 * A*       milliseconds in day     (Number)            69540000
 * '        escape for text         (Delimiter)         'Date='
 * ''       single quote            (Literal)           'o''clock'
 *
 * Item marked with '*' are not supported yet.
 * Item marked with '#' works different than java
 *
 * The count of pattern letters determine the format.
 * (Text): 4 or more, use full form, <4, use short or abbreviated form if it
 * exists. (e.g., "EEEE" produces "Monday", "EEE" produces "Mon")
 *
 * (Number): the minimum number of digits. Shorter numbers are zero-padded to
 * this amount (e.g. if "m" produces "6", "mm" produces "06"). Year is handled
 * specially; that is, if the count of 'y' is 2, the Year will be truncated to
 * 2 digits. (e.g., if "yyyy" produces "1997", "yy" produces "97".) Unlike other
 * fields, fractional seconds are padded on the right with zero.
 *
 * (Text & Number): 3 or over, use text, otherwise use number. (e.g., "M"
 * produces "1", "MM" produces "01", "MMM" produces "Jan", and "MMMM" produces
 * "January".)
 *
 * Any characters in the pattern that are not in the ranges of ['a'..'z'] and
 * ['A'..'Z'] will be treated as quoted text. For instance, characters like ':',
 * '.', ' ', '#' and '@' will appear in the resulting time text even they are
 * not embraced within single quotes.
 * </pre>
 *
 */

/**
 * Construct a DateTimeFormat object based on current locale by using
 * the symbol table passed in.
 * @constructor
 */
gadgets.i18n.DateTimeFormat = function(symbol) {
    this.symbols_ = symbol;
};

/**
 * regular expression pattern for parsing pattern string
 * @private
 */
gadgets.i18n.DateTimeFormat.TOKENS_ = [
    //quote string
    /^\'(?:[^\']|\'\')*\'/,
    // pattern chars
    /^(?:G+|y+|M+|k+|S+|E+|a+|h+|K+|H+|c+|L+|Q+|d+|m+|s+|v+|z+|Z+)/,
    // and all the other chars
    /^[^\'GyMkSEahKHcLQdmsvzZ]+/  // and all the other chars
];

/**
 * These are token types, corresponding to above token definitions.
 * @enum {number}
 */
gadgets.i18n.DateTimeFormat.PartTypes = {
    QUOTED_STRING : 0,
    FIELD : 1,
    LITERAL : 2
};

/**
 * Pads number to given length and optionally rounds it to a given precision.
 * For example:
 * <pre>padNumber(1.25, 2, 3) -> '01.250'
 * padNumber(1.25, 2) -> '01.25'
 * padNumber(1.25, 2, 1) -> '01.3'
 * padNumber(1.25, 0) -> '1.25'</pre>
 *
 * @param {number} num The number to pad.
 * @param {number} length The desired length.
 * @return {string} {@code num} as a string with the given options.
 * @private
 */
gadgets.i18n.DateTimeFormat.padNumber_ = function(num, length) {
    var s = String(num);
    var index = s.indexOf('.');
    if (index == -1) {
        index = s.length;
    }
    var tempArray = new Array(Math.max(0, length - index) + 1);
    return tempArray.join('0') + s;
};

/**
 * Apply specified pattern to this formatter object.
 * @param {string} pattern String specifying how the date should be formatted.
 */
gadgets.i18n.DateTimeFormat.prototype.applyPattern = function(pattern) {
    this.patternParts_ = [];

  // lex the pattern, once for all uses
    while (pattern) {
        for (var i = 0; i < gadgets.i18n.DateTimeFormat.TOKENS_.length; ++i) {
            var m = pattern.match(gadgets.i18n.DateTimeFormat.TOKENS_[i]);
            if (m) {
                var part = m[0];
                pattern = pattern.substring(part.length);
                if (i == gadgets.i18n.DateTimeFormat.PartTypes.QUOTED_STRING) {
                    if (part == "''") {
                        part = "'";  // '' -> '
                    } else {
                        // strip quotes
                        part = part.substring(1, part.length - 1);
                        part = part.replace(/\'\'/, "'");
                    }
                }
                this.patternParts_.push({ text: part, type: i });
                break;
            }
        }
    }
};

/**
 * Format the given date object according to preset pattern and current lcoale.
 * @param {Date} date The Date object that is being formatted.
 * @return {string} Formatted string for the given date.
 */
gadgets.i18n.DateTimeFormat.prototype.format = function(date) {
    /*  if (!opt_timeZone) {
        opt_timeZone =
          gadgets.i18n.TimeZone.createTimeZone(date.getTimezoneOffset());
      }

      // We don't want to write code to calculate each date field because we
      // want to maximize performance and minimize code size.
      // JavaScript only provide API to render local time.
      // Suppose target date is: 16:00 GMT-0400
      // OS local time is:       12:00 GMT-0800
      // We want to create a Local Date Object : 16:00 GMT-0800, and fix the
      // time zone display ourselves.
      // Thing get a little bit tricky when daylight time transition happens. For
      // example, suppose OS timeZone is America/Los_Angeles, it is impossible to
      // represent "2006/4/2 02:30" even for those timeZone that has no transition
      // at this time. Because 2:00 to 3:00 on that day does not exising in
      // America/Los_Angeles time zone. To avoid calculating date field through
      // our own code, we uses 3 Date object instead, one for "Year, month, day",
      // one for time within that day, and one for timeZone object since it need
      // the real time to figure out actual time zone offset.
      var diff = (date.getTimezoneOffset() - opt_timeZone.getOffset(date)) * 60000;
      var dateForDate = diff ? new Date(date.getTime() + diff) : date;
      var dateForTime = dateForDate;
      // in daylight time switch on/off hour, diff adjustment could alter time
      // because of timeZone offset change, move 1 day forward or backward.
      if (dateForDate.getTimezoneOffset() != date.getTimezoneOffset()) {
        diff += diff > 0 ? -24 * 60 * 60000 : 24 * 60 * 60000;
        dateForTime = new Date(date.getTime() + diff);
      }
    */
    var out = [];
    for (var i = 0; i < this.patternParts_.length; ++i) {
        var text = this.patternParts_[i].text;
        if (gadgets.i18n.DateTimeFormat.PartTypes.FIELD ==
            this.patternParts_[i].type) {
            out.push(this.formatField_(text, date));
        } else {
            out.push(text);
        }
    }
    return out.join('');
};

/**
 * Apply a predefined pattern as identified by formatType, which is stored in
 * locale specific repository.
 * @param {number} formatType A number that identified the predefined pattern.
 */
gadgets.i18n.DateTimeFormat.prototype.applyStandardPattern =
function(formatType) {
    var pattern;
    if (formatType < 4) {
        pattern = this.symbols_.DATEFORMATS[formatType];
    } else if (formatType < 8) {
        pattern = this.symbols_.TIMEFORMATS[formatType - 4];
    } else if (formatType < 12) {
        pattern = this.symbols_.DATEFORMATS[formatType - 8] +
                  ' ' + this.symbols_.TIMEFORMATS[formatType - 8];
    } else {
        this.applyStandardPattern(gadgets.i18n.MEDIUM_DATETIME_FORMAT);
    }
    return this.applyPattern(pattern);
};

/**
 * Formats Era field according to pattern specified.
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} Formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatEra_ = function(count, date) {
    var value = date.getFullYear() > 0 ? 1 : 0;
    return count >= 4 ? this.symbols_.ERANAMES[value] : this.symbols_.ERAS[value];
};

/**
 * Formats Year field according to pattern specified
 *   Javascript Date object seems incapable handling 1BC and
 *   year before. It can show you year 0 which does not exists.
 *   following we just keep consistent with javascript's
 *   toString method. But keep in mind those things should be
 *   unsupported.
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} Formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatYear_ = function(count, date) {
    var value = date.getFullYear();
    if (value < 0) {
        value = -value;
    }
    return count == 2 ?
           gadgets.i18n.DateTimeFormat.padNumber_(value % 100, 2) :
           String(value);
};

/**
 * Formats Month field according to pattern specified
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} Formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatMonth_ = function(count, date) {
    var value = date.getMonth();
    switch (count) {
        case 5: return this.symbols_.NARROWMONTHS[value];
        case 4: return this.symbols_.MONTHS[value];
        case 3: return this.symbols_.SHORTMONTHS[value];
        default:
            return gadgets.i18n.DateTimeFormat.padNumber_(value + 1, count);
    }
};

/**
 * Formats (1..24) Hours field according to pattern specified
 *
 * @param {number} count Number of time pattern char repeats. This controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} Formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.format24Hours_ = function(count, date) {
    return gadgets.i18n.DateTimeFormat.padNumber_(date.getHours() || 24, count);
};

/**
 * Formats Fractional seconds field according to pattern specified
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 *
 * @return {string} Formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatFractionalSeconds_ =
function(count, date) {
    // Fractional seconds left-justify, append 0 for precision beyond 3
    var value = date.getTime() % 1000 / 1000;
    return value.toFixed(Math.min(3, count)).substr(2) +
           (count > 3 ? gadgets.i18n.DateTimeFormat.padNumber_(0, count - 3) : '');
};

/**
 * Formats Day of week field according to pattern specified
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} Formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatDayOfWeek_ = function(count, date) {
    var value = date.getDay();
    return count >= 4 ? this.symbols_.WEEKDAYS[value] :
           this.symbols_.SHORTWEEKDAYS[value];
};

/**
 * Formats Am/Pm field according to pattern specified
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} Formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatAmPm_ = function(count, date) {
    var hours = date.getHours();
    return this.symbols_.AMPMS[hours >= 12 && hours < 24 ? 1 : 0];
};

/**
 * Formats (1..12) Hours field according to pattern specified
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.format1To12Hours_ = function(count, date) {
    return gadgets.i18n.DateTimeFormat.padNumber_(date.getHours() % 12 || 12, count);
};

/**
 * Formats (0..11) Hours field according to pattern specified
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.format0To11Hours_ = function(count, date) {
    return gadgets.i18n.DateTimeFormat.padNumber_(date.getHours() % 12, count);
};

/**
 * Formats (0..23) Hours field according to pattern specified
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.format0To23Hours_ = function(count, date) {
    return gadgets.i18n.DateTimeFormat.padNumber_(date.getHours(), count);
};

/**
 * Formats Standalone weekday field according to pattern specified
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatStandaloneDay_ =
function(count, date) {
    var value = date.getDay();
    switch (count) {
        case 5:
            return this.symbols_.STANDALONENARROWWEEKDAYS[value];
        case 4:
            return this.symbols_.STANDALONEWEEKDAYS[value];
        case 3:
            return this.symbols_.STANDALONESHORTWEEKDAYS[value];
        default:
            return gadgets.i18n.DateTimeFormat.padNumber_(value, 1);
    }
};

/**
 * Formats Standalone Month field according to pattern specified
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatStandaloneMonth_ =
function(count, date) {
    var value = date.getMonth();
    switch (count) {
        case 5:
            return this.symbols_.STANDALONENARROWMONTHS[value];
        case 4:
            return this.symbols_.STANDALONEMONTHS[value];
        case 3:
            return this.symbols_.STANDALONESHORTMONTHS[value];
        default:
            return gadgets.i18n.DateTimeFormat.padNumber_(value + 1, count);
    }
};

/**
 * Formats Quarter field according to pattern specified
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} Formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatQuarter_ = function(count, date) {
    var value = Math.floor(date.getMonth() / 3);
    return count < 4 ? this.symbols_.SHORTQUARTERS[value] :
           this.symbols_.QUARTERS[value];
};

/**
 * Formats Date field according to pattern specified
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} Formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatDate_ = function(count, date) {
    return gadgets.i18n.DateTimeFormat.padNumber_(date.getDate(), count);
};

/**
 * Formats Minutes field according to pattern specified
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} Formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatMinutes_ = function(count, date) {
    return gadgets.i18n.DateTimeFormat.padNumber_(date.getMinutes(), count);
};

/**
 * Formats Seconds field according to pattern specified
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @return {string} Formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatSeconds_ = function(count, date) {
    return gadgets.i18n.DateTimeFormat.padNumber_(date.getSeconds(), count);
};

/**
 * Formats TimeZone field following RFC
 *
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date It holds the date object to be formatted.
 * @param {gadgets.i18n.TimeZone} timeZone holds current time zone info.
 * @return {string} Formatted string that represent this field.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatTimeZoneRFC_ =
function(count, date) {
    if (count < 4) {
        // 'short' (standard Java) form, must use ASCII digits
        var val = date.getTimezoneOffset();
        var sign = '-';
        if (val < 0) {
            val = -val;
            sign = '+';
        }
        val = val / 3 * 5 + val % 60;
    // minutes => KKmm
        return sign + gadgets.i18n.DateTimeFormat.padNumber_(val, 4);
    }
    return this.formatGMT_(count, date);
};

/**
 * Generate GMT timeZone string for given date
 * @param {number} count Number of time pattern char repeats, it controls
 *     how a field should be formatted.
 * @param {Date} date Whose value being evaluated.
 * @return {string} GMT timeZone string.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatGMT_ = function(count, date) {
    var value = date.getTimezoneOffset();
    var out = [];
    if (value > 0) {
        out.push('GMT-');
    } else {
        value = -value;
        out.push('GMT+');
    }

    out.push(gadgets.i18n.DateTimeFormat.padNumber_(value / 60, 2));
    out.push(':');
    out.push(gadgets.i18n.DateTimeFormat.padNumber_(value % 60, 2));
    return out.join('');
};

/**
 * Formatting one date field.
 * @param {string} patternStr The pattern string for the field being formatted.
 * @param {Date} date The Date object whose field will be formatted.
 * @private
 */
gadgets.i18n.DateTimeFormat.prototype.formatField_ = function(patternStr, date) {
    var count = patternStr.length;
    switch (patternStr.charAt(0)) {
        case 'G': return this.formatEra_(count, date);
        case 'y': return this.formatYear_(count, date);
        case 'M': return this.formatMonth_(count, date);
        case 'k': return this.format24Hours_(count, date);
        case 'S': return this.formatFractionalSeconds_(count, date);
        case 'E': return this.formatDayOfWeek_(count, date);
        case 'a': return this.formatAmPm_(count, date);
        case 'h': return this.format1To12Hours_(count, date);
        case 'K': return this.format0To11Hours_(count, date);
        case 'H': return this.format0To23Hours_(count, date);
        case 'c': return this.formatStandaloneDay_(count, date);
        case 'L': return this.formatStandaloneMonth_(count, date);
        case 'Q': return this.formatQuarter_(count, date);
        case 'd': return this.formatDate_(count, date);
        case 'm': return this.formatMinutes_(count, date);
        case 's': return this.formatSeconds_(count, date);
        case 'v': return this.formatGMT_(count, date);
        case 'z': return this.formatGMT_(count, date);
        case 'Z': return this.formatTimeZoneRFC_(count, date);
        default: return '';
    }
};



/**
 * Base Function 2
 */
/**
 * DateTimeParse is for parsing date in a locale-sensitive manner. It allows
 * user to use any customized patterns to parse date-time string under certain
 * locale. Things varies across locales like month name, weekname, field
 * order, etc.
 *
 * This module is the counter-part of DateTimeFormat. They use the same
 * date/time pattern specification, which is borrowed from ICU/JDK.
 *
 * This implementation could parse partial date/time.
 *
 * Time Format Syntax: To specify the time format use a time pattern string.
 * In this pattern, following letters are reserved as pattern letters, which
 * are defined as the following:
 *
 * <pre>
 * Symbol   Meaning                 Presentation        Example
 * ------   -------                 ------------        -------
 * G        era designator          (Text)              AD
 * y#       year                    (Number)            1996
 * M        month in year           (Text & Number)     July & 07
 * d        day in month            (Number)            10
 * h        hour in am/pm (1~12)    (Number)            12
 * H        hour in day (0~23)      (Number)            0
 * m        minute in hour          (Number)            30
 * s        second in minute        (Number)            55
 * S        fractional second       (Number)            978
 * E        day of week             (Text)              Tuesday
 * D        day in year             (Number)            189
 * a        am/pm marker            (Text)              PM
 * k        hour in day (1~24)      (Number)            24
 * K        hour in am/pm (0~11)    (Number)            0
 * z        time zone               (Text)              Pacific Standard Time
 * Z        time zone (RFC 822)     (Number)            -0800
 * v        time zone (generic)     (Text)              Pacific Time
 * '        escape for text         (Delimiter)         'Date='
 * ''       single quote            (Literal)           'o''clock'
 * </pre>
 *
 * The count of pattern letters determine the format. <p>
 * (Text): 4 or more pattern letters--use full form,
 *         less than 4--use short or abbreviated form if one exists.
 *         In parsing, we will always try long format, then short. <p>
 * (Number): the minimum number of digits. <p>
 * (Text & Number): 3 or over, use text, otherwise use number. <p>
 * Any characters that not in the pattern will be treated as quoted text. For
 * instance, characters like ':', '.', ' ', '#' and '@' will appear in the
 * resulting time text even they are not embraced within single quotes. In our
 * current pattern usage, we didn't use up all letters. But those unused
 * letters are strongly discouraged to be used as quoted text without quote.
 * That's because we may use other letter for pattern in future. <p>
 *
 * Examples Using the US Locale:
 *
 * Format Pattern                         Result
 * --------------                         -------
 * "yyyy.MM.dd G 'at' HH:mm:ss vvvv" ->>  1996.07.10 AD at 15:08:56 Pacific Time
 * "EEE, MMM d, ''yy"                ->>  Wed, July 10, '96
 * "h:mm a"                          ->>  12:08 PM
 * "hh 'o''clock' a, zzzz"           ->>  12 o'clock PM, Pacific Daylight Time
 * "K:mm a, vvv"                     ->>  0:00 PM, PT
 * "yyyyy.MMMMM.dd GGG hh:mm aaa"    ->>  01996.July.10 AD 12:08 PM
 *
 * <p> When parsing a date string using the abbreviated year pattern ("yy"),
 * DateTimeParse must interpret the abbreviated year relative to some
 * century. It does this by adjusting dates to be within 80 years before and 20
 * years after the time the parse function is called. For example, using a
 * pattern of "MM/dd/yy" and a DateTimeParse instance created on Jan 1, 1997,
 * the string "01/11/12" would be interpreted as Jan 11, 2012 while the string
 * "05/04/64" would be interpreted as May 4, 1964. During parsing, only
 * strings consisting of exactly two digits, as defined by {@link
 * java.lang.Character#isDigit(char)}, will be parsed into the default
 * century. Any other numeric string, such as a one digit string, a three or
 * more digit string will be interpreted as its face value.
 *
 * <p> If the year pattern does not have exactly two 'y' characters, the year is
 * interpreted literally, regardless of the number of digits. So using the
 * pattern "MM/dd/yyyy", "01/11/12" parses to Jan 11, 12 A.D.
 *
 * <p> When numeric fields abut one another directly, with no intervening
 * delimiter characters, they constitute a run of abutting numeric fields. Such
 * runs are parsed specially. For example, the format "HHmmss" parses the input
 * text "123456" to 12:34:56, parses the input text "12345" to 1:23:45, and
 * fails to parse "1234". In other words, the leftmost field of the run is
 * flexible, while the others keep a fixed width. If the parse fails anywhere in
 * the run, then the leftmost field is shortened by one character, and the
 * entire run is parsed again. This is repeated until either the parse succeeds
 * or the leftmost field is one character in length. If the parse still fails at
 * that point, the parse of the run fails.
 *
 * <p> Now timezone parsing only support GMT:hhmm, GMT:+hhmm, GMT:-hhmm
 *
 */

/**
 * Construct a DateTimeParse object based on current locale by using
 * the symbol table passed in.
 * @constructor
 */
gadgets.i18n.DateTimeParse = function(symbol) {
    this.symbols_ = symbol;
};

/**
 * Year.
 * @type {number}
 */
gadgets.i18n.DateTimeParse.prototype.year = 0;


/**
 * Month.
 * @type {number}
 */
gadgets.i18n.DateTimeParse.prototype.month = 0;


/**
 * Day of month.
 * @type {number}
 */
gadgets.i18n.DateTimeParse.prototype.dayOfMonth = 0;


/**
 * Hours.
 * @type {number}
 */
gadgets.i18n.DateTimeParse.prototype.hours = 0;


/**
 * Minutes.
 * @type {number}
 */
gadgets.i18n.DateTimeParse.prototype.minutes = 0;


/**
 * Seconds.
 * @type {number}
 */
gadgets.i18n.DateTimeParse.prototype.seconds = 0;


/**
 * Milliseconds.
 * @type {number}
 */
gadgets.i18n.DateTimeParse.prototype.milliseconds = 0;


/**
 * Number of years prior to now that the century used to
 * disambiguate two digit years will begin
 *
 * @type {number}
 */
gadgets.i18n.DateTimeParse.ambiguousYearCenturyStart = 80;

/**
 * Apply a pattern to this Parser. The pattern string will be parsed and saved
 * in "compiled" form.
 * Note: this method is somewhat similar to the pattern parsing methold in
 *       datetimeformat. If you see something wrong here, you might want
 *       to check the other.
 * @param {string} pattern It describes the format of date string that need to
 *     be parsed.
 */
gadgets.i18n.DateTimeParse.prototype.applyPattern = function(pattern) {
    this.patternParts_ = [];
    var inQuote = false;
    var buf = '';

    for (var i = 0; i < pattern.length; i++) {
        var ch = pattern.charAt(i);

    // handle space, add literal part (if exist), and add space part
        if (ch == ' ') {
            if (buf.length > 0) {
                this.patternParts_.push({text: buf, count: 0, abutStart: false});
                buf = '';
            }
            this.patternParts_.push({text: ' ', count: 0, abutStart: false});
            while (i + 1 < pattern.length && pattern.charAt(i + 1) == ' ') {
                i++;
            }
        } else if (inQuote) {
            // inside quote, except '', just copy or exit
            if (ch == '\'') {
                if (i + 1 < pattern.length && pattern.charAt(i + 1) == '\'') {
                    // quote appeared twice continuously, interpret as one quote.
                    buf += ch;
                    ++i;
                } else {
                    // exit quote
                    inQuote = false;
                }
            } else {
                // literal
                buf += ch;
            }
        } else if (gadgets.i18n.DateTimeParse.PATTERN_CHARS_.indexOf(ch) >= 0) {
            // outside quote, it is a pattern char
            if (buf.length > 0) {
                this.patternParts_.push({text: buf, count: 0, abutStart: false});
                buf = '';
            }
            var count = this.getNextCharCount_(pattern, i);
            this.patternParts_.push({text: ch, count: count, abutStart: false});
            i += count - 1;
        } else if (ch == '\'') {
            // Two consecutive quotes is a quote literal, inside or outside of quotes.
            if (i + 1 < pattern.length && pattern.charAt(i + 1) == '\'') {
                buf += "'";
                i++;
            } else {
                inQuote = true;
            }
        } else {
            buf += ch;
        }
    }

    if (buf.length > 0) {
        this.patternParts_.push({text: buf, count: 0, abutStart: false});
    }

    this.markAbutStart_();
};


/**
 * Apply a predefined pattern to this Parser.
 * @param {number} formatType A constant used to identified the predefined
 *     pattern string stored in locale repository.
 * @return {void} nothing
 */
gadgets.i18n.DateTimeParse.prototype.applyStandardPattern = function(formatType)
{
    var pattern;
  // formatType constants are defined in a way so that following resolution is
    // possible.
    if (formatType < 4) {
        pattern = this.symbols_.DATEFORMATS[formatType];
    } else if (formatType < 8) {
        pattern = this.symbols_.TIMEFORMATS[formatType - 4];
    } else if (formatType < 12) {
        pattern = this.symbols_.DATEFORMATS[formatType - 8];
        pattern += ' ';
        pattern += this.symbols_.TIMEFORMATS[formatType - 8];
    } else {
        return this.applyStandardPattern(gadgets.i18n.MEDIUM_DATETIME_FORMAT);
    }
    return this.applyPattern(pattern);
};


/**
 * Parse the given string and fill info into date object. This version does
 * not validate the input.
 * @param {string} text The string being parsed.
 * @param {number} start The position from where parse should begin.
 * @param {Date} date The Date object to hold the parsed date.
 * @return {number} How many characters parser advanced.
 */
gadgets.i18n.DateTimeParse.prototype.parse = function(text, start, date) {
    return this.internalParse_(text, start, date, false);
};


/**
 * Parse the given string and fill info into date object.
 * @param {string} text The string being parsed.
 * @param {number} start The position from where parse should begin.
 * @param {Date} date The Date object to hold the parsed date.
 * @param {boolean} validation If true, input string need to be a valid
 *     date/time string.
 * @return {number} How many characters parser advanced.
 * @private
 */
gadgets.i18n.DateTimeParse.prototype.internalParse_ =
function(text, start, date, validation) {
    var cal = new gadgets.i18n.DateTimeParse.MyDate_();
    var parsePos = [start];

  // For parsing abutting numeric fields. 'abutPat' is the
    // offset into 'pattern' of the first of 2 or more abutting
    // numeric fields. 'abutStart' is the offset into 'text'
    // where parsing the fields begins. 'abutPass' starts off as 0
    // and increments each time we try to parse the fields.
    var abutPat = -1; // If >=0, we are in a run of abutting numeric fields
    var abutStart = 0;
    var abutPass = 0;

    for (var i = 0; i < this.patternParts_.length; ++i) {
        if (this.patternParts_[i].count > 0) {
            if (abutPat < 0 && this.patternParts_[i].abutStart) {
                abutPat = i;
                abutStart = start;
                abutPass = 0;
            }

      // Handle fields within a run of abutting numeric fields. Take
            // the pattern "HHmmss" as an example. We will try to parse
            // 2/2/2 characters of the input text, then if that fails,
            // 1/2/2. We only adjust the width of the leftmost field; the
            // others remain fixed. This allows "123456" => 12:34:56, but
            // "12345" => 1:23:45. Likewise, for the pattern "yyyyMMdd" we
            // try 4/2/2, 3/2/2, 2/2/2, and finally 1/2/2.
            if (abutPat >= 0) {
                // If we are at the start of a run of abutting fields, then
                // shorten this field in each pass. If we can't shorten
                // this field any more, then the parse of this set of
                // abutting numeric fields has failed.
                var count = this.patternParts_[i].count;
                if (i == abutPat) {
                    count -= abutPass;
                    abutPass++;
                    if (count == 0) {
                        // tried all possible width, fail now
                        return 0;
                    }
                }

                if (!this.subParse_(text, parsePos, this.patternParts_[i], count,
                        cal)) {
                    // If the parse fails anywhere in the run, back up to the
                    // start of the run and retry.
                    i = abutPat - 1;
                    parsePos[0] = abutStart;
                    continue;
                }
            }

                // Handle non-numeric fields and non-abutting numeric fields.
            else {
                abutPat = -1;
                if (!this.subParse_(text, parsePos, this.patternParts_[i], 0, cal)) {
                    return 0;
                }
            }
        } else {
            // Handle literal pattern characters. These are any
            // quoted characters and non-alphabetic unquoted
            // characters.
            abutPat = -1;
      // A run of white space in the pattern matches a run
            // of white space in the input text.
            if (this.patternParts_[i].text.charAt(0) == ' ') {
                // Advance over run in input text
                var s = parsePos[0];
                this.skipSpace_(text, parsePos);

        // Must see at least one white space char in input
                if (parsePos[0] > s) {
                    continue;
                }
            } else if (text.indexOf(this.patternParts_[i].text, parsePos[0]) ==
                       parsePos[0]) {
                parsePos[0] += this.patternParts_[i].text.length;
                continue;
            }
      // We fall through to this point if the match fails
            return 0;
        }
    }

  // return progress
    return cal.calcDate_(date, validation) ? parsePos[0] - start : 0;
};

/**
 * Calculate character repeat count in pattern.
 *
 * @param {string} pattern It describes the format of date string that need to
 *     be parsed.
 * @param {number} start the position of pattern character.
 *
 * @return {number} Repeat count.
 * @private
 */
gadgets.i18n.DateTimeParse.prototype.getNextCharCount_ =
function(pattern, start) {
    var ch = pattern.charAt(start);
    var next = start + 1;
    while (next < pattern.length && pattern.charAt(next) == ch) {
        ++next;
    }
    return next - start;
};

/**
 * All acceptable pattern characters.
 * @private
 */
gadgets.i18n.DateTimeParse.PATTERN_CHARS_ = 'GyMdkHmsSEDahKzZv';

/**
 * Pattern characters that specify numerical field.
 * @private
 */
gadgets.i18n.DateTimeParse.NUMERIC_FORMAT_CHARS_ = 'MydhHmsSDkK';

/**
 * Check if the pattern part is a numeric field.
 *
 * @param {Object} part pattern part to be examined.
 *
 * @return {boolean} true if the pattern part is numberic field.
 * @private
 */
gadgets.i18n.DateTimeParse.prototype.isNumericField_ = function(part) {
    if (part.count <= 0) {
        return false;
    }
    var i = gadgets.i18n.DateTimeParse.NUMERIC_FORMAT_CHARS_.indexOf(
            part.text.charAt(0));
    return i > 0 || i == 0 && part.count < 3;
};


/**
 * Identify the start of an abutting numeric fields' run. Taking pattern
 * "HHmmss" as an example. It will try to parse 2/2/2 characters of the input
 * text, then if that fails, 1/2/2. We only adjust the width of the leftmost
 * field; the others remain fixed. This allows "123456" => 12:34:56, but
 * "12345" => 1:23:45. Likewise, for the pattern "yyyyMMdd" we try 4/2/2,
 * 3/2/2, 2/2/2, and finally 1/2/2. The first field of connected numeric
 * fields will be marked as abutStart, its width can be reduced to accomodate
 * others.
 *
 * @private
 */
gadgets.i18n.DateTimeParse.prototype.markAbutStart_ = function() {
    // abut parts are continuous numeric parts. abutStart is the switch
    // point from non-abut to abut
    var abut = false;

    for (var i = 0; i < this.patternParts_.length; i++) {
        if (this.isNumericField_(this.patternParts_[i])) {
            // if next part is not following abut sequence, and isNumericField_
            if (!abut && i + 1 < this.patternParts_.length &&
                this.isNumericField_(this.patternParts_[i + 1])) {
                abut = true;
                this.patternParts_[i].abutStart = true;
            }
        } else {
            abut = false;
        }
    }
};


/**
 * Skip space in the string.
 *
 * @param {string} text input string.
 * @param {Array} pos where skip start, and return back where the skip stops.
 * @private
 */
gadgets.i18n.DateTimeParse.prototype.skipSpace_ = function(text, pos) {
    var m = text.substring(pos[0]).match(/^\s+/);
    if (m) {
        pos[0] += m[0].length;
    }
};

/**
 * Protected method that converts one field of the input string into a
 * numeric field value.
 *
 * @param {string} text the time text to be parsed.
 * @param {Array} pos Parse position.
 * @param {Object} part the pattern part for this field.
 * @param {number} digitCount when > 0, numeric parsing must obey the count.
 * @param {Object} cal MyDate_ object that will hold parsed value.
 *
 * @return {boolean} True if it parses successfully.
 * @private
 */
gadgets.i18n.DateTimeParse.prototype.subParse_ =
function(text, pos, part, digitCount, cal) {
    this.skipSpace_(text, pos);

    var start = pos[0];
    var ch = part.text.charAt(0);

  // parse integer value if it is a numeric field
    var value = -1;
    if (this.isNumericField_(part)) {
        if (digitCount > 0) {
            if ((start + digitCount) > text.length) {
                return false;
            }
            value = this.parseInt_(
                    text.substring(0, start + digitCount), pos);
        } else {
            value = this.parseInt_(text, pos);
        }
    }

    switch (ch) {
        case 'G': // ERA
            cal.era = this.matchString_(text, pos, this.symbols_.ERAS);
            return true;
        case 'M': // MONTH
            return this.subParseMonth_(text, pos, cal, value);
        case 'E':
            return this.subParseDayOfWeek_(text, pos, cal);
        case 'a': // AM_PM
            cal.ampm = this.matchString_(text, pos, this.symbols_.AMPMS);
            return true;
        case 'y': // YEAR
            return this.subParseYear_(text, pos, start, value, part, cal);
        case 'd': // DATE
            cal.day = value;
            return true;
        case 'S': // FRACTIONAL_SECOND
            return this.subParseFractionalSeconds_(value, pos, start, cal);
        case 'h': // HOUR (1..12)
            if (value == 12) {
                value = 0;
            }
        case 'K': // HOUR (0..11)
        case 'H': // HOUR_OF_DAY (0..23)
        case 'k': // HOUR_OF_DAY (1..24)
            cal.hours = value;
            return true;
        case 'm': // MINUTE
            cal.minutes = value;
            return true;
        case 's': // SECOND
            cal.seconds = value;
            return true;

        case 'z': // ZONE_OFFSET
        case 'Z': // TIMEZONE_RFC
        case 'v': // TIMEZONE_GENERIC
            return this.subparseTimeZoneInGMT_(text, pos, cal);
        default:
            return false;
    }
};

/**
 * Parse year field. Year field is special because
 * 1) two digit year need to be resolved.
 * 2) we allow year to take a sign.
 * 3) year field participate in abut processing.
 *
 * @param {string} text the time text to be parsed.
 * @param {Array} pos Parse position.
 * @param {number} start where this field start.
 * @param {number} value integer value of year.
 * @param {Object} part the pattern part for this field.
 * @param {Object} cal MyDate_ object that will hold parsed value.
 *
 * @return {boolean} True if successful.
 * @private
 */
gadgets.i18n.DateTimeParse.prototype.subParseYear_ =
function(text, pos, start, value, part, cal) {
    var ch;
    if (value < 0) {
        //possible sign
        ch = text.charAt(pos[0]);
        if (ch != '+' && ch != '-') {
            return false;
        }
        pos[0]++;
        value = this.parseInt_(text, pos);
        if (value < 0) {
            return false;
        }
        if (ch == '-') {
            value = -value;
        }
    }

  // only if 2 digit was actually parsed, and pattern say it has 2 digit.
    if (ch == null && (pos[0] - start) == 2 && part.count == 2) {
        cal.setTwoDigitYear_(value);
    } else {
        cal.year = value;
    }
    return true;
};

/**
 * Parse Month field.
 *
 * @param {string} text the time text to be parsed.
 * @param {Array} pos Parse position.
 * @param {Object} cal MyDate_ object that will hold parsed value.
 * @param {number} value numeric value if this field is expressed using
 *      numeric pattern, or -1 if not.
 *
 * @return {boolean} True if parsing successful.
 * @private
 */
gadgets.i18n.DateTimeParse.prototype.subParseMonth_ =
function(text, pos, cal, value) {
    // when month is symbols, i.e., MMM or MMMM, value will be -1
    if (value < 0) {
        // Want to be able to parse both short and long forms.
        // Try count == 4 first:
        value = this.matchString_(text, pos, this.symbols_.MONTHS);
        if (value < 0) { // count == 4 failed, now try count == 3
            value = this.matchString_(text, pos, this.symbols_.SHORTMONTHS);
        }
        if (value < 0) {
            return false;
        }
        cal.month = value;
        return true;
    } else {
        cal.month = value - 1;
        return true;
    }
};

/**
 * Parse Day of week field.
 * @param {string} text the time text to be parsed.
 * @param {Array} pos Parse position.
 * @param {Object} cal MyDate_ object that holds parsed value.
 *
 * @return {boolean} True if successful.
 * @private
 */
gadgets.i18n.DateTimeParse.prototype.subParseDayOfWeek_ =
function(text, pos, cal) {
    // Handle both short and long forms.
    // Try count == 4 (DDDD) first:
    var value = this.matchString_(text, pos, this.symbols_.WEEKDAYS);
    if (value < 0) {
        value = this.matchString_(text, pos, this.symbols_.SHORTWEEKDAYS);
    }
    if (value < 0) {
        return false;
    }
    cal.dayOfWeek = value;
    return true;
};

/**
 * Parse fractional seconds field.
 *
 * @param {number} value parsed numberic value.
 * @param {Array} pos current parse position.
 * @param {number} start where this field start.
 * @param {Object} cal MyDate_ object that holds parsed value.
 *
 * @return {boolean} True if successful.
 * @private
 */
gadgets.i18n.DateTimeParse.prototype.subParseFractionalSeconds_ =
function(value, pos, start, cal) {
    // Fractional seconds left-justify
    var len = pos[0] - start;
    cal.milliseconds = len < 3 ? value * Math.pow(10, 3 - len) :
                       Math.round(value / Math.pow(10, len - 3));
    return true;
};

/**
 * Parse GMT type timezone.
 *
 * @param {string} text the time text to be parsed.
 * @param {Array} pos Parse position.
 * @param {Object} cal MyDate_ object that holds parsed value.
 *
 * @return {boolean} True if successful.
 * @private
 */
gadgets.i18n.DateTimeParse.prototype.subparseTimeZoneInGMT_ =
function(text, pos, cal) {
    // First try to parse generic forms such as GMT-07:00. Do this first
    // in case localized DateFormatZoneData contains the string "GMT"
    // for a zone; in that case, we don't want to match the first three
    // characters of GMT+/-HH:MM etc.

    // For time zones that have no known names, look for strings
    // of the form:
    //    GMT[+-]hours:minutes or
    //    GMT[+-]hhmm or
    //    GMT.
    if (text.indexOf('GMT', pos[0]) == pos[0]) {
        pos[0] += 3;  // 3 is the length of GMT
        return this.parseTimeZoneOffset_(text, pos, cal);
    }

  // TODO(shanjian): check for named time zones by looking through the locale
    // data from the DateFormatZoneData strings. should parse both short and long
    // forms.
    // subParseZoneString(text, start, cal);

    // As a last resort, look for numeric timezones of the form
    // [+-]hhmm as specified by RFC 822.  This code is actually
    // a little more permissive than RFC 822.  It will try to do
    // its best with numbers that aren't strictly 4 digits long.
    return this.parseTimeZoneOffset_(text, pos, cal);
};

/**
 * Parse time zone offset.
 *
 * @param {string} text the time text to be parsed.
 * @param {Array} pos Parse position.
 * @param {Object} cal MyDate_ object that holds parsed value.
 *
 * @return {boolean} True if successful.
 * @private
 */
gadgets.i18n.DateTimeParse.prototype.parseTimeZoneOffset_ =
function(text, pos, cal) {
    if (pos[0] >= text.length) {
        cal.tzOffset = 0;
        return true;
    }

    var sign = 1;
    switch (text.charAt(pos[0])) {
        case '-': sign = -1;  // fall through
        case '+': pos[0]++;
    }

  // Look for hours:minutes or hhmm.
    var st = pos[0];
    var value = this.parseInt_(text, pos);
    if (value == 0 && pos[0] == st) {
        return false;
    }

    var offset;
    if (pos[0] < text.length && text.charAt(pos[0]) == ':') {
        // This is the hours:minutes case
        offset = value * 60;
        pos[0]++;
        st = pos[0];
        value = this.parseInt_(text, pos);
        if (value == 0 && pos[0] == st) {
            return false;
        }
        offset += value;
    } else {
        // This is the hhmm case.
        offset = value;
    // Assume "-23".."+23" refers to hours.
        if (offset < 24 && (pos[0] - st) <= 2)
            offset *= 60;
        else
            // todo: this looks questionable, should have more error checking
            offset = offset % 100 + offset / 100 * 60;
    }

    offset *= sign;
    cal.tzOffset = -offset;
    return true;
};

/**
 * Parse a integer string and return integer value.
 *
 * @param {string} text string being parsed.
 * @param {Array} pos parse position.
 *
 * @return {number} Converted integer value.
 * @private
 */
gadgets.i18n.DateTimeParse.prototype.parseInt_ = function(text, pos) {
    var m = text.substring(pos[0]).match(/^\d+/);
    if (!m) {
        return -1;
    }
    pos[0] += m[0].length;
    return parseInt(m[0], 10);
};

/**
 * Attempt to match the text at a given position against an array of strings.
 * Since multiple strings in the array may match (for example, if the array
 * contains "a", "ab", and "abc", all will match the input string "abcd") the
 * longest match is returned.
 *
 * @param {string} text The string to match to.
 * @param {Array} pos parsing position.
 * @param {Array} data The string array that is used to found match from.
 *
 * @return {number} the new start position if matching succeeded; a negative
 *     number indicating matching failure.
 * @private
 */
gadgets.i18n.DateTimeParse.prototype.matchString_ = function(text, pos, data) {
    // There may be multiple strings in the data[] array which begin with
    // the same prefix (e.g., Cerven and Cervenec (June and July) in Czech).
    // We keep track of the longest match, and return that. Note that this
    // unfortunately requires us to test all array elements.
    var bestMatchLength = 0;
    var bestMatch = -1;
    var lower_text = text.substring(pos[0]).toLowerCase();
    for (var i = 0; i < data.length; ++i) {
        var len = data[i].length;
    // Always compare if we have no match yet; otherwise only compare
        // against potentially better matches (longer strings).
        if (len > bestMatchLength &&
            lower_text.indexOf(data[i].toLowerCase()) == 0) {
            bestMatch = i;
            bestMatchLength = len;
        }
    }
    if (bestMatch >= 0) {
        pos[0] += bestMatchLength;
    }
    return bestMatch;
};


/**
 * This class hold the intermediate parsing result. After all fields are
 * consumed, final result will be resolved from this class.
 * @constructor
 * @private
 */
gadgets.i18n.DateTimeParse.MyDate_ = function() {
};

/**
 * 2 digit year special handling. Assuming for example that the
 * defaultCenturyStart is 6/18/1903. This means that two-digit years will be
 * forced into the range 6/18/1903 to 6/17/2003. As a result, years 00, 01, and
 * 02 correspond to 2000, 2001, and 2002. Years 04, 05, etc. correspond
 * to 1904, 1905, etc. If the year is 03, then it is 2003 if the
 * other fields specify a date before 6/18, or 1903 if they specify a
 * date afterwards. As a result, 03 is an ambiguous year. All other
 * two-digit years are unambiguous.
 *
 * @param {number} year 2 digit year value before adjustment.
 * @return {number} disambiguated year
 * @private
 */
gadgets.i18n.DateTimeParse.MyDate_.prototype.setTwoDigitYear_ = function(year)
{
    var now = new Date();
    var defaultCenturyStartYear =
            now.getFullYear() - gadgets.i18n.DateTimeParse.ambiguousYearCenturyStart;
    var ambiguousTwoDigitYear = defaultCenturyStartYear % 100;
    this.ambiguousYear = (year == ambiguousTwoDigitYear);
    year += Math.floor(defaultCenturyStartYear / 100) * 100 +
            (year < ambiguousTwoDigitYear ? 100 : 0);
    return this.year = year;
};

/**
 * Based on the fields set, fill a Date object. For those fields that not
 * set, use the passed in date object's value.
 *
 * @param {Date} date Date object to be filled.
 * @param {boolean} validation If true, input string will be checked to make
 *     sure it is valid.
 *
 * @return {boolean} false if fields specify a invalid date.
 * @private
 */
gadgets.i18n.DateTimeParse.MyDate_.prototype.calcDate_ =
function(date, validation) {
    // year 0 is 1 BC, and so on.
    if (this.era != undefined && this.year != undefined &&
        this.era == 0 && this.year > 0) {
        this.year = -(this.year - 1);
    }

    if (this.year != undefined) {
        date.setFullYear(this.year);
    }

  // The setMonth and setDate logic is a little tricky. We need to make sure
    // day of month is smaller enough so that it won't cause a month switch when
    // setting month. For example, if data in date is Nov 30, when month is set
    // to Feb, because there is no Feb 30, JS adjust it to Mar 2. So Feb 12 will
    // become  Mar 12.
    var org_date = date.getDate();
    date.setDate(1); // every month has a 1st day, this can actually be anything
    // less than 29.

    if (this.month != undefined) {
        date.setMonth(this.month);
    }

    if (this.day != undefined) {
        date.setDate(this.day);
    } else {
        date.setDate(org_date);
    }

    if (this.hours == undefined) {
        this.hours = date.getHours();
    }

  // adjust ampm
    if (this.ampm != undefined && this.ampm > 0) {
        if (this.hours < 12) {
            this.hours += 12;
        }
    }
    date.setHours(this.hours);

    if (this.minutes != undefined) {
        date.setMinutes(this.minutes);
    }

    if (this.seconds != undefined) {
        date.setSeconds(this.seconds);
    }

    if (this.milliseconds != undefined) {
        date.setMilliseconds(this.milliseconds);
    }

  // If validation is needed, verify that the uncalculated date fields
    // match the calculated date fields.  We do this before we set the
    // timezone offset, which will skew all of the dates.
    //
    // Don't need to check the day of week as it is guaranteed to be
    // correct or return false below.
    if (validation &&
        (this.year != undefined && this.year != date.getFullYear() ||
         this.month != undefined && this.month != date.getMonth() ||
         this.dayOfMonth != undefined && this.dayOfMonth != date.getDate() ||
         this.hours >= 24 || this.minutes >= 60 || this.seconds >= 60 ||
         this.milliseconds >= 1000)) {
        return false;
    }

  // adjust time zone
    if (this.tzOffset != undefined) {
        var offset = date.getTimezoneOffset();
        date.setTime(date.getTime() + (this.tzOffset - offset) * 60 * 1000);
    }

  // resolve ambiguous year if needed
    if (this.ambiguousYear) { // the two-digit year == the default start year
        var defaultCenturyStart = new Date();
        defaultCenturyStart.setFullYear(
                defaultCenturyStart.getFullYear() -
                gadgets.i18n.DateTimeParse.ambiguousYearCenturyStart);
        if (date.getTime() < defaultCenturyStart.getTime()) {
            date.setFullYear(defaultCenturyStart.getFullYear() + 100);
        }
    }

  // dayOfWeek, validation only
    if (this.dayOfWeek != undefined) {
        if (this.day == undefined) {
            // adjust to the nearest day of the week
            var adjustment = (7 + this.dayOfWeek - date.getDay()) % 7;
            if (adjustment > 3) {
                adjustment -= 7;
            }
            var orgMonth = date.getMonth();
            date.setDate(date.getDate() + adjustment);

      // don't let it switch month
            if (date.getMonth() != orgMonth) {
                date.setDate(date.getDate() + (adjustment > 0 ? -7 : 7));
            }
        } else if (this.dayOfWeek != date.getDay()) {
            return false;
        }
    }
    return true;
};


/**
 * Base Function 3
 */
/**
 * Construct a NumberFormat object based on current locale by using
 * the symbol table passed in.
 * @constructor
 */
gadgets.i18n.NumberFormat = function(symbol) {
    this.symbols_ = symbol;
};

/**
 * Apply a predefined pattern to NumberFormat object.
 * @param {number} patternType The number that indicates a predefined number
 *     format pattern.
 * @param {string} opt_currency Optional international currency code. This
 *     determines the currency code/symbol used in format/parse. If not given,
 *     the currency code for current locale will be used.
 */
gadgets.i18n.NumberFormat.prototype.applyStandardPattern =
function(patternType, opt_currency) {
    switch (patternType) {
        case gadgets.i18n.DECIMAL_PATTERN:
            this.applyPattern(this.symbols_.DECIMAL_PATTERN, opt_currency);
            break;
        case gadgets.i18n.SCIENTIFIC_PATTERN:
            this.applyPattern(this.symbols_.SCIENTIFIC_PATTERN, opt_currency);
            break;
        case gadgets.i18n.PERCENT_PATTERN:
            this.applyPattern(this.symbols_.PERCENT_PATTERN, opt_currency);
            break;
        case gadgets.i18n.CURRENCY_PATTERN:
            this.applyPattern(this.symbols_.CURRENCY_PATTERN, opt_currency);
            break;
        default:
            throw Error('Unsupported pattern type.');
    }
};


/**
 * Apply a pattern to NumberFormat object.
 * @param {string} pattern The number format pattern string.
 * @param {string} opt_currency Optional international currency code. This
 *     determines the currency code/symbol used in format/parse. If not given,
 *     the currency code for current locale will be used.
 */
gadgets.i18n.NumberFormat.prototype.applyPattern =
function(pattern, opt_currency) {
    this.pattern_ = pattern;
    this.intlCurrencyCode_ = opt_currency || this.symbols_.DEF_CURRENCY_CODE;
    this.currencySymbol_ = gadgets.i18n.CurrencyCodeMap[this.intlCurrencyCode_];

    this.maximumIntegerDigits_ = 40;
    this.minimumIntegerDigits_ = 1;
    this.maximumFractionDigits_ = 3; // invariant, >= minFractionDigits
    this.minimumFractionDigits_ = 0;
    this.minExponentDigits_ = 0;

    this.positivePrefix_ = '';
    this.positiveSuffix_ = '';
    this.negativePrefix_ = '-';
    this.negativeSuffix_ = '';

  // The multiplier for use in percent, per mille, etc.
    this.multiplier_ = 1;
    this.groupingSize_ = 3;
    this.decimalSeparatorAlwaysShown_ = false;
    this.isCurrencyFormat_ = false;
    this.useExponentialNotation_ = false;

    this.parsePattern_(this.pattern_);
};


/**
 * Parses text string to produce a Number.
 *
 * This method attempts to parse text starting from position "opt_pos" if it
 * is given. Otherwise the parse will start from the beginning of the text.
 * When opt_pos presents, opt_pos will be updated to the character next to where
 * parsing stops after the call. If an error occurs, opt_pos won't be updated.
 *
 * @param {string} text the string to be parsed.
 * @param {Array} opt_pos position to pass in and get back.
 * @return {number} Parsed number, or 0 if the parse fails.
 */
gadgets.i18n.NumberFormat.prototype.parse = function(text, opt_pos) {
    var pos = opt_pos || [0];

    var start = pos[0];
    var ret = 0;

    var gotPositive = text.indexOf(this.positivePrefix_, pos[0]) == pos[0];
    var gotNegative = text.indexOf(this.negativePrefix_, pos[0]) == pos[0];

  // check for the longest match
    if (gotPositive && gotNegative) {
        if (this.positivePrefix_.length > this.negativePrefix_.length) {
            gotNegative = false;
        } else if (this.positivePrefix_.length < this.negativePrefix_.length) {
            gotPositive = false;
        }
    }

    if (gotPositive) {
        pos[0] += this.positivePrefix_.length;
    } else if (gotNegative) {
        pos[0] += this.negativePrefix_.length;
    }

  // process digits or Inf, find decimal position
    if (text.indexOf(this.symbols_.INFINITY, pos[0]) == pos[0]) {
        pos[0] += this.symbols_.INFINITY.length;
        ret = Infinity;
    } else {
        ret = this.parseNumber_(text, pos);
    }

  // check for suffix
    if (gotPositive) {
        if (!(text.indexOf(this.positiveSuffix_, pos[0]) == pos[0])) {
            pos[0] = start;
            return 0;
        }
        pos[0] += this.positiveSuffix_.length;
    } else if (gotNegative) {
        if (!(text.indexOf(this.negativeSuffix_, pos[0]) == pos[0])) {
            pos[0] = start;
            return 0;
        }
        pos[0] += this.negativeSuffix_.length;
    }

    return gotNegative ? -ret : ret;
};


/**
 * This function will parse a "localized" text into a Number. It needs to
 * handle locale specific decimal, grouping, exponent and digits.
 *
 * @param {string} text The text that need to be parsed.
 * @param {Array} pos  In/out parsing position. In case of failure, pos value
 *   won't be changed.
 * @return {number} Number value, could be 0.0 if nothing can be parsed.
 * @private
 */
gadgets.i18n.NumberFormat.prototype.parseNumber_ = function(text, pos) {
    var sawDecimal = false;
    var sawExponent = false;
    var sawDigit = false;
    var scale = 1;
    var decimal = this.isCurrencyFormat_ ? this.symbols_.MONETARY_SEP :
                  this.symbols_.DECIMAL_SEP;
    var grouping = this.isCurrencyFormat_ ? this.symbols_.MONETARY_GROUP_SEP :
                   this.symbols_.GROUP_SEP;
    var exponentChar = this.symbols_.EXP_SYMBOL;

    var normalizedText = '';
    for (; pos[0] < text.length; pos[0]++) {
        var ch = text.charAt(pos[0]);
        var digit = this.getDigit_(ch);
        if (digit >= 0 && digit <= 9) {
            normalizedText += digit;
            sawDigit = true;
        } else if (ch == decimal.charAt(0)) {
            if (sawDecimal || sawExponent) {
                break;
            }
            normalizedText += '.';
            sawDecimal = true;
        } else if (ch == grouping.charAt(0) || '\u00a0' == grouping.charAt(0) &&
                                               ch == ' ' && pos[0] + 1 < text.length &&
                                               this.getDigit_(text.charAt(pos[0] + 1)) >= 0) {
            if (sawDecimal || sawExponent) {
                break;
            }
            continue;
        } else if (ch == exponentChar.charAt(0)) {
            if (sawExponent) {
                break;
            }
            normalizedText += 'E';
            sawExponent = true;
        } else if (ch == '+' || ch == '-') {
            normalizedText += ch;
        } else if (ch == this.symbols_.PERCENT.charAt(0)) {
            if (scale != 1) {
                break;
            }
            scale = 100;
            if (sawDigit) {
                pos[0]++; // eat this character if parse end here
                break;
            }
        } else if (ch == this.symbols_.PERMILL.charAt(0)) {
            if (scale != 1) {
                break;
            }
            scale = 1000;
            if (sawDigit) {
                pos[0]++; // eat this character if parse end here
                break;
            }
        } else {
            break;
        }
    }
    return parseFloat(normalizedText) / scale;
};


/**
 * Formats a Number to produce a string.
 *
 * @param {number} number The Number to be formatted.
 * @return {string} The formatted number string.
 */
gadgets.i18n.NumberFormat.prototype.format = function(number) {
    if (isNaN(number)) {
        return this.symbols_.NAN;
    }

    var parts = [];

  // in icu code, it is commented that certain computation need to keep the
    // negative sign for 0.
    var isNegative = number < 0.0 || number == 0.0 && 1 / number < 0.0;

    parts.push(isNegative ? this.negativePrefix_ : this.positivePrefix_);

    if (!isFinite(number)) {
        parts.push(this.symbols_.INFINITY);
    } else {
        // convert number to non-negative value
        number *= isNegative ? -1 : 1;

        number *= this.multiplier_;
        this.useExponentialNotation_ ?
        this.subformatExponential_(number, parts) :
        this.subformatFixed_(number, this.minimumIntegerDigits_, parts);
    }

    parts.push(isNegative ? this.negativeSuffix_ : this.positiveSuffix_);

    return parts.join('');
};


/**
 * Formats a Number in fraction format.
 *
 * @param {number} number Value need to be formated.
 * @param {number} minIntDigits Minimum integer digits.
 * @param {Array} parts This array holds the pieces of formatted string.
 *     This function will add its formatted pieces to the array.
 * @private
 */
gadgets.i18n.NumberFormat.prototype.subformatFixed_ = function(number,
                                                               minIntDigits,
                                                               parts) {
    // round the number
    var power = Math.pow(10, this.maximumFractionDigits_);
    number = Math.round(number * power);
    var intValue = Math.floor(number / power);
    var fracValue = Math.floor(number - intValue * power);

    var fractionPresent = this.minimumFractionDigits_ > 0 || fracValue > 0;

    var intPart = '';
    var translatableInt = intValue;
    while (translatableInt > 1E20) {
        // here it goes beyond double precision, add '0' make it look better
        intPart = '0' + intPart;
        translatableInt = Math.round(translatableInt / 10);
    }
    intPart = translatableInt + intPart;

    var decimal = this.isCurrencyFormat_ ? this.symbols_.MONETARY_SEP :
                  this.symbols_.DECIMAL_SEP;
    var grouping = this.isCurrencyFormat_ ? this.symbols_.MONETARY_GROUP_SEP :
                   this.symbols_.GROUP_SEP;

    var zeroCode = this.symbols_.ZERO_DIGIT.charCodeAt(0);
    var digitLen = intPart.length;

    if (intValue > 0 || minIntDigits > 0) {
        for (var i = digitLen; i < minIntDigits; i++) {
            parts.push(this.symbols_.ZERO_DIGIT);
        }

        for (var i = 0; i < digitLen; i++) {
            parts.push(String.fromCharCode(zeroCode + intPart.charAt(i) * 1));

            if (digitLen - i > 1 && this.groupingSize_ > 0 &&
                ((digitLen - i) % this.groupingSize_ == 1)) {
                parts.push(grouping);
            }
        }
    } else if (!fractionPresent) {
        // If there is no fraction present, and we haven't printed any
        // integer digits, then print a zero.
        parts.push(this.symbols_.ZERO_DIGIT);
    }

  // Output the decimal separator if we always do so.
    if (this.decimalSeparatorAlwaysShown_ || fractionPresent) {
        parts.push(decimal);
    }

    var fracPart = '' + (fracValue + power);
    var fracLen = fracPart.length;
    while (fracPart.charAt(fracLen - 1) == '0' &&
           fracLen > this.minimumFractionDigits_ + 1) {
        fracLen--;
    }

    for (var i = 1; i < fracLen; i++) {
        parts.push(String.fromCharCode(zeroCode + fracPart.charAt(i) * 1));
    }
};


/**
 * Formats exponent part of a Number.
 *
 * @param {number} exponent exponential value.
 * @param {Array} parts This array holds the pieces of formatted string.
 *     This function will add its formatted pieces to the array.
 * @private
 */
gadgets.i18n.NumberFormat.prototype.addExponentPart_ = function(exponent,
                                                                parts) {
    parts.push(this.symbols_.EXP_SYMBOL);

    if (exponent < 0) {
        exponent = -exponent;
        parts.push(this.symbols_.MINUS_SIGN);
    }

    var exponentDigits = '' + exponent;
    for (var i = exponentDigits.length; i < this.minExponentDigits_; i++) {
        parts.push(this.symbols_.ZERO_DIGIT);
    }
    parts.push(exponentDigits);
};


/**
 * Formats Number in exponential format.
 *
 * @param {number} number Value need to be formated.
 * @param {Array} parts This array holds the pieces of formatted string.
 *     This function will add its formatted pieces to the array.
 * @private
 */
gadgets.i18n.NumberFormat.prototype.subformatExponential_ = function(number,
                                                                     parts) {
    if (number == 0.0) {
        this.subformatFixed_(number, this.minimumIntegerDigits_, parts);
        this.addExponentPart_(0, parts);
        return;
    }

    var exponent = Math.floor(Math.log(number) / Math.log(10));
    number /= Math.pow(10, exponent);

    var minIntDigits = this.minimumIntegerDigits_;
    if (this.maximumIntegerDigits_ > 1 &&
        this.maximumIntegerDigits_ > this.minimumIntegerDigits_) {
        // A repeating range is defined; adjust to it as follows.
        // If repeat == 3, we have 6,5,4=>3; 3,2,1=>0; 0,-1,-2=>-3;
        // -3,-4,-5=>-6, etc. This takes into account that the
        // exponent we have here is off by one from what we expect;
        // it is for the format 0.MMMMMx10^n.
        while ((exponent % this.maximumIntegerDigits_) != 0) {
            number *= 10;
            exponent--;
        }
        minIntDigits = 1;
    } else {
        // No repeating range is defined; use minimum integer digits.
        if (this.minimumIntegerDigits_ < 1) {
            exponent++;
            number /= 10;
        } else {
            exponent -= this.minimumIntegerDigits_ - 1;
            number *= Math.pow(10, this.minimumIntegerDigits_ - 1);
        }
    }
    this.subformatFixed_(number, minIntDigits, parts);
    this.addExponentPart_(exponent, parts);
};


/**
 * Returns the digit value of current character. The character could be either
 * '0' to '9', or a locale specific digit.
 *
 * @param {string} ch Character that represents a digit.
 * @return {number} The digit value, or -1 on error.
 * @private
 */
gadgets.i18n.NumberFormat.prototype.getDigit_ = function(ch) {
    var code = ch.charCodeAt(0);
  // between '0' to '9'
    if (48 <= code && code < 58) {
        return code - 48;
    } else {
        var zeroCode = this.symbols_.ZERO_DIGIT.charCodeAt(0);
        return zeroCode <= code && code < zeroCode + 10 ? code - zeroCode : -1;
    }
};


// ----------------------------------------------------------------------
// CONSTANTS
// ----------------------------------------------------------------------
// Constants for characters used in programmatic (unlocalized) patterns.
/**
 * A zero digit character.
 * @type {string}
 * @private
 */
gadgets.i18n.NumberFormat.PATTERN_ZERO_DIGIT_ = '0';


/**
 * A grouping separator character.
 * @type {string}
 * @private
 */
gadgets.i18n.NumberFormat.PATTERN_GROUPING_SEPARATOR_ = ',';


/**
 * A decimal separator character.
 * @type {string}
 * @private
 */
gadgets.i18n.NumberFormat.PATTERN_DECIMAL_SEPARATOR_ = '.';


/**
 * A per mille character.
 * @type {string}
 * @private
 */
gadgets.i18n.NumberFormat.PATTERN_PER_MILLE_ = '\u2030';


/**
 * A percent character.
 * @type {string}
 * @private
 */
gadgets.i18n.NumberFormat.PATTERN_PERCENT_ = '%';


/**
 * A digit character.
 * @type {string}
 * @private
 */
gadgets.i18n.NumberFormat.PATTERN_DIGIT_ = '#';


/**
 * A separator character.
 * @type {string}
 * @private
 */
gadgets.i18n.NumberFormat.PATTERN_SEPARATOR_ = ';';


/**
 * An exponent character.
 * @type {string}
 * @private
 */
gadgets.i18n.NumberFormat.PATTERN_EXPONENT_ = 'E';


/**
 * A minus character.
 * @type {string}
 * @private
 */
gadgets.i18n.NumberFormat.PATTERN_MINUS_ = '-';


/**
 * A quote character.
 * @type {string}
 * @private
 */
gadgets.i18n.NumberFormat.PATTERN_CURRENCY_SIGN_ = '\u00A4';


/**
 * A quote character.
 * @type {string}
 * @private
 */
gadgets.i18n.NumberFormat.QUOTE_ = '\'';


/**
 * Parses affix part of pattern.
 *
 * @param {string} pattern Pattern string that need to be parsed.
 * @param {Array} pos  One element position array to set and receive parsing
 *     position.
 *
 * @return {string} affix received from parsing.
 * @private
 */
gadgets.i18n.NumberFormat.prototype.parseAffix_ = function(pattern, pos) {
    var affix = '';
    var inQuote = false;
    var len = pattern.length;

    for (; pos[0] < len; pos[0]++) {
        var ch = pattern.charAt(pos[0]);
        if (ch == gadgets.i18n.NumberFormat.QUOTE_) {
            if (pos[0] + 1 < len &&
                pattern.charAt(pos[0] + 1) == gadgets.i18n.NumberFormat.QUOTE_) {
                pos[0]++;
                affix += '\''; // 'don''t'
            } else {
                inQuote = !inQuote;
            }
            continue;
        }

        if (inQuote) {
            affix += ch;
        } else {
            switch (ch) {
                case gadgets.i18n.NumberFormat.PATTERN_DIGIT_:
                case gadgets.i18n.NumberFormat.PATTERN_ZERO_DIGIT_:
                case gadgets.i18n.NumberFormat.PATTERN_GROUPING_SEPARATOR_:
                case gadgets.i18n.NumberFormat.PATTERN_DECIMAL_SEPARATOR_:
                case gadgets.i18n.NumberFormat.PATTERN_SEPARATOR_:
                    return affix;
                case gadgets.i18n.NumberFormat.PATTERN_CURRENCY_SIGN_:
                    this.isCurrencyFormat_ = true;
                    if ((pos[0] + 1) < len &&
                        pattern.charAt(pos[0] + 1) ==
                        gadgets.i18n.NumberFormat.PATTERN_CURRENCY_SIGN_) {
                        pos[0]++;
                        affix += this.intlCurrencyCode_;
                    } else {
                        affix += this.currencySymbol_;
                    }
                    break;
                case gadgets.i18n.NumberFormat.PATTERN_PERCENT_:
                    if (this.multiplier_ != 1) {
                        throw Error('Too many percent/permill');
                    }
                    this.multiplier_ = 100;
                    affix += this.symbols_.PERCENT;
                    break;
                case gadgets.i18n.NumberFormat.PATTERN_PER_MILLE_:
                    if (this.multiplier_ != 1) {
                        throw Error('Too many percent/permill');
                    }
                    this.multiplier_ = 1000;
                    affix += this.symbols_.PERMILL;
                    break;
                default:
                    affix += ch;
            }
        }
    }

    return affix;
};


/**
 * Parses the trunk part of a pattern.
 *
 * @param {string} pattern Pattern string that need to be parsed.
 * @param {Array} pos One element position array to set and receive parsing
 *     position.
 * @private
 */
gadgets.i18n.NumberFormat.prototype.parseTrunk_ = function(pattern, pos) {
    var decimalPos = -1;
    var digitLeftCount = 0;
    var zeroDigitCount = 0;
    var digitRightCount = 0;
    var groupingCount = -1;

    var len = pattern.length;
    for (var loop = true; pos[0] < len && loop; pos[0]++) {
        var ch = pattern.charAt(pos[0]);
        switch (ch) {
            case gadgets.i18n.NumberFormat.PATTERN_DIGIT_:
                if (zeroDigitCount > 0) {
                    digitRightCount++;
                } else {
                    digitLeftCount++;
                }
                if (groupingCount >= 0 && decimalPos < 0) {
                    groupingCount++;
                }
                break;
            case gadgets.i18n.NumberFormat.PATTERN_ZERO_DIGIT_:
                if (digitRightCount > 0) {
                    throw Error('Unexpected "0" in pattern "' + pattern + '"');
                }
                zeroDigitCount++;
                if (groupingCount >= 0 && decimalPos < 0) {
                    groupingCount++;
                }
                break;
            case gadgets.i18n.NumberFormat.PATTERN_GROUPING_SEPARATOR_:
                groupingCount = 0;
                break;
            case gadgets.i18n.NumberFormat.PATTERN_DECIMAL_SEPARATOR_:
                if (decimalPos >= 0) {
                    throw Error('Multiple decimal separators in pattern "'
                            + pattern + '"');
                }
                decimalPos = digitLeftCount + zeroDigitCount + digitRightCount;
                break;
            case gadgets.i18n.NumberFormat.PATTERN_EXPONENT_:
                if (this.useExponentialNotation_) {
                    throw Error('Multiple exponential symbols in pattern "'
                            + pattern + '"');
                }
                this.useExponentialNotation_ = true;
                this.minExponentDigits_ = 0;

            // Use lookahead to parse out the exponential part
            // of the pattern, then jump into phase 2.
                while ((pos[0] + 1) < len && pattern.charAt(pos[0] + 1) ==
                                             this.symbols_.ZERO_DIGIT.charAt(0)) {
                    pos[0]++;
                    this.minExponentDigits_++;
                }

                if ((digitLeftCount + zeroDigitCount) < 1 ||
                    this.minExponentDigits_ < 1) {
                    throw Error('Malformed exponential pattern "' + pattern + '"');
                }
                loop = false;
                break;
            default:
                pos[0]--;
                loop = false;
                break;
        }
    }

    if (zeroDigitCount == 0 && digitLeftCount > 0 && decimalPos >= 0) {
        // Handle '###.###' and '###.' and '.###'
        var n = decimalPos;
        if (n == 0) { // Handle '.###'
            n++;
        }
        digitRightCount = digitLeftCount - n;
        digitLeftCount = n - 1;
        zeroDigitCount = 1;
    }

  // Do syntax checking on the digits.
    if (decimalPos < 0 && digitRightCount > 0 ||
        decimalPos >= 0 && (decimalPos < digitLeftCount ||
                            decimalPos > digitLeftCount + zeroDigitCount) ||
        groupingCount == 0) {
        throw Error('Malformed pattern "' + pattern + '"');
    }
    var totalDigits = digitLeftCount + zeroDigitCount + digitRightCount;

    this.maximumFractionDigits_ = decimalPos >= 0 ? totalDigits - decimalPos : 0;
    if (decimalPos >= 0) {
        this.minimumFractionDigits_ = digitLeftCount + zeroDigitCount - decimalPos;
        if (this.minimumFractionDigits_ < 0) {
            this.minimumFractionDigits_ = 0;
        }
    }

  // The effectiveDecimalPos is the position the decimal is at or would be at
    // if there is no decimal. Note that if decimalPos<0, then digitTotalCount ==
    // digitLeftCount + zeroDigitCount.
    var effectiveDecimalPos = decimalPos >= 0 ? decimalPos : totalDigits;
    this.minimumIntegerDigits_ = effectiveDecimalPos - digitLeftCount;
    if (this.useExponentialNotation_) {
        this.maximumIntegerDigits_ = digitLeftCount + this.minimumIntegerDigits_;

    // in exponential display, we need to at least show something.
        if (this.maximumFractionDigits_ == 0 && this.minimumIntegerDigits_ == 0) {
            this.minimumIntegerDigits_ = 1;
        }
    }

    this.groupingSize_ = Math.max(0, groupingCount);
    this.decimalSeparatorAlwaysShown_ = decimalPos == 0 ||
                                        decimalPos == totalDigits;
};


/**
 * Parses provided pattern, result are stored in member variables.
 *
 * @param {string} pattern string pattern being applied.
 * @private
 */
gadgets.i18n.NumberFormat.prototype.parsePattern_ = function(pattern) {
    var pos = [0];

    this.positivePrefix_ = this.parseAffix_(pattern, pos);
    var trunkStart = pos[0];
    this.parseTrunk_(pattern, pos);
    var trunkLen = pos[0] - trunkStart;
    this.positiveSuffix_ = this.parseAffix_(pattern, pos);

    if (pos[0] < pattern.length &&
        pattern.charAt(pos[0]) == gadgets.i18n.NumberFormat.PATTERN_SEPARATOR_) {
        pos[0]++;
        this.negativePrefix_ = this.parseAffix_(pattern, pos);
    // we assume this part is identical to positive part.
        // user must make sure the pattern is correctly constructed.
        pos[0] += trunkLen;
        this.negativeSuffix_ = this.parseAffix_(pattern, pos);
    }
};

/**
 * Base Function 4: main function
 */
gadgets.i18n.dtFormatter_ = null;
gadgets.i18n.dtParser_ = null;
gadgets.i18n.numFormatter_ = null;

/**
 * Format the given date object into a string representation using pattern
 * specified.
 * @param {string/number} pattern String to specify patterns or Number used to reference predefined
 *        pattern that a date should be formatted into.
 * @param {Date} date Date object being formatted.
 *
 * @return {string} string representation of date/time.
 */
gadgets.i18n.formatDateTime = function(pattern, date) {
    if (!gadgets.i18n.dtFormatter_) {
        //gadgets.i18n.dtFormatter_ = new gadgets.i18n.DateTimeFormat(gadgets.i18n.DateTimeConstants);
        gadgets.i18n.dtFormatter_ = new gadgets.i18n.DateTimeFormat(gadgets.i18n.Constants);
        typeof pattern == 'string'
                ? gadgets.i18n.dtFormatter_.applyPattern(pattern)
                : gadgets.i18n.dtFormatter_.applyStandardPattern(pattern);
        gadgets.i18n.dtFormatter_.patternInUse_ = pattern;
    } else if (gadgets.i18n.dtFormatter_.patternInUse_ != pattern) {
        typeof pattern == 'string'
                ? gadgets.i18n.dtFormatter_.applyPattern(pattern)
                : gadgets.i18n.dtFormatter_.applyStandardPattern(pattern);
        gadgets.i18n.dtFormatter_.patternInUse_ = pattern;
    }
    return gadgets.i18n.dtFormatter_.format(date);
};


/**
 * Parse a string using the format as specified in pattern string, and
 * return date in the passed "date" parameter.
 *
 * @param {string/number} pattern String to specify patterns or Number used to
 *        reference predefined
 *        pattern that a date should be parsed from.
 * @param {string} text The string that need to be parsed.
 * @param {number} start The character position in "text" where parse begins.
 * @param {Date} date The date object that will hold parsed value.
 *
 * @return {number} The number of characters advanced or 0 if failed.
 */
gadgets.i18n.parseDateTime = function(pattern, text, start, date) {
    if (!gadgets.i18n.dtParser_) {
        //gadgets.i18n.dtParser_ = new gadgets.i18n.DateTimeParse(gadgets.i18n.DateTimeConstants);//淇敼鍘熸湁浠ｇ爜
        gadgets.i18n.dtParser_ = new gadgets.i18n.DateTimeParse(gadgets.i18n.Constants);
        typeof pattern == 'string'
                ? gadgets.i18n.dtParser_.applyPattern(pattern)
                : gadgets.i18n.dtParser_.applyStandardPattern(pattern);
        gadgets.i18n.dtParser_.patternInUse_ = pattern;
    } else if (gadgets.i18n.dtParser_.patternInUse_ != pattern) {
        typeof pattern == 'string'
                ? gadgets.i18n.dtParser_.applyPattern(pattern)
                : gadgets.i18n.dtParser_.applyStandardPattern(pattern);
        gadgets.i18n.dtParser_.patternInUse_ = pattern;
    }
    return gadgets.i18n.dtParser_.parse(text, start, date);
};


/**
 * Format the number using given pattern.
 * @param {string/number} pattern String to specify patterns or Number used to
 *        reference predefined
 *        pattern that a number should be formatted into.
 * @param {number} value The number being formatted.
 * @param {string} opt_currencyCode optional international currency code, it
 *     determines the currency code/symbol should be used in format/parse. If
 *     not given, the currency code for current locale will be used.
 * @return {string} The formatted string.
 */
gadgets.i18n.formatNumber = function(pattern, value, opt_currencyCode) {
    if (!gadgets.i18n.numFormatter_) {
        //gadgets.i18n.numFormatter_ = new gadgets.i18n.NumberFormat(gadgets.i18n.NumberFormatConstants);
        gadgets.i18n.numFormatter_ = new gadgets.i18n.NumberFormat(gadgets.i18n.Constants);
        typeof pattern == 'string'
                ? gadgets.i18n.numFormatter_.applyPattern(
                  pattern, opt_currencyCode)
                : gadgets.i18n.numFormatter_.applyStandardPattern(
                  pattern, opt_currencyCode);
        gadgets.i18n.numFormatter_.patternInUse_ = pattern;
    } else if (gadgets.i18n.numFormatter_.patternInUse_ != pattern) {
        typeof pattern == 'string'
                ? gadgets.i18n.numFormatter_.applyPattern(
                  pattern, opt_currencyCode)
                : gadgets.i18n.numFormatter_.applyStandardPattern(
                  pattern, opt_currencyCode);
        gadgets.i18n.numFormatter_.patternInUse_ = pattern;
    }
    return gadgets.i18n.numFormatter_.format(value);
};


/**
 * Parse the given text using specified pattern to get a number.
 * @param {string/number} pattern String to specify patterns or Number used
 *        to reference predefined
 *        pattern that a number should be parsed from.
 * @param {string} text input text being parsed.
 * @param {Array} opt_pos optional one element array that holds position
 *     information. It tells from where parse should begin. Upon return, it
 *     holds parse stop position.
 * @param {string} opt_currencyCode optional international currency code, it
 *     determines the currency code/symbol should be used in format/parse. If
 *     not given, the currency code for current locale will be used.
 * @return {number} Parsed number, 0 if in error.
 */
gadgets.i18n.parseNumber = function(pattern, text, opt_pos, opt_currencyCode) {
    if (!gadgets.i18n.numFormatter_) {
        gadgets.i18n.numFormatter_ = new gadgets.i18n.NumberFormat();
        typeof pattern == 'string'
                ? gadgets.i18n.numFormatter_.applyPattern(pattern,
                                                          opt_currencyCode)
                : gadgets.i18n.numFormatter_.applyStandardPattern(
                  pattern, opt_currencyCode);
        gadgets.i18n.numFormatter_.patternInUse_ = pattern;
        gadgets.i18n.numFormatter_.currencyCodeInUse_ = opt_currencyCode;
    } else if (gadgets.i18n.numFormatter_.patternInUse_ != pattern ||
               gadgets.i18n.numFormatter_.currencyCodeInUse_
                       != opt_currencyCode) {
        typeof pattern == 'string'
                ? gadgets.i18n.numFormatter_.applyPattern(pattern,
                                                          opt_currencyCode)
                : gadgets.i18n.numFormatter_.applyStandardPattern(
                  pattern, opt_currencyCode);
        gadgets.i18n.numFormatter_.patternInUse_ = pattern;
        gadgets.i18n.numFormatter_.currencyCodeInUse_ = opt_currencyCode;
    }
    return gadgets.i18n.numFormatter_.parse(text, opt_pos);
};

// Couple of constants to represent predefined Date/Time format type.

/**
 * Format for full representations of dates.
 * @type {number}
 */
gadgets.i18n.FULL_DATE_FORMAT = 0;


/**
 * Format for long representations of dates.
 * @type {number}
 */
gadgets.i18n.LONG_DATE_FORMAT = 1;


/**
 * Format for medium representations of dates.
 * @type {number}
 */
gadgets.i18n.MEDIUM_DATE_FORMAT = 2;


/**
 * Format for short representations of dates.
 * @type {number}
 */
gadgets.i18n.SHORT_DATE_FORMAT = 3;


/**
 * Format for full representations of times.
 * @type {number}
 */
gadgets.i18n.FULL_TIME_FORMAT = 4;


/**
 * Format for long representations of times.
 * @type {number}
 */
gadgets.i18n.LONG_TIME_FORMAT = 5;


/**
 * Format for medium representations of times.
 * @type {number}
 */
gadgets.i18n.MEDIUM_TIME_FORMAT = 6;


/**
 * Format for short representations of times.
 * @type {number}
 */
gadgets.i18n.SHORT_TIME_FORMAT = 7;


/**
 * Format for short representations of datetimes.
 * @type {number}
 */
gadgets.i18n.FULL_DATETIME_FORMAT = 8;


/**
 * Format for short representations of datetimes.
 * @type {number}
 */
gadgets.i18n.LONG_DATETIME_FORMAT = 9;


/**
 * Format for medium representations of datetimes.
 * @type {number}
 */
gadgets.i18n.MEDIUM_DATETIME_FORMAT = 10;


/**
 * Format for short representations of datetimes.
 * @type {number}
 */
gadgets.i18n.SHORT_DATETIME_FORMAT = 11;


/**
 * Predefined number format pattern type. The actual pattern is defined
 * separately for each locale.
 */


/**
 * Pattern for decimal numbers.
 * @type {number}
 */
gadgets.i18n.DECIMAL_PATTERN = 1;


/**
 * Pattern for scientific numbers.
 * @type {number}
 */
gadgets.i18n.SCIENTIFIC_PATTERN = 2;


/**
 * Pattern for percentages.
 * @type {number}
 */
gadgets.i18n.PERCENT_PATTERN = 3;


/**
 * Pattern for currency.
 * @type {number}
 */
gadgets.i18n.CURRENCY_PATTERN = 4;


