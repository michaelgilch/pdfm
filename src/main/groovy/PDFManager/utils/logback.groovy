import java.nio.charset.Charset

def LOG_PATH = 'log'
def LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %level - %msg%n"

appender('Console-Appender', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName("UTF-8")
        pattern = LOG_PATTERN
    }
}

appender('File-Appender', RollingFileAppender) {
    file = "${LOG_PATH}/system.0.log"
    rollingPolicy(FixedWindowRollingPolicy) {
        fileNamePattern = "${LOG_PATH}/system.%i.log"
        minIndex = 1
        maxIndex = 9
    }
    triggeringPolicy(SizeBasedTriggeringPolicy) {
        maxFileSize = "10MB"
    }
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName("UTF-8")
        pattern = LOG_PATTERN
    }
}

root(INFO, ["Console-Appender", "File-Appender"])
logger("dev.softreset", DEBUG, ["Console-Appender", "File-Appender"], false)