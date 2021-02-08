package PDFManager.utils

import groovy.util.logging.Slf4j

@Slf4j
class LogHelper {

    static synchronized def logInfo(msg) {
        log.info(msg.toString())
    }

    static synchronized def logError(msg) {
        log.error(msg.toString(), null)
    }

    static synchronized def logError(msg, Exception e) {
        log.error(msg.toString(), e)
    }

    static synchronized def logWarn(msg) {
        log.warn(msg.toString())
    }

}
