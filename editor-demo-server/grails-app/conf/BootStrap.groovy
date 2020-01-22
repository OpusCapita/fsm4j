import com.opuscapita.fsm.Invoice
import com.opuscapita.fsm.WorkflowTransitionHistory

class BootStrap {

    def init = { servletContext ->
        Invoice.findOrSaveWhere([
                invoiceNo  : "invoice-0",
                customerId : "Customer 0",
                supplierId : "Supplier 0",
                netAmount  : 122 as double,
                grossAmount: 232 as double,
                currencyId : "AUD",
                status     : "inspectionRequired"
        ])
        Invoice.findOrSaveWhere([
                invoiceNo : "invoice-1",
                customerId: "Customer 1",
                supplierId: "Supplier 0",
                netAmount : 22 as double,
                currencyId: "USD",
                status    : "inspectionRequired"
        ])
        Invoice.findOrSaveWhere([
                invoiceNo  : "invoice-2",
                customerId : "Customer 0",
                supplierId : "Supplier 0",
                netAmount  : 122 as double,
                grossAmount: 232 as double,
                currencyId : "AUD",
                status     : "inspectionRequired"
        ])
        Invoice.findOrSaveWhere([
                invoiceNo  : "invoice-3",
                customerId : "Customer 2",
                supplierId : "Supplier 0",
                netAmount  : 12 as double,
                grossAmount: 23 as double,
                currencyId : "CAD",
                status     : "inspectionRequired"
        ])
        Invoice.findOrSaveWhere([
                invoiceNo  : "invoice-4",
                customerId : "Customer 1",
                supplierId : "Supplier 0",
                netAmount  : 122 as double,
                grossAmount: 232 as double,
                currencyId : "JPY",
                status     : "inspectionRequired"
        ])
        Invoice.findOrSaveWhere([
                invoiceNo  : "invoice-5",
                customerId : "Customer 2",
                supplierId : "Supplier 1",
                netAmount  : 12 as double,
                grossAmount: 33 as double,
                currencyId : "EUR",
                status     : "inspectionRequired"
        ])

        WorkflowTransitionHistory.findOrSaveWhere([
                from           : "NULL",
                to             : "inspectionRequired",
                event          : "__START__",
                businessObjType: "invoice",
                businessObjId  : "invoice-0",
                user           : "demouser",
                workflowName   : "InvoiceApproval",
                description    : "Init flow"
        ])
        WorkflowTransitionHistory.findOrSaveWhere([
                from           : "NULL",
                to             : "inspectionRequired",
                event          : "__START__",
                businessObjType: "invoice",
                businessObjId  : "invoice-1",
                user           : "demouser",
                workflowName   : "InvoiceApproval",
                description    : "Init flow"
        ])
        WorkflowTransitionHistory.findOrSaveWhere([
                from           : "NULL",
                to             : "inspectionRequired",
                event          : "__START__",
                businessObjType: "invoice",
                businessObjId  : "invoice-2",
                user           : "demouser",
                workflowName   : "InvoiceApproval",
                description    : "Init flow"
        ])
        WorkflowTransitionHistory.findOrSaveWhere([
                from           : "NULL",
                to             : "inspectionRequired",
                event          : "__START__",
                businessObjType: "invoice",
                businessObjId  : "invoice-3",
                user           : "demouser",
                workflowName   : "InvoiceApproval",
                description    : "Init flow"
        ])
        WorkflowTransitionHistory.findOrSaveWhere([
                from           : "NULL",
                to             : "inspectionRequired",
                event          : "__START__",
                businessObjType: "invoice",
                businessObjId  : "invoice-4",
                user           : "demouser",
                workflowName   : "InvoiceApproval",
                description    : "Init flow"
        ])
        WorkflowTransitionHistory.findOrSaveWhere([
                from           : "NULL",
                to             : "inspectionRequired",
                event          : "__START__",
                businessObjType: "invoice",
                businessObjId  : "invoice-5",
                user           : "demouser",
                workflowName   : "InvoiceApproval",
                description    : "Init flow"
        ])
    }
    def destroy = {
    }
}
