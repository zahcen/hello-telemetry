<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My Web App</title>
<script src="https://unpkg.com/@elastic/apm-rum/dist/bundles/elastic-apm-rum.umd.min.js" crossorigin></script>
<script>
  elasticApm.init({
    serviceName: 'hello-telemetry',
    serverUrl: 'apmserver.zitaconseil.fr',
  })
</script>
    <style>
        body {
            font-family: Arial, sans-serif;
            text-align: center;
            margin-top: 50px;
        }
        h1 {
            color: #333;
        }
        a {
            text-decoration: none;
            color: #007bff;
        }
        a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <h1>Welcome to hello-telemetry Application</h1>
    <a href="results">View Database Results</a>
    
</body>
</html>