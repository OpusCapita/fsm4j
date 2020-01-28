package com.opuscapita.fsm

/**
 * Simple flow object
 *
 * @author Dmitry Divin
 */
class Invoice {
    String currencyId
    String customerId
    Double grossAmount
    String invoiceNo
    Double netAmount
    String status
    String supplierId
    Double vatAmount

    static constraints = {
        currencyId nullable: true
        customerId nullable: true
        grossAmount nullable: true
        invoiceNo nullable: true
        netAmount nullable: true
        status nullable: true
        supplierId nullable: true
        vatAmount nullable: true
    }
}
