package com.opuscapita.healthcheck

/**
 * The controller get status of health checks
 *
 * @author Dmitry Divin
 */
class HealthController {
    def alive() {
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store")

        render text: "SERVER_IS_NOT_SHUTTING_DOWN", status: 200
    }

    def ready() {
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store")

        render text: "SERVER_IS_READY", status: 200
    }
}
