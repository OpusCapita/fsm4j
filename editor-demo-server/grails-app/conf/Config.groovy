import grails.util.GrailsUtil
import grails.util.Holders
import org.springframework.context.ApplicationContext
import org.springframework.context.i18n.LocaleContextHolder

grails.doc.title = 'Provisioning Manager'
grails.doc.subtitle = 'The main purpose of the provisioning manager is to provision different types of rights and roles to a user of the application.'
grails.doc.authors = 'Alexey Sergeev (Alexey.Sergeev@opuscapita.com), Dmitry Divin (Dmitry.Divin@opuscapita.com) and Daniel Zhitomirsky (Daniel.Zhitomirsky@opuscapita.com)'
grails.doc.license = 'OpusCapita Software AG'
grails.doc.copyright = "© 2000-${Calendar.getInstance().get(Calendar.YEAR)} <a href='http://www.opuscapita.com/' target='_blank'>OpusCapita Software AG</a>"
grails.doc.footer = '<p>Please contact the authors with any corrections or suggestions</p>'

grails.project.groupId = "com.jcatalog.prov"

naming_strategy = 'org.hibernate.cfg.DefaultNamingStrategy'

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
    html: ['text/html', 'application/xhtml+xml'],
    xml: ['text/xml', 'application/xml'],
    text: 'text/plain',
    js: 'text/javascript',
    rss: 'application/rss+xml',
    atom: 'application/atom+xml',
    css: 'text/css',
    csv: 'text/csv',
    all: '*/*',
    json: ['application/json', 'text/json'],
    form: 'application/x-www-form-urlencoded',
    multipartForm: 'multipart/form-data'
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
/* runtime reloadable gsp pages */
grails.gsp.enable.reload = true
plugin.platformCore.show.startup.info = false
plugin.platformCore.site.name = 'PROV'

plugin.platformCore.events.disabled = true
plugin.platformCore.events.gorm.disabled = true
plugin.platformCore.navigation.disabled = true

grails.databinding.useSpringBinder = true
