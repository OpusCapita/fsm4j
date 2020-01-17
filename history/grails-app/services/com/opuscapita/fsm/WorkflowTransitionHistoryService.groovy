package com.opuscapita.fsm

import grails.transaction.Transactional

/**
 * The service provide methods for history
 */
class WorkflowTransitionHistoryService {
    static transactional = true


    /**
     * Create restriction to exists builder according to input params
     *
     * @param builder - hibernate criteria builder
     * @param searchParams - input search parameters
     */
    private void addSearchRestrictions(builder, Map searchParams) {
        List expectedSearchFields = ["from", "to", "event", "businessObjId", "businessObjType", "user", "workflowName", "description", "finishedOn"]

        List unsuportedFields = searchParams.keySet().toList() - expectedSearchFields
        if (!unsuportedFields.empty) {
            throw new IllegalArgumentException("Search parameters [${unsuportedFields.join(", ")}] are not supported!")
        }

        for (paramName in expectedSearchFields) {
            def paramValue = searchParams[paramName]
            if (paramValue != null) {
                if (paramName == "finishedOn") {
                    if (paramValue instanceof Date) {
                        builder.eq("finishedOn", paramValue)
                    } else {
                        if (paramValue.gt != null) {
                            builder.gt("finishedOn", paramValue.gt)
                        } else if (paramValue.gte) {
                            builder.ge("finishedOn", paramValue.gte)
                        } else if (paramValue.lt != null) {
                            builder.lt("finishedOn", paramValue.lt)
                        } else if (paramValue.lte != null) {
                            builder.le("finishedOn", paramValue.lte)
                        }
                    }
                } else {
                    builder.eq(paramName, paramValue)
                }
            }
        }
    }

    /**
     * Search workflow transition history items by search parameters
     *
     * @param searchParams - search parameters
     * @param paging - paging parameters
     * @param sorting - sorting parameters
     * @return list of found items
     */
    @Transactional(readOnly = true)
    List<WorkflowTransitionHistory> search(Map searchParams = [:], Map paging = [:], Map sorting = [:]) {
        Integer max = paging.max
        Integer offset = paging.offset

        String orderField = sorting.order
        String sortField = sorting.by

        //by default sort by descending finishedOn
        if (!sortField) {
            sortField = "finishedOn"
            if (!orderField) {
                orderField = "desc"
            }
        } else if (!orderField) {
            orderField = "asc"
        }

        return WorkflowTransitionHistory.createCriteria().list {
            addSearchRestrictions(delegate, searchParams)

            if (max != null) {
                maxResults(max)
            }
            if (offset != null) {
                firstResult(offset)
            }
            if (sortField) {
                order(sortField, orderField)
            }
        }
    }

    /**
     * Add workflow history item by bind params
     *
     * @param bindParams - parameters contains
     *
     * @thrown
     */
    WorkflowTransitionHistory add(Map bindParams = [:]) {
        return new WorkflowTransitionHistory(bindParams).save(flush: true, failOnError: true)
    }

    /**
     * Delete history items by restriction params
     *
     * @param whereParams - restriction parameters
     * @param batchSize - delete batch size
     *
     * @return deleted items count
     */
    Integer delete(Map whereParams = [:], Integer batchSize = 100) {
        Integer deletedTotalCount = 0
        while (true) {
            Integer deletedCount = WorkflowTransitionHistory.withTransaction {
                List items = WorkflowTransitionHistory.createCriteria().list {
                    addSearchRestrictions(delegate, whereParams)
                    maxResults(batchSize)
                }

                items*.delete(flush: true)
                return items.size()
            }

            deletedTotalCount += deletedCount
            if (!(deletedCount > 0)) {
                break
            }
        }

        return deletedTotalCount
    }
}
