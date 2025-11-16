package sql

import java.time.format.DateTimeFormatter

//======SQL========
const val url = "jdbc:mysql://localhost:3306/genshincharacters"
const val user = "root"
const val password = "password"

//=======LOGGER=========

//==========COLORS===========

const val RESET = "\u001B[0m"
const val GREEN = "\u001B[32m"
const val BLUE = "\u001B[34m"
const val RED = "\u001B[31m"
const val YELLOW = "\u001B[33m"

val TIME: DateTimeFormatter? = DateTimeFormatter.ofPattern("HH:mm:ss:ms")
val FILE: DateTimeFormatter? = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

//==========OTHER INFO===========
const val LOG_VERSION = "0.6.0"
/**
 * Limit of the maximal count of log files saved in the logs folder.
 * @see LOG_DIR
 * @see Log.cleanUp
 * */
const val MAX_LOGS = 32
/**
 * Directory of where logs are saved
 * @see Log.saveLogFiles
 * */
const val LOG_DIR = "logs"