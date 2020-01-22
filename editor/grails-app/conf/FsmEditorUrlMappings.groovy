class FsmEditorUrlMappings {
    static mappings = {
        "/api/fsm/$businessObjectType/$businessObjectId"(controller: "fsmEditorApi") {
            action = [GET: "edit"]
        }

//        "/api/attributes"(controller: "attributeApi") {
//            action = [GET: 'index']
//        }
//        "/api/attributes/$id"(controller: "attributeApi") {
//            action = [GET: 'show']
//        }
//
//        "/api/attributes/$id/typeExtension"(controller: "attributeTypeExtensionApi", action: "index", method: "GET")
//        "/api/attributeSections"(controller: "attributeSectionApi") {
//            action = [GET: "index", POST: "create"]
//        }
//        "/api/attributeSections/$attributeSectionId"(controller: "attributeSectionApi") {
//            action = [GET: "show", PUT: "update", DELETE: "delete"]
//        }
    }
}