package com.opuscapita.fsm

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Demo FSM editor
 *
 * @author Dmitry Divin
 */
@Log4j
class FsmEditorService implements ApplicationContextAware {
    def workflowTransitionHistoryService

    private ReadWriteLock schemaReadWriteLock = new ReentrantReadWriteLock()
    private Lock schemaReadLock = schemaReadWriteLock.readLock()
    private Lock schemaWriteLock = schemaReadWriteLock.writeLock()


    ApplicationContext applicationContext

    private File getConfigurationRootFile() {
        //get servlet context resource
        applicationContext.getResource("WEB-INF/configuration").file
    }

    private Map getJSONFromConfiguration(String filePath) {
        new JsonSlurper().parse(new File(configurationRootFile, filePath))
    }

    private Closure getConditionFromConfiguration(String filePath) {
        File groovyFile = new File(configurationRootFile, filePath)
        assert groovyFile.exists(), "File [${filePath}] doesn't exists"

        return { args ->
            GroovyShell groovyShell = new GroovyShell(applicationContext.classLoader, new Binding([
                    args: args,
                    log : log
            ]))

            return groovyShell.evaluate(groovyFile)
        }
    }

    private List getConditionItems(String filePath) {
        new File(configurationRootFile, filePath).listFiles({ File dir, String name ->
            String nameWithoutExt = name.lastIndexOf('.').with { it != -1 ? name[0..<it] : name }
            return name == "${nameWithoutExt}.groovy" && new File(dir, "${nameWithoutExt}.schema.json").exists()
        } as FilenameFilter).collect {
            String name = it.name
            return name.lastIndexOf('.').with { it != -1 ? name[0..<it] : name }
        }
    }

    Map getSchema() {
        schemaReadLock.lock()
        try {
            getJSONFromConfiguration("schema.json")
        } catch (ignore) {
            getJSONFromConfiguration("default-schema.json")
        } finally {
            schemaReadLock.unlock()
        }
    }

    void setSchema(Map updatedSchema) {
        schemaWriteLock.lock()
        try {
            new File(configurationRootFile, "schema.json").withWriter {
                it << JsonOutput.toJson(updatedSchema)
            }
        } finally {
            schemaWriteLock.unlock()
        }
    }

    Map getObjectConfiguration() {
        getJSONFromConfiguration("objectConfiguration.json")
    }

    Map getConditions() {
        getConditionItems("conditions").collectEntries {
            [it, [
                    'paramsSchema': getJSONFromConfiguration("conditions/${it}.schema.json")
            ]]
        }
    }

    Map getActions() {
        getConditionItems("actions").collectEntries {
            [it, [
                    'paramsSchema': getJSONFromConfiguration("actions/${it}.schema.json")
            ]]
        }
    }

    Machine getMachine() {
        MachineDefinition machineDefinition = new MachineDefinition([
                schema             : schema,
                objectConfiguration: objectConfiguration,
                conditions         : getConditionItems("conditions").collectEntries {
                    [it, getConditionFromConfiguration("conditions/${it}.groovy")]
                },
                actions            : getConditionItems("actions").collectEntries {
                    [it, getConditionFromConfiguration("actions/${it}.groovy")]
                }
        ])

        Machine machine = new Machine([
                machineDefinition       : machineDefinition,
                history                 : workflowTransitionHistoryService,
                convertObjectToReference: { object ->
                    return [
                            businessObjType: "invoice",
                            businessObjId  : "IN01"
                    ]
                }
        ])

        return machine
    }
}
