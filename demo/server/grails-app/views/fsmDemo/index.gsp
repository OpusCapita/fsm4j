<!DOCTYPE html>

<jc:decorateLayoutResources>
    <html>
    <head>
        <title>Workflow Demo App</title>
        <link rel="stylesheet" href="https://opuscapita.github.io/styles/index.css">
    </head>

    <body style="padding-bottom: 0; overflow-x: hidden;">
    <div id="main"></div>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/react/15.6.2/react.js" type="text/javascript"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/react-dom/15.6.2/react-dom.js" type="text/javascript"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/prop-types/15.6.2/prop-types.js" type="text/javascript"></script>


    <r:require modules="editor"/>

    <r:script>
      fsm4jDemo.render( document.getElementById("main"), { baseUrl: "${baseUrl}" } )
    </r:script>

    <jc:layoutResources/>
    <jc:layoutResources/>
    </body>
    </html>

</jc:decorateLayoutResources>